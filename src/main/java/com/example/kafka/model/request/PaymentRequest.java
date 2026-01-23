package com.example.kafka.model.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "KAF.400.003")
    private Integer number;

    @NotBlank(message = "KAF.400.003")
    private String description;

    @NotNull(message = "KAF.400.003")
    private BigDecimal value;

}
