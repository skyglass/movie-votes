package net.skycomposer.moviebets.common.dto.bet;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetData {
    private UUID betId;
    private UUID marketId;
    private String customerId;
    private UUID requestId;
    private UUID cancelRequestId;
    private String item1Id;
    private String item2Id;
    private String item1Name;
    private String item2Name;
    private ItemType itemType;
    private Integer stake;
    private MarketResult result;
    private BetStatus status;
    private Boolean betWon;

}
