package cn.loblok.upc.common.base;

import lombok.Data;

import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
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