package cn.loblok.upc.community.service.impl;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.community.config.MeilisearchConfig;
import cn.loblok.upc.community.dto.*;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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


    private final MeiliSearchService meiliSearchService;

    @Override
    public List<CreatorLeaderboardResponse> getCreatorLeaderboard(Long userId) {
        List<LeaderboardData> topCreators = this.getTopCreators(10);
        return getLeaderboardResponses(userId, topCreators);
    }

    private List<LeaderboardData> getTopCreators(int counts) {
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

    //todo 获取近期前10名作者
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


    @Override
    public String likeOrUnlikeComment(Long userId, Long commentId) {
        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted()) {
            throw new RuntimeException("评论不存在或已被删除");
        }

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
        int offset = (page - 1) * pageSize;
        int limit = pageSize;
        // 1. 调用 Meilisearch 进行全文检索
        // 这里建议封装一个 MeilisearchUtil
        try {
            SearchResult results  = meiliSearchService.searchUsers(keyword, offset, limit);
            // 2. 提取搜索到的用户 ID 和 基础信息
            // 将变量类型改为:

            List<? extends Map<String, Object>> hits = results.getHits();
            if (hits.isEmpty()) return Collections.emptyList();

            List<Long> searchedUserIds = hits.stream()
                    .map(h -> Long.valueOf(h.get("id").toString()))
                    .toList();
            // 3. 批量查询本地关注关系 (SELECT followee_id FROM follow WHERE follower_id = ? AND followee_id IN (...))
            List<Long> followedIds = followService.findFollowedIds(currentUserId, searchedUserIds);

            // 4. 合并数据并返回
            return hits.stream().map(hit -> {
                CommunityUserVO dto = new CommunityUserVO();
                Long userId = Long.valueOf(hit.get("id").toString());

                dto.setId(userId);
                dto.setName((String) hit.get("username"));
                dto.setAvatar((String) hit.get("avatar_url"));
                dto.setFollowers((Integer) hit.get("followers"));

                // 核心：设置关注状态
                dto.setIsFollowed(followedIds.contains(userId));

                return dto;
            }).collect(Collectors.toList());

        } catch (MeilisearchException e) {
            throw new RuntimeException("Meilisearch 搜索异常", e);
        }

    }
}