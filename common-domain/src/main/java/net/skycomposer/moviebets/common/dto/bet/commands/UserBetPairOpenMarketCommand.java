package net.skycomposer.moviebets.common.dto.bet.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.item.ItemType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBetPairOpenMarketCommand {
    private String user1Id;
    private String user2Id;
    private String item1Id;
    private String item1Name;
    private String item2Id;
    private String item2Name;
    private ItemType itemType;
}
