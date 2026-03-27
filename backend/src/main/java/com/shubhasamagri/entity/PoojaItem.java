package com.shubhasamagri.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents an individual pooja item (e.g., Agarbatti, Coconut, Kumkum).
 * These items are grouped into PoojaKits.
 */
@Entity
@Table(name = "pooja_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoojaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Unit of measurement: pcs, grams, ml, packets, etc. */
    @Column(nullable = false)
    private String unit;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
