package com.stock.stock_management.entity;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@SQLDelete(sql = "UPDATE purchase_order SET deleted = true, deleted_at = now() WHERE uuid = ?")
@Where(clause = "deleted = false")
@Table(
    name = "purchase_order", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_purchase_order_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_purchase_order_uuid", columnList = "uuid"),
        @Index(name = "idx_purchase_order_supplier_id", columnList = "supplier_id"),
        @Index(name = "idx_purchase_order_warehouse_id", columnList = "warehouse_id")
    }
)
public class PurchaseOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_date")
    private java.time.OffsetDateTime orderDate;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private java.math.BigDecimal totalAmount;

    @Column(name = "amount_paid", precision = 12, scale = 2)
    private java.math.BigDecimal amountPaid;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "is_quote")
    private Boolean isQuote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;


}
