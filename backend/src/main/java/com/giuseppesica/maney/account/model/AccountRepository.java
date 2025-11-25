package com.giuseppesica.maney.account.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByPortfolioId(Long portfolioId);
    List<Account> findByInstitution(String institution);
    Optional<Account> findByPortfolioIdAndName(Long portfolioId, String name);
}
