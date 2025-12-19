package cn.loblok.upc.modules.member.service.impl;

import cn.loblok.upc.modules.member.entity.DailyUsage;
import cn.loblok.upc.mapper.DailyUsageMapper;
import cn.loblok.upc.modules.member.service.DailyUsageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-10
 */
@Service
public class DailyUsageServiceImpl extends ServiceImpl<DailyUsageMapper, DailyUsage> implements DailyUsageService {

    @Override
    public DailyUsage selectByUserId(Long userId) {
        String today = LocalDate.now().toString(); // "2025-12-11"

        LambdaQueryWrapper<DailyUsage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DailyUsage::getUserId, userId)
                .eq(DailyUsage::getDate, today);

        return this.getOne(queryWrapper);
    }

    @Override
    public void insert(DailyUsage dailyUsage) {
        this.save(dailyUsage);
    }
}
