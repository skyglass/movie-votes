package net.skycomposer.moviebets.common.dto.votes;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItemVotesList {
    private List<UserItemVotes> result;
}

