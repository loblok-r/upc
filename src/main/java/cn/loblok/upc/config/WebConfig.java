package cn.loblok.upc.config;

import cn.loblok.upc.intercepter.IpRateLimitInterceptor;
import cn.loblok.upc.intercepter.LevelAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 *
 * Spring MVC 请求处理关键顺序
 *
 * 匹配请求路径->执行拦截器的 preHandle()（按注册顺序,levelAuthInterceptor 的逻辑）,if 返回 false，请求直接终止，不会进入 Controller
 * ->解析方法参数（调用 HandlerMethodArgumentResolver）,currentUserArgumentResolver 的工作
 * ->调用 Controller 方法
 * ->执行拦截器的 postHandle()
 * ->视图渲染 / 返回响应
 * ->执行拦截器的 afterCompletion()
 *
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LevelAuthInterceptor levelAuthInterceptor;

    @Autowired
    private IpRateLimitInterceptor ipRateLimitInterceptor;

    @Autowired
    private CurrentUserArgumentResolver currentUserArgumentResolver;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(levelAuthInterceptor)
                // 拦截所有路径
                .addPathPatterns("/api/**")
                // 放行登录注册
                .excludePathPatterns("/user/**", "/public/**");

        registry.addInterceptor(ipRateLimitInterceptor)
                .addPathPatterns("/api/chat/completions");
    }

    /**
     * 添加参数解析器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }


}