package com.camicompany.PsyCare.model;

import com.camicompany.PsyCare.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="clinical_records")
public class ClinicalRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reasonConsult;
    private String diagnosis;
    private String obs;
    private String medication;

    @OneToOne(mappedBy = "clinicalRecord")
    private Patient patient;
    @OneToMany(mappedBy = "clinicalRecord")
    @OrderBy("sessionDate ASC")
    private List<Session> sessions = new ArrayList<>();
}
