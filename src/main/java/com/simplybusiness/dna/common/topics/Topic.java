package com.simplybusiness.dna.common.topics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class Topic {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    String name;
    int partitions;
    int replicationFactor;
    Map<String, String> config;

    public Topic(String name, int partitions, int replicationFactor, Map<String, String> config) {
        this.name = name;
        this.partitions = partitions;
        this.replicationFactor = replicationFactor;
        this.config = config;
    }


    public String jsonConfig() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "failed to convert to json";
        }
    }
}
