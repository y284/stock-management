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
@SQLDelete(sql = "UPDATE warehouse SET deleted = true, deleted_at = now() WHERE uuid = ?")
@Where(clause = "deleted = false")
@Table(
    name = "warehouse", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_warehouse_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_warehouse_uuid", columnList = "uuid"),
        @Index(name = "idx_warehouse_code", columnList = "code"),
        @Index(name = "idx_warehouse_enterprise_id", columnList = "enterprise_id")
    }
)
public class Warehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private Enterprise enterprise;


}
