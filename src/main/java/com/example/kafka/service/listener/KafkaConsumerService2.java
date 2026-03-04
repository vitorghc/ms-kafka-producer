package com.example.kafka.service.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService2 {

    @KafkaListener(topics = "${topics.payment}", groupId = "payment-request-consumer-2")
    public void consume(String message) {
        System.out.println("Message consumed 2: " + message);
    }

}
