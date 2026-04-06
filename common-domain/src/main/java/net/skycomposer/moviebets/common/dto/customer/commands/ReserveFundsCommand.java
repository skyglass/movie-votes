package net.skycomposer.moviebets.common.dto.customer.commands;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveFundsCommand {
    private UUID betId;
    private String customerId;
    private UUID marketId;
    private UUID requestId;
    private UUID cancelRequestId;
    private BigDecimal funds;
}
