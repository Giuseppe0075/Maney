package com.giuseppesica.maney.portfolio.model;

import com.giuseppesica.maney.illiquidasset.model.IlliquidAsset;
import com.giuseppesica.maney.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a user's portfolio.
 * A portfolio contains all financial assets owned by a user.
 * Each user has exactly one portfolio.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Portfolio {

    /**
     * Unique identifier for the portfolio.
     * Uses the same ID as the associated user (shared primary key).
     */
    @Id
    private long id;

    /**
     * User who owns this portfolio.
     * One-to-one bidirectional relationship with User entity.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", unique = true, nullable = false, foreignKey = @ForeignKey(name = "fk_portfolio_user"))
    private User user;

    /**
     * List of illiquid assets in this portfolio.
     * One-to-many relationship: one portfolio can have many illiquid assets.
     * Cascade operations ensure that deleting a portfolio also deletes its assets.
     */
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IlliquidAsset> illiquidAssets = new ArrayList<>();
}
