package com.yu.market.server.raffle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.server.raffle.model.pojo.Strategy;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yu
* @description 针对表【strategy(抽奖策略)】的数据库操作 Mapper
* @Entity com.yu.server.raffle.model.pojo.Strategy
*/
@Mapper
public interface StrategyMapper extends BaseMapper<Strategy> {

}




