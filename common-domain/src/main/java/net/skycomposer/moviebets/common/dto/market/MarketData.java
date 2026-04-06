package net.skycomposer.moviebets.common.dto.market;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.item.ItemType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    private UUID marketId;
    private String item1Id;
    private String item1Name;
    private String item2Id;
    private String item2Name;
    private ItemType itemType;
    private MarketStatus status;
    private MarketResult result;
    private Instant closesAt;
    private boolean open;

}