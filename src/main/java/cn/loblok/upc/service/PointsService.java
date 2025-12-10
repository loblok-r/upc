package cn.loblok.upc.service;


public interface PointsService {

    /**
     * 更新用户积分
     *
     * @param userId      用户ID
     * @param deltaPoints 增加的积分数
     * @return 是否成功
     */
    boolean updateUserPoints(Long userId, int deltaPoints);

    /**
     * 减少用户积分
     *
     * @param userId      用户ID
     * @param deltaPoints 减少的积分数
     * @return 是否成功
     */
    boolean reduceUserPoints(Long userId, int deltaPoints);

    /**
     * 增加用户积分
     *
     * @param userId      用户ID
     * @param deltaPoints 增加的积分数
     * @return 是否成功
     */
    boolean addUserPoints(Long userId, int deltaPoints);


}
