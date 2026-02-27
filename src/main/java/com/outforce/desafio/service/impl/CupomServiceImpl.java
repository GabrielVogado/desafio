package com.outforce.desafio.service.impl;

import com.outforce.desafio.dto.CupomRequestDTO;
import com.outforce.desafio.dto.CupomResponseDTO;
import com.outforce.desafio.enums.CupomStatus;
import com.outforce.desafio.exception.BusinessException;
import com.outforce.desafio.exception.NotFoundException;
import com.outforce.desafio.model.Cupom;
import com.outforce.desafio.repository.CupomRepository;
import com.outforce.desafio.service.CupomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static com.outforce.desafio.mapper.CupomResponseToDtoMapper.toResponseDTO;

@Service
public class CupomServiceImpl implements CupomService {

	@Autowired
	private CupomRepository cupomRepository;

	@Override
	public CupomResponseDTO create(CupomRequestDTO cupomRequestDTO) {
		validarCamposObrigatorios(cupomRequestDTO);

		String codigoLimpo = removeCaracteresEspeciaisEValidaCupom(cupomRequestDTO.code());

		validarCodigoDuplicado(codigoLimpo);

		validarDescontoEData(cupomRequestDTO);

		Cupom cupom = Cupom.builder()
				.code(codigoLimpo)
				.description(cupomRequestDTO.description().trim())
				.discountValue(cupomRequestDTO.discountValue())
				.expirationDate(cupomRequestDTO.expirationDate())
				.published(cupomRequestDTO.published() != null ? cupomRequestDTO.published() : false)
				.build();

		Cupom savedCupom = cupomRepository.save(cupom);

		return toResponseDTO(savedCupom);
	}

	@Override
	public void delete(UUID id) {
		Cupom cupom = cupomRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Cupom não encontrado"));

		if (cupom.isDeleted()) {
			throw new BusinessException("COUPON_ALREADY_DELETED", "Cupom já foi deletado");
		}

		cupom.setStatus(CupomStatus.DELETED);
		cupom.setDeletedAt(Instant.now());
		cupomRepository.save(cupom);
	}

	private void validarCamposObrigatorios(CupomRequestDTO dto) {
		if (dto.code() == null || dto.code().isBlank()) {
			throw new BusinessException("COUPON_CODE_REQUIRED", "Código do cupom é obrigatório");
		}
		if (dto.description() == null || dto.description().isBlank()) {
			throw new BusinessException("COUPON_DESCRIPTION_REQUIRED", "Descrição do cupom é obrigatória");
		}
		if (dto.discountValue() == null) {
			throw new BusinessException("COUPON_DISCOUNT_REQUIRED", "Valor de desconto é obrigatório");
		}
		if (dto.expirationDate() == null) {
			throw new BusinessException("COUPON_EXPIRATION_REQUIRED", "Data de expiração é obrigatória");
		}
	}

	private String removeCaracteresEspeciaisEValidaCupom(String code) {
		String codigoLimpo = code.replaceAll("[^A-Za-z0-9]", "");
		if (codigoLimpo.length() != 6) {
			throw new BusinessException("COUPON_CODE_LENGTH", "Código do cupom deve ter 6 caracteres alfanuméricos");
		}
		return codigoLimpo.toUpperCase();
	}

	private void validarDescontoEData(CupomRequestDTO dto) {
		if (dto.discountValue().compareTo(new BigDecimal("0.5")) < 0) {
			throw new BusinessException("COUPON_DISCOUNT_MIN", "Valor de desconto deve ser no mínimo 0.5");
		}

		if (dto.expirationDate().isBefore(LocalDate.now())) {
			throw new BusinessException("COUPON_EXPIRATION_PAST", "Data de expiração não pode estar no passado");
		}
	}

	private void validarCodigoDuplicado(String code) {
		cupomRepository.findByCode(code).ifPresent(cupom -> {
			throw new BusinessException("COUPON_CODE_DELETED", "Código do cupom com Status deletado");
		});
	}
}

