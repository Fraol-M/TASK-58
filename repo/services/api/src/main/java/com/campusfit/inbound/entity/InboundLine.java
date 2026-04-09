package com.campusfit.inbound.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_inbound_line")
public class InboundLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_id", nullable = false)
    private Long receiptId;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(name = "expected_qty", nullable = false, precision = 12, scale = 2)
    private BigDecimal expectedQty;

    @Column(name = "received_qty", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal receivedQty = BigDecimal.ZERO;

    @Column(name = "inspected_qty", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal inspectedQty = BigDecimal.ZERO;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_result", nullable = false, length = 20)
    @Builder.Default
    private InspectionResult inspectionResult = InspectionResult.PENDING;

    @Column(name = "inspection_notes", columnDefinition = "TEXT")
    private String inspectionNotes;

    public enum InspectionResult {
        PENDING, PASS, FAIL
    }
}
