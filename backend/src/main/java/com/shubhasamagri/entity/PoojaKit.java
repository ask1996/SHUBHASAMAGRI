package com.shubhasamagri.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a curated Pooja Kit for a specific occasion.
 * A kit contains multiple PoojaItems bundled together as recommended by temple poojaris.
 */
@Entity
@Table(name = "pooja_kits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoojaKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** The occasion this kit is curated for */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occasion_id", nullable = false)
    private Occasion occasion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "estimated_delivery_days")
    @Builder.Default
    private Integer estimatedDeliveryDays = 3;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Items included in this kit */
    @OneToMany(mappedBy = "poojaKit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<KitItem> kitItems;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
