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
    name = "product", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_product_uuid", columnList = "uuid"),
        @Index(name = "idx_product_sku", columnList = "sku")
    }
)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku", nullable = false, unique = true, length = 64)
    private String sku;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "price", precision = 12, scale = 2)
    private java.math.BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @Column(name = "tva")
    private Long tva;


}
