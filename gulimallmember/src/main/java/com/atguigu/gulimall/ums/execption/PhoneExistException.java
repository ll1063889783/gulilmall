package com.atguigu.gulimall.ums.execption;

public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号存在");
    }
}
