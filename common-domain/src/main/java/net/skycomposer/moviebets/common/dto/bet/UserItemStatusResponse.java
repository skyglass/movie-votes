package net.skycomposer.moviebets.common.dto.bet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItemStatusResponse {
    private String userId;
    private String itemId;
    private String message;
}
