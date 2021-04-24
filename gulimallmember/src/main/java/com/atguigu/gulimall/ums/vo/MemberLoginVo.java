package com.atguigu.gulimall.ums.vo;

import lombok.Data;

@Data
public class MemberLoginVo {

    //登录的用户名
    private String loginAccount;
    //登录的密码
    private String password;
}
