package cn.loblok.upc.controller;

import cn.loblok.upc.dto.ExpTransactionDTO;
import cn.loblok.upc.dto.PageResult;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.service.ExpTransactionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户经验值流水表 前端控制器
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
@RestController
@RequestMapping("/api/exp-transactions")
public class ExpTransactionController {

    @Autowired
    private ExpTransactionService expTransactionService;

    /**
     * 查询用户经验流水
     *
     * @param userId 用户ID
     * @param page 页码，默认为1
     * @param size 页面大小，默认为20
     * @param bizType 业务类型（可选）
     * @return 经验流水列表
     */
    @GetMapping
    public Result<PageResult<ExpTransactionDTO>> getUserTransactions(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam(value = "userId", required = true) Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "bizType", required = false) String bizType) {

        // TODO: 权限校验，上线前必须补！
        IPage<ExpTransactionDTO> pageResult = expTransactionService.getUserTransactions(tenantId, userId, bizType, page, size);

        PageResult<ExpTransactionDTO> response = PageResult.of(pageResult);
        return Result.success(response);
    }

}