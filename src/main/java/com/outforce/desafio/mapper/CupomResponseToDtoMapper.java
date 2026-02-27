package com.outforce.desafio.mapper;

import com.outforce.desafio.dto.CupomResponseDTO;
import com.outforce.desafio.model.Cupom;

public class CupomResponseToDtoMapper {

	public static CupomResponseDTO toResponseDTO(Cupom cupom) {
		return new CupomResponseDTO(
				cupom.getId(),
				cupom.getCode(),
				cupom.getDescription(),
				cupom.getDiscountValue(),
				cupom.getExpirationDate(),
				cupom.getStatus(),
				cupom.getPublished(),
				cupom.getRedeemed()
		);
	}
}
