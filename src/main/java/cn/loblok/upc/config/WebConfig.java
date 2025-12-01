package cn.loblok.upc.config;

import cn.loblok.upc.intercepter.LevelAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 注册拦截器
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LevelAuthInterceptor levelAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(levelAuthInterceptor)
                .addPathPatterns("/api/**") // 拦截需要权限的路径
                .excludePathPatterns("/user/**", "/public/**"); // 放行登录注册
    }
}