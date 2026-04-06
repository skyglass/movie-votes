package net.skycomposer.moviebets.bet.dao.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.bet.dao.converter.ItemTypeConverter;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.market.MarketOpenStatus;

@Table(name = "market_open_status")
@Entity
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketOpenStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "market_id", nullable = false)
    private UUID marketId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MarketOpenStatus status;

    @Column(name = "item1_id", nullable = false)
    private String item1Id;

    @Column(name = "item2_id", nullable = false)
    private String item2Id;

    @Convert(converter = ItemTypeConverter.class)
    @Column(name = "item_type")
    private ItemType itemType;

}