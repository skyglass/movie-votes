package net.skycomposer.moviebets.common.dto.market.events;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.item.ItemType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketOpenedEvent {
    private UUID marketId;
    private String item1Id;
    private String item2Id;
    private ItemType itemType;
}
