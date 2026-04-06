package net.skycomposer.moviebets.common.dto.votes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.item.ItemType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItemVotes {
    private String userId;
    private String itemId;
    private ItemType itemType;
    private boolean canVote;
    private Long votes;
}
