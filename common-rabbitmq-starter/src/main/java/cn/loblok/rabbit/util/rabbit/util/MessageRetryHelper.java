package cn.loblok.rabbit.util.rabbit.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRetryHelper {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 通用消息处理模板
     *
     * @param message         原始消息
     * @param channel         RabbitMQ 通道
     * @param businessLogic   业务逻辑
     * @param retryExchange   重试交换机名称
     * @param retryRoutingKey 重试路由键
     * @param maxRetryCount   最大重试次数
     */
    public void processWithRetry(
            Message message,
            com.rabbitmq.client.Channel channel,
            Runnable businessLogic,
            String retryExchange,
            String retryRoutingKey,
            int maxRetryCount) {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            businessLogic.run();
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            Integer retryCount = Optional.ofNullable(
                            (Integer) message.getMessageProperties().getHeader("x-retry-count"))
                    .orElse(0);

            if (retryCount < maxRetryCount) {
                int newRetryCount = retryCount + 1;
                log.warn("消息将第 {} 次重试，routingKey: {}", newRetryCount, retryRoutingKey);

                // 直接在当前 message 对象中设置新的 Header
                message.getMessageProperties().setHeader("x-retry-count", newRetryCount);

                // 调用最简单的 send 方法，不使用容易产生歧义的 Lambda 处理器
                rabbitTemplate.send(
                        retryExchange,
                        retryRoutingKey,
                        message
                );

                // ACK 原消息
                safeAck(channel, deliveryTag);
            } else {
                log.error("超过最大重试次数（{}），进入死信队列", maxRetryCount, e);
                safeNack(channel, deliveryTag, false); // requeue = false → DLQ
            }
        }
    }

    private void safeAck(com.rabbitmq.client.Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("ACK 失败, deliveryTag={}", deliveryTag, e);
        }
    }

    private void safeNack(com.rabbitmq.client.Channel channel, long deliveryTag, boolean requeue) {
        try {
            channel.basicNack(deliveryTag, false, requeue);
        } catch (IOException e) {
            log.error("NACK 失败, deliveryTag={}", deliveryTag, e);
        }
    }
}