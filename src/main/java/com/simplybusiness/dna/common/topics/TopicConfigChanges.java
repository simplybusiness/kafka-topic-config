package com.simplybusiness.dna.common.topics;

import org.apache.kafka.clients.admin.AlterConfigOp;

import java.util.HashMap;
import java.util.Map;

public class TopicConfigChanges {

    public TopicConfigChanges(YamlTopicConfig topicConfig) {
        this.topicNotPresent = true;
        this.partitions = topicConfig.partitions;
        this.replication = topicConfig.replication;
        topicConfig.config.forEach((key,value) -> {
           config.put(key, new TopicConfigChange(value, AlterConfigOp.OpType.SET));
        });
    }

    boolean topicNotPresent;
    int partitions;
    int replication;
    Map<String, TopicConfigChange> config = new HashMap<>();

    public TopicConfigChanges(boolean topicNotPresent, int partitions, int replication, Map<String, TopicConfigChange> config) {
        this.topicNotPresent = topicNotPresent;
        this.partitions = partitions;
        this.replication = replication;
        this.config = config;
    }
}
