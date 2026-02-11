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
@SQLDelete(sql = "UPDATE sales_order_line SET deleted = true, deleted_at = now() WHERE uuid = ?")
@Where(clause = "deleted = false")
@Table(
    name = "sales_order_line", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sales_order_line_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_sales_order_line_uuid", columnList = "uuid"),
        @Index(name = "idx_sales_order_line_sales_order_id", columnList = "sales_order_id"),
        @Index(name = "idx_sales_order_line_product_id", columnList = "product_id")
    }
)
public class SalesOrderLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 3)
    private java.math.BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal unitPrice;

    @Column(name = "discount", nullable = false, precision = 5, scale = 4)
    private java.math.BigDecimal discount;


}
