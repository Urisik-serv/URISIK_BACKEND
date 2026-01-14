package com.urisik.backend.domain.familyroom.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FamilyRoom {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_name", length = 50, nullable = false, unique = true)
    private String familyName;

    private FamilyRoom(String familyName) {
        this.familyName = familyName;
    }

    public static FamilyRoom create(String familyName) {
        return new FamilyRoom(familyName);
    }
}
