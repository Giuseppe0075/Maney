package com.giuseppesica.maney.user.model;

import com.giuseppesica.maney.portfolio.model.Portfolio;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entity representing a user in the system.
 * Each user has one portfolio and can manage their financial assets.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "app_user")
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * Username chosen by the user.
     */
    @NotNull
    private String username;

    /**
     * User's email address, used for authentication.
     */
    @NotNull
    @Email
    private String email;

    /**
     * Timestamp when the email was verified.
     * Null if email has not been verified yet.
     */
    private Instant emailVerifiedAt;

    /**
     * Hashed password for secure authentication.
     */
    @NotNull
    private String passwordHash;

    /**
     * Timestamp when the user account was created.
     */
    @NotNull
    private Instant createdAt;

    /**
     * Timestamp when the user account was last updated.
     */
    @NotNull
    private Instant updatedAt;

    /**
     * Portfolio owned by this user.
     * One-to-one bidirectional relationship with Portfolio entity.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Portfolio portfolio;

    /**
     * Sets the portfolio for this user and maintains bidirectional relationship.
     *
     * @param p the portfolio to set
     */
    public void setPortfolio(Portfolio p){
        this.portfolio = p;
        if(p != null) {
            p.setUser(this);
        }
    }

    /**
     * JPA callback executed before persisting a new user.
     * Sets creation and update timestamps.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * JPA callback executed before updating an existing user.
     * Updates the modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
