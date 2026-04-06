package net.skycomposer.moviebets.common.dto.item;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItemVoteStatusList {
    private List<UserItemVoteStatus> result;
}

