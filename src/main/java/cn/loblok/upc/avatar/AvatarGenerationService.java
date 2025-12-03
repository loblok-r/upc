package cn.loblok.upc.avatar;

import cn.loblok.upc.common.dto.AvatarResult;
import cn.loblok.upc.common.dto.GenerateRequest;

public interface AvatarGenerationService {
    /**
     * 核心生成方法（预留，后续替换为真实 AI 调用）
     * @param userId 当前用户 ID（用于记录日志/配额）
     * @param request 生成请求
     * @return 生成结果（Mock 返回假图）
     */
    AvatarResult generate(Long userId, GenerateRequest request);
}