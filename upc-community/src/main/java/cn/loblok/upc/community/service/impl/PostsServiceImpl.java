package cn.loblok.upc.community.service.impl;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.StatUpdateMsgDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.PostsTab;
import cn.loblok.upc.community.dto.*;
import cn.loblok.upc.community.entity.LikeRecord;
import cn.loblok.upc.community.entity.Posts;
import cn.loblok.upc.community.mapper.CommentMapper;
import cn.loblok.upc.community.mapper.LikeRecordMapper;
import cn.loblok.upc.community.mapper.PostsMapper;
import cn.loblok.upc.community.service.FollowService;
import cn.loblok.upc.community.service.PostsService;
import cn.loblok.upc.common.utils.ImageUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户作品/帖子表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Service
@Slf4j
@AllArgsConstructor
public class PostsServiceImpl extends ServiceImpl<PostsMapper, Posts> implements PostsService {


    private final FollowService followService;


    private final UserFeignClient userFeignClient;

    private final RabbitTemplate rabbitTemplate;


    /**
     * 获取推荐帖子
     *
     * @param userId 用户ID
     * @return 推荐帖子列表
     */
    @Override
    public List<PostResponse> getPostsTab(Long userId, int page, int pageSize, PostsTab postsTab) {


        log.info("获取帖子列表,用户ID:{},page:{},pageSize:{},postsTab:{}", userId, page, pageSize, postsTab.getDescription());
        List<Long> IdList;
        List<Long> followedIds = followService.findFollowedIds(userId);
        Page<Posts> postsPage;

        switch (postsTab) {
            case RECOMMEND -> {
                // 推荐的帖子列表，获取所有帖子按点赞数倒序排列
                QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("is_deleted", false);

                // 排除已关注用户的作品
                if (!followedIds.isEmpty()) {
                    queryWrapper.notIn("user_id", followedIds);
                }

                queryWrapper.orderByDesc("likes_count");

                postsPage = new Page<>(page, pageSize);
                this.page(postsPage, queryWrapper);

                IdList = postsPage.getRecords().stream().map(Posts::getUserId).toList();
                //空列表
                followedIds = new ArrayList<>();
            }
            case FOLLOW -> {
                //关注帖子
                if (followedIds.isEmpty()) {
                    return List.of(); // 如果没有关注任何人，则返回空列表
                }

                // 查询关注用户发布的帖子
                QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("user_id", followedIds);
                queryWrapper.eq("is_deleted", false);
                queryWrapper.orderByDesc("created_at");

                postsPage = new Page<>(page, pageSize);
                this.page(postsPage, queryWrapper);

                IdList = postsPage.getRecords().stream().map(Posts::getUserId).toList();
            }
            case LATEST -> {
                // 最新帖子
                QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("is_deleted", false);
                queryWrapper.orderByDesc("created_at");

                postsPage = new Page<>(page, pageSize);
                this.page(postsPage, queryWrapper);
                IdList = postsPage.getRecords().stream().map(Posts::getUserId).toList();
            }
            default -> {
                //查询个人的帖子
                QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", userId);
                queryWrapper.eq("is_deleted", false);
                queryWrapper.orderByDesc("created_at");

                postsPage = new Page<>(page, pageSize);
                this.page(postsPage, queryWrapper);
                IdList = postsPage.getRecords().stream().map(Posts::getUserId).toList();
                //空列表，默认为空
                followedIds = new ArrayList<>();
            }
        }
        log.info("查询结果{},用户IdList.size:{},folledIds.size:{}", postsTab.getDescription(), IdList.size(), followedIds.size());

        return getPostResponses(IdList, postsPage, followedIds);
    }


    private List<PostResponse> getPostResponses(List<Long> IdList, Page<Posts> postsPage, List<Long> followedIds) {

        Result<Map<Long, UserPublicInfoDTO>> useMap = userFeignClient.getUserPublicInfoBatch(IdList);
        if (useMap != null && useMap.getData() != null) {
        return postsPage.getRecords().stream()
                .map(post -> {
                    CommunityUserVO vo = new CommunityUserVO();
                    UserPublicInfoDTO userPublicInfoDTO = useMap.getData().get(post.getUserId());
                    BeanUtils.copyProperties(userPublicInfoDTO, vo);
                    vo.setIsFollowed(followedIds.contains(post.getUserId()));
                    PostResponse response = new PostResponse();
                    response.setId(post.getId());
                    response.setTitle(post.getTitle());
                    response.setContent(post.getContent());
                    response.setAuthor(vo);
                    response.setWidth(post.getWidth());
                    response.setHeight(post.getHeight());
                    response.setCommentsCount(post.getCommentsCount());
//                    String tmpImageUrl = tencentCOSUtil.getTmpImageUrl(post.getImageUrl(), 30);
                    response.setImageUrl(ImageUtil.getOptimizedUrl(post.getImageUrl(), false));
                    response.setLikesCount(post.getLikesCount());
                    response.setCreatedAt(post.getCreatedAt());
                    response.setUpdatedAt(post.getUpdatedAt());

                    return response;
                })
                .collect(Collectors.toList());
         }else{
            return List.of();
        }
    }


    @Override
    public Result<Posts> createPost(Long userId, CreatePostRequest createPostRequest) {
        Posts posts = new Posts();
        // 设置帖子的基本信息
        posts.setUserId(userId);
        posts.setTitle(createPostRequest.getTitle());
        posts.setContent(createPostRequest.getContent());
        posts.setImageUrl(createPostRequest.getImageUrl());
        posts.setCreatedAt(LocalDateTime.now());
        posts.setUpdatedAt(LocalDateTime.now());
        posts.setLikesCount(0);
        posts.setSize(createPostRequest.getSize());
        posts.setWidth(createPostRequest.getWidth());
        posts.setHeight(createPostRequest.getHeight());
        posts.setCommentsCount(0);
        posts.setIsDeleted(false);

        // 保存帖子
        boolean saved = this.save(posts);

        StatUpdateMsgDTO msg = new StatUpdateMsgDTO();
        msg.setUserId(userId);
        msg.setDelta(1);
        msg.setType("POST");
        rabbitTemplate.convertAndSend("upc.direct.exchange", "mq.route.stats_update", msg);




        if (saved) {
            return Result.success(posts);
        } else {
            return Result.error(500, "发布失败", "帖子保存失败");
        }
    }



    @Override
    public void deletePost(Long postId, Long userId) {
        // 检查帖子是否存在
        Posts post = this.getById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 检查帖子是否属于当前用户
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除他人的帖子");
        }

        StatUpdateMsgDTO msg = new StatUpdateMsgDTO();
        msg.setUserId(userId);
        msg.setDelta(-1);
        msg.setType("POST");
        rabbitTemplate.convertAndSend("upc.direct.exchange", "mq.route.stats_update", msg);



        // 逻辑删除帖子，将is_deleted设置为true
        post.setIsDeleted(true);
        post.setUpdatedAt(LocalDateTime.now());
        this.updateById(post);
    }
}