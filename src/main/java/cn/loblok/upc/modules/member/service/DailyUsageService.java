package cn.loblok.upc.modules.member.service;

import cn.loblok.upc.modules.member.entity.DailyUsage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-10
 */
public interface DailyUsageService extends IService<DailyUsage> {

    DailyUsage selectByUserId(Long userId);

    void insert(DailyUsage dailyUsage);

}
