package com.atguigu.gulimall.cart.vo;

import lombok.Data;

/**
 * 用户的cookie信息
 */
@Data
public class UserInfoTo {
    private Long userId;//用户id
    private String userKey;//用户key
    private boolean tempUser = false;//是否是临时用户
}
