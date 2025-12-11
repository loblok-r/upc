package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.UserItems;
import cn.loblok.upc.enums.UserItemSourceType;
import cn.loblok.upc.enums.UserItemType;
import cn.loblok.upc.mapper.UserItemsMapper;
import cn.loblok.upc.service.UserItemsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户虚拟道具/资产表（不含优惠券） 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-10
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserItemsServiceImpl extends ServiceImpl<UserItemsMapper, UserItems> implements UserItemsService {

    private final UserItemsMapper userItemMapper;
    @Override
    public List<UserItems> getByUserId(Long userId, UserItemType userItemType) {
        return this.lambdaQuery()
                .eq(UserItems::getUserId, userId)
                .eq(UserItems::getItemType, userItemType)
                .list();
    }

    @Override
    public void addItem(Long userId, UserItemType itemType, UserItemSourceType sourceType,
                        String sourceId, Map<String, Object> extra, Integer quantity) {

        // 1. 标准化 extra 为 JSON 字符串（空时用 "{}"）
        String extraJson = (extra == null || extra.isEmpty()) ? "{}" : cn.hutool.json.JSONUtil.toJsonStr(extra);

        // 2. 查找是否存在【完全相同】的记录（userId + itemType + extraJson）
        UserItems existing = this.lambdaQuery()
                .eq(UserItems::getUserId, userId)
                .eq(UserItems::getItemType, itemType)
                .eq(UserItems::getExtra, extraJson)
                .one();

        if (existing == null) {
            // 新增
            UserItems item = new UserItems();
            item.setUserId(userId);
            item.setItemType(itemType);
            item.setQuantity(quantity);
            item.setSourceType(sourceType);
            item.setSourceId(sourceId);
            item.setExtra(extraJson);
            item.setValidFrom(LocalDateTime.now());
            // 可设置默认过期时间，例如30天
            item.setValidTo(LocalDateTime.now().plusDays(30));
            this.save(item);
        } else {
            // 合并数量
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setSourceType(sourceType);
            existing.setSourceId(sourceId);
            this.updateById(existing);
        }
    }

    @Override
    public int getTotalChances(Long userId, UserItemType itemType) {

        log.info("开始获取道具{}次数: userId={}", itemType,userId);
        if (userId == null) {
            return 0;
        }

        List<UserItems> userItems = this.lambdaQuery()
                .eq(UserItems::getUserId, userId)
                .eq(UserItems::getItemType, itemType)
                .list();

        return userItems.stream()
                .mapToInt(UserItems::getQuantity)
                .sum();
    }

    @Override
    public int consumeOneChanceWithOptimisticLock(Long id) {
        int i = userItemMapper.consumeOneChance(id);

        log.info("产品{} 扣减结果: {}", id, i);
        return i;
    }


}
