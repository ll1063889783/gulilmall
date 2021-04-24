package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 远程调用gulimall-member微服务的Controller里面的注册用户的方法
 */
@FeignClient(value = "gulimall-member")
public interface MemberFeignService {

    @PostMapping(value="/ums/member/regist")
    public R regist(@RequestBody UserRegistVo registVo);

    /**
     * 用户登录
     * @param loginVo
     * @return
     */
    @PostMapping(value = "/ums/member/login")
    public R login(@RequestBody UserLoginVo loginVo);

    /**
     * 用户社交登录
     * @param socialUser 社交登录用户
     * @return
     */
    @PostMapping(value = "/ums/member/oauth2/login")
    public R oauth2Login(@RequestBody SocialUser socialUser) throws Exception;
}
