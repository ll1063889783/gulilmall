package com.atguigu.gulimall.ums.service;

import com.atguigu.gulimall.ums.execption.PhoneExistException;
import com.atguigu.gulimall.ums.execption.UserNameExistException;
import com.atguigu.gulimall.ums.vo.MemberLoginVo;
import com.atguigu.gulimall.ums.vo.MemberRegistVo;
import com.atguigu.gulimall.ums.vo.SocialUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ums.entity.MemberEntity;

import java.util.Map;

/**
 * 
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:43:49
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo registVo);

    void checkUsernameUnique(String username) throws UserNameExistException;

    void checkPhoneUnique(String phone) throws PhoneExistException;

    MemberEntity login(MemberLoginVo memberLoginVo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}

