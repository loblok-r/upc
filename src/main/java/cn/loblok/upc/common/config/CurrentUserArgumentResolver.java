package cn.loblok.upc.common.config;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.util.JwtUtil;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {

        // 从 Header 中获取 Token
        String authHeader = webRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 去掉 "Bearer "
            // 假设你有一个工具类解析用户ID
            Long userId = JwtUtil.getUserIdFromToken(token); // ← 你需要实现这个逻辑
            if (userId == null) {
                throw new RuntimeException("无效用户凭证");
            }
            return userId;
        }
        throw new RuntimeException("请先登录");
    }
}