package cn.loblok.upc.service;

import cn.loblok.upc.dto.DrawResultDTO;
import cn.loblok.upc.dto.Result;

/**
 * 抽奖服务接口
 */
public interface LotteryService {

    /**
     * 抽奖
     * @return 抽奖结果
     */
     Result<DrawResultDTO> draw(Long userId);


}