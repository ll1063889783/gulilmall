package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.auth.vo.MemberResponseVo;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证登录注册控制类
 */
@RestController
public class LoginController {
    /**
     * 发送一个请求直接跳转至一个页面
     * SpringMVC viewcontroller;将请求和页面映射过来
     * @return
     */
//    @GetMapping(value = "/login.html")
//    public String loginPage(){
//        return "login";
//    }
//
//    @GetMapping(value = "/reg.html")
//    public String registPage(){
//        return "regist";
//    }
      @Autowired
      private ThirdPartyFeignService thirdPartyFeignService;

      @Autowired
      private StringRedisTemplate redisTemplate;

      @Autowired
      private MemberFeignService memberFeignService;

    /**
     * 注册页面发送短信验证码，输入手机号
     * @param phone
     * @return
     */
    @GetMapping(value="/sms/sendCode")
    public R sendCode(@RequestParam(value="phone") String phone){
        //接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(redisCode)){
            //首次发送验证码时的发送时间
            long redisTime = Long.parseLong(redisCode.split("_")[1]);
            //从redis里面取出的时间与当前时间的差值小于60s则不能再次发送验证码
            if(System.currentTimeMillis()-redisTime < 60000){
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(),BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        //验证码的再次校验 redis 存key phone value redisCode1=code+当前时间的时间毫秒数(System.currentTimeMillis())
        String code = UUID.randomUUID().toString().substring(0,5);//发送的验证码
        String redisCode1 = code+"_"+System.currentTimeMillis();
        //把手机号和验证码缓存到redis中，设置其过期时间防止同一个手机号在60秒再次发送验证。
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,redisCode1,10, TimeUnit.MINUTES);
        //远程调用gulimall-third-party微服务进行发送验证码
        thirdPartyFeignService.sendCode(phone,code);
        return R.ok();
    }

    /**
     * 防止重复提交注册信息，必须使用重定向，使用RedirectAttributes可以携带数据到页面
     * 利用session原理，将数据放在session中。只要跳到下一个页面取出这个数据后，
     * session里面的数据就会被删掉。
     * @param userRegistVo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping(value="/regist")
    public String regist(@Valid UserRegistVo userRegistVo, BindingResult result, RedirectAttributes redirectAttributes){
        //用户注册校验出现错误,转发到注册页
        if(result.hasErrors()){
             //Map<String, String> errors = new HashMap<>();
            //获取所有校验出错的字段集合，遍历集合
            /*result.getFieldErrors().stream().map(fieldError -> {
                //校验出错的字段名
                String field = fieldError.getField();
                //校验出错的字段信息 vo对象里面的message @NotEmpty(message = "密码必须填写")
                String defaultMessage = fieldError.getDefaultMessage();
                errors.put(field,defaultMessage);
                return errors;
            });*/
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(
                    fieldError -> {
                        return fieldError.getField();
                    }, fieldError -> {
                        return fieldError.getDefaultMessage();
                    }
            ));
            redirectAttributes.addFlashAttribute("errors",errors);
            //不加redirect默认是转发，可能拿到request作用域的数据
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //校验验证码
        //页面上填写的传到后天的验证码
        String code = userRegistVo.getCode();
        //从redis中取出该手机号的验证码
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
        if(!StringUtils.isEmpty(redisCode)){
            //截窜之后的得到真正的验证码
            String verfityCode = redisCode.split("_")[0];
            //验证码通过
            if(code.equals(verfityCode)){
                //删除验证码 从redis中移除key;令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
                //真正注册，调用远程服务进行注册
                R r = memberFeignService.regist(userRegistVo);
                if(r.getCode() == 0){
                    //成功返回登录页
                    //注册成功回到首页，回到登录页
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    Map<String,String> errors = new HashMap<>();
                    //errors.put("msg",r.getData(new TypeReference<String>()));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    //失败返回注册页
                    return "redirect:http://auth.gulimall.com/reg.html";

                }
            } else{
                //验证码错误
                Map<String, String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors",errors);
                //不加redirect默认是转发，可能拿到request作用域的数据
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            //验证码错误
            Map<String, String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            //不加redirect默认是转发，可能拿到request作用域的数据
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    /**
     * 点击首页登录按钮跳转到登录页面（登录的入口）
     * @param session
     * @return
     */
    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute == null){
            //没登录
            return "login";
        }else {
            return "redirect:http://gulimall.com";
        }
    }

    /**
     * 处理登录请求
     * @param userLoginVo
     * @param redirectAttributes
     * @param session
     * @return
     */
    @PostMapping(value="/login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes redirectAttributes, HttpSession session){
        //远程登录
        R r = memberFeignService.login(userLoginVo);
        if(r.getCode() == 0){
            MemberResponseVo data = (MemberResponseVo)r.getData("key", new TypeReference<MemberResponseVo>() {
            });
            //登录成功，即会重定向到商城首页,把用户放在session中
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:http://gulimall.com";
        }else {
            Map<String,String> errors = new HashMap<>();
            errors.put("msg","");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }
}
