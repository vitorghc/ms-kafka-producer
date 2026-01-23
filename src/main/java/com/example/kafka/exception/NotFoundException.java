package com.example.kafka.exception;

import com.example.kafka.model.response.ErrorResponse;
import org.springframework.http.HttpStatus;

public class NotFoundException extends RestException {

    private static final long serialVersionUID = -4546342692615580312L;

    private final String responseBodyCode;

    private final ErrorResponse responseBody;

    public NotFoundException(String message) {
        super(message);
        responseBody = null;
        responseBodyCode = null;
    }

    public NotFoundException(String codeError, String message) {
        super(message);
        responseBody = null;
        responseBodyCode = codeError;
    }

    public NotFoundException(ErrorResponse responseBody) {
        this.responseBodyCode = null;
        this.responseBody = responseBody;
    }

    public NotFoundException() {
        this.responseBodyCode = null;
        this.responseBody = null;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getResponseBodyCode() {
        return responseBodyCode;
    }

    @Override
    public ErrorResponse getResponseBody() {
        return responseBody;
    }
}
