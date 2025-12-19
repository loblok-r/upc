package cn.loblok.upc.modules.payment.controller;

import cn.loblok.upc.modules.member.service.MembershipOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信支付异步通知处理控制器
 */
@RestController
@Slf4j
public class WechatPayNotifyController {

    @Autowired
    private MembershipOrderService membershipOrderService;

    /**
     * 微信支付异步通知接收接口
     * URL 必须和统一下单时传的 notify_url 一致
     */
    @PostMapping("/api/membership/notify/wechat")
    public String handleWechatNotify(@RequestBody String xmlData) {
        log.info("收到微信支付通知: {}", xmlData);

        try {
            // 处理通知
            membershipOrderService.handleWechatPayNotify(xmlData);
            // 成功必须返回 SUCCESS，否则微信会一直重试
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
        } catch (Exception e) {
            log.error("处理微信通知失败", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[ERROR]]></return_msg></xml>";
        }
    }
}