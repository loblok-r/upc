package cn.loblok.upc.gateway.filter;


import cn.loblok.upc.common.utils.JwtUtil;
import cn.loblok.upc.gateway.config.IgnoreUrlsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 鉴权过滤器
 */

@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {


    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    /**
     * 判断请求是否在白名单（登录、注册、公开接口）。
     * 从 Header 获取 Token。
     * 调用 upc-common 里的 JwtUtil 验证 Token。
     * 关键点：解析出 userId 后，将其放入 Header 传给下游微服务。
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // --- 如果是 OPTIONS 请求，直接放行 ---
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = request.getURI().getPath();

        // 1. 白名单放行 (登录、注册、公开图片等)
        if (isIgnore(path)) {
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String token = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return unAuthorized(exchange, "未携带凭证");
        }

        // 3. 校验 Token 并提取 UserId
        try {
            log.info("token:"+token);
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return unAuthorized(exchange, "凭证无效");
            }

            // 4. 将 userId 放入 Header，下游微服务直接取，无需再解析 Token
            ServerHttpRequest mutableRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId.toString())
                    // 还可以顺便传用户名或等级
                    .build();

            return chain.filter(exchange.mutate().request(mutableRequest).build());

        } catch (Exception e) {
            return unAuthorized(exchange, "凭证过期或解析错误");
        }
    }

    private boolean isIgnore(String path) {
        // 逻辑判断 path 是否在 ignoreUrlsConfig 的列表里
        return ignoreUrlsConfig.getUrls().stream().anyMatch(url -> path.contains(url));
    }

    private Mono<Void> unAuthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 这里可以手动拼接 Result 对象的 JSON 字符串返回
        String body = "{\"code\":401,\"msg\":\"" + msg + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }


    @Override
    public int getOrder() {
        return -100;
    }
}