package com.example.kafka.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class ApplicationConfig {

    @Value("${topics.payment}")
    private String topicsPayment;

}
