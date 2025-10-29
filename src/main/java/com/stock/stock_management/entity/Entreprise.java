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
    name = "entreprise", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_entreprise_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_entreprise_uuid", columnList = "uuid")
    }
)
public class Entreprise extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 64)
    private String name;

    @Column(name = "location", length = 128)
    private String location;


}
