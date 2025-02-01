package com.yu.market.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.infrastructure.pojo.UserAwardRecord;

/**
* @description 针对表【user_award_record(用户中奖记录表)】的数据库操作Mapper
* @Entity com.yu.market.infrastructure.pojo.UserAwardRecord
*/
public interface UserAwardRecordMapper extends BaseMapper<UserAwardRecord> {

	int updateAwardRecordCompletedState(UserAwardRecord userAwardRecordReq);


}




