package com.simplybusiness.dna.common.topics;



public class Result {
    Boolean sucessful;
    String topicName;
    String message;

    public Result(Boolean sucessful, String topicName, String message) {
        this.sucessful = sucessful;
        this.topicName = topicName;
        this.message = message;
    }

    public Boolean getSucessful() {
        return sucessful;
    }

    public void setSucessful(Boolean sucessful) {
        this.sucessful = sucessful;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
