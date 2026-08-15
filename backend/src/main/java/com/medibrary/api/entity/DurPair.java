package com.medibrary.api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dur_pairs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DurPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drug_id_a")
    private Drug drugA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drug_id_b")
    private Drug drugB;

    private String reason;
    private String severity;

    public DurPair(Drug drugA, Drug drugB, String reason, String severity) {
        this.drugA = drugA;
        this.drugB = drugB;
        this.reason = reason;
        this.severity = severity;
    }
}
