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
    name = "sale_commande_line", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sale_commande_line_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_sale_commande_line_uuid", columnList = "uuid")
    }
)
public class SaleCommandeLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_commande_id", nullable = false)
    private SaleCommande saleCommande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal price;

    @Column(name = "discount", nullable = false, precision = 2, scale = 2)
    private java.math.BigDecimal discount;


}
