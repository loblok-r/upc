package cn.loblok.upc.ai.aspect;

import cn.loblok.upc.ai.dto.AiGenerateRequest;
import cn.loblok.upc.ai.service.assist.AiCostCalculator;
import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.common.annotation.RequireAiQuota;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.AppMode;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.common.exception.BusinessException;
import cn.loblok.upc.common.exception.DailyLimitExceededException;
import cn.loblok.upc.common.exception.InsufficientComputingPowerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AiQuotaAspect {

    private final UserFeignClient userFeignClient;
    private final AiCostCalculator costCalculator;


    // Spring 提供的 SpEL 解析器
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();


    @Around("@annotation(quotaConfig)")
    public Object checkQuota(ProceedingJoinPoint joinPoint, RequireAiQuota quotaConfig) throws Throwable {
        // 1. 从方法参数中获取 userId 和 request
        Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 1. 获取动态的 mode
        String modeExpression = quotaConfig.mode();
        AppMode mode;

        if (modeExpression.startsWith("#")) {
            // 解析 SpEL
            EvaluationContext context = new MethodBasedEvaluationContext(joinPoint.getTarget(), method, args, nameDiscoverer);
            String modeStr = parser.parseExpression(modeExpression).getValue(context, String.class);
            mode = AppMode.fromMode(modeStr);
        } else {
            // 如果没写表达式，就取默认逻辑（比如报错或给个默认值）
            throw new BusinessException("AI模式未指定");
        }

        // 2. 获取请求对象计算成本 (假设参数里一定有 AiGenerateRequest)
        AiGenerateRequest req = null;
        Long userId = null;
        for (Object arg : args) {
            if (arg instanceof AiGenerateRequest) req = (AiGenerateRequest) arg;
            if (arg instanceof Long) userId = (Long) arg;
        }

        // 3. 执行校验逻辑
        int cost = costCalculator.calculate(mode, req.getPrompt(), req.getReferenceImage());
        log.info("[动态AOP预检] 用户: {}, 模式: {}, 成本: {}", userId, mode, cost);

        Result<Void> checkResult = userFeignClient.checkAiAccess(userId, mode, cost);

        // 4. 根据结果判断是否抛出异常（利用项目现有的异常类）
        if (checkResult.getCode() != 200) {
            if (checkResult.getCode() == CommonStatusEnum.INSUFFICIENT_COMPUTING_POWER.getCode()) {
                throw new InsufficientComputingPowerException("算力不足，请充值或完成任务获取");
            } else if (checkResult.getCode() == CommonStatusEnum.DAILY_LIMIT_EXCEEDED.getCode()) {
                throw new DailyLimitExceededException("今日免费额度已用完，请明天再来或升级会员");
            }
            throw new BusinessException(checkResult.getMsg());
        }

        return joinPoint.proceed();
    }
}