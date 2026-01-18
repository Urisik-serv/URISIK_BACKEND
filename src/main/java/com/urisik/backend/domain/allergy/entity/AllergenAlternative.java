package com.urisik.backend.domain.allergy.entity;

import com.urisik.backend.domain.allergy.enums.Allergen;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "allergen_alternative")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AllergenAlternative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Allergen allergen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private AlternativeIngredient ingredient;

    public AllergenAlternative(Allergen allergen, AlternativeIngredient ingredient) {
        this.allergen = allergen;
        this.ingredient = ingredient;
    }

}
