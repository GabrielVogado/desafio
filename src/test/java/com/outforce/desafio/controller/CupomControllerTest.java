package com.outforce.desafio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outforce.desafio.dto.CupomRequestDTO;
import com.outforce.desafio.dto.CupomResponseDTO;
import com.outforce.desafio.enums.CupomStatus;
import com.outforce.desafio.exception.BusinessException;
import com.outforce.desafio.exception.NotFoundException;
import com.outforce.desafio.service.CupomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CupomController.class)
@DisplayName("Testes Unitários - CupomController")
class CupomControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CupomService cupomService;

	private CupomRequestDTO requestValido;
	private CupomResponseDTO responseValido;

	@BeforeEach
	void setUp() {
		requestValido = new CupomRequestDTO(
				"ABC123",
				"Teste Description",
				new BigDecimal("15.00"),
				LocalDate.of(2026, 12, 31),
				true
		);

		responseValido = new CupomResponseDTO(
				UUID.randomUUID(),
				"ABC123",
				"Teste Description",
				new BigDecimal("15.00"),
				LocalDate.of(2026, 12, 31),
				CupomStatus.ACTIVE,
				true,
				false
		);
	}

	@Test
	@DisplayName("POST /coupons - Deve criar cupom e retornar 201 Created")
	void deveCriarCupomComSucesso() throws Exception {
		when(cupomService.create(any(CupomRequestDTO.class))).thenReturn(responseValido);

		mockMvc.perform(post("/coupons")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestValido)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.code", is("ABC123")))
				.andExpect(jsonPath("$.description", is("Teste Description")))
				.andExpect(jsonPath("$.discountValue", is(15.00)))
				.andExpect(jsonPath("$.expirationDate", is("2026-12-31")))
				.andExpect(jsonPath("$.status", is("ACTIVE")))
				.andExpect(jsonPath("$.published", is(true)))
				.andExpect(jsonPath("$.redeemed", is(false)));

		verify(cupomService, times(1)).create(any(CupomRequestDTO.class));
	}

	@Test
	@DisplayName("POST /coupons - Deve retornar 400 quando código inválido")
	void deveRetornar400QuandoCodigoInvalido() throws Exception {
		when(cupomService.create(any(CupomRequestDTO.class)))
				.thenThrow(new BusinessException("COUPON_CODE_LENGTH", "Código do cupom deve ter 6 caracteres alfanuméricos"));

		CupomRequestDTO requestInvalido = new CupomRequestDTO(
				"AB12",
				"Teste",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				false
		);

		mockMvc.perform(post("/coupons")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestInvalido)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code", is("COUPON_CODE_LENGTH")))
				.andExpect(jsonPath("$.message", containsString("6 caracteres")));

		verify(cupomService, times(1)).create(any(CupomRequestDTO.class));
	}

	@Test
	@DisplayName("POST /coupons - Deve retornar 400 quando desconto abaixo do mínimo")
	void deveRetornar400QuandoDescontoAbaixoDoMinimo() throws Exception {
		CupomRequestDTO requestInvalido = new CupomRequestDTO(
				"ABC123",
				"Teste",
				new BigDecimal("0.49"),
				LocalDate.of(2026, 12, 31),
				false
		);

		mockMvc.perform(post("/coupons")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestInvalido)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
				.andExpect(jsonPath("$.message", containsString("0.5")));

		verify(cupomService, never()).create(any(CupomRequestDTO.class));
	}

	@Test
	@DisplayName("POST /coupons - Deve retornar 400 quando data no passado")
	void deveRetornar400QuandoDataNoPassado() throws Exception {
		when(cupomService.create(any(CupomRequestDTO.class)))
				.thenThrow(new BusinessException("COUPON_EXPIRATION_PAST", "Data de expiração não pode estar no passado"));

		CupomRequestDTO requestInvalido = new CupomRequestDTO(
				"ABC123",
				"Teste",
				new BigDecimal("10.00"),
				LocalDate.of(2025, 1, 1),
				false
		);

		mockMvc.perform(post("/coupons")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestInvalido)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code", is("COUPON_EXPIRATION_PAST")))
				.andExpect(jsonPath("$.message", containsString("passado")));
	}

	@Test
	@DisplayName("POST /coupons - Deve limpar código com caracteres especiais")
	void deveLimparCodigoComCaracteresEspeciais() throws Exception {
		CupomResponseDTO responseComCodigoLimpo = new CupomResponseDTO(
				UUID.randomUUID(),
				"AB12C3",
				"Teste Description",
				new BigDecimal("15.00"),
				LocalDate.of(2026, 12, 31),
				CupomStatus.ACTIVE,
				true,
				false
		);

		when(cupomService.create(any(CupomRequestDTO.class))).thenReturn(responseComCodigoLimpo);

		CupomRequestDTO requestComEspeciais = new CupomRequestDTO(
				"AB-12$C3",
				"Teste Description",
				new BigDecimal("15.00"),
				LocalDate.of(2026, 12, 31),
				true
		);

		mockMvc.perform(post("/coupons")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestComEspeciais)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code", is("AB12C3")));
	}

	@Test
	@DisplayName("DELETE /coupons/{id} - Deve deletar cupom e retornar 204 No Content")
	void deveDeletarCupomComSucesso() throws Exception {
		UUID cupomId = UUID.randomUUID();
		doNothing().when(cupomService).delete(cupomId);

		mockMvc.perform(delete("/coupons/" + cupomId))
				.andExpect(status().isNoContent());

		verify(cupomService, times(1)).delete(cupomId);
	}

	@Test
	@DisplayName("DELETE /coupons/{id} - Deve retornar 404 quando cupom não existe")
	void deveRetornar404QuandoCupomNaoExiste() throws Exception {
		UUID cupomId = UUID.randomUUID();
		doThrow(new NotFoundException("Cupom não encontrado"))
				.when(cupomService).delete(cupomId);

		mockMvc.perform(delete("/coupons/" + cupomId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code", is("COUPON_NOT_FOUND")))
				.andExpect(jsonPath("$.message", containsString("não encontrado")));

		verify(cupomService, times(1)).delete(cupomId);
	}

	@Test
	@DisplayName("DELETE /coupons/{id} - Deve retornar 409 quando cupom já deletado")
	void deveRetornar409QuandoCupomJaDeletado() throws Exception {
		UUID cupomId = UUID.randomUUID();
		doThrow(new BusinessException("COUPON_ALREADY_DELETED", "Cupom já foi deletado"))
				.when(cupomService).delete(cupomId);

		mockMvc.perform(delete("/coupons/" + cupomId))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code", is("COUPON_ALREADY_DELETED")))
				.andExpect(jsonPath("$.message", containsString("já foi deletado")));

		verify(cupomService, times(1)).delete(cupomId);
	}

	@Test
	@DisplayName("POST /coupons - Deve aceitar cupom sem published (default false)")
	void deveAceitarCupomSemPublished() throws Exception {
		CupomRequestDTO requestSemPublished = new CupomRequestDTO(
				"ABC123",
				"Teste",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				null
		);

		CupomResponseDTO responseComPublishedFalse = new CupomResponseDTO(
				UUID.randomUUID(),
				"ABC123",
				"Teste",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				CupomStatus.ACTIVE,
				false,
				false
		);

		when(cupomService.create(any(CupomRequestDTO.class))).thenReturn(responseComPublishedFalse);

		mockMvc.perform(post("/coupons")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestSemPublished)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.published", is(false)));
	}

	@Test
	@DisplayName("POST /coupons - Deve retornar 400 para JSON malformado")
	void deveRetornar400ParaJsonMalformado() throws Exception {
		String jsonMalformado = "{\"code\": \"ABC123\", \"description\": }";

		mockMvc.perform(post("/coupons")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonMalformado))
				.andExpect(status().isBadRequest());

		verify(cupomService, never()).create(any(CupomRequestDTO.class));
	}

	@Test
	@DisplayName("POST /coupons - Deve aceitar desconto mínimo (0.5)")
	void deveAceitarDescontoMinimo() throws Exception {

		CupomRequestDTO requestComDescontoMinimo = new CupomRequestDTO(
				"ABC123",
				"Desconto mínimo",
				new BigDecimal("0.5"),
				LocalDate.of(2026, 12, 31),
				false
		);

		CupomResponseDTO response = new CupomResponseDTO(
				UUID.randomUUID(),
				"ABC123",
				"Desconto mínimo",
				new BigDecimal("0.5"),
				LocalDate.of(2026, 12, 31),
				CupomStatus.ACTIVE,
				false,
				false
		);

		when(cupomService.create(any(CupomRequestDTO.class))).thenReturn(response);

		mockMvc.perform(post("/coupons")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestComDescontoMinimo)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.discountValue", is(0.5)));
	}

	@Test
	@DisplayName("DELETE /coupons/{id} - Deve aceitar UUID válido")
	void deveAceitarUuidValido() throws Exception {
		UUID cupomId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
		doNothing().when(cupomService).delete(cupomId);

		mockMvc.perform(delete("/coupons/" + cupomId))
				.andExpect(status().isNoContent());

		verify(cupomService, times(1)).delete(cupomId);
	}

	@Test
	@DisplayName("POST /coupons - Deve retornar 400 quando código já existe")
	void deveRetornar400QuandoCodigoDuplicado() throws Exception {
		when(cupomService.create(any(CupomRequestDTO.class)))
				.thenThrow(new BusinessException("COUPON_CODE_DELETED", "Código do cupom com Status deletado"));

		CupomRequestDTO requestComCodigoDuplicado = new CupomRequestDTO(
				"ABC123",
				"Teste",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				false
		);

		mockMvc.perform(post("/coupons")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestComCodigoDuplicado)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code", is("COUPON_CODE_DELETED")))
				.andExpect(jsonPath("$.message", containsString("deletado")));

		verify(cupomService, times(1)).create(any(CupomRequestDTO.class));
	}
}

