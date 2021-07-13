package com.simplybusiness.dna.common.topics;

import org.apache.kafka.clients.admin.AlterConfigOp;


/**
 * Represents a single item of configuration change for a Kafka topic
 */
public class TopicConfigChange {
    String configItemName;
    AlterConfigOp.OpType opType;

    public TopicConfigChange(String configItemName, AlterConfigOp.OpType opType) {
        this.configItemName = configItemName;
        this.opType = opType;
    }
}