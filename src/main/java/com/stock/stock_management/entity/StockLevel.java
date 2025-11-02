package com.stock.stock_management.entity;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "stock_level", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_stock_level_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_stock_level_uuid", columnList = "uuid")
    }
)
public class StockLevel extends BaseEntity {

    @EmbeddedId
    private StockLevelId id;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @MapsId("warehouseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "current_qty", precision = 14, scale = 3)
    private java.math.BigDecimal currentQty;

    @Column(name = "reserved_qty", precision = 14, scale = 3)
    private java.math.BigDecimal reservedQty;

    @Column(name = "stock_alert_qty", precision = 14, scale = 3)
    private java.math.BigDecimal stockAlertQty;


}
