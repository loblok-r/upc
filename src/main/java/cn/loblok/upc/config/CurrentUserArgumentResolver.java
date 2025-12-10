package cn.loblok.upc.config;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.util.JwtUtil;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 自定义参数解析器，用于解析当前用户ID
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {


    /**
     * 判断参数类型是否支持解析
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(Long.class);
    }


    /**
     * 解析参数
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        // 从 Header 中获取 Token
        String authHeader = webRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 去掉 "Bearer "
            String token = authHeader.substring(7);
            // 解析 Token 获取 userId
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new RuntimeException("无效用户凭证");
            }
            return userId;
        }
        throw new RuntimeException("请先登录");
    }
}