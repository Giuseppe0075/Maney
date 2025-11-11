package com.giuseppesica.maney.portfolio.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findOwnerIdById(Long id);
}
