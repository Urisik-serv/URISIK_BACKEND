package com.urisik.backend.domain.familyroom.entity;

import java.util.ArrayList;
import java.util.List;

import com.urisik.backend.domain.familyroom.enums.FamilyPolicy;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
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
    @Column(name = "family_policy", nullable = false)
    private FamilyPolicy familyPolicy;

    // 연관관계 매핑
    @OneToMany(mappedBy = "familyRoom", fetch = FetchType.LAZY)
    private final List<FamilyMember> familyMembers = new ArrayList<>();

    @OneToMany(mappedBy = "familyRoom", fetch = FetchType.LAZY)
    private final List<FamilyWishList> familyWishLists = new ArrayList<>();

    @OneToMany(mappedBy = "familyRoom", fetch = FetchType.LAZY)
    private final List<Invite> invites = new ArrayList<>();

    @OneToMany(mappedBy = "familyRoom", fetch = FetchType.LAZY)
    private final List<MealPlan> mealPlans = new ArrayList<>();

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
