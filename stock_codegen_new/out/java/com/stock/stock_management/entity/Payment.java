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
@SQLDelete(sql = "UPDATE payment SET deleted = true, deleted_at = now() WHERE uuid = ?")
@Where(clause = "deleted = false")
@Table(
    name = "payment", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_payment_uuid", columnList = "uuid"),
        @Index(name = "idx_payment_sales_order_id", columnList = "sales_order_id")
    }
)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal amount;

    @Column(name = "payment_method", nullable = false, length = 32)
    private String paymentMethod;

    @Column(name = "payment_type", nullable = false, length = 16)
    private String paymentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = true)
    private SalesOrder salesOrder;


}
