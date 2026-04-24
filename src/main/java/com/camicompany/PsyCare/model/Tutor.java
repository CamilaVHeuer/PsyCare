package com.camicompany.PsyCare.model;

import com.camicompany.PsyCare.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="tutors")
public class Tutor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstname;
    private String lastname;
    private String phone;
    @Column(unique = true)
    private String cuil;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TutorRelation relation;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (this.cuil != null) {
            this.cuil = this.cuil.replaceAll("[^\\d]", "");
        }
    }
}
