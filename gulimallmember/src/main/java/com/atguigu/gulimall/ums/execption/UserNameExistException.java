package com.atguigu.gulimall.ums.execption;

public class UserNameExistException extends RuntimeException{
    public UserNameExistException() {
        super("用户已存在");
    }
}
