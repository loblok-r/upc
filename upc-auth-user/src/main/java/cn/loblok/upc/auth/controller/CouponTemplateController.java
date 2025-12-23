package cn.loblok.upc.auth.controller;

import cn.loblok.upc.auth.entity.CouponTemplate;
import cn.loblok.upc.auth.service.CouponTemplateService;
import cn.loblok.upc.common.base.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *      优惠券模板 前端控制器
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
@RestController
@RequestMapping("/api/coupon-templates")
public class CouponTemplateController {

    @Autowired
    private CouponTemplateService couponTemplateService;

    /**
     * 创建优惠券模板
     * @param template 优惠券模板信息
     * @return 创建结果
     */
    @PostMapping
    public Result<Void> createTemplate(@RequestBody CouponTemplate template) {
        try {
            couponTemplateService.createTemplate(template);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("创建优惠券模板失败: " + e.getMessage());
        }
    }

    /**
     * 根据模板编码获取优惠券模板详情
     * @param templateCode 模板编码
     * @return 优惠券模板详情
     */
    @GetMapping("/{templateCode}")
    public Result<CouponTemplate> getByCode(@PathVariable String templateCode) {
        try {
            CouponTemplate template = couponTemplateService.getByCode(templateCode);
            if (template == null) {
                return Result.error("未找到对应编码的优惠券模板");
            }
            return Result.success(template);
        } catch (Exception e) {
            return Result.error("查询优惠券模板失败: " + e.getMessage());
        }
    }
}