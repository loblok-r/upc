package cn.loblok.upc.service;

import cn.loblok.upc.dto.GenerateRequest;
import cn.loblok.upc.dto.Result;

import java.util.Map;

public interface AiService {
    /**
     * 核心生成方法（预留，后续替换为真实 AI 调用）
     * @param userId 当前用户 ID（用于记录日志/配额）
     * @param request 生成请求
     * @return 生成结果（Mock 返回假图）
     */
//    Result<Map<String, Object>> generate(Long userId, GenerateRequest request);
//
//
//    Result<Map<String, Object>> callOpenAI(String prompt);
//
//
//    Result<Map<String, Object>> callStabilityAI(String prompt);
}