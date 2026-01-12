package com.urisik.backend.domain;

import com.urisik.backend.domain.family.entity.FamilyRoom;
import com.urisik.backend.global.apiPayload.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "week_food_list")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeekFoodList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week", nullable = false)
    private java.time.LocalDate week;

    // 아침
    @Column(name = "monday_break")    private Long mondayBreak;
    @Column(name = "tuesday_break")   private Long tuesdayBreak;
    @Column(name = "wednesday_break") private Long wednesdayBreak;
    @Column(name = "thursday_break")  private Long thursdayBreak;
    @Column(name = "friday_break")    private Long fridayBreak;
    @Column(name = "saturday_break")  private Long saturdayBreak;
    @Column(name = "sunday_break")    private Long sundayBreak;

    // 저녁
    @Column(name = "monday_dinner")    private Long mondayDinner;
    @Column(name = "tuesday_dinner")   private Long tuesdayDinner;
    @Column(name = "wednesday_dinner") private Long wednesdayDinner;
    @Column(name = "thursday_dinner")  private Long thursdayDinner;
    @Column(name = "friday_dinner")    private Long fridayDinner;
    @Column(name = "saturday_dinner")  private Long saturdayDinner;
    @Column(name = "sunday_dinner")    private Long sundayDinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_room_id", nullable = false)
    private FamilyRoom familyRoom;


}
