package cn.loblok.upc.modules.community.service.impl;

import cn.loblok.upc.modules.community.entity.Comment;
import cn.loblok.upc.mapper.CommentMapper;
import cn.loblok.upc.modules.community.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

}
