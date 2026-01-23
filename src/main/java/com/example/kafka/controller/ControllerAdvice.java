package com.example.kafka.controller;

import com.example.kafka.exception.NotFoundException;
import com.example.kafka.exception.RestException;
import com.example.kafka.model.response.ErrorItemResponse;
import com.example.kafka.model.response.ErrorResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {

    private static final Integer JVM_MAX_STRING_LEN = 2147483647;
    private static final String MS_ERROR_CODE_PREFIX = "KAF.";

    private final MessageSource messageSource;

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> missingRequestHeaderException(final MissingRequestHeaderException e) {
        String errorCode = MS_ERROR_CODE_PREFIX + "400.003";
        return new ResponseEntity<>(new ErrorResponse(errorCode, getMessage(errorCode, e.getHeaderName())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> methodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        String errorCode = MS_ERROR_CODE_PREFIX + "400.000";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorCode, getMessage(errorCode, e.getName())));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Error> mediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException e) {
        return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<List<ErrorResponse>> assertionException(final HttpMessageNotReadableException e) {
        if (e.getCause() instanceof JsonMappingException cause) {
            String field = cause.getPath().stream()
                    .map(reference -> reference.getFrom() instanceof Collection<?>
                            ? "[" + reference.getIndex() + "]"
                            : reference.getFieldName())
                    .collect(Collectors.joining("."))
                    .replaceAll(".\\[", "[");

            String errorCode = MS_ERROR_CODE_PREFIX + "400.002";
            List<ErrorResponse> response = Stream.of(new ErrorResponse(errorCode, getMessage(errorCode, field))).toList();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return defaultBadRequestError();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<List<ErrorResponse>> missingServletRequestParameterException(final MissingServletRequestParameterException e) {
        String field = e.getParameterName();
        String errorCode = MS_ERROR_CODE_PREFIX + "400.003";
        List<ErrorResponse> response = Stream.of(new ErrorResponse(errorCode, getMessage(errorCode, field))).toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponse>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException methodArgumentNotValidException) {
        List<ErrorResponse> errorResponses = Optional.ofNullable(methodArgumentNotValidException)
                .filter(argumentNotValidException -> !ObjectUtils.isEmpty(argumentNotValidException.getBindingResult()))
                .map(MethodArgumentNotValidException::getBindingResult)
                .filter(bindingResult -> !ObjectUtils.isEmpty(bindingResult.getAllErrors()))
                .map(BindingResult::getAllErrors).stream()
                .flatMap(Collection::stream)
                .filter(objectError -> !ObjectUtils.isEmpty(objectError))
                .map(this::createValidationError)
                .toList();

        if (errorResponses.isEmpty()) {
            String errorCode = MS_ERROR_CODE_PREFIX + "400.001";
            List<ErrorResponse> response = Stream.of(new ErrorResponse(errorCode, getMessage(errorCode))).toList();
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.badRequest().body(errorResponses);
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<Object> handleRestException(RestException exception) {
        String message = exception.getMessage();
        ResponseEntity<Object> response = ResponseEntity.status(exception.getStatus()).build();
        if (exception.getResponseBodyCode() != null) {
            message = getMessage(exception.getResponseBodyCode());
            response = ResponseEntity.status(exception.getStatus())
                    .body(new ErrorResponse(
                            exception.getResponseBodyCode(),
                            message,
                            Collections.emptyList()));
        }
        if (exception.getResponseBody() != null) {
            message = getMessage(exception.getResponseBody().getCode());
            response = ResponseEntity.status(exception.getStatus())
                    .body(new ErrorResponse(
                            exception.getResponseBody().getCode(),
                            message,
                            exception.getResponseBody().getErrors()));
        }
        log.error(message, exception);
        return response;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Error> handleNoResourceFoundException(final NoResourceFoundException ex) {
        log.info("{}", ex.getMessage(), ex);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException exception) {
        String message = exception.getMessage();
        ResponseEntity<Object> response = ResponseEntity.status(exception.getStatus()).build();
        log.info(message, exception);
        return response;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        List<ErrorItemResponse> errors = e.getConstraintViolations().stream()
                .map(constraint -> {
                    String field = (((PathImpl) constraint.getPropertyPath()).getLeafNode().asString());
                    return new ErrorItemResponse(field, getMessage(constraint.getMessageTemplate(), field));
                })
                .toList();
        ErrorResponse errorResponse = new ErrorResponse(
                MS_ERROR_CODE_PREFIX + "400.000",
                getMessage(MS_ERROR_CODE_PREFIX + "400.000"),
                errors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private ResponseEntity<List<ErrorResponse>> defaultBadRequestError() {
        String errorCode = MS_ERROR_CODE_PREFIX + "400.000";
        List<ErrorResponse> response = Stream.of(new ErrorResponse(errorCode, getMessage(errorCode))).toList();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private ErrorResponse createValidationError(ObjectError error) {
        String field = "";
        if (error instanceof FieldError fieldError) {
            field = fieldError.getField();
        }

        String code = error.getDefaultMessage();
        Object[] errorArguments = error.getArguments();
        if ("Size".equals(error.getCode())) {
            return retrieveSizeErrorResponse(code, field, errorArguments);
        }

        if ("DecimalMin".equals(error.getCode()) && errorArguments != null) {
            return new ErrorResponse(code, getMessage(code, field, errorArguments[2]));
        }

        if (errorArguments != null && errorArguments.length > 0) {
            Object[] args = ArrayUtils.add(errorArguments, field);
            return new ErrorResponse(code, getMessage(code, args));
        }

        return new ErrorResponse(code, getMessage(code, field));
    }

    private ErrorResponse retrieveSizeErrorResponse(String code, String field, Object[] errorArguments) {
        String errorMessage = "";
        Integer min = null;
        Integer max = null;
        if (errorArguments != null && errorArguments.length > 2) {
            Integer rawMax = (Integer) errorArguments[1];
            max = rawMax.equals(JVM_MAX_STRING_LEN) ? null : rawMax;

            Integer rawMin = (Integer) errorArguments[2];
            min = rawMin == 0 ? null : rawMin;
        }

        if (min != null && max != null) {
            errorMessage = getMessage(code, field, min, max);
        } else if (min != null) {
            errorMessage = getMessage(code, field, min);
        } else if (max != null) {
            errorMessage = getMessage(code, field, max);
        }

        return new ErrorResponse(code, errorMessage);
    }

    private String getMessage(String code, Object... args) {
        try {
            return this.messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            log.error("Message with code \"{}\" does not exists.", code);
            throw e;
        }
    }
}
