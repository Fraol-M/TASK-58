package com.campusfit.inbound.dto;

import com.campusfit.inbound.entity.InboundReceipt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundReceiptRequest {

    @NotNull(message = "Receipt type is required")
    private InboundReceipt.ReceiptType receiptType;

    private String referenceNumber;

    private String supplierName;

    private LocalDate expectedDate;
}
