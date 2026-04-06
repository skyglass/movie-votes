package net.skycomposer.moviebets.bet.dao.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.bet.dao.converter.ItemTypeConverter;
import net.skycomposer.moviebets.bet.dao.converter.MarketResultConverter;
import net.skycomposer.moviebets.common.dto.bet.BetStatus;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

@Table(name = "bet")
@Entity
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "market_id", nullable = false)
    private UUID marketId;

    @Column(name = "item1_id", nullable = false)
    private String item1Id;

    @Column(name = "item1_name", nullable = false)
    private String item1Name;

    @Column(name = "item2_id", nullable = false)
    private String item2Id;

    @Column(name = "item2_name", nullable = false)
    private String item2Name;

    @Convert(converter = ItemTypeConverter.class)
    @Column(name = "item_type")
    private ItemType itemType;

    @Column(name = "stake", nullable = false)
    private Integer stake;

    @Convert(converter = MarketResultConverter.class)
    @Column(name = "result")
    private MarketResult result;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BetStatus status;

    @Column(name = "bet_won", nullable = false)
    private Boolean betWon;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.betWon = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

}
