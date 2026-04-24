package com.camicompany.PsyCare.model;

import com.camicompany.PsyCare.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="patients")
public class Patient extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String firstname;
    @Column(nullable = false)
    private String lastname;

    @Column(unique = true, nullable = false)
    private String nationalId;
    private LocalDate birthDate;
    private String phone;
    private String insuranceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatientStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tutor_id") //owner
    private Tutor tutor;

    @ManyToOne(optional = true, fetch = FetchType.LAZY) //owner
    @JoinColumn(name="insurance_id")
    private Insurance insurance;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) //owner
    @JoinColumn(name="clinical_record_id", nullable = true)
    private ClinicalRecord clinicalRecord;

    // Derived properties
    @Transient
    public Integer getAge() {
        if (birthDate == null) return null;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
