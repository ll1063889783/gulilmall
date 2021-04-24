package com.atguigu.gulimall.seckill.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberResponseVo;
import org.apache.shiro.util.AntPathMatcher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor{
    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();

        String uri =request.getRequestURI();

        AntPathMatcher matcher = new AntPathMatcher();
        //url路径匹配，匹配到了就进行登录判断
        boolean match = matcher.match("/kill/**", uri);

        if(match){
            MemberResponseVo memberResponseVo = (MemberResponseVo)session.getAttribute(AuthServerConstant.LOGIN_USER);
            if(memberResponseVo!=null){
                //用户登录,
                loginUser.set(memberResponseVo);
                return true;
            } else {
                //没有登录就去登录
                request.getSession().setAttribute("msg","请先登录");
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        }
        return true;

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

    }
}