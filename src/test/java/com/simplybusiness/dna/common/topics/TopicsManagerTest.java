package com.simplybusiness.dna.common.topics;

import com.salesforce.kafka.test.junit5.SharedKafkaTestResource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TopicsManagerTest {
    /**
     * We have a single embedded Kafka server that gets started when this test class is initialized.
     *
     * It's automatically started before any methods are run via the @ClassRule annotation.
     * It's automatically stopped after all of the tests are completed via the @ClassRule annotation.
     */
    @RegisterExtension
     static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource().withBrokers(1);

    @Test
    public void createSaveLoadAndCompareConfig() {

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", sharedKafkaTestResource.getKafkaConnectString());
        TopicsManager example = new TopicsManager(properties);
        try {
            example.updateKafkaFromConfigFile(getClass().getClassLoader().getResource("compare_config_base.yaml").getPath());
            Map<String, YamlTopicConfig> listings = example.topicListing();
            example.saveToFile(listings, new File("/tmp/test.yaml"));
            Map<String, YamlTopicConfig> reloaedConfigs = example.asConfigs("/tmp/test.yaml");
            assertEquals(listings.size(),  reloaedConfigs.size());
            Map<String, TopicConfigChanges> diffs = example.findDifferences(listings, reloaedConfigs);
            assertEquals( 0, diffs.size());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void compareConfig() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", sharedKafkaTestResource.getKafkaConnectString());
        TopicsManager example = new TopicsManager(properties);

        Map<String, YamlTopicConfig> configsBase = example.asConfigs(getClass().getClassLoader().getResource("compare_config_base.yaml").getPath());
        Map<String, YamlTopicConfig> configsWanted = example.asConfigs(getClass().getClassLoader().getResource("compare_config_wanted.yaml").getPath());
        Map<String, YamlTopicConfig> expectedDifferences = example.asConfigs(getClass().getClassLoader().getResource("compare_config_differences.yaml").getPath());
        Map<String, TopicConfigChanges> differences = example.findDifferences(configsBase, configsWanted);

        assertEquals( expectedDifferences.size(), differences.size());
        assertEquals( 3, differences.get("testtopic1").replication);
        assertEquals( "704800000", differences.get("testtopic2").config.get("segment.ms").configItemName);
    }

    @Test
    public void updateExistingTopic() {
        try {
            AdminClient client = sharedKafkaTestResource.getKafkaTestUtils().getAdminClient();
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", sharedKafkaTestResource.getKafkaConnectString());
            TopicsManager example = new TopicsManager(properties);


            ListTopicsResult listtopics = client.listTopics();
            if (listtopics.namesToListings().get().containsKey("testtopic1")) {
                client.deleteTopics(Arrays.asList("testtopic1")).all().get();
            }
            client.createTopics(Arrays.asList(new NewTopic("testtopic1", 1, (short) 1))).all().get();
            List<Result> results = example.updateKafkaFromConfigFile(getClass().getClassLoader().getResource("compare_config_wanted.yaml").getPath());
            Map<String, YamlTopicConfig> now = example.topicListing();
            System.out.println(now);
            Map<String, YamlTopicConfig> configsWanted = example.asConfigs(getClass().getClassLoader().getResource("compare_config_wanted.yaml").getPath());
            Map<String, TopicConfigChanges> diffs = example.findDifferences(now, configsWanted);
            // we won't have been able to change replication because there is only 1 broker
            assertTrue( diffs.get("testtopic1").replication == 3);
            Stream<Result> res = results.stream().filter(r -> !r.sucessful);
            List<Result> thing = res.collect(Collectors.toList());
            assertTrue(results.stream().filter(r -> !r.sucessful).count() == 1 );

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            assertTrue( false);
        }
    }


    @Test
    public void collectConfigurationFailures() {
        try {
            AdminClient client = sharedKafkaTestResource.getKafkaTestUtils().getAdminClient();
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", sharedKafkaTestResource.getKafkaConnectString());
            TopicsManager example = new TopicsManager(properties);

            ListTopicsResult listtopics = client.listTopics();
            if (listtopics.namesToListings().get().containsKey("testtopic1")) {
                client.deleteTopics(Arrays.asList("testtopic1")).all().get();
            }
            List<Result> results = example.updateKafkaFromConfigFile(getClass().getClassLoader().getResource("too_many_replicas.yaml").getPath());

            System.out.println(results);
            assertTrue( results.stream().filter(r -> r.sucessful).count() == 0);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

}