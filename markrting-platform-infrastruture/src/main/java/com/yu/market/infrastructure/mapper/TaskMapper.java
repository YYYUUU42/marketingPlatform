package com.yu.market.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.infrastructure.pojo.Task;
import org.apache.ibatis.annotations.Param;

/**
* @description 针对表【task(任务表，发送MQ)】的数据库操作Mapper
* @Entity com.yu.market.infrastructure.pojo.Task
*/
public interface TaskMapper extends BaseMapper<Task> {

	/**
	 * 更新 - 消息发送成功
	 */
	void updateTaskSendMessageCompleted(@Param("userId") String userId, @Param("messageId") String messageId);

	/**
	 * 更新 - 消息发送失败
	 */
	void updateTaskSendMessageFail(@Param("userId") String userId, @Param("messageId") String messageId);

}




