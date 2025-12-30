package cn.loblok.upc.community.controller;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.community.dto.PayloadDTO;
import cn.loblok.upc.community.dto.TComment;
import cn.loblok.upc.community.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 评论表 前端控制器
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@RestController
@RequestMapping("/api/community/posts")
@Tag(name = "评论接口", description = "评论接口")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取帖子评论
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/{postId}/comments")
    @Operation(summary = "获取帖子评论")
    public Result<List<TComment>> getPostComments(@PathVariable("postId") Long postId,
                                                  @CurrentUser Long userId) {
        List<TComment> comments = commentService.getCommentsList(postId, userId);
        return Result.success(comments);
    }

    /**
     * 添加评论
     *
     * @param postId     帖子ID
     * @param payloadDTO 评论内容
     * @param userId     当前用户ID
     * @return 添加结果
     */
    @PostMapping("/{postId}/comments")
    @Operation(summary = "添加评论")
    public Result<TComment> addComment(@PathVariable("postId") Long postId,
                                       @RequestBody PayloadDTO payloadDTO,
                                       @CurrentUser Long userId) {
        try {

            return commentService.addComment(postId, payloadDTO, userId);
        } catch (Exception e) {
            return Result.error(500, "添加评论失败", e.getMessage());
        }
    }



    @DeleteMapping("/{postId}/comments/{commentId}")
    @Operation(summary = "删除评论")
    public Result<String> deleteComment(@PathVariable("postId") Long postId,
                                       @PathVariable("commentId") Long commentId,
                                       @CurrentUser Long userId) {
        try {
            return commentService.deleteComment(postId, commentId, userId);
        } catch (Exception e) {
            return Result.error(500, "删除评论失败", e.getMessage());
        }
    }
}
