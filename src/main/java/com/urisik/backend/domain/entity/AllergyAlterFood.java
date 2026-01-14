package com.urisik.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "allergy_alter_food")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AllergyAlterFood {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alter_food", nullable = false)
    private String alterFood;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allergy_id", nullable = false)
    private Allergy allergy;

}