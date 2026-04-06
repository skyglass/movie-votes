package net.skycomposer.moviebets.common.dto.bet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.item.ItemType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceBetData {
    private UUID marketId;
    private String item1Id;
    private String item2Id;
    private String item1Name;
    private String item2Name;
    private ItemType itemType;
}
