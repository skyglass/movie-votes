package net.skycomposer.moviebets.common.dto.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItemVoteStatus {

    private String itemId;
    private boolean alreadyVoted;
}
