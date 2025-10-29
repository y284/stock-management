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
    name = "invoice_client", schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_invoice_client_uuid", columnNames = {"uuid"})
    },
    indexes = {
        @Index(name = "idx_invoice_client_uuid", columnList = "uuid")
    }
)
public class InvoiceClient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_commande_id", nullable = false)
    private SaleCommande saleCommande;


}
