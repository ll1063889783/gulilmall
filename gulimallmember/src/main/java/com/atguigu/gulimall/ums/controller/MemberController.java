package com.atguigu.gulimall.ums.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.gulimall.ums.execption.PhoneExistException;
import com.atguigu.gulimall.ums.execption.UserNameExistException;
import com.atguigu.gulimall.ums.vo.MemberLoginVo;
import com.atguigu.gulimall.ums.vo.MemberRegistVo;
import com.atguigu.gulimall.ums.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.ums.entity.MemberEntity;
import com.atguigu.gulimall.ums.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:43:49
 */
@RestController
@RequestMapping("/ums/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 微服务之间相互调用使用的是http+restful形式，微服务接口传值
     * 必须使用@RequestBody注解
     * @param registVo
     * @return
     */
    @PostMapping(value="/regist")
    public R regist(@RequestBody MemberRegistVo registVo){
        try{
            memberService.regist(registVo);
        }catch (UserNameExistException e){
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(),BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }catch(PhoneExistException e){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 用户社交登录
     * @param socialUser 社交登录用户
     * @return
     */
    @PostMapping(value = "/oauth2/login")
    public R oauth2Login(@RequestBody SocialUser socialUser) throws Exception {
        //调用社交登录的重载方法
        MemberEntity entity = memberService.login(socialUser);
        if(entity!=null){
            //TODO 1,登录成功处理
            return R.ok().setData1(entity);
        } else {
            return R.error(BizCodeEnume.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnume.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    /**
     * 用户登录
     * @param memberLoginVo
     * @return
     */
    @PostMapping(value = "/login")
    public R login(@RequestBody MemberLoginVo memberLoginVo){
        MemberEntity entity = memberService.login(memberLoginVo);
        if(entity!=null){
            //TODO 1,登录成功处理
            return R.ok().setData1(entity);
        } else {
            return R.error(BizCodeEnume.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnume.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ums:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ums:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ums:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ums:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ums:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
