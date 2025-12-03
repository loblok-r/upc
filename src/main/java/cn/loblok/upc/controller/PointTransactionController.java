package cn.loblok.upc.controller;

import cn.loblok.upc.dto.PageResult;
import cn.loblok.upc.dto.PointTransactionDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.service.PointTransactionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户积分流水表 前端控制器
 * </p>
 *
 * @author loblok
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/point-transactions")
public class PointTransactionController {

    @Autowired
    private PointTransactionService pointTransactionService;

    /**
     * 查询用户积分流水
     *
     * @param userId 用户ID
     * @param page 页码，默认为1
     * @param size 页面大小，默认为20
     * @param bizType 业务类型（可选）
     * @return 积分流水列表
     */
    @GetMapping
    public Result<PageResult<PointTransactionDTO>> getUserTransactions(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam(value = "userId", required = true) Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "bizType", required = false) String bizType) {

        // TODO: 权限校验`，上线前必须补！
        IPage<PointTransactionDTO> pageResult = pointTransactionService.getUserTransactions(tenantId, userId, bizType, page, size);

        PageResult<PointTransactionDTO> response = PageResult.of(pageResult);
        return Result.success(response);
    }

}
