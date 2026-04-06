package net.skycomposer.moviebets.market.dao.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.market.MarketResult;
import net.skycomposer.moviebets.common.dto.market.MarketStatus;
import net.skycomposer.moviebets.market.dao.converter.ItemTypeConverter;
import net.skycomposer.moviebets.market.dao.converter.MarketResultConverter;

@Table(name = "market")
@Entity
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MarketStatus status;

    @Column(name = "open", nullable = false)
    private Boolean open;

    @Convert(converter = MarketResultConverter.class)
    @Column(name = "result")
    private MarketResult result;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closes_at", nullable = false)
    private Instant closesAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.open = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

}
