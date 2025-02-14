package com.yu.market.infrastructure.raffle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.infrastructure.raffle.pojo.UserCreditAccount;

/**
* @description 针对表【user_credit_account(用户积分账户)】的数据库操作Mapper
* @Entity com.yu.market.infrastructure.pojo.UserCreditAccount
*/
public interface UserCreditAccountMapper extends BaseMapper<UserCreditAccount> {

	int updateAddAmount(UserCreditAccount userCreditAccountReq);


}




