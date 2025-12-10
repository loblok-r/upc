package cn.loblok.upc.mapper;

import cn.loblok.upc.entity.UserItems;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.data.repository.query.Param;

/**
 * <p>
 * 用户虚拟道具/资产表（不含优惠券） Mapper 接口
 * </p>
 *
 * @author loblok
 * @since 2025-12-10
 */
public interface UserItemsMapper extends BaseMapper<UserItems> {

    /**
     * 扣减抽奖次数
     *
     * @param Id
     * @return
     */
    int consumeOneChance(@Param("Id") Long Id);

}
