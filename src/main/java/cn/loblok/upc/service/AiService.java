package cn.loblok.upc.service;

import cn.loblok.upc.dto.AiGenerateRequest;
import cn.loblok.upc.dto.AiGenerateResponse;
import cn.loblok.upc.dto.InvoiceSummaryRequest;
import cn.loblok.upc.dto.SummaryResponse;

public interface AiService {

    /**
     * 核心生成方法（预留，后续替换为真实 AI 调用）
     * @param userId 当前用户 ID（用于记录日志/配额）
     * @param request 生成请求
     * @return 生成结果（Mock 返回假图）
     */
    AiGenerateResponse generate(Long userId, AiGenerateRequest request);

    /**
     * 创建发票摘要
     * @param userId 当前用户 ID（用于记录日志/配额）
     * @param request 创建发票摘要
     * @return 创建结果（Mock 创建假摘要）
     */
    String generateInvoiceSummary(Long userId, InvoiceSummaryRequest request);
}