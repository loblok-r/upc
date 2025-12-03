package cn.loblok.upc.membershiporder;

import cn.loblok.upc.pay.client.WechatPayClient;
import cn.loblok.upc.common.config.WechatPayConfig;
import cn.loblok.upc.common.dto.PayParamsResponse;
import cn.loblok.upc.common.dto.Result;
import cn.loblok.upc.common.dto.WechatNativePayResponse;
import cn.loblok.upc.common.dto.WechatUnifiedOrderRequest;
import cn.loblok.upc.user.User;
import cn.loblok.upc.common.enums.MembershipOrderStatus;
import cn.loblok.upc.common.enums.MembershipType;
import cn.loblok.upc.common.exception.BusinessException;
import cn.loblok.upc.user.UserService;
import cn.loblok.upc.common.util.WechatSignatureUtil;
import cn.loblok.upc.common.util.XmlUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-03
 */
@Service
@Primary
@Slf4j
public class MembershipOrderServiceImpl extends ServiceImpl<MembershipOrderMapper, MembershipOrder> implements MembershipOrderService {

    @Autowired
    private MembershipOrderMapper orderMapper;
    
    @Autowired
    private WechatPayClient wechatPayClient;
    
    @Autowired
    private UserService userService;

    @Override
    public Result<?> createOrder(Long userId, String membershipType) {
        // 参数校验
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        
        if (membershipType == null || membershipType.isEmpty()) {
            return Result.error("会员类型不能为空");
        }
        
        // 创建会员订单
        MembershipOrder order = new MembershipOrder();
        order.setUserId(userId);
        order.setType(membershipType.toUpperCase());
        
        try {
            // 根据会员类型设置价格和有效期
            MembershipType type = MembershipType.fromCode(membershipType);
            order.setAmount(type.getPrice());
            order.setDurationDays(type.getDurationDays());
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
        
        // 设置订单状态为待支付
        order.setStatus(MembershipOrderStatus.CREATED);
        
        // 生成订单号
        String orderNo = "MEM" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                        + String.format("%06d", userId % 1000000);
        order.setOrderNo(orderNo);
        
        // 设置创建时间
        order.setCreatedAt(LocalDateTime.now());
        
        // 保存订单
        boolean saved = this.save(order);
        if (saved) {
            return Result.success(order);
        } else {
            return Result.error("创建订单失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PayParamsResponse generatePayParams(String orderNo) {
        // 1. 查订单
        MembershipOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if(!order.getStatus().canTransitionTo(MembershipOrderStatus.PAID)){
             throw new BusinessException("订单状态异常");
        }

        // 3. 调用微信统一下单 API（Native模式）
        WechatUnifiedOrderRequest request = WechatUnifiedOrderRequest.builder()
                .outTradeNo(order.getOrderNo())           // 商户订单号
                .totalFee(order.getAmount().multiply(BigDecimal.valueOf(100)).intValue()) // 单位：分
                .description("会员订阅-" + order.getType())
                .spbillCreateIp("127.0.0.1")             // 实际应取用户 IP
                .notifyUrl("https://yourdomain.com/api/membership/notify/wechat") // 回调地址
                .tradeType("NATIVE")                      // Native模式
                .build();

        WechatNativePayResponse response = wechatPayClient.unifiedOrderNative(request);

        return PayParamsResponse.builder()
                .orderNo(orderNo)
                .amount(order.getAmount())
                .codeUrl(response.getCodeUrl())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatPayNotify(String xmlData) {
        // 1. 解析 XML
        Map<String, String> notifyMap = XmlUtils.toMap(xmlData);

        // 2. 验签（最关键！）
        if (!verifySignature(notifyMap)) {
            throw new SecurityException("微信通知验签失败");
        }

        // 3. 检查通信和业务结果
        String returnCode = notifyMap.get("return_code");
        String resultCode = notifyMap.get("result_code");
        if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
            log.warn("微信支付失败: {}", notifyMap.get("err_code_des"));
            return; // 不处理失败订单（可选）
        }

        // 4. 提取关键信息
        String outTradeNo = notifyMap.get("out_trade_no"); // 商户订单号
        String totalFeeStr = notifyMap.get("total_fee");   // 实际支付金额（分）
        String transactionId = notifyMap.get("transaction_id"); // 微信支付订单号

        // 5. 查询订单
        MembershipOrder order = orderMapper.selectByOrderNo(outTradeNo);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + outTradeNo);
        }

        // 6. 幂等性检查：避免重复处理
        if (MembershipOrderStatus.PAID.equals(order.getStatus())) {
            log.info("订单已支付，跳过重复通知: {}", outTradeNo);
            return;
        }

        // 7. 金额校验（防止篡改）
        Integer actualAmount = Integer.valueOf(totalFeeStr); // 分
        Integer expectedAmount = order.getAmount().multiply(BigDecimal.valueOf(100)).intValue();
        if (!actualAmount.equals(expectedAmount)) {
            log.error("支付金额不匹配! 订单: {}, 期望: {}分, 实际: {}分", outTradeNo, expectedAmount, actualAmount);
            throw new RuntimeException("金额校验失败");
        }

        // 8. 更新订单状态
        order.setStatus(MembershipOrderStatus.PAID);
        order.setTransactionId(transactionId);
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 9. 开通会员（核心业务）
        activateMembership(order.getUserId(), order.getType());

        log.info("订单支付成功: {}, 微信单号: {}", outTradeNo, transactionId);
    }

    @Override
    public void activateMembership(Long userId, String type) {
        // 1. 查询用户
        User user = userService.getById(userId);
        // 2. 设置会员过期时间（例如 30 天）
        LocalDateTime expireAt = LocalDateTime.now().plusDays(1);
        user.setIsPermanentMember(false);
        if(type.equals(MembershipType.PERMANENT.getCode())){
            user.setIsPermanentMember(true);
            expireAt = null;
        }else if (type.equals(MembershipType.MONTHLY.getCode())){
            expireAt = LocalDateTime.now().plusDays(30);
        }
        // 3. 更新用户表
        user.setMemberExpireAt(expireAt);
        userService.updateById(user);
        // 4. 记录日志 or 发送消息（可选）
        log.info("用户 {} 开通 {} 会员，有效期至 {}", userId, type, expireAt);
    }


    private boolean verifySignature(Map<String, String> params) {
        // 1. 取出 sign
        String signFromWechat = params.get("sign");
        if (signFromWechat == null) return false;

        // 2. 重新计算签名（排除 sign 字段）
        String signGenerated = WechatSignatureUtil.generateSign(params, WechatPayConfig.API_KEY);

        // 3. 比较
        return signGenerated.equals(signFromWechat);
    }
}