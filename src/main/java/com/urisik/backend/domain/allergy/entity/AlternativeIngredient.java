package com.urisik.backend.domain.allergy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alternative_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlternativeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public AlternativeIngredient(String name) {
        this.name = name;
    }

}
