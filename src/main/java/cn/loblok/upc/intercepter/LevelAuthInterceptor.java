package cn.loblok.upc.intercepter;

import cn.loblok.upc.annotation.RequireLevel;
import cn.loblok.upc.util.CaculateUtils;
import cn.loblok.upc.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
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
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只拦截带 @RequireLevel 注解的方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireLevel requireLevel = handlerMethod.getMethodAnnotation(RequireLevel.class);
        if (requireLevel == null) {
            return true; // 无需权限校验
        }

        //从 Header 获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeErrorResponse(response, "Missing or invalid token", 401);
            return false;
        }
        String token = authHeader.substring(7);

        // 验证 Token 并获取 userId
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                writeErrorResponse(response, "Invalid token", 401);
                return false;
            }

            // 【核心】获取用户等级并校验
            String userLevel = caculateUtils.getUserLevel(userId);
            String requiredLevel = requireLevel.value();

            if (!isLevelSufficient(userLevel, requiredLevel)) {
                writeErrorResponse(response, "Insufficient permission: required " + requiredLevel, 403);
                return false;
            }

            // 权限通过，继续执行
            return true;

        } catch (Exception e) {
            log.error("Permission check failed", e);
            writeErrorResponse(response, "Authentication error", 500);
            return false;
        }
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
            Claims claims = Jwts.parser()
                    .setSigningKey("upc_secret_key")
                    .parseClaimsJws(token)
                    .getBody();
            return ((Number) claims.get("userId")).longValue();
        } catch (Exception e) {
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