package cn.loblok.upc.auth.service;


import cn.loblok.upc.auth.dto.UserPointsResponse;
import cn.loblok.upc.auth.entity.UserPoints;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
public interface UserPointsService extends IService<UserPoints> {

    boolean addUserPoints(Long userId, int deltaPoints);

    boolean reduceUserPoints(Long userId, int deltaPoints);

    UserPointsResponse getUserPoints(Long userId);
}
