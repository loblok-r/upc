package cn.loblok.upc.community.service.impl;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MeiliSearchService {

    private final Client meilisearchClient;

    /**
     * 搜索用户（限制返回字段以节省内存）
     */
    public SearchResult searchUsers(String keyword, int offset, int limit) throws MeilisearchException {
        Index index = meilisearchClient.index("users");

        // 使用 SearchRequest 构造高级搜索
        SearchRequest request = new SearchRequest(keyword)
                .setOffset(offset)
                .setLimit(limit)
                // 只拿必须展示的字段，不 SELECT *
                .setAttributesToRetrieve(new String[]{"id", "username", "avatar_url", "followers"});

        return (SearchResult) index.search(request);
    }

    /**
     * 同步或更新单个用户（用于 RabbitMQ 消费者调用）
     */
    public void syncUser(String userJson) {
        try {
            Index index = meilisearchClient.index("users");
            // Meilisearch 的 addDocuments 是异步的任务，返回一个 task 对象
            index.addDocuments(userJson);
            log.debug("已提交 Meilisearch 同步任务");
        } catch (Exception e) {
            log.error("Meilisearch 同步失败: {}", e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) throws MeilisearchException {
        meilisearchClient.index("users").deleteDocument(userId.toString());
    }
}