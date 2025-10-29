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
    name = "warehouse", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_warehouse_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_warehouse_uuid", columnList = "uuid"),
        @Index(name = "idx_warehouse_code", columnList = "code")
    }
)
public class Warehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;


}
