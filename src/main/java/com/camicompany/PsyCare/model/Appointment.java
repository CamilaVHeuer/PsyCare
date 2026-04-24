package com.camicompany.PsyCare.model;

import com.camicompany.PsyCare.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="appointments")
public class Appointment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime appDateTime;
    private BigDecimal price;
    private String patientFirstName;
    private String patientLastName;
    private String patientPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentPaymentStatus appointmentPaymentStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentType type;


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name="patient_id") //owner
    private Patient patient;
    @OneToMany(mappedBy="appointment")
    List<Payment> payments = new ArrayList<>();
}
