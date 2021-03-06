# kafka-topic-config
[![Java CI with Gradle](https://github.com/simplybusiness/kafka-topic-config/actions/workflows/gradle.yml/badge.svg)](https://github.com/simplybusiness/kafka-topic-config/actions/workflows/gradle.yml)

> Save, edit, apply Kafka topic configurations from yaml

## 🚩 Table of Contents

- [Packages](#-packages)
- [Why kafka-topic-config](#-why-kafka-topic-config)
- [Features](#-features)
- [Examples](#-examples)
- [Pull Request Steps](#-pull-request-steps)
- [Contributing](#-contributing)
- [Used By](#-used-by)
- [License](#-license)


## Artifacts

[On maven central](https://search.maven.org/artifact/com.simplybusiness/kafka-topic-config/1.0.0/jar)

## 📦 Packages

com.simplybusiness.dna.common.topics

This is really it. The project come out of a need to do everything we can using Source Code Control.

## 🐾 Examples

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



## 🔧 Pull Request Steps

- Get in touch and tell us what you are planning to do
- There may be reasons why things are the way they are
- We will get back to you as soon as possible
- Of course, we'll still welcome pull requests if you haven't done this

#### Run tests

./gradlew test

### Pull Request

Before creating a PR, test and check for any errors. If there are no errors, then commit and push.

For more information, please refer to the Contributing section.

## 💬 Contributing

We would welcome additions, bugfixes or documentation.

Please note that this project is released with a Contributor Code of Conduct. By participating in this project, you agree to abide by its terms. The Code of Conduct can be found [here](CODE_OF_CONDUCT.md).

## 🚀 Used By

[Simply Business](https://www.simplybusiness.co.uk/)

## 📜 License

MIT License

Copyright (c) 2021 Xbridge Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

