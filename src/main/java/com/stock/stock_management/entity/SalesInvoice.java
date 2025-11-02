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
    name = "sales_invoice", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sales_invoice_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_sales_invoice_uuid", columnList = "uuid"),
        @Index(name = "idx_sales_invoice_sales_order_id", columnList = "sales_order_id")
    }
)
public class SalesInvoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

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
