package cn.loblok.upc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean("pointTaskExecutor")
    public Executor pointTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数 = CPU 核数 * 2（根据签到 QPS 调整）
        executor.setCorePoolSize(4);
        // 最大线程数（防突发流量）
        executor.setMaxPoolSize(16);
        // 队列容量（避免内存爆炸）
        executor.setQueueCapacity(1000);
        // 线程名前缀（便于排查）
        executor.setThreadNamePrefix("point-async-");

        // 拒绝策略：队列满时，由调用线程执行（降级，避免丢数据）

        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // 初始化
        executor.initialize();

//        // 注册到 Micrometer（Spring Boot Actuator）
//        if (executor.getThreadPoolExecutor() != null) {
//            MeterRegistry registry = ...; // 从 ApplicationContext 获取
//            new ThreadPoolExecutorMetrics(executor.getThreadPoolExecutor(), "point_task", Collections.emptyMap())
//                    .bindTo(registry);
//        }

        return executor;
    }
}