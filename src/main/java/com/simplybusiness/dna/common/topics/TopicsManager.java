package com.simplybusiness.dna.common.topics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TopicsManager {
    private final static Logger LOGGER = Logger.getLogger(TopicsManager.class.getName());

    private AdminClient client;

    TopicsManager(Properties properties, String saslUser, String saslPassword) {
        if (properties.containsKey("sasl.jaas.config")) {
            String sasl = properties.getProperty("sasl.jaas.config");
            sasl = sasl.replace("SASL_USERNAME", saslUser);
            sasl = sasl.replace("SASL_PASSWORD", saslPassword);
            properties.put("sasl.jaas.config", sasl);
        }
        client = AdminClient.create(properties);
    }

    public TopicsManager(Properties properties) {
        this(properties, System.getenv().get("SASL_USERNAME"), System.getenv().get("SASL_PASSWORD"));
    }


    public List<Topic> getTopics() throws InterruptedException, ExecutionException {
        Map<String, YamlTopicConfig> listing = topicListing();

        Stream<Topic> r = listing.entrySet().stream().map(entry -> new Topic(entry.getKey(), entry.getValue().partitions,
                entry.getValue().replication, entry.getValue().getConfig()));

        return r.collect(Collectors.toList());
    }

    public Map<String, YamlTopicConfig> topicListing() throws InterruptedException, ExecutionException {
        ListTopicsResult ltr = client.listTopics();
        Set<String> names = ltr.names().get();
        DescribeTopicsResult descriptions = client.describeTopics(names);
        Map<String, TopicDescription> topicsDescriptions = descriptions.all().get();
        Stream<String> stream = names.stream();
        Stream<ConfigResource> streamMapped = stream.map((value) -> new ConfigResource(ConfigResource.Type.TOPIC, value));
        List<ConfigResource> cr = streamMapped.collect(Collectors.toList());
        DescribeConfigsResult streamDescriptions = client.describeConfigs(cr);
        Map<ConfigResource, Config> configDescriptions = streamDescriptions.all().get();

        Stream<Map.Entry<String, YamlTopicConfig>> cfgs = names.stream().map((topicName) -> {
            TopicDescription topicDescription = topicsDescriptions.get(topicName);
            Stream<ConfigEntry> configEntryStream = configDescriptions.get(new ConfigResource(ConfigResource.Type.TOPIC, topicName)).entries().stream();
            Map<String, String> configEntries = configEntryStream.filter((entry) -> (
                    (entry.source() == ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG)
                    || (entry.name().equals("cleanup.policy") && entry.value().equals("compact")))
            ).collect(Collectors.toMap(ConfigEntry::name, ConfigEntry::value));
            YamlTopicConfig config = new YamlTopicConfig(topicDescription.partitions().size(), topicDescription.partitions().get(0).replicas().size(), configEntries);
            return new AbstractMap.SimpleEntry<>(topicName, config);
        });
        return cfgs.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public String asYaml(Map<String, YamlTopicConfig> configs) {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        String yaml = null;
        try {
            yaml = om.writeValueAsString(configs);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return yaml;
    }

    /**
     * @param path to a file containing configs
     * @return a map of the topics/configs
     */
    public Map<String, YamlTopicConfig> asConfigs(String path) {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        try {
            String yamlConfig = new String ( Files.readAllBytes( Paths.get(path) ));
            return om.readValue(yamlConfig, new TypeReference<Map<String, YamlTopicConfig>>() {
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot convert file at" + path, e);
        }
        return null;
    }

    public class Error {
        public String getFilePath() {
            return filePath;
        }

        public Exception getException() {
            return exception;
        }

        public String getReason() {
            return reason;
        }

        String filePath;
        Exception  exception;
        String reason;

        public Error(String filePath, Exception exception, String reason) {
            this.filePath = filePath;
            this.exception = exception;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "filePath='" + filePath + '\'' +
                    ", exception=" + exception +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }

    public List<TopicsManager.Error> checkConfigs(String path) {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        try {
            String yamlConfig = new String ( Files.readAllBytes( Paths.get(path) ));
            om.readValue(yamlConfig, new TypeReference<Map<String, YamlTopicConfig>>() {
            });
            return new ArrayList<>();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot convert file at" + path, e);
            ArrayList<Error> results = new ArrayList<Error>();
            results.add(new Error(path, e, "cannot convert file"));
            return results;
        }
    }

    public void saveToFile(Map<String, YamlTopicConfig> configs, File file) {
        String yaml = asYaml(configs);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
            writer.write(yaml);
            writer.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot save file at" + file.getAbsolutePath(), e);
        }
    }

    /**
     * @param configsBase
     * @param configs
     * @return
     */
    public Map<String, TopicConfigChanges> findDifferences(Map<String, YamlTopicConfig> configsBase, Map<String, YamlTopicConfig> configs) {
        HashMap<String, TopicConfigChanges> differences = new HashMap<>();
        configs.forEach((topicName, topicConfig) -> {
            if (!configsBase.containsKey(topicName)) {
                differences.put(topicName, new TopicConfigChanges(topicConfig));
            } else {
                TopicConfigChanges d = configDifferences(configsBase.get(topicName), topicConfig);
                if (d.partitions > 0 || d.replication > 0 || d.config.size() > 0) differences.put(topicName, d);
            }
        });
        return differences;
    }

    private TopicConfigChanges configDifferences(YamlTopicConfig existingConfig, YamlTopicConfig requiredConfig) {
        Map<String, TopicConfigChange> config = new HashMap<>();
        TopicConfigChanges changes = new TopicConfigChanges(false, 0, 0, config);
        // find stuff to change
        if (requiredConfig.replication != existingConfig.replication) changes.replication = requiredConfig.replication;
        if (requiredConfig.partitions != existingConfig.partitions) changes.partitions = requiredConfig.partitions;

        requiredConfig.config.forEach((settingName, settingValue) -> {
            if (!existingConfig.config.containsKey(settingName)) {
                config.put(settingName, new TopicConfigChange(settingValue, AlterConfigOp.OpType.SET));
            } else {
                if (!existingConfig.config.get(settingName).equals(settingValue)) {
                    config.put(settingName, new TopicConfigChange(settingValue, AlterConfigOp.OpType.SET));
                }
            }
        });
        return changes;
    }

    ConfigEntry getConfigEntry(String key, String value) {
        return new ConfigEntry(key, value);
    }

    public List<Result> updateKafkaFromConfigFile(String file)  {
        Map<String, YamlTopicConfig> fileConfigs = asConfigs(file);
        Map<String, YamlTopicConfig> existingTopics = null;
        try {
            existingTopics = topicListing();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Map<String, TopicConfigChanges> differences = findDifferences(existingTopics, fileConfigs);
        return applyChangesToKafka(differences);
    }

    List<Result> applyChangesToKafka(Map<String, TopicConfigChanges> topicChanges) {
        List<Result> results = new ArrayList<>();
        topicChanges.forEach((topicName, configChanges) -> {
            boolean topicFailure = false;
            // if topic does not exist create it
            if (configChanges.topicNotPresent) {
                try {
                    client.createTopics(Arrays.asList(new NewTopic(topicName, configChanges.partitions, (short) configChanges.replication))).all().get();
                    results.add(new Result(true, topicName, "created topic"));
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Failed to create topic " + topicName, e);
                    results.add(new Result(false, topicName, e.toString()));
                    topicFailure = true;
                }
            }

            if (!topicFailure) {
                // warn of replication or partition changes
                // we won't apply them at the moment
                if(!configChanges.topicNotPresent && configChanges.replication > 0) {
                    results.add(new Result(false, topicName, "replication level change to " + configChanges.replication + " will not be processed"));
                }
                if(!configChanges.topicNotPresent && configChanges.partitions > 0) {
                    results.add(new Result(false, topicName, "partitions level change to " + configChanges.partitions + " will not be processed"));
                }
                //apply config changes
                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
                configChanges.config.forEach((key, value) -> {
                    try {
                        Map<ConfigResource, Collection<AlterConfigOp>> updateConfig = new HashMap<ConfigResource, Collection<AlterConfigOp>>();
                        ConfigEntry entry = getConfigEntry(key, value.configItemName);
                        updateConfig.put(resource, Collections.singleton(new AlterConfigOp(entry, value.opType)));
                        AlterConfigsResult alterConfigsResult = client.incrementalAlterConfigs(updateConfig);
                        alterConfigsResult.all().get();
                        results.add(new Result(true, topicName, key + "=" + value.configItemName + " operation=" + value.opType.toString()));
                    } catch (InterruptedException | ExecutionException e) {
                        LOGGER.log(Level.SEVERE, "Failed to alter topic" + topicName, e);
                        results.add(new Result(false, topicName, e.toString()));
                    }
                });
            }
        });
        return results;
    }

    public Map<String, TopicConfigChanges> asChanges(String path) {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        try {
            String yamlConfig = new String ( Files.readAllBytes( Paths.get(path) ));
            return om.readValue(yamlConfig, new TypeReference<Map<String, TopicConfigChanges>>() {
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot convert file at" + path, e);
        }
        return null;
    }
}
