package cn.loblok.upc.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
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

        initCustomApiDefs();
        // 1. 加载限流规则
        initGatewayRules();
        // 2. 加载熔断规则
        initDegradeRules();
        // 3. 设置自定义异常处理逻辑
        initBlockHandler();
    }

    private void initCustomApiDefs() {
        Set<ApiDefinition> definitions = new HashSet<>();
        // 定义一个名为 "user_login_api" 的组，包含登录和注册接口
        ApiDefinition loginApi = new ApiDefinition("user_login_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/api/user/login"));
                    add(new ApiPathPredicateItem().setPattern("/api/user/register"));
                    add(new ApiPathPredicateItem().setPattern("/api/user/sendCode"));
                }});


        ApiDefinition chatApi = new ApiDefinition("chat_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/api/chat/completions"));
                }});


        definitions.add(loginApi);
        definitions.add(chatApi);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 1.  AI 模块限流
        rules.add(new GatewayFlowRule("ai-service-route")
                .setCount(10) // 每秒允许10个请求
                .setIntervalSec(1));

        // 2. 针对登录/注册/验证码的 IP 级限流
        rules.add(new GatewayFlowRule("user_login_api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(3)             // 阈值次数
                .setIntervalSec(60)      // 统计窗口：60秒
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
                )
        );

        // 3. 针对客服聊天接口的 IP 级限流
        rules.add(new GatewayFlowRule("chat_api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(8)             // 阈值次数：10次
                .setIntervalSec(60)      // 统计窗口：60秒
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
                )
        );
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
                    msg = "{\"code\": 429, \"msg\": \"服务维护中，请稍后再试\"}";
                } else if (t.getMessage().contains("flow")) { // 简单的流控判断
                    msg = "{\"code\": 429, \"msg\": \"操作太快了，请喝杯咖啡休息下\"}";
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