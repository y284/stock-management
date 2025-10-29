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
    name = "supplier", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_supplier_uuid", columnList = "uuid")
    }
)
public class Supplier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fullname", nullable = false, length = 255)
    private String fullname;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "rib", nullable = false, unique = true, length = 255)
    private String rib;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;


}
