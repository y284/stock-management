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
@SQLDelete(sql = "UPDATE user SET deleted = true, deleted_at = now() WHERE uuid = ?")
@Where(clause = "deleted = false")
@Table(
    name = "user", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_user_uuid", columnList = "uuid"),
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_warehouse_id", columnList = "warehouse_id")
    }
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 128)
    private String username;

    @Column(name = "firstname", nullable = false, length = 128)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 128)
    private String lastname;

    @Column(name = "rib", nullable = true, unique = true, length = 34)
    private String rib;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 255)
    private String keycloakId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "is_active")
    private Boolean isActive;


}
