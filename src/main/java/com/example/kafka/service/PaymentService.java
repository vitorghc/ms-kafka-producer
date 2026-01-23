package com.example.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.kafka.model.request.PaymentRequest;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final KafkaProduceService kafkaProduceService;

    public void cretePayment(PaymentRequest request) {
        try {
            kafkaProduceService.sendMessage(request);
        } catch (JsonProcessingException e) {
            log.error("Error on send message to kafka: {}", e.getMessage());
        }
    }

}
