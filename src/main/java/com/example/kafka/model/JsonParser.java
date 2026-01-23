package com.example.kafka.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class JsonParser {

    private static ObjectMapper mapper;

    public static void initialize(ObjectMapper objectMapper) {
        if (mapper == null) {
            mapper = objectMapper;
        }
    }

    @SneakyThrows
    public static String objectToStringJson(Object value) {
        return mapper.writeValueAsString(value);
    }

    @SneakyThrows
    public static <T> T stringJsonToObject(String json, Class<T> clazz) {
        return mapper.readValue(json, clazz);
    }

    @SneakyThrows
    public static <T> List<T> stringJsonToList(String json, Class<T> clazz) {
        return mapper.readValue(json, new TypeReference<List<T>>() {
            @Override
            public Type getType() {
                return mapper.getTypeFactory().constructCollectionType(List.class, clazz);
            }
        });
    }

    @SneakyThrows
    public static Map<String, Object> objectToMap(Object object) {
        return mapper.convertValue(object, new TypeReference<Map<String, Object>>() {
        });
    }

    @SneakyThrows
    public static <T> T convertValue(Object value, Class<T> clazz) {
        return mapper.convertValue(value, clazz);
    }

    private JsonParser() {
    }
}
