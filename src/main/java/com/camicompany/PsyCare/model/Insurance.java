package com.camicompany.PsyCare.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="insurances")
public class Insurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    String name;
    @Column(unique = true)
    String cuit;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (this.cuit != null) {
            this.cuit = this.cuit.replaceAll("[^\\d]", "");
        }
    }
}
