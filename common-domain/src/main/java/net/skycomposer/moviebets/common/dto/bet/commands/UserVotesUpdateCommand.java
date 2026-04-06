package net.skycomposer.moviebets.common.dto.bet.commands;

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
public class UserVotesUpdateCommand {
    private UUID marketId;

    private String itemWon;

    private String itemLost;

    private ItemType itemType;

    private boolean item1Won;
}
