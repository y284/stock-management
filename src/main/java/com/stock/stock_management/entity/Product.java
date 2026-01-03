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
@SQLDelete(sql = "UPDATE product SET deleted = true, deleted_at = now() WHERE uuid = ?")
@Where(clause = "deleted = false")
@Table(
    name = "product", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_product_uuid", columnList = "uuid"),
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category_id", columnList = "category_id")
    }
)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "description", nullable = false, unique = true, length = 255)
    private String description;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "price", precision = 12, scale = 2)
    private java.math.BigDecimal price;

    @Column(name = "tva", precision = 5, scale = 2)
    private java.math.BigDecimal tva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @Column(name = "unit_of_measure", length = 32)
    private String unitOfMeasure;

    @Column(name = "is_active")
    private Boolean isActive;


}
