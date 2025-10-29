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
    name = "supplier_commande_line", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_commande_line_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_supplier_commande_line_uuid", columnList = "uuid")
    }
)
public class SupplierCommandeLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_commande_id", nullable = false)
    private SupplierCommande supplierCommande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 3)
    private java.math.BigDecimal quantity;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal price;

    @Column(name = "discount", nullable = false, precision = 2, scale = 2)
    private java.math.BigDecimal discount;


}
