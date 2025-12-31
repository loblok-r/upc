package cn.loblok.upc.auth.controller;

import cn.loblok.upc.auth.dto.AiHistoryResponse;
import cn.loblok.upc.auth.service.AiGenerationLogsService;
import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.auth.common.util.PageConverter;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/ai-history")
@AllArgsConstructor
@Slf4j
@Tag(name = "AI生成历史记录接口", description = "AI生成历史记录接口")
public class AiGenerationHistoryController {

    private final AiGenerationLogsService aiGenerationLogsService;

    /**
     * 获取用户历史记录
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户ai生成的历史记录")
    public Result<PageResult<AiHistoryResponse>> getHistoryList(@CurrentUser Long userId,
                                                                @RequestParam(value = "page",defaultValue = "1") Integer page,
                                                                @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){


        log.info("获取用户历史记录");

        IPage<AiHistoryResponse> pageResult = aiGenerationLogsService.getHistoryList(userId,page,pageSize);
        PageResult<AiHistoryResponse> response = PageConverter.toPageResult(pageResult);
        return Result.success(response);
    }

    /**
     * 删除历史记录
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除AI生成历史记录")
    public Result<Boolean> deleteHistory(@PathVariable Long id){
        log.info("删除历史记录，ID: {}", id);
        //todo 删除
//        Boolean result = aiGenerationLogsService.deleteHistoryById(id);
        return Result.success(false);
    }

}
