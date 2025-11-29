package com.giuseppesica.maney.account.model;

import com.giuseppesica.maney.portfolio.model.Portfolio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name="account",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_account_portfolio_name",
                        columnNames = {"portfolio_id", "name"}
                )
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @NotNull
    private String name;

    @NotBlank
    private String institution;

    private Instant openedAt;

    private Instant closedAt;

    private String note;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

}
