package net.skycomposer.moviebets.common.dto.bet.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.item.UserItemStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItemStatusRequest {
    private String userId;
    private String itemId;
    private ItemType itemType;
    private String itemName;
    private UserItemStatus userItemStatus;
}
