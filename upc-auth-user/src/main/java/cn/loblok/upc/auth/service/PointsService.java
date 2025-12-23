package cn.loblok.upc.auth.service;


import cn.loblok.upc.common.base.Result;

public interface PointsService {

    /**
     * 减少用户积分
     *
     * @param userId      用户ID
     * @param deltaPoints 减少的积分数
     * @return 是否成功
     */
    Result<Void> reduceUserPoints(Long userId, int deltaPoints);

    /**
     * 增加用户积分
     *
     * @param userId      用户ID
     * @param deltaPoints 增加的积分数
     * @return 是否成功
     */
    Result<Void>  addUserPoints(Long userId, int deltaPoints);


}
