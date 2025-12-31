package cn.loblok.upc.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class GatewaySentinelConfig {

    @PostConstruct
    public void init() {
        // 1. 加载限流规则
        initGatewayRules();
        // 2. 加载熔断规则
        initDegradeRules();
        // 3. 设置自定义异常处理逻辑
        initBlockHandler();
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("ai-service-route")
                .setCount(10) // 每秒允许10个请求
                .setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);
    }

    private void initDegradeRules() {
        List<DegradeRule> degradeRules = new ArrayList<>();

        DegradeRule aiDegradeRule = new DegradeRule("ai-service-route")
                .setGrade(RuleConstant.DEGRADE_GRADE_RT) // 基于响应时间
                .setCount(5000)      // 阈值：5秒
                .setTimeWindow(15)   // 熔断时长：15秒
                .setMinRequestAmount(5) // 最小请求数
                .setStatIntervalMs(10000); // 统计窗口10秒

        degradeRules.add(aiDegradeRule);

        // 加载规则
        DegradeRuleManager.loadRules(degradeRules);
    }

    private void initBlockHandler() {
        BlockRequestHandler handler = new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
                String msg = "{\"code\": 429, \"msg\": \"访问过于频繁\"}";

                if (t instanceof DegradeException) {
                    msg = "{\"code\": 429, \"msg\": \"AI服务压力过大，已触发熔断保护，请稍后再试\"}";
                }

                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(msg));
            }
        };

        // 注册处理器
        GatewayCallbackManager.setBlockHandler(handler);
    }
}