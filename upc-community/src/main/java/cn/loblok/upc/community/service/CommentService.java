package cn.loblok.upc.community.service;

import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.community.dto.PayloadDTO;
import cn.loblok.upc.community.dto.TComment;
import cn.loblok.upc.community.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 评论表 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
public interface CommentService extends IService<Comment> {

    /**
     * 添加评论
     * @param postId
     * @param payloadDTO
     * @param userId
     * @return
     */
    public Result<TComment> addComment(Long postId, PayloadDTO payloadDTO, Long userId) ;

    /**
     * 获取帖子评论
     * @param postId
     * @param userId
     * @return
     */
    List<TComment> getCommentsList(Long postId, Long userId);

    /**
     * 删除评论
     * @param postId
     * @param commentId
     * @param userId
     * @return
     */
    Result<String> deleteComment(Long postId, Long commentId, Long userId);
}
