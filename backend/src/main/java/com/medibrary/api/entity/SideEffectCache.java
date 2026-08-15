package com.medibrary.api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "side_effect_cache", indexes = @Index(name = "idx_drug_source", columnList = "drug_id,source"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SideEffectCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drug_id")
    private Drug drug;

    @Column(nullable = false)
    private String source;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    @PrePersist
    void prePersist() {
        fetchedAt = LocalDateTime.now();
    }

    public SideEffectCache(Drug drug, String source, String content) {
        this.drug = drug;
        this.source = source;
        this.content = content;
    }
}
