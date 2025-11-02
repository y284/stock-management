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
    name = "client", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_client_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_client_uuid", columnList = "uuid"),
        @Index(name = "idx_client_email", columnList = "email"),
        @Index(name = "idx_client_warehouse_id", columnList = "warehouse_id")
    }
)
public class Client extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fullname", nullable = false, length = 255)
    private String fullname;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "iban", nullable = false, unique = true, length = 34)
    private String iban;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "is_active")
    private Boolean isActive;


}
