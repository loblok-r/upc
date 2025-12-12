package cn.loblok.upc.service;

import cn.loblok.upc.dto.AiHistoryResponse;
import cn.loblok.upc.entity.AiGenerationLogs;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AI 生成请求日志表 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-12
 */
public interface AiGenerationLogsService extends IService<AiGenerationLogs> {

    /**
     * 获取用户历史记录
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 页大小
     * @return 用户历史记录
     */
    IPage<AiHistoryResponse> getHistoryList(Long userId, Integer page, Integer pageSize);

}
