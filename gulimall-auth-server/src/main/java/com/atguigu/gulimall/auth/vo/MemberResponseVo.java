package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@ToString
@Data
public class MemberResponseVo implements Serializable{
    /**
     * id
     */
    private Long id;
    /**
     *
     */
    private Long levelId;
    /**
     *
     */
    private String username;
    /**
     *
     */
    private String password;
    /**
     *
     */
    private String nickname;
    /**
     *
     */
    private String mobile;
    /**
     *
     */
    private String email;
    /**
     * ͷ
     */
    private String header;
    /**
     *
     */
    private Integer gender;
    /**
     *
     */
    private Date birth;
    /**
     *
     */
    private String city;
    /**
     * ְҵ
     */
    private String job;
    /**
     *
     */
    private String sign;
    /**
     *
     */
    private Integer sourceType;
    /**
     *
     */
    private Integer integration;
    /**
     *
     */
    private Integer growth;
    /**
     *
     */
    private Integer status;
    /**
     * ע
     */
    private Date createTime;
    //社交用户登录id
    private String social_Uid;
    //社交用户访问令牌
    private String accessToken;
    //社交用户访问令牌过期时间
    private Long expiresIn;
}
