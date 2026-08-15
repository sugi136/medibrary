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
@Table(name = "drugs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Drug {
    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    private String shape;
    private String color;

    @Column(name = "mark_front")
    private String markFront;

    @Column(name = "mark_back")
    private String markBack;

    @Column(name = "ingredient_en")
    private String ingredientEn;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Lob
    private String efficacy;

    @Lob
    @Column(name = "usage_info")
    private String usageInfo;

    @Lob
    private String caution;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }

    public Drug(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
