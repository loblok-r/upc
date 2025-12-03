package cn.loblok.upc.client;

import cn.loblok.upc.config.WechatPayConfig;
import cn.loblok.upc.dto.WechatNativePayResponse;
import cn.loblok.upc.dto.WechatUnifiedOrderRequest;
import cn.loblok.upc.util.HttpUtil;
import cn.loblok.upc.util.WechatSignatureUtil;
import cn.loblok.upc.util.XmlUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WechatPayClient {

    private static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    public WechatNativePayResponse unifiedOrderNative(WechatUnifiedOrderRequest request) {
        // 1. 构建参数 Map
        Map<String, String> params = new HashMap<>();
        params.put("appid", WechatPayConfig.APP_ID);
        params.put("mch_id", WechatPayConfig.MCH_ID);
        params.put("nonce_str", UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        params.put("body", request.getDescription());
        params.put("out_trade_no", request.getOutTradeNo());
        params.put("total_fee", String.valueOf(request.getTotalFee()));
        params.put("spbill_create_ip", request.getSpbillCreateIp());
        params.put("notify_url", request.getNotifyUrl());
        params.put("trade_type", request.getTradeType()); // "NATIVE"

        // 2. 生成签名
        String sign = WechatSignatureUtil.generateSign(params, WechatPayConfig.API_KEY);
        params.put("sign", sign);

        // 3. 转 XML 并发送请求
        String xmlRequest = XmlUtils.toXml(params);
        String xmlResponse = HttpUtil.post(UNIFIED_ORDER_URL, xmlRequest);

        // 4. 解析响应
        Map<String, String> respMap = XmlUtils.toMap(xmlResponse);

        String returnCode = respMap.get("return_code");
        String resultCode = respMap.get("result_code");

        if (!"SUCCESS".equals(returnCode)) {
            throw new RuntimeException("微信通信失败: " + respMap.get("return_msg"));
        }
        if (!"SUCCESS".equals(resultCode)) {
            throw new RuntimeException("微信业务失败: " + respMap.get("err_code_des") + " (" + respMap.get("err_code") + ")");
        }

        // 5. 提取 code_url
        String codeUrl = respMap.get("code_url");
        if (codeUrl == null || codeUrl.isEmpty()) {
            throw new RuntimeException("微信未返回 code_url");
        }

        return new WechatNativePayResponse(codeUrl);
    }
}