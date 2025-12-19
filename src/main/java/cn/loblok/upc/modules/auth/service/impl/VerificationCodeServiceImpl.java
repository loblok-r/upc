package cn.loblok.upc.modules.auth.service.impl;

import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.modules.auth.enums.VerificationCodeType;
import cn.loblok.upc.modules.auth.service.VerificationCodeService;
import cn.loblok.upc.common.util.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private String mailPort;

    @Override
    public Result generationCode(String email, String type) {
        // 验证邮箱地址不为空
        if (StringUtils.isBlank(email)) {
            return Result.error(CommonStatusEnum.ERROR.getCode(), "邮箱地址不能为空");
        }

        log.info("开始发送验证码 - 邮箱: {}, 类型: {}", email, type);
        log.info("邮件配置 - 发件人: {}, SMTP服务器: {}, 端口: {}", fromEmail, mailHost, mailPort);

        int numbercode = getNumberCode(6);

        String key = RedisUtils.buildVerificationCodeKey(email,type);

        log.info("登录 验证码的 key:"+key);
        log.info("Redis连接工厂: {}", stringRedisTemplate.getConnectionFactory());
        log.info("Redis模板配置: {}", stringRedisTemplate);


        stringRedisTemplate.opsForValue().set(key,numbercode+"",2, TimeUnit.MINUTES);

        // 调用第三方发送到邮箱
        try {
            sendEmail(email, numbercode, type);
            log.info("邮件发送成功 - 邮箱: {}, 验证码: {}", email, numbercode);
            return Result.success("验证码已发送至您的邮箱");
        } catch (Exception e) {
            log.error("发送邮件验证码失败", e);
            return Result.error(CommonStatusEnum.ERROR.getCode(), "验证码发送失败，请稍后重试");
        }
    }


    @Override
    public boolean checkCode(String email, String varificationCode, String type) {
        // 验证邮箱地址不为空
        if (StringUtils.isBlank(email)) {
            return false;
        }

        String key = RedisUtils.buildVerificationCodeKey(email, type);

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

    /**
     * 发送邮件验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @param type 验证码类型
     */
    private void sendEmail(String email, int code, String type) throws MessagingException {
        // 验证邮箱地址不为空
        if (StringUtils.isBlank(email)) {
            throw new IllegalArgumentException("收件人邮箱地址不能为空");
        }

        // 验证发件人邮箱
        if (StringUtils.isBlank(fromEmail)) {
            throw new IllegalArgumentException("发件人邮箱地址未配置");
        }


        log.info("开始构造邮件 - 发件人: {}, 收件人: {}", fromEmail, email);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");


        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("UPC平台验证码");

        String operation = "";
        if (VerificationCodeType.login.getType().equals(type)) {
            operation = "登录账号";
        } else if (VerificationCodeType.register.getType().equals(type)) {
            operation = "注册账号";
        } else if (VerificationCodeType.forget.getType().equals(type)) {
            operation = "重置密码";
        } else {
            operation = "相关操作";
        }

        String content = """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                    <h2 style="color: #333; text-align: center;">UPC平台验证码</h2>
                    <p>尊敬的UPC用户，您好！</p>
                    <p>您正在进行 <strong>%s</strong> 操作，验证码如下：</p>
                    <div style="background-color: #f8f9fa; padding: 20px; text-align: center; margin: 20px 0; border-radius: 5px;">
                        <h1 style="color: #1890ff; font-size: 36px; letter-spacing: 10px; margin: 0;">%s</h1>
                    </div>
                    <p><strong>请注意：</strong></p>
                    <ul>
                        <li>此验证码有效期为 <strong>2分钟</strong></li>
                        <li>请勿将验证码泄露给他人</li>
                        <li>如果您没有进行此操作，请忽略此邮件</li>
                    </ul>
                    <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;">
                    <p style="color: #666; font-size: 12px; text-align: center;">
                        此邮件由系统自动发送，请勿回复<br>
                        © 2025 UPC平台 版权所有
                    </p>
                </div>
            </body>
            </html>
            """.formatted(operation, String.valueOf(code));

        helper.setText(content, true);


        log.info("邮件内容预览（文本部分）: \n{}",
                "UPC平台验证码\n" +
                        "操作: " + operation + "\n" +
                        "验证码: " + code + "\n" +
                        "有效期: 2分钟");

        // 发送邮件
        log.info("正在发送邮件...");
        mailSender.send(mimeMessage);
        log.info("邮件发送完成");
    }
}