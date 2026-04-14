package com.mes.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "units")
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean mainUnit = false;

    @Column(nullable = false)
    private Boolean enabled = true;

    private String description;
}
