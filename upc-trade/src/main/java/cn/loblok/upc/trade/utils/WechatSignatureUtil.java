package cn.loblok.upc.trade.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class WechatSignatureUtil {

    public static String generateSign(Map<String, String> params, String key) {
        // 1. 过滤空值和 sign 字段
        params = params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty() && !"sign".equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 2. 按 key 字典序排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 3. 拼接 key1=value1&key2=value2...
        StringBuilder sb = new StringBuilder();
        for (String k : keys) {
            sb.append(k).append("=").append(params.get(k)).append("&");
        }
        sb.append("key=").append(key); // 最后拼接 key

        // 4. MD5 大写
        return cn.hutool.crypto.digest.DigestUtil.md5Hex(sb.toString()).toUpperCase();
    }
}