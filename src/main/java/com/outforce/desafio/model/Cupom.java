package com.outforce.desafio.model;

import com.outforce.desafio.enums.CupomStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cupons")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cupom {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(columnDefinition = "UUID")
	private UUID id;

	@Column(nullable = false, length = 6, unique = true)
	private String code;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal discountValue;

	@Column(nullable = false)
	private LocalDate expirationDate;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CupomStatus status = CupomStatus.ACTIVE;

	@Builder.Default
	@Column(nullable = false)
	private Boolean published = false;

	@Builder.Default
	@Column(nullable = false)
	private Boolean redeemed = false;

	@Column
	private Instant deletedAt;

	public boolean isDeleted() {
		return deletedAt != null;
	}
}



