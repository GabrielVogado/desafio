package com.outforce.desafio.dto;

import com.outforce.desafio.enums.CupomStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CupomResponseDTO(
		UUID id,
		String code,
		String description,
		BigDecimal discountValue,
		LocalDate expirationDate,
		CupomStatus status,
		Boolean published,
		Boolean redeemed
) {
}

