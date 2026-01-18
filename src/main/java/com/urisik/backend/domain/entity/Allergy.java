package com.urisik.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "allergy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Allergy {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;



    @OneToMany(mappedBy = "allergy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AllergyAlterFood> alterFoods = new ArrayList<>();

    /*
    @OneToMany(mappedBy = "allergy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberAllergy> memberAllergyList  = new ArrayList<>();
     */
}
