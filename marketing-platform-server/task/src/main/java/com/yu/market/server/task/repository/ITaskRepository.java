package com.yu.market.server.task.repository;


import com.yu.market.server.task.model.bo.TaskBO;

import java.util.List;

/**
 * @author yu
 * @description 任务服务仓储接口
 * @date 2025-01-26
 */
public interface ITaskRepository {

    /**
     * 查询发送MQ失败和超时1分钟未发送的MQ
     */
    List<TaskBO> queryNoSendMessageTaskList();

    /**
     * 发送消息
     */
    void sendMessage(TaskBO taskBO);

    /**
     * 更新 - 消息发送成功
     */
    void updateTaskSendMessageCompleted(String userId, String messageId);

    /**
     * 更新 - 消息发送失败
     */
    void updateTaskSendMessageFail(String userId, String messageId);

}
