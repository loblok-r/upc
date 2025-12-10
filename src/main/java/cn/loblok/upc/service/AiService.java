package cn.loblok.upc.service;

import cn.loblok.upc.dto.AiGenerateRequest;
import cn.loblok.upc.dto.AiGenerateResponse;

public interface AiService {

    /**
     * 核心生成方法（预留，后续替换为真实 AI 调用）
     * @param userId 当前用户 ID（用于记录日志/配额）
     * @param request 生成请求
     * @return 生成结果（Mock 返回假图）
     */
    AiGenerateResponse generate(Long userId, AiGenerateRequest request);
}