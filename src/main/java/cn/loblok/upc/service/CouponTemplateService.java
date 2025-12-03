package cn.loblok.upc.service;

import cn.loblok.upc.entity.CouponTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *   管理优惠券模板（配置） 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
public interface CouponTemplateService extends IService<CouponTemplate> {

    /**
     * 创建优惠券模板
     * @param template 优惠券模板
     */
    void createTemplate(CouponTemplate template);

    /**
     * 根据优惠券模板编码获取模板详情
     * @param templateCode 优惠券模板编码
     * @return 优惠券模板详情
     */
    CouponTemplate getByCode(String templateCode);

    /**
     * 根据活动编码获取模板详情
     * @param activityCode 活动编码
     * @return 优惠券模板详情
     */
    CouponTemplate getByActivityCode(String activityCode);


    // 提供：创建模板、修改库存、查询模板详情等

}
