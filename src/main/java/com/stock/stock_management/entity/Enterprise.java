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
    name = "enterprise", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_enterprise_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_enterprise_uuid", columnList = "uuid"),
        @Index(name = "idx_enterprise_name", columnList = "name")
    }
)
public class Enterprise extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "location", length = 255)
    private String location;


}
