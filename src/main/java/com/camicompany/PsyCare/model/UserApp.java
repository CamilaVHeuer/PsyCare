package com.camicompany.PsyCare.model;

import com.camicompany.PsyCare.common.BaseEntity;
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
@Table(name="users")
public class UserApp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(nullable = false)
    private boolean accountNotExpired = true;
    @Column(nullable = false)
    private boolean accountNotLocked = true;
    @Column(nullable = false)
    private boolean credentialsNotExpired = true;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name= "role_id", nullable = false)
    private Role role;
}
