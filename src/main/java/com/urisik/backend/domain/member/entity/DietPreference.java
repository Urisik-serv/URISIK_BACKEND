package com.urisik.backend.domain.member.entity;

import com.urisik.backend.domain.member.enums.DietPreferenceList;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diet_preference")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DietPreference {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DietPreferenceList dietPreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_profile_id", nullable = false)
    private FamilyMemberProfile familyMemberProfile;


    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_category_id", nullable = false)
    private FoodCategory foodCategory;
     */


    public static DietPreference of(DietPreferenceList dietPreference) {
        DietPreference d = new DietPreference();
        d.dietPreference = dietPreference;
        return d;
    }

    public void setFamilyMemberProfile(FamilyMemberProfile profile) {
        this.familyMemberProfile = profile;
    }
}
