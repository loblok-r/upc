package cn.loblok.upc.controller;

import cn.loblok.upc.constraints.SendVerificationCodeGroup;
import cn.loblok.upc.cto.VerificationCodeCTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class VerificationCodeController {

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 获取验证码
     * @param verificationCodeCTO 验证码请求参数
     * @return 验证码响应对象
     */
    @PostMapping("/sendCode")
    public Result sendCode(@Validated(SendVerificationCodeGroup.class)
                           @RequestBody VerificationCodeCTO verificationCodeCTO) {
        log.info("用户请求发送验证码: {}", verificationCodeCTO);
        
        // 验证邮箱不为空
        if (!StringUtils.hasText(verificationCodeCTO.getEmail())) {
            return Result.error(400, "邮箱地址不能为空");
        }
        
        return verificationCodeService.generationCode(verificationCodeCTO.getEmail(), verificationCodeCTO.getType());
    }
}