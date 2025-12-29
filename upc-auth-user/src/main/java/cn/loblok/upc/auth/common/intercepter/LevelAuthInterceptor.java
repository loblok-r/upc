package cn.loblok.upc.auth.common.intercepter;

import cn.loblok.upc.auth.common.util.CaculateUtils;
import cn.loblok.upc.auth.common.annotation.RequireLevel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 权限拦截器
 */
@Component
@Slf4j
public class LevelAuthInterceptor implements HandlerInterceptor {


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CaculateUtils caculateUtils;

    /**
     * 请求处理之前执行
     */
    // 在 LevelAuthInterceptor.java 中
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 1. 依然只拦截带 @RequireLevel 注解的方法
        if (!(handler instanceof HandlerMethod)) {return true;}
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireLevel requireLevel = handlerMethod.getMethodAnnotation(RequireLevel.class);
        if (requireLevel == null) {return true;}

        // 2. 【关键】不再解析 Token，直接取网关传来的 X-User-Id
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            writeErrorResponse(response, "Access denied: identity not verified by gateway", 401);
            return false;
        }
        Long userId = Long.parseLong(userIdStr);

        // 3. 只做等级校验
        String userLevel = caculateUtils.getUserLevel(userId);
        String requiredLevel = requireLevel.value();

        if (!isLevelSufficient(userLevel, requiredLevel)) {
            writeErrorResponse(response, "Insufficient level: required " + requiredLevel, 403);
            return false;
        }

        return true;
    }

    /**
     * 从 Token 中获取 userId
     */
    private Long getUserIdFromToken(String token) {
        // 从 Redis 查（已存 token -> userId）
        String userIdStr = redisTemplate.opsForValue().get("token:" + token);
        if (userIdStr != null) {
            return Long.parseLong(userIdStr);
        }

        // 从 JWT 解析（备用）
        try {
            // 将字符串密钥转为安全的 SecretKey 对象
            SecretKey key = Keys.hmacShaKeyFor("upc_secret_key_should_be_long_enough_32_chars".getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder() // 使用 parserBuilder() 替代 parser()
                    .setSigningKey(key)          // 传入 SecretKey 对象
                    .build()                     // 构建实例
                    .parseClaimsJws(token)
                    .getBody();

            return ((Number) claims.get("userId")).longValue();
        } catch (Exception e) {
            log.error("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断用户等级是否满足要求
     */
    private boolean isLevelSufficient(String userLevel, String requiredLevel) {
        // 简单字符串比较（假设 LEVEL1 < LEVEL2 < ...）
        Map<String, Integer> levelMap = Map.of(
                "LEVEL1", 1,
                "LEVEL2", 2,
                "LEVEL3", 3,
                "LEVEL4", 4,
                "LEVEL5", 5
        );
        int user = levelMap.getOrDefault(userLevel, 0);
        int required = levelMap.getOrDefault(requiredLevel, 999);
        return user >= required;
    }

    /**
     * 封装错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"msg\":\"" + message + "\"}");
    }
}