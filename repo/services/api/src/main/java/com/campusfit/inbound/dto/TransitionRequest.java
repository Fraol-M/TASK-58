package com.campusfit.inbound.dto;

import com.campusfit.inbound.entity.InboundReceipt;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionRequest {

    @NotNull(message = "Target state is required")
    private InboundReceipt.ReceiptStatus targetState;

    private String reason;
}
