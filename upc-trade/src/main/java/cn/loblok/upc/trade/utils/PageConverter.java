package cn.loblok.upc.trade.utils;

import cn.loblok.upc.common.base.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

public class PageConverter {

    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), (int)page.getCurrent(), (int)page.getSize());
    }

}