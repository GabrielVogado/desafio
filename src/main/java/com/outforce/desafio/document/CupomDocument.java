package com.outforce.desafio.document;

import com.outforce.desafio.dto.CupomRequestDTO;
import com.outforce.desafio.dto.CupomResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

public interface CupomDocument {

	@Operation(summary = "Cria um novo cupom de desconto")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Cupom criado com sucesso",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = CupomResponseDTO.class)) }),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada",
					content = @Content)
	})
	ResponseEntity<CupomResponseDTO> create(
			@Parameter(description = "Dados para criação do cupom")
			@RequestBody @Valid CupomRequestDTO cupomRequestDTO
	);

	@Operation(summary = "Deleta um cupom existente (soft delete)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Cupom deletado com sucesso",
					content = @Content),
			@ApiResponse(responseCode = "404", description = "Cupom não encontrado",
					content = @Content),
			@ApiResponse(responseCode = "409", description = "Cupom já foi deletado anteriormente",
					content = @Content)
	})
	ResponseEntity<Void> delete(
			@Parameter(description = "ID do cupom a ser deletado")
			@PathVariable UUID id
	);
}


