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
@SQLDelete(sql = "UPDATE purchase_invoice SET deleted = true, deleted_at = now() WHERE uuid = ?")
@Where(clause = "deleted = false")
@Table(
    name = "purchase_invoice", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_purchase_invoice_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_purchase_invoice_uuid", columnList = "uuid"),
        @Index(name = "idx_purchase_invoice_purchase_order_id", columnList = "purchase_order_id")
    }
)
public class PurchaseInvoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "issue_date")
    private java.time.OffsetDateTime issueDate;

    @Column(name = "due_date")
    private java.time.OffsetDateTime dueDate;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private java.math.BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private java.math.BigDecimal paidAmount;

    @Column(name = "status", length = 32)
    private String status;


}
