package com.yu.market.server.activity.envent.topic;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author yu
 * @description topic 配置
 * @date 2025-01-26
 */
@Component
@ConfigurationProperties(prefix = "mq.topic")
public class TopicProperties {

    private Map<String, String> topics;

    public Map<String, String> getTopics() {
        return topics;
    }

    public void setTopics(Map<String, String> topics) {
        this.topics = topics;
    }

    public int getTopicCount() {
        return topics == null ? 0 : topics.size();
    }
}