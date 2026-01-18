package com.urisik.backend.domain.allergy.entity;

import com.urisik.backend.domain.allergy.enums.Allergen;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_allergy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Allergen allergen;

    public MemberAllergy(Long memberId, Allergen allergen) {
        this.memberId = memberId;
        this.allergen = allergen;
    }

}
