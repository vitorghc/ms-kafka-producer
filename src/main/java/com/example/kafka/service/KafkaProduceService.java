package com.example.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.kafka.configuration.ApplicationConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProduceService {

    private final ObjectMapper objectMapper;
    private final ApplicationConfig applicationConfig;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(Object request) throws JsonProcessingException {
        String context = objectMapper.writeValueAsString(request);
        kafkaTemplate.send(applicationConfig.getTopicsPayment(), context);
        log.info("Message sent: {}", context);
    }

}
