package cn.loblok.upc.worker.service;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    private final Client meilisearchClient;

    private final ObjectMapper objectMapper; // Spring 自带的 JSON 转化器

    /**
     * 同步数据到 Meilisearch
     */
    public void upsertUser(Object userDto) {
        try {
            Index index = meilisearchClient.index("users");
            // 将 DTO 转为 JSON 字符串
            String json = objectMapper.writeValueAsString(userDto);
            // Meilisearch addDocuments 是异步的，会返回 task,（存在即更新，不存在即插入
            index.addDocuments(json);
            log.info("Meilisearch 数据同步成功 (UPSERT)");
        } catch (Exception e) {
            log.error("Meilisearch 同步失败: {}", e.getMessage());
            throw new RuntimeException("MeiliSyncError"); // 抛出异常触发 MQ 重试
        }
    }

    /**
     * 从 Meilisearch 删除数据
     */
    public void deleteUser(Long userId) {
        try {
            meilisearchClient.index("users").deleteDocument(userId.toString());
            log.info("Meilisearch 数据删除成功: id={}", userId);
        } catch (Exception e) {
            log.error("Meilisearch 删除失败: {}", e.getMessage());
        }
    }
}