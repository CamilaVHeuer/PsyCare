package com.camicompany.PsyCare.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="roles")

public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String roleName;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable( name= "role_permissions",
            joinColumns = @JoinColumn(name= "role_id"),
            inverseJoinColumns = @JoinColumn(name= "permission_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "permission_id"})}
    )
    private Set<Permission> permissionsList = new HashSet<>();

}
