package com.outforce.desafio.service;

import com.outforce.desafio.dto.CupomRequestDTO;
import com.outforce.desafio.dto.CupomResponseDTO;

import java.util.UUID;

public interface CupomService {

	CupomResponseDTO create(CupomRequestDTO cupomRequestDTO);

	void delete(UUID id);
}

