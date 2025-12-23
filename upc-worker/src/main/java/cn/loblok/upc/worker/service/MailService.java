package cn.loblok.upc.worker.service;

import cn.loblok.upc.api.worker.dto.EmailMsgDTO;
import cn.loblok.upc.common.enums.VerificationCodeType;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private JavaMailSender mailSender;


    public void sendAsync(EmailMsgDTO msgDTO) throws MessagingException {
        // 验证邮箱地址不为空
        if (StringUtils.isBlank(msgDTO.getEmail())) {
            throw new IllegalArgumentException("收件人邮箱地址不能为空");
        }

        // 验证发件人邮箱
        if (StringUtils.isBlank(fromEmail)) {
            throw new IllegalArgumentException("发件人邮箱地址未配置");
        }


        log.info("开始构造邮件 - 发件人: {}, 收件人: {}", fromEmail, msgDTO.getEmail());
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");


        helper.setFrom(fromEmail);
        helper.setTo(msgDTO.getEmail());
        helper.setSubject("UPC平台验证码");

        String operation = "";
        if (VerificationCodeType.login.getType().equals(msgDTO.getType())) {
            operation = "登录账号";
        } else if (VerificationCodeType.register.getType().equals(msgDTO.getType())) {
            operation = "注册账号";
        } else if (VerificationCodeType.forget.getType().equals(msgDTO.getType())) {
            operation = "重置密码";
        } else {
            operation = "相关操作";
        }

        String htmlContent = buildHtmlTemplate(operation, msgDTO.getCode());

        helper.setText(htmlContent, true);


        log.info("邮件内容预览（文本部分）: \n{}",
                "UPC平台验证码\n" +
                        "操作: " + operation + "\n" +
                        "验证码: " + msgDTO.getCode() + "\n" +
                        "有效期: 2分钟");

        // 发送邮件
        log.info("正在发送邮件...");
        mailSender.send(mimeMessage);
        log.info("邮件发送完成");
    }

    /**
     * 构造 HTML 模版（从原单体迁移并精简）
     */
    private String buildHtmlTemplate(String operation, String code) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                    <h2 style="color: #333;">UPC平台验证码</h2>
                    <p>您正在进行 <strong>%s</strong> 操作，验证码如下：</p>
                    <div style="background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 5px;">
                        <h1 style="color: #1890ff; font-size: 32px; letter-spacing: 5px;">%s</h1>
                    </div>
                    <p style="font-size: 12px; color: #666;">此验证码 2 分钟内有效。如非本人操作请忽略。</p>
                </div>
            </body>
            </html>
            """.formatted(operation, code);
    }
}