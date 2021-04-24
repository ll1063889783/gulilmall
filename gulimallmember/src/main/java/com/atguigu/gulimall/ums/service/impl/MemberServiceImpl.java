package com.atguigu.gulimall.ums.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.common.utils.HttpUtil;
import com.atguigu.gulimall.ums.dao.MemberLevelDao;
import com.atguigu.gulimall.ums.entity.MemberLevelEntity;
import com.atguigu.gulimall.ums.execption.PhoneExistException;
import com.atguigu.gulimall.ums.execption.UserNameExistException;
import com.atguigu.gulimall.ums.utils.HttpUtils;
import com.atguigu.gulimall.ums.vo.MemberLoginVo;
import com.atguigu.gulimall.ums.vo.MemberRegistVo;
import com.atguigu.gulimall.ums.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ums.dao.MemberDao;
import com.atguigu.gulimall.ums.entity.MemberEntity;
import com.atguigu.gulimall.ums.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo registVo) {
        MemberEntity memberEntity = new MemberEntity();

        //设置会员的默认等级
        //执行sql SELECT * FROM ums_member_level WHERE default_status = 1
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());
        //检查用户名和手机号是否唯一,为了让controller感知异常，异常机制
        checkPhoneUnique(registVo.getPhone());

        checkUsernameUnique(registVo.getUserName());

        memberEntity.setMobile(registVo.getPhone());

        memberEntity.setUsername(registVo.getUserName());
        //昵称
        memberEntity.setNickname(registVo.getUserName());
        //密码要进行加密处理
        //密码加密器
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        //给前端页面填写的明文密码加密
        String encode = passwordEncoder.encode(registVo.getPassword());
        memberEntity.setPassword(encode);
        //其他默认信息.....此处省略

        //保存用户注册信息
        this.getBaseMapper().insert(memberEntity);
    }

    @Override
    public void checkUsernameUnique(String username) throws UserNameExistException{
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if(count>0){
            throw new UserNameExistException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(count>0){
            throw new PhoneExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo memberLoginVo) {
        //用户的登录名可以是用户名和手机号
        String loginAccount = memberLoginVo.getLoginAccount();
        //登录页提交的密码（明文）
        String password = memberLoginVo.getPassword();

        //去查询数据库验证登录用户名是否存在 select * from ums_member where username=? or mobile=?
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAccount).or().eq("mobile", loginAccount));
        if(entity == null){
            //登录失败
            return null;
        } else {
            //获取到数据库的password(密文用BcryptPasswordEncoder加密过的)
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //用matches方法匹配用户提交的密码和数据库里面的密码
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if(matches){
                return entity;
            } else {
                return null;
            }

        }

    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception{
        //登录和注册合并逻辑(根据uid来判断用户是否是登录或注册)
        String uid = socialUser.getUid();
        //判断当前社交用户是否已经登录过系统
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if(entity!=null){
            //这个用户已经注册过
            MemberEntity update = new MemberEntity();
            update.setId(entity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            //更新数据库表ums_member里面的access_token和expires_in字段
            this.baseMapper.updateById(update);
            entity.setAccessToken(socialUser.getAccess_token());
            entity.setExpiresIn(socialUser.getExpires_in());
            return entity;
        } else{
            //2,没有查到当前社交用户对应的记录我们就需要注册
            MemberEntity regist = new MemberEntity();
            try{
                //查询当前社交用户的社交账号信息（昵称、性别等）
                Map<String, String> query = new HashMap<>();
                query.put("access_token",socialUser.getAccess_token());
                query.put("uid",socialUser.getUid());
                //查询微博开发文档的查询接口
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
                if(response.getStatusLine().getStatusCode() == 200){
                    //查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    //.....其他数据省略
                    regist.setNickname(name);
                    regist.setGender("m".equals(gender)? 1:0);
                }
            }catch (Exception e){

            }
            regist.setSocial_Uid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());
            //插入到数据库表ums_member
            this.baseMapper.insert(regist);
            return regist;
        }
    }

}