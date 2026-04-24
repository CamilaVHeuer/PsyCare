package com.camicompany.PsyCare.model;

import com.camicompany.PsyCare.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="sessions")
public class Session extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate sessionDate;
    private String evolutionNotes;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="clinical_record_id", nullable = false) //owner
    private ClinicalRecord clinicalRecord;
}
