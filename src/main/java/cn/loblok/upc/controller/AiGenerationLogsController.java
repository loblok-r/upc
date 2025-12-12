package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.AiHistoryResponse;
import cn.loblok.upc.dto.PageResult;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.service.AiGenerationLogsService;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * AI 生成请求日志表 前端控制器
 * </p>
 *
 * @author loblok
 * @since 2025-12-12
 */
@RestController
@RequestMapping("/api/history")
@AllArgsConstructor
@Slf4j
public class AiGenerationLogsController {

    private final AiGenerationLogsService aiGenerationLogsService;

    /**
     * 获取用户历史记录
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/list")
    public Result<PageResult<AiHistoryResponse>> getHistoryList(@CurrentUser Long userId,
                                                                @RequestParam Integer page,
                                                                @RequestParam Integer pageSize){


        log.info("获取用户历史记录");

        IPage<AiHistoryResponse> pageResult = aiGenerationLogsService.getHistoryList(userId,page,pageSize);
        PageResult<AiHistoryResponse> response = PageResult.of(pageResult);
        return Result.success(response);
    }

}
