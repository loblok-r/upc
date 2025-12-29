package cn.loblok.upc.community.service.impl;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.community.dto.CommunityUserVO;
import cn.loblok.upc.community.dto.PayloadDTO;
import cn.loblok.upc.community.dto.TComment;
import cn.loblok.upc.community.entity.Comment;
import cn.loblok.upc.community.entity.LikeRecord;
import cn.loblok.upc.community.entity.Posts;
import cn.loblok.upc.community.mapper.CommentMapper;
import cn.loblok.upc.community.mapper.LikeRecordMapper;
import cn.loblok.upc.community.service.CommentService;
import cn.loblok.upc.community.service.FollowService;
import cn.loblok.upc.community.service.PostsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Service
@Slf4j
@AllArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final PostsService postsService;

    private final UserFeignClient userFeignClient;

    private final FollowService followService;

    private final CommentMapper commentMapper;

    private final LikeRecordMapper likeRecordMapper;

    private final StringRedisTemplate redisTemplate;

    private final String LEADERBOARD_KEY = KeyUtils.buildCommunityLeaderboardCreatorsKey();


    @Override
    public Result<TComment> addComment(Long postId, PayloadDTO payloadDTO, Long userId) {
        // 检查帖子是否存在
        Posts post = postsService.getById(postId);

        if (post == null || post.getIsDeleted()) {
            return Result.error(404, "帖子不存在或已被删除", null);
        }

        // 创建评论对象
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType("post");
        comment.setTargetId(postId);
        comment.setContent(payloadDTO.getContent());
        comment.setLikesCount(0);
        comment.setIsDeleted(false);
        comment.setTenantId("default"); // 在实际应用中应该从上下文中获取
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        // 保存评论
        boolean saved = this.save(comment);

        if (!saved) {
            return Result.error(500, "评论保存失败", null);
        }

        // 更新帖子的评论数
        post.setCommentsCount(post.getCommentsCount() + 1);
        postsService.updateById(post);

        redisTemplate.opsForZSet().incrementScore(LEADERBOARD_KEY,String.valueOf(post.getUserId()), 0.6);


        // 构造返回的TComment对象
        TComment tComment = new TComment();
        tComment.setId(String.valueOf(comment.getId()));
        tComment.setUserId(String.valueOf(userId));

        Result<UserPublicInfoDTO> userInfo = userFeignClient.getUserInfo(userId);

        // 获取用户信息
        CommunityUserVO vo = new CommunityUserVO();

        BeanUtils.copyProperties(userInfo.getData(), vo);
        vo.setIsFollowed(false); //不可能关注自己

        tComment.setUser(vo);

        tComment.setText(payloadDTO.getContent());
        tComment.setLikes(0);
        tComment.setIsLiked(false);
        tComment.setCreatedAt(comment.getCreatedAt().toString());
        tComment.setTimeAgo("刚刚");
        tComment.setReplies(new TComment[0]);

        return Result.success(tComment);
    }

    @Override
    public List<TComment> getCommentsList(Long postId, Long userId) {
            // 查询帖子的所有评论
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("target_type", "post");
            queryWrapper.eq("target_id", postId);
            queryWrapper.eq("is_deleted", false);
            queryWrapper.orderByAsc("created_at");

            List<Comment> comments = commentMapper.selectList(queryWrapper);
            List<Long> ulist = comments.stream().map(Comment::getUserId).collect(Collectors.toList());


            if (ulist.isEmpty()) {
                return List.of();
            }

            Result<Map<Long, UserPublicInfoDTO>> useMap= userFeignClient.getUserPublicInfoBatch(ulist);

            if (useMap.getCode() != 200 || useMap.getData() == null) {
                log.error("获取用户信息失败");
                return List.of();
            }
            List<Long> followedIds = followService.findFollowedIds(userId, ulist);
            // 转换为TComment对象
            return comments.stream().map(comment -> {
                TComment tComment = new TComment();
                tComment.setId(String.valueOf(comment.getId()));
                tComment.setUserId(String.valueOf(comment.getUserId()));

                CommunityUserVO vo = new CommunityUserVO();

                // 获取用户信息
                UserPublicInfoDTO userPublicInfoDTO = useMap.getData().get(comment.getUserId());

                if (userPublicInfoDTO == null) {
                    log.error("用户信息为空，请检查用户ID是否正确");
                    return null;
                }
                BeanUtils.copyProperties(userPublicInfoDTO, vo);
                vo.setIsFollowed(followedIds.contains(comment.getUserId()));

                tComment.setUser(vo);

                tComment.setText(comment.getContent());
                tComment.setLikes(comment.getLikesCount());

                // 检查当前用户是否点赞了该评论
                QueryWrapper<LikeRecord> likeQueryWrapper = new QueryWrapper<>();
                likeQueryWrapper.eq("user_id", userId);
                likeQueryWrapper.eq("target_type", "comment");
                likeQueryWrapper.eq("target_id", comment.getId());
                tComment.setIsLiked(likeRecordMapper.selectCount(likeQueryWrapper) > 0);

                tComment.setCreatedAt(comment.getCreatedAt().toString());
                // 简化处理，实际项目中应该根据创建时间和当前时间计算
                tComment.setTimeAgo("刚刚");

                // 这里简化处理，实际项目中可能需要查询回复评论
                tComment.setReplies(new TComment[0]);

                return tComment;
            }).toList();

    }

@Override
public Result<String> deleteComment(Long postId, Long commentId, Long userId) {
    // 检查帖子是否存在
    Posts post = postsService.getById(postId);
    if (post == null || post.getIsDeleted()) {
        return Result.error(404, "帖子不存在或已被删除", null);
    }

    // 检查评论是否存在
    Comment comment = commentMapper.selectById(commentId);
    if (comment == null || comment.getIsDeleted()) {
        return Result.error(404, "评论不存在或已被删除", null);
    }

    // 验证评论是否属于该帖子
    if (!comment.getTargetId().equals(postId) || !"post".equals(comment.getTargetType())) {
        return Result.error(400, "评论不属于指定帖子", null);
    }

    // 验证用户是否有权限删除评论（评论创建者才能删除）
    if (!comment.getUserId().equals(userId)) {
        return Result.error(403, "无权限删除该评论", null);
    }

    // 执行删除操作（软删除）
    comment.setIsDeleted(true);
    comment.setUpdatedAt(LocalDateTime.now());
    commentMapper.updateById(comment);

    // 更新帖子的评论数
    post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
    postsService.updateById(post);

    redisTemplate.opsForZSet().incrementScore(LEADERBOARD_KEY, String.valueOf(post.getUserId()), -0.6);

    return Result.success("评论删除成功");
}
}
