package cn.loblok.upc.ai.service;

import cn.loblok.upc.ai.dto.AiResult;

import java.io.InputStream;

public interface FileStorageService {
    /**
     * 上传图片并返回访问对象
     */
    AiResult uploadImage(Long userId, InputStream inputStream, String originalFilename, int expireMinutes);
}