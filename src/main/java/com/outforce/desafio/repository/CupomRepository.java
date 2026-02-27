package com.outforce.desafio.repository;

import com.outforce.desafio.model.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CupomRepository extends JpaRepository<Cupom, UUID> {

	@Query("SELECT c FROM Cupom c WHERE c.code = :code")
	Optional<Cupom> findByCode(@Param("code") String code);
}
