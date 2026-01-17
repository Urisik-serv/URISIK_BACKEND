package com.urisik.backend.domain.familyroom.entity;

import com.urisik.backend.domain.familyroom.enums.FamilyPolicy;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "family_room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_family_room_family_name", columnNames = "family_name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FamilyRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_name", length = 50, nullable = false)
    private String familyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "family_policy", length = 20, nullable = false)
    private FamilyPolicy familyPolicy;

    private FamilyRoom(String familyName, FamilyPolicy familyPolicy) {
        this.familyName = familyName;
        this.familyPolicy = familyPolicy;
    }

    /**
     * 가족방 생성 (필수값 지정)
     */
    public static FamilyRoom create(String familyName, FamilyPolicy familyPolicy) {
        return new FamilyRoom(familyName, familyPolicy);
    }
}
