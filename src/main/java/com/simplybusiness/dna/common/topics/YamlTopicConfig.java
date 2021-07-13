package com.simplybusiness.dna.common.topics;

import java.util.Map;
public class YamlTopicConfig {
    int partitions;
    int replication;
    Map<String, String> config;


    YamlTopicConfig() {

    }

    YamlTopicConfig(int partitions, int replication, Map<String, String> config) {
       this.partitions = partitions;
       this.replication = replication;
       this.config =  config;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public int getReplication() {
        return replication;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
