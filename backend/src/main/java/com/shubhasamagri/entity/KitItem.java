package com.shubhasamagri.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Junction entity representing a PoojaItem within a PoojaKit with its quantity.
 */
@Entity
@Table(name = "kit_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pooja_kit_id", nullable = false)
    private PoojaKit poojaKit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pooja_item_id", nullable = false)
    private PoojaItem poojaItem;

    @Column(nullable = false)
    private Integer quantity;

    /** Override unit for this kit context (e.g., "2 packets" instead of item's default) */
    private String unit;
}
