package cn.loblok.upc.auth.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.loblok.rabbit.util.rabbit.constants.MQConstants;
import cn.loblok.upc.api.worker.dto.EmailMsgDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.auth.service.VerificationCodeService;
import cn.loblok.upc.common.utils.KeyUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Result generationCode(String email, String type) {
        // 验证邮箱地址不为空
        if (StringUtils.isBlank(email)) {
            return Result.error(CommonStatusEnum.ERROR.getCode(), "邮箱地址不能为空");
        }

        log.info("开始发送验证码 - 邮箱: {}, 类型: {}", email, type);

        int numbercode = getNumberCode(6);

        String key = KeyUtils.buildVerificationCodeKey(email,type);

        log.info("登录 验证码的 key:"+key);
        log.info("Redis连接工厂: {}", stringRedisTemplate.getConnectionFactory());
        log.info("Redis模板配置: {}", stringRedisTemplate);


        stringRedisTemplate.opsForValue().set(key,numbercode+"",2, TimeUnit.MINUTES);


        EmailMsgDTO msg = EmailMsgDTO.builder()
                .email(email)
                .code(String.valueOf(numbercode))
                .type(type)
                .build();

        // 调用第三方发送到邮箱
        try {
            String bizId = IdUtil.randomUUID();
            CorrelationData correlationData = new CorrelationData(bizId);
            // 发送到定义的交换机和 RoutingKey
            rabbitTemplate.convertAndSend(
                    MQConstants.EXCHANGE_NAME,
                    MQConstants.ROUTE_EMAIL,
                    msg,
                    message -> {
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    },
                    correlationData
            );
            log.info("验证码请求已进入队列: {}", email);
            return Result.success("验证码已发送至您的邮箱");
        } catch (Exception e) {
            log.error("消息队列投递失败", e);
            return Result.error(CommonStatusEnum.ERROR.getCode(), "验证码服务繁忙");
        }
    }


    @Override
    public boolean checkCode(String email, String varificationCode, String type) {
        // 验证邮箱地址不为空
        if (StringUtils.isBlank(email)) {
            return false;
        }

        String key = KeyUtils.buildVerificationCodeKey(email, type);

        log.info("checkcode取出的验证码key为："+key);
        String redisCode = stringRedisTemplate.opsForValue().get(key);

        log.info("varificationCode"+varificationCode);

        log.info("redisCode:"+redisCode);


        //校验数据

        if(StringUtils.isBlank(redisCode)){
            return false;
        }

        if(!varificationCode.trim().equals(redisCode.trim())){
            return false;
        }
        // 验证成功后删除验证码，防止重复使用
//        stringRedisTemplate.delete(key);
        return true;
    }


    public int getNumberCode(int size){
        double randomnumble = (Math.random()*9+1)*(Math.pow(10,size-1));
        int numbercode = (int)randomnumble;

        log.info("获取数字验证码: {}", numbercode);
        return numbercode;
    }


}