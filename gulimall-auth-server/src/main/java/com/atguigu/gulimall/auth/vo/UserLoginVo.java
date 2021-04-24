package com.atguigu.gulimall.auth.vo;

import lombok.Data;

/**
 * 用户登录的vo
 */
@Data
public class UserLoginVo {
    //登录的用户名
    private String loginAccount;
    //登录的密码
    private String password;
}
