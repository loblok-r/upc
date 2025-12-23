package cn.loblok.upc.common.config;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.exception.UnauthorizedException;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(Long.class);

    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {


        String userIdStr = webRequest.getHeader("X-User-Id");

        if (StringUtils.hasText(userIdStr)) {
            return Long.valueOf(userIdStr);
        }

        // 如果能走到这里，说明是受保护的接口但没拿到 userId，返回 null 或抛异常
        return new UnauthorizedException("请先登录");
    }
}