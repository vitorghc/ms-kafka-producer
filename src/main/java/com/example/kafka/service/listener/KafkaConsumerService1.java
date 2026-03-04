package com.example.kafka.service.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService1 {

    @KafkaListener(topics = "${topics.payment}", groupId = "payment-request-consumer-1")
    public void consume(String message) {
        System.out.println("Message consumed 1: " + message);
    }

}
