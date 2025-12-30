package cn.loblok.upc.common.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> {
    @Schema(description = "数据列表")
    private List<T> list;
    @Schema(description = "数据总数")
    private long total;
    @Schema(description = "当前页码")
    private int pageNum;
    @Schema(description = "每页数据量")
    private int pageSize;
    @Schema(description = "总页数")
    private int totalPages;

    // 不再提供 .of(IPage) 方法，改为全参构造或 Builder
    public PageResult(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        // 自动计算总页数
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    public PageResult() {}
}