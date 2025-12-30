package cn.loblok.upc.community.service.impl;

import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.community.entity.Posts;
import cn.loblok.upc.community.mapper.PostsMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostCacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private PostsMapper postsMapper;

    public List<Posts> getPostsBatch(List<Long> postIds) {
        if (CollectionUtils.isEmpty(postIds)) return Collections.emptyList();

        // 1. 批量构造 Keys
        List<String> keys = postIds.stream().map(id -> KeyUtils.buildPostDetailKey(id)).toList();

        // 2. 一次网络请求获取所有详情
        List<Object> cachedList = redisTemplate.opsForValue().multiGet(keys);

        List<Posts> results = new ArrayList<>();
        List<Long> missIds = new ArrayList<>();

        for (int i = 0; i < postIds.size(); i++) {
            Object data = cachedList.get(i);
            if (data != null) {
                results.add((Posts) data); // 序列化后的对象
            } else {
                missIds.add(postIds.get(i));
            }
        }

        // 3. 补齐缓存未命中的数据,缓存旁路
        if (!missIds.isEmpty()) {
            List<Posts> dbPosts = postsMapper.selectBatchIds(missIds);
            for (Posts p : dbPosts) {
                redisTemplate.opsForValue().set(KeyUtils.buildPostDetailKey(p.getId()), p, 1, TimeUnit.HOURS);
                results.add(p);
            }
        }

        // 4. 排序：按传入的 postIds 顺序排列返回（重要！）
        Map<Long, Posts> postMap = results.stream().collect(Collectors.toMap(Posts::getId, p -> p));
        return postIds.stream().map(postMap::get).filter(Objects::nonNull).toList();
    }
}