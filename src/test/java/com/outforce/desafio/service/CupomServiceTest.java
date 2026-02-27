package com.outforce.desafio.service;

import com.outforce.desafio.dto.CupomRequestDTO;
import com.outforce.desafio.dto.CupomResponseDTO;
import com.outforce.desafio.enums.CupomStatus;
import com.outforce.desafio.exception.BusinessException;
import com.outforce.desafio.exception.NotFoundException;
import com.outforce.desafio.model.Cupom;
import com.outforce.desafio.repository.CupomRepository;
import com.outforce.desafio.service.impl.CupomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - CupomService")
class CupomServiceTest {

	@Mock
	private CupomRepository cupomRepository;

	@InjectMocks
	private CupomServiceImpl cupomService;

	private CupomRequestDTO requestValido;
	private Cupom cupomMock;

	@BeforeEach
	void setUp() {
		requestValido = new CupomRequestDTO(
				"ABC123",
				"Cupom de teste",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				true
		);

		cupomMock = Cupom.builder()
				.id(UUID.randomUUID())
				.code("ABC123")
				.description("Cupom de teste")
				.discountValue(new BigDecimal("10.00"))
				.expirationDate(LocalDate.of(2026, 12, 31))
				.status(CupomStatus.ACTIVE)
				.published(true)
				.redeemed(false)
				.deletedAt(null)
				.build();
	}

	@Test
	@DisplayName("Deve criar cupom com código limpo (removendo caracteres especiais)")
	void deveCriarCupomComCodigoLimpo() {
		CupomRequestDTO request = new CupomRequestDTO(
				"AB-12$C3",
				"Summer Sale",
				new BigDecimal("15.00"),
				LocalDate.of(2026, 6, 30),
				true
		);

		Cupom cupomEsperado = Cupom.builder()
				.id(UUID.randomUUID())
				.code("AB12C3")
				.description("Summer Sale")
				.discountValue(new BigDecimal("15.00"))
				.expirationDate(LocalDate.of(2026, 6, 30))
				.status(CupomStatus.ACTIVE)
				.published(true)
				.redeemed(false)
				.build();

		when(cupomRepository.save(any(Cupom.class))).thenReturn(cupomEsperado);

		CupomResponseDTO response = cupomService.create(request);

		assertThat(response).isNotNull();
		assertThat(response.code()).isEqualTo("AB12C3");
		assertThat(response.description()).isEqualTo("Summer Sale");
		assertThat(response.discountValue()).isEqualByComparingTo("15.00");
		assertThat(response.status()).isEqualTo(CupomStatus.ACTIVE);
		assertThat(response.published()).isTrue();
		assertThat(response.redeemed()).isFalse();

		verify(cupomRepository, times(1)).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve criar cupom com código alfanumérico válido")
	void deveCriarCupomComCodigoValido() {
		when(cupomRepository.save(any(Cupom.class))).thenReturn(cupomMock);

		CupomResponseDTO response = cupomService.create(requestValido);

		assertThat(response).isNotNull();
		assertThat(response.code()).isEqualTo("ABC123");
		verify(cupomRepository, times(1)).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando código é nulo")
	void deveLancarExcecaoQuandoCodigoNulo() {
		CupomRequestDTO request = new CupomRequestDTO(
				null,
				"Descrição",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				false
		);

		assertThatThrownBy(() -> cupomService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_CODE_REQUIRED")
				.hasMessageContaining("obrigatório");

		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando código está vazio")
	void deveLancarExcecaoQuandoCodigoVazio() {
		CupomRequestDTO request = new CupomRequestDTO(
				"   ",
				"Descrição",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				false
		);

		assertThatThrownBy(() -> cupomService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_CODE_REQUIRED");

		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando código tem menos de 6 caracteres alfanuméricos")
	void deveLancarExcecaoQuandoCodigoComMenosDe6Caracteres() {
		CupomRequestDTO request = new CupomRequestDTO(
				"AB-12",
				"Descrição",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				false
		);

		assertThatThrownBy(() -> cupomService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_CODE_LENGTH")
				.hasMessageContaining("6 caracteres");

		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando código tem mais de 6 caracteres alfanuméricos")
	void deveLancarExcecaoQuandoCodigoComMaisDe6Caracteres() {
		CupomRequestDTO request = new CupomRequestDTO(
				"ABCDEFG",
				"Descrição",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				false
		);

		assertThatThrownBy(() -> cupomService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_CODE_LENGTH");

		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando descrição é nula")
	void deveLancarExcecaoQuandoDescricaoNula() {
		CupomRequestDTO request = new CupomRequestDTO(
				"ABC123",
				null,
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				false
		);

		assertThatThrownBy(() -> cupomService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_DESCRIPTION_REQUIRED");

		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando valor de desconto é nulo")
	void deveLancarExcecaoQuandoDescontoNulo() {
		CupomRequestDTO request = new CupomRequestDTO(
				"ABC123",
				"Descrição",
				null,
				LocalDate.of(2026, 12, 31),
				false
		);

		assertThatThrownBy(() -> cupomService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_DISCOUNT_REQUIRED");

		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando desconto é menor que 0.5")
	void deveLancarExcecaoQuandoDescontoMenorQueMinimo() {
		CupomRequestDTO request = new CupomRequestDTO(
				"ABC123",
				"Descrição",
				new BigDecimal("0.49"),
				LocalDate.of(2026, 12, 31),
				false
		);

		assertThatThrownBy(() -> cupomService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_DISCOUNT_MIN")
				.hasMessageContaining("0.5");

		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve aceitar desconto exatamente 0.5")
	void deveAceitarDescontoMinimo() {
		CupomRequestDTO request = new CupomRequestDTO(
				"ABC123",
				"Descrição",
				new BigDecimal("0.5"),
				LocalDate.of(2026, 12, 31),
				false
		);

		Cupom cupomComDescontoMinimo = Cupom.builder()
				.id(UUID.randomUUID())
				.code("ABC123")
				.description("Descrição")
				.discountValue(new BigDecimal("0.5"))
				.expirationDate(LocalDate.of(2026, 12, 31))
				.status(CupomStatus.ACTIVE)
				.published(false)
				.redeemed(false)
				.build();

		when(cupomRepository.save(any(Cupom.class))).thenReturn(cupomComDescontoMinimo);

		CupomResponseDTO response = cupomService.create(request);

		assertThat(response).isNotNull();
		assertThat(response.discountValue()).isEqualByComparingTo("0.5");
		verify(cupomRepository, times(1)).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando data de expiração é nula")
	void deveLancarExcecaoQuandoDataExpiracaoNula() {
		CupomRequestDTO request = new CupomRequestDTO(
				"ABC123",
				"Descrição",
				new BigDecimal("10.00"),
				null,
				false
		);

		assertThatThrownBy(() -> cupomService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_EXPIRATION_REQUIRED");

		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando data de expiração está no passado")
	void deveLancarExcecaoQuandoDataNoPassado() {
		CupomRequestDTO request = new CupomRequestDTO(
				"ABC123",
				"Descrição",
				new BigDecimal("10.00"),
				LocalDate.of(2025, 1, 1),
				false
		);

		assertThatThrownBy(() -> cupomService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_EXPIRATION_PAST")
				.hasMessageContaining("passado");

		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve deletar cupom com soft delete")
	void deveDeletarCupomComSoftDelete() {
		UUID cupomId = UUID.randomUUID();
		Cupom cupomExistente = Cupom.builder()
				.id(cupomId)
				.code("ABC123")
				.description("Cupom")
				.discountValue(new BigDecimal("10.00"))
				.expirationDate(LocalDate.of(2026, 12, 31))
				.status(CupomStatus.ACTIVE)
				.published(false)
				.redeemed(false)
				.deletedAt(null)
				.build();

		when(cupomRepository.findById(cupomId)).thenReturn(Optional.of(cupomExistente));
		when(cupomRepository.save(any(Cupom.class))).thenReturn(cupomExistente);

		cupomService.delete(cupomId);

		verify(cupomRepository, times(1)).findById(cupomId);
		verify(cupomRepository, times(1)).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar NotFoundException ao deletar cupom inexistente")
	void deveLancarNotFoundAoDeletarCupomInexistente() {
		UUID cupomId = UUID.randomUUID();
		when(cupomRepository.findById(cupomId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cupomService.delete(cupomId))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("não encontrado");

		verify(cupomRepository, times(1)).findById(cupomId);
		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar BusinessException ao tentar deletar cupom já deletado")
	void deveLancarExcecaoAoDeletarCupomJaDeletado() {
		UUID cupomId = UUID.randomUUID();
		Cupom cupomDeletado = Cupom.builder()
				.id(cupomId)
				.code("ABC123")
				.description("Cupom")
				.discountValue(new BigDecimal("10.00"))
				.expirationDate(LocalDate.of(2026, 12, 31))
				.status(CupomStatus.DELETED)
				.published(false)
				.redeemed(false)
				.deletedAt(Instant.now())
				.build();

		when(cupomRepository.findById(cupomId)).thenReturn(Optional.of(cupomDeletado));

		assertThatThrownBy(() -> cupomService.delete(cupomId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_ALREADY_DELETED")
				.hasMessageContaining("já foi deletado");

		verify(cupomRepository, times(1)).findById(cupomId);
		verify(cupomRepository, never()).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve criar cupom com published=false quando não informado")
	void deveCriarCupomComPublishedFalseQuandoNaoInformado() {
		CupomRequestDTO request = new CupomRequestDTO(
				"ABC123",
				"Descrição",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				null
		);

		Cupom cupomNaoPublicado = Cupom.builder()
				.id(UUID.randomUUID())
				.code("ABC123")
				.description("Descrição")
				.discountValue(new BigDecimal("10.00"))
				.expirationDate(LocalDate.of(2026, 12, 31))
				.status(CupomStatus.ACTIVE)
				.published(false)
				.redeemed(false)
				.build();

		when(cupomRepository.save(any(Cupom.class))).thenReturn(cupomNaoPublicado);

		CupomResponseDTO response = cupomService.create(request);

		assertThat(response.published()).isFalse();
		verify(cupomRepository, times(1)).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve fazer trim na descrição antes de salvar")
	void deveFazerTrimNaDescricao() {
		CupomRequestDTO request = new CupomRequestDTO(
				"ABC123",
				"  Descrição com espaços  ",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				false
		);

		when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> {
			Cupom cupomSalvo = invocation.getArgument(0);
			assertThat(cupomSalvo.getDescription()).isEqualTo("Descrição com espaços");
			return cupomSalvo;
		});

		cupomService.create(request);

		verify(cupomRepository, times(1)).save(any(Cupom.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando código já existe")
	void deveLancarExcecaoQuandoCodigoDuplicado() {
		CupomRequestDTO request = new CupomRequestDTO(
				"ABC123",
				"Descrição",
				new BigDecimal("10.00"),
				LocalDate.of(2026, 12, 31),
				false
		);

		when(cupomRepository.findByCode("ABC123")).thenReturn(Optional.of(cupomMock));

		assertThatThrownBy(() -> cupomService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", "COUPON_CODE_DELETED")
				.hasMessageContaining("deletado");

		verify(cupomRepository, never()).save(any(Cupom.class));
	}
}

