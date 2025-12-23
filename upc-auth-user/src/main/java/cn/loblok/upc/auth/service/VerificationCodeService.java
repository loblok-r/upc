package cn.loblok.upc.auth.service;

import cn.loblok.upc.common.base.Result;

/**
 * 验证码服务
 */
public interface VerificationCodeService {

    /**
     * 生成验证码
     * @param email 邮箱
     * @param type 验证码类型
     * @return 验证码响应对
     */
    Result generationCode(String email, String type);

    
    /**
     * 校验验证码
     * @param email 邮箱
     * @param varificationCode 验证码
     * @param type 验证码类型
     * @return 验证结果
     */
    boolean checkCode(String email, String varificationCode, String type);


}