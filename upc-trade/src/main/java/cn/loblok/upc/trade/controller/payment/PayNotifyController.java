package cn.loblok.upc.trade.controller.payment;

import cn.loblok.upc.trade.service.TOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付通知控制器
 * 处理来自第三方支付平台的异步通知
 */
@RestController
@RequestMapping("/api/payment/notify")
@Slf4j
@Tag(name = "支付通知", description = "支付通知")
public class PayNotifyController {

    @Autowired
    private TOrderService tOrderService;

    /**
     * 支付宝异步通知处理
     *
     * @param
     * @return 处理结果
     */
    @PostMapping("/alipay")
    @Operation(summary = "支付宝异步通知处理")
    public String handleAlipayNotify(HttpServletRequest request) {
        // 将 Request 中的参数转为 Map
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }
        try {
            return tOrderService.handleAliPayNotify(params);
        } catch (Exception e) {
            log.error("支付宝回调验签异常", e);
        }

        return "fail";
    }

    /**
     * 微信支付异步通知接收接口
     * URL 必须和统一下单时传的 notify_url 一致
     */
    @PostMapping("/wechat")
    @Operation(summary = "微信支付异步通知接收接口")
    public String handleWechatNotify(@RequestBody String xmlData) {
        log.info("收到微信支付通知: {}", xmlData);

        try {
            // 处理通知
            tOrderService.handleWechatPayNotify(xmlData);
            // 成功必须返回 SUCCESS，否则微信会一直重试
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
        } catch (Exception e) {
            log.error("处理微信通知失败", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[ERROR]]></return_msg></xml>";
        }
    }
}