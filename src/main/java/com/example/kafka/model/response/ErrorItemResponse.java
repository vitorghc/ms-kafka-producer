package com.example.kafka.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorItemResponse {

    private String field;

    private String message;

    private String title;

    public ErrorItemResponse(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public ErrorItemResponse(String message) {
        this.message = message;
    }
}
