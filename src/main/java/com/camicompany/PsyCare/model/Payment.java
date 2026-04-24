package com.camicompany.PsyCare.model;

import com.camicompany.PsyCare.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="payments")
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate payDate;
    //amount can be total o partial. It is used to calculate paymentStatus in appointment
    @Column(nullable = false)
    private BigDecimal amount;
    private String payMethod;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="appointment_id") //owner
    private Appointment appointment;

}
