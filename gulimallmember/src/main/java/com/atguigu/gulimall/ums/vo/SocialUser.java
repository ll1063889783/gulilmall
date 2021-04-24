package com.atguigu.gulimall.ums.vo;

import lombok.Data;

/**
 * 社交登录用户
 */
@Data
public class SocialUser {
    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;
}
