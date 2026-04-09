package com.campusfit.inbound.dto;

import com.campusfit.inbound.entity.InboundLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundLineResponse {

    private Long id;
    private Long receiptId;
    private String itemCode;
    private String itemName;
    private BigDecimal expectedQty;
    private BigDecimal receivedQty;
    private BigDecimal inspectedQty;
    private BigDecimal unitCost;
    private InboundLine.InspectionResult inspectionResult;
    private String inspectionNotes;
}
