package com.outforce.desafio.controller;

import com.outforce.desafio.document.CupomDocument;
import com.outforce.desafio.dto.CupomRequestDTO;
import com.outforce.desafio.dto.CupomResponseDTO;
import com.outforce.desafio.service.CupomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/coupons")
public class CupomController implements CupomDocument {

	@Autowired
	private CupomService cupomService;

	@Override
	@PostMapping
	public ResponseEntity<CupomResponseDTO> create(@RequestBody @Valid CupomRequestDTO cupomRequestDTO) {
		CupomResponseDTO response = cupomService.create(cupomRequestDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Override
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		cupomService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

