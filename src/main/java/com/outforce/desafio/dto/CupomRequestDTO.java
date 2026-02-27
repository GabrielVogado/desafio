package com.outforce.desafio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CupomRequestDTO(
		@NotBlank(message = "Código do cupom é obrigatório")
		String code,

		@NotBlank(message = "Descrição do cupom é obrigatória")
		String description,

		@NotNull(message = "Valor de desconto é obrigatório")
		@DecimalMin(value = "0.5", message = "Valor de desconto deve ser no mínimo 0.5")
		BigDecimal discountValue,

		@NotNull(message = "Data de expiração é obrigatória")
		LocalDate expirationDate,

		Boolean published
) {
}

