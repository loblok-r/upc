package cn.loblok.upc.community.service;

import cn.loblok.upc.community.entity.LikeRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 点赞记录表 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
public interface LikeRecordService extends IService<LikeRecord> {

    void likePost(Long postId, Boolean isLikedBool, Long userId);

}
