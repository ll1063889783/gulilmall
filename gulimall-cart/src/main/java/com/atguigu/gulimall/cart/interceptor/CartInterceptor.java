package com.atguigu.gulimall.cart.interceptor;

import com.alibaba.cloud.context.utils.StringUtils;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 在执行目标方法之前，判断用户的登录状态，并封装传递给Controller目标请求。
 */
@Component
public class CartInterceptor implements HandlerInterceptor{

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    /**
     * 目标方法执行之前，拦截器在Controller执行之前往cookie里面新增用户信息
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberResponseVo memberResponseVo = (MemberResponseVo)session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(memberResponseVo!=null){
            //用户登录
            userInfoTo.setUserId(memberResponseVo.getId());
        }
        Cookie[] cookies = request.getCookies();
        if(cookies!=null & cookies.length>0){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                //当前已经分配了临时用户
                if(CartConstant.TEMP_USER_COOKIE_NAME.equals(name)){
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }
        //用户第一次进网站还没有登录，没有临时用户分配一个临时用户
        if(StringUtils.isEmpty(userInfoTo.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //目标方法执行之前
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 业务执行之后，分配临时用户，浏览器保存cookie，过期时间是一个月
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        //没有分配临时用户则分配临时用户加入cookie里面
        if(!userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            //cookie作用域
            cookie.setDomain("gulimall.com");
            //cookie过期时间
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

    }
}
