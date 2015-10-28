package com.scienjus.authorization.interceptor;

import com.scienjus.authorization.annotation.Authorization;
import com.scienjus.authorization.manager.TokenManager;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 自定义拦截器，判断此次请求是否有权限
 * @see com.scienjus.authorization.annotation.Authorization
 * @author ScienJus
 * @date 2015/7/30.
 */
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

    public static final String REQUEST_CURRENT_KEY = "REQUEST_CURRENT_KEY";

    private TokenManager manager;
    private String httpHeaderName = "Authorization";
    private String httpHeaderPrefix = "";
    private String unauthorizedErrorMessage = "401 unauthorized";

    public void setManager(TokenManager manager) {
        this.manager = manager;
    }

    public void setHttpHeaderName(String httpHeaderName) {
        this.httpHeaderName = httpHeaderName;
    }

    public void setHttpHeaderPrefix(String httpHeaderPrefix) {
        this.httpHeaderPrefix = httpHeaderPrefix;
    }

    public void setUnauthorizedErrorMessage(String unauthorizedErrorMessage) {
        this.unauthorizedErrorMessage = unauthorizedErrorMessage;
    }

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        //从header中得到token
        String authorization = request.getHeader(httpHeaderName);
        //在这个方法中可以对得到的authorization进行一些验证，例如时间戳，URL签名等
        String token = manager.decodeToken(authorization, request);
        if (token != null && token.startsWith(httpHeaderPrefix) && token.length() > 0) {
            token = token.substring(httpHeaderPrefix.length());
            //验证token
            String key = manager.getKey(token);
            if (key != null) {
                //如果token验证成功，将token对应的用户id存在request中，便于之后注入
                request.setAttribute(REQUEST_CURRENT_KEY, key);
                return true;
            }
        }
        //如果验证token失败，并且方法注明了Authorization，返回401错误
        if (method.getAnnotation(Authorization.class) != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("gbk");
            response.getWriter().write(unauthorizedErrorMessage);
            response.getWriter().close();
            return false;
        }
        //为了防止以某种直接在REQUEST_CURRENT_KEY写入key，将其设为null
        request.setAttribute(REQUEST_CURRENT_KEY, null);
        return true;
    }
}
