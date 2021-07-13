package com.simplybusiness.dna.common.topics.examples;

import com.simplybusiness.dna.common.topics.TopicsManager;
import com.simplybusiness.dna.common.topics.YamlTopicConfig;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

// you will need to start kafka on localhost:9092 to run this
public class Examples {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // create a new topicManager
        Properties props = new Properties();
        props.setProperty("bootstrap.servers","localhost:9092");
        TopicsManager topicManager = new TopicsManager(props);

        // apply config to kafka
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String file = classloader.getResource("example.yaml").getFile();
        topicManager.updateKafkaFromConfigFile(file);

        // get the topic config from kafka
        Map<String, YamlTopicConfig> topicConfigs = topicManager.topicListing();

        //save the configs to a yaml file
        File f = File.createTempFile("kafka-topics", "yaml");
        topicManager.saveToFile(topicConfigs, f);

    }
}
