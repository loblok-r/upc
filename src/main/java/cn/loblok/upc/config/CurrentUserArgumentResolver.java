package cn.loblok.upc.config;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.exception.UnauthorizedException;
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

        String authHeader = webRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new UnauthorizedException("无效用户凭证");
            }
            return userId;
        }
        throw new UnauthorizedException("请先登录");
    }
}