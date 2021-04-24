package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.utils.HttpUtils;
import com.atguigu.gulimall.auth.vo.MemberResponseVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求（微博登录）
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;
    /**
     * 点击微博按钮跳转到微博授权页面，输入正确的微博账号确认授权之后会跳转到
     * 你指定的页面同时会给你一个随机的code码
     * @param code
     * @return
     */
    @GetMapping(value="/oauth2.0/weibo/success")
    public String weiboLogin(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception{
        //1,根据code换取accessToken
        Map<String,String> map = new HashMap<>();
        map.put("client_id","2636917288");//应用appId
        map.put("client_secret","2636917288");//应用appCode
        map.put("grant_type","authorization_code");//授权方式(固定模式)
        map.put("redirect_uri","http://gulimall.com/oauth2.0/weibo/success");
        map.put("code",code);
        //得到响应
        HttpResponse response = HttpUtils.doPost("api.weibo.com","/oauth2/access_token","post",null,null,map);
        HttpEntity entity = response.getEntity();
        //2,处理
        if(response.getStatusLine().getStatusCode()==200){
            //获取到了accessToken
            String jsonStr = EntityUtils.toString(entity);
            SocialUser socialUser = JSON.parseObject(jsonStr, SocialUser.class);
            //知道当前是那个社交用户
            //1,当前用户如果是第一次进网站，自动注册进来（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员）
            //登录或者注册这个社交用户
            R oauth2Login = memberFeignService.oauth2Login(socialUser);
            if(oauth2Login.getCode() == 0){
                MemberResponseVo vo = (MemberResponseVo)oauth2Login.getData("data", new TypeReference<MemberResponseVo>() {
                });
                log.info("登录成功,用户:{}",vo.toString());
                //第一次使用session命令浏览器保存卡号JSESSIONID的cookie（cookie的name为JSESSIONID）
                //以后浏览器访问哪个网站就会带上这个网站的cookie
                //子域之间gulimall.com、auth.gulimall.com
                //发卡的时候（指定域名为父域名），子域系统发的卡，也能让父域直接使用。
                //Todo 默认发的令牌。session=sdfsdff.作用域：当前域（解决session共享问题）
                //Todo 默认使用json的序列化来序列化对象数据到redis中
                session.setAttribute("loginUser",vo);
                //servletResponse.addCookie(new Cookie(""));
                //社交登录成功
                //登录成功就跳转回首页
                return "redirect:http://gulimall.com";

            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }

        }else {
            //换取accessToken失败则跳转到商城登录界面
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
