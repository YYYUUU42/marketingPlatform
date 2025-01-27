package com.yu.market.server.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.server.activity.model.pojo.UserCreditAccount;

/**
* @description 针对表【user_credit_account(用户积分账户)】的数据库操作Mapper
* @Entity com.yu.market.server.activity.model.pojo.UserCreditAccount
*/
public interface UserCreditAccountMapper extends BaseMapper<UserCreditAccount> {

	int updateAddAmount(UserCreditAccount userCreditAccountReq);


}




