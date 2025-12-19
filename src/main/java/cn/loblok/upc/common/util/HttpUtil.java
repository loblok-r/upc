package cn.loblok.upc.common.util;

import okhttp3.*;

import java.io.IOException;

public class HttpUtil {

    private static final OkHttpClient client = new OkHttpClient();

    public static String post(String url, String xmlBody) {
        RequestBody body = RequestBody.create(xmlBody, MediaType.get("application/xml; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("HTTP 请求失败: " + response.code());
            }
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException("网络请求异常", e);
        }
    }
}