package com.shubhasamagri.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a Hindu religious occasion (e.g., Marriage, Gruha Pravesh).
 * Each occasion has one or more PoojaKits curated by poojaris.
 */
@Entity
@Table(name = "occasions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Occasion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // One occasion can have multiple pooja kits
    @OneToMany(mappedBy = "occasion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PoojaKit> poojaKits;
}
