package com.gsr.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageListener {

    @KafkaListener(topics = "candle-data", groupId = "dataGrpId")
    public void listener(String message) {
        System.out.println("*Received Message from Kafka*: " + message);
    }
}
