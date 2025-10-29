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
    name = "category", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_category_uuid", columnList = "uuid"),
        @Index(name = "idx_category_name", columnList = "name")
    }
)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 64)
    private String name;

    @Column(name = "details", length = 255)
    private String details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    private Category parent;


}
