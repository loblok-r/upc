package cn.loblok.upc.membershiporder;

import cn.loblok.upc.common.dto.PayParamsResponse;
import cn.loblok.upc.common.dto.Result;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-03
 */
public interface MembershipOrderService extends IService<MembershipOrder> {

    /**
     * 创建订单
     * @param id 用户ID
     * @param membershipType 会员类型
     * @return 订单信息
     */
    Result<?> createOrder(Long id, String membershipType);
    
    /**
     * 生成支付参数
     * @param orderNo 订单号
     * @return 支付参数
     */
    PayParamsResponse generatePayParams(String orderNo);

    /**
     * 微信支付回调处理
     * @param xmlData 微信回调数据
     */
    void handleWechatPayNotify(String xmlData);


    /**
     * 开通会员
     * @param userId 用户ID
     * @param type 会员类型
     */
    void activateMembership(Long userId, String type);

}