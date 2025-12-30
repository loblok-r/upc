package cn.loblok.upc.community.service.impl;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.community.dto.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;

import cn.loblok.upc.community.entity.Comment;
import cn.loblok.upc.community.entity.LikeRecord;
import cn.loblok.upc.community.entity.Posts;
import cn.loblok.upc.community.mapper.CommentMapper;
import cn.loblok.upc.community.mapper.LikeRecordMapper;
import cn.loblok.upc.community.mapper.PostsMapper;
import cn.loblok.upc.community.service.CommunityService;
import cn.loblok.upc.community.service.FollowService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.SearchResult;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 社区服务实现类
 */
@Slf4j
@Service
@Primary
@AllArgsConstructor
public class CommunityServiceImpl implements CommunityService {


    private final FollowService followService;

    private final CommentMapper commentMapper;

    private final LikeRecordMapper likeRecordMapper;

    private final PostsMapper postsMapper;

    private final UserFeignClient userFeignClient;

    private final StringRedisTemplate redisTemplate;

    private final MeiliSearchService meiliSearchService;

    private final String LEADERBOARD_KEY = KeyUtils.buildCommunityLeaderboardCreatorsKey();


    @PostConstruct
    public void initCreatorLeaderboard() {
        loadCreatorLeaderboard();
    }

    /**
     * 初始化创作者排行榜
     */
    private void loadCreatorLeaderboard() {
        List<LeaderboardData> dbTopCreators = this.getTopCreators(1000);
        for (LeaderboardData data : dbTopCreators) {
            // ZADD: 如果存在会更新分数，符合需求
            redisTemplate.opsForZSet().add(LEADERBOARD_KEY,String.valueOf(data.getUserId()), data.getScore());
        }
        log.info("初始化排行榜 {} 用户", dbTopCreators.size());
    }


    /**
     * 获取创作者排行榜
     *
     * @param userId
     * @return
     */
    @Override
    public List<CreatorLeaderboardResponse> getCreatorLeaderboard(Long userId) {
        log.info("获取创作者排行榜");
        Set<ZSetOperations.TypedTuple<String>> topTuples= redisTemplate.opsForZSet().reverseRangeWithScores(LEADERBOARD_KEY, 0, 9);

        if (topTuples == null || topTuples.isEmpty()) {
            loadCreatorLeaderboard();
            topTuples = redisTemplate.opsForZSet().reverseRangeWithScores(LEADERBOARD_KEY, 0, 9);
            if (topTuples == null) {topTuples = Collections.emptySet();}
        }

        List<Long> userIds = topTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, UserPublicInfoDTO> userMap = userFeignClient.getUserPublicInfoBatch(userIds).getData();
        List<Long> followedIds = followService.getFollowingList(userId).stream().map(UserPublicInfoDTO::getId).toList();


        // 构建响应
        List<CreatorLeaderboardResponse> responses = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : topTuples) {
            Long uid = Long.parseLong(tuple.getValue());
            CreatorLeaderboardResponse resp = new CreatorLeaderboardResponse();
            resp.setRank(rank++);
            resp.setScore(tuple.getScore().intValue());

            CommunityUserVO vo = new CommunityUserVO();
            UserPublicInfoDTO rawInfo = userMap.get(uid);
            if (rawInfo != null) {
                BeanUtils.copyProperties(rawInfo, vo);
            }
            vo.setIsFollowed(followedIds.contains(uid));
            resp.setAuthor(vo);

            responses.add(resp);
        }
        return responses;
    }

    private List<LeaderboardData> getTopCreators(int counts) {
        log.info("初次构造创作者排行榜，getTopCreators()");
        return postsMapper.selectList(new LambdaQueryWrapper<Posts>()
                        .select(Posts::getUserId,
                                Posts::getLikesCount,
                                Posts::getCommentsCount)
                        .eq(Posts::getIsDeleted, false))
                .stream()
                .collect(Collectors.groupingBy(Posts::getUserId,
                        Collectors.averagingDouble(p -> p.getLikesCount() * 0.4 + p.getCommentsCount() * 0.6)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(counts)
                .map(entry -> {
                    LeaderboardData data = new LeaderboardData();
                    data.setUserId(entry.getKey());
                    data.setScore(entry.getValue().intValue());
                    return data;
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<CreatorLeaderboardResponse> getNewCreatorLeaderboard(Long userId) {

        List<LeaderboardData> topCreators = this.getNewTopCreators(10);
        return getLeaderboardResponses(userId, topCreators);
    }

    //todo 获取新人名人排行榜（暂时未开发）
    private List<LeaderboardData> getNewTopCreators(int counts) {
        return getTopCreators(counts);
    }


    private List<CreatorLeaderboardResponse> getLeaderboardResponses(Long userId, List<LeaderboardData> topCreators) {
        List<Long> userIds = topCreators.stream().map(LeaderboardData::getUserId).toList();

        Map<Long, UserPublicInfoDTO> userMap = userFeignClient.getUserPublicInfoBatch(userIds).getData();


        List<Long> followedIds = followService.getFollowingList(userId).stream().map(UserPublicInfoDTO::getId).toList();


        return topCreators.stream().map(data -> {
            CreatorLeaderboardResponse resp = new CreatorLeaderboardResponse();
            resp.setRank(data.getRank());
            resp.setScore(data.getScore());

            // 拼装 VO
            UserPublicInfoDTO rawInfo = userMap.get(data.getUserId());
            CommunityUserVO vo = new CommunityUserVO();
            BeanUtils.copyProperties(rawInfo, vo);

            // 设置是否关注
            vo.setIsFollowed(followedIds.contains(data.getUserId()));

            resp.setAuthor(vo);
            return resp;
        }).toList();
    }


    /**
     * 点赞或取消点赞评论
     *
     * @param userId
     * @param commentId
     * @return
     */
    @Override
    public String likeOrUnlikeComment(Long userId, Long commentId) {

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted()) {
            throw new RuntimeException("评论不存在或已被删除");
        }

//        Long postId = findPostIdByCommentId(commentId);
//
//        if (postId == null) {
//            throw new RuntimeException("该评论未关联到有效帖子");
//        }
//        Posts post = postsMapper.selectById(postId);
//        if (post == null || post.getIsDeleted()) {
//            throw new RuntimeException("关联的帖子不存在或已删除");
//        }
//        Long authorId = post.getUserId();
        // 检查是否已经点赞
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("target_type", "comment");
        queryWrapper.eq("target_id", commentId);

        LikeRecord existingLike = likeRecordMapper.selectOne(queryWrapper);

        if (existingLike != null) {
            // 已经点赞，执行取消点赞操作
            likeRecordMapper.deleteById(existingLike.getId());

            // 减少评论的点赞数
            comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
            commentMapper.updateById(comment);


            return "取消点赞成功";
        } else {
            // 未点赞，执行点赞操作
            LikeRecord likeRecord = new LikeRecord();
            likeRecord.setUserId(userId);
            likeRecord.setTargetType("comment");
            likeRecord.setTargetId(commentId);
            likeRecord.setTenantId("default"); // 根据实际情况设置租户ID
            likeRecord.setCreatedAt(LocalDateTime.now());
            likeRecordMapper.insert(likeRecord);

            // 增加评论的点赞数
            comment.setLikesCount(comment.getLikesCount() + 1);
            commentMapper.updateById(comment);


            return "点赞成功";
        }
    }

//    /**
//     * 根据评论ID查找关联的帖子ID
//     */
//    private Long findPostIdByCommentId(Long commentId) {
//        Comment current = commentMapper.selectById(commentId);
//        if (current == null || current.getIsDeleted()) {
//            return null;
//        }
//
//        // 最多追溯 10 层，防止无限循环
//        int maxDepth = 10;
//        while (maxDepth-- > 0) {
//            if ("post".equals(current.getTargetType())) {
//                return current.getTargetId(); // 这就是 postId
//            } else if ("comment".equals(current.getTargetType())) {
//                Long parentCommentId = current.getTargetId();
//                current = commentMapper.selectById(parentCommentId);
//                if (current == null || current.getIsDeleted()) {
//                    break;
//                }
//            } else {
//                break; // 不支持的 target_type
//            }
//        }
//        return null; // 未找到关联的帖子
//    }

    @Override
    public CommunityUserVO getUserProfile(Long targetUserId, Long currentUserId) {
        // 获取目标用户信息
        Result<UserPublicInfoDTO> userInfo = userFeignClient.getUserInfo(targetUserId);
        if (userInfo.getData() == null) {
            throw new RuntimeException("用户不存在");
        }


        CommunityUserVO vo = new CommunityUserVO();
        BeanUtils.copyProperties(userInfo.getData(), vo);

        // 检查当前用户是否关注了目标用户
        boolean isFollowed = followService.isFollowed(currentUserId, targetUserId);
        vo.setIsFollowed(isFollowed);

        return vo;
    }

    @Override
    public List<CommunityUserVO> searchUsers(String keyword, int page,int pageSize,Long currentUserId) {

        // 1. 计算偏移量
        int offset = Math.max(0, page - 1) * pageSize;
        int limit = pageSize;
        // 1. 调用 Meilisearch 进行全文检索
        // 这里建议封装一个 MeilisearchUtil
        try {
            SearchResult results  = meiliSearchService.searchUsers(keyword, offset, limit);
            // 2. 提取搜索到的用户 ID 和 基础信息
            // 将变量类型改为:

            List<? extends Map<String, Object>> hits = results.getHits();
            if (hits.isEmpty()) {return Collections.emptyList();}

            List<Long> searchedUserIds = hits.stream()
                    .map(h -> Long.valueOf(h.get("id").toString()))
                    .toList();
            // 3. 批量查询本地关注关系 (SELECT followee_id FROM follow WHERE follower_id = ? AND followee_id IN (...))
            List<Long> followedIds = followService.findFollowedIds(currentUserId, searchedUserIds);

            Set<Long> followedIdSet = new HashSet<>(followedIds);
            // 4. 合并数据并返回
            return hits.stream().map(hit -> {
                CommunityUserVO dto = new CommunityUserVO();
                Long userId = Long.valueOf(hit.get("id").toString());

                dto.setId(userId);
                dto.setName((String) hit.get("username"));
                dto.setAvatar((String) hit.get("avatar_url"));
                dto.setFollowers((Integer) hit.get("followers"));

                // 核心：设置关注状态,O(1) 查询
                dto.setIsFollowed(followedIdSet.contains(userId));

                return dto;
            }).collect(Collectors.toList());

        } catch (MeilisearchException e) {
            throw new RuntimeException("Meilisearch 搜索异常", e);
        }

    }
}