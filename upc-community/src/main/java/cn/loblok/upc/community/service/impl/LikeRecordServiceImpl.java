package cn.loblok.upc.community.service.impl;

import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.StatUpdateMsgDTO;
import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.community.entity.LikeRecord;
import cn.loblok.upc.community.entity.Posts;
import cn.loblok.upc.community.mapper.LikeRecordMapper;
import cn.loblok.upc.community.service.LikeRecordService;
import cn.loblok.upc.community.service.PostsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 点赞记录表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Service
@AllArgsConstructor
@Slf4j
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeRecordService {

    private final LikeRecordMapper likeRecordMapper;

    private final UserFeignClient userFeignClient;

    private final PostsService postsService;

    private final StringRedisTemplate redisTemplate;


    private final RabbitTemplate rabbitTemplate;

    private final String LEADERBOARD_KEY = KeyUtils.buildCommunityLeaderboardCreatorsKey();


    @Override
    public void likePost(Long postId, Boolean isLiked, Long userId) {
        // 检查帖子是否存在
        Posts post = postsService.getById(postId);
        if (post == null || post.getIsDeleted()) {
            throw new RuntimeException("帖子不存在或已被删除");
        }

        if (isLiked) {
            // 点赞操作
            // 检查是否已经点赞
            QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("target_type", "post");
            queryWrapper.eq("target_id", postId);

            if (likeRecordMapper.selectCount(queryWrapper) <= 0) {
                // 未点赞，执行点赞操作
                LikeRecord likeRecord = new LikeRecord();
                likeRecord.setUserId(userId);
                likeRecord.setTargetType("post");
                likeRecord.setTargetId(postId);
                likeRecord.setTenantId("default"); // 这里应该是从上下文中获取tenantId
                likeRecord.setCreatedAt(LocalDateTime.now());
                likeRecordMapper.insert(likeRecord);


                // 更新帖子的点赞数
                post.setLikesCount(post.getLikesCount() + 1);
                postsService.updateById(post);
                // 更新用户点赞数
                userFeignClient.updateLikeCounts(userId, 1);

                //更新排行榜分数
                redisTemplate.opsForZSet().incrementScore(LEADERBOARD_KEY, String.valueOf(post.getUserId()), 0.4);

                StatUpdateMsgDTO msg = new StatUpdateMsgDTO();
                msg.setUserId(userId);
                msg.setDelta(1);
                msg.setType("LIKE");
                rabbitTemplate.convertAndSend("upc.direct.exchange", "mq.route.stats_update", msg);
            }
        } else {
            // 取消点赞操作
            QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("target_type", "post");
            queryWrapper.eq("target_id", postId);

            // 删除点赞记录
            likeRecordMapper.delete(queryWrapper);

            // 更新帖子的点赞数
            if (post.getLikesCount() > 0) {
                post.setLikesCount(post.getLikesCount() - 1);
                postsService.updateById(post);
            }

            redisTemplate.opsForZSet().incrementScore(LEADERBOARD_KEY, String.valueOf(post.getUserId()), -0.4);

            StatUpdateMsgDTO msg = new StatUpdateMsgDTO();
            msg.setUserId(userId);
            msg.setDelta(-1);
            msg.setType("LIKE");
            rabbitTemplate.convertAndSend("upc.direct.exchange", "mq.route.stats_update", msg);
        }
    }
}
