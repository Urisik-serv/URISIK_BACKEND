package com.urisik.backend.domain.allergy.init;

import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.entity.AlternativeIngredient;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.AllergenAlternativeRepository;
import com.urisik.backend.domain.allergy.repository.AlternativeIngredientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AllergyDataInitializer implements CommandLineRunner {

    private final AlternativeIngredientRepository ingredientRepository;
    private final AllergenAlternativeRepository allergenAlternativeRepository;

    @Override
    @Transactional
    public void run(String... args) {

        // =========================
        // 대체 식재료 등록
        // =========================
        AlternativeIngredient tofu = saveIngredient("두부");
        AlternativeIngredient chickpea = saveIngredient("병아리콩");
        AlternativeIngredient flaxEgg = saveIngredient("아마씨 대체제");

        AlternativeIngredient soyMilk = saveIngredient("두유");
        AlternativeIngredient oatMilk = saveIngredient("오트밀크");
        AlternativeIngredient coconutMilk = saveIngredient("코코넛밀크");

        AlternativeIngredient riceNoodle = saveIngredient("쌀국수");
        AlternativeIngredient cornNoodle = saveIngredient("옥수수면");

        AlternativeIngredient sunflowerButter = saveIngredient("해바라기씨버터");
        AlternativeIngredient lentil = saveIngredient("렌틸콩");

        AlternativeIngredient riceFlour = saveIngredient("쌀가루");
        AlternativeIngredient tapioca = saveIngredient("타피오카");

        AlternativeIngredient chicken = saveIngredient("닭고기");
        AlternativeIngredient beef = saveIngredient("소고기");

        AlternativeIngredient mushroom = saveIngredient("버섯");
        AlternativeIngredient soyProtein = saveIngredient("콩단백");

        AlternativeIngredient shiitake = saveIngredient("표고버섯");
        AlternativeIngredient konjac = saveIngredient("곤약");

        AlternativeIngredient apple = saveIngredient("사과");
        AlternativeIngredient pear = saveIngredient("배");

        AlternativeIngredient paprika = saveIngredient("파프리카");
        AlternativeIngredient beet = saveIngredient("비트");

        AlternativeIngredient freshFruit = saveIngredient("생과일");
        AlternativeIngredient additiveFreeDriedFruit = saveIngredient("무첨가 건과일");

        AlternativeIngredient sunflowerSeed = saveIngredient("해바라기씨");
        AlternativeIngredient sesame = saveIngredient("참깨");

        AlternativeIngredient plantMeat = saveIngredient("대체육");

        AlternativeIngredient kelp = saveIngredient("다시마");
        AlternativeIngredient vegetable = saveIngredient("채소");

        AlternativeIngredient agar = saveIngredient("한천");
        AlternativeIngredient pectin = saveIngredient("펙틴");

        // =========================
        // 2️알레르기 ↔ 대체 식재료 매핑
        // =========================
        map(Allergen.EGG, tofu, chickpea, flaxEgg);
        map(Allergen.MILK, soyMilk, oatMilk, coconutMilk);
        map(Allergen.BUCKWHEAT, riceNoodle, cornNoodle);
        map(Allergen.PEANUT, sunflowerButter);
        map(Allergen.SOY, chickpea, lentil);
        map(Allergen.WHEAT, riceFlour, tapioca);
        map(Allergen.MACKEREL, chicken, tofu);
        map(Allergen.CRAB, mushroom, soyProtein);
        map(Allergen.SHRIMP, shiitake, konjac);
        map(Allergen.PORK, beef, chicken);
        map(Allergen.PEACH, apple, pear);
        map(Allergen.TOMATO, paprika, beet);
        map(Allergen.SULFITES, freshFruit, additiveFreeDriedFruit);
        map(Allergen.WALNUT, sunflowerSeed);
        map(Allergen.CHICKEN, beef, soyProtein);
        map(Allergen.BEEF, chicken, plantMeat);
        map(Allergen.SQUID, mushroom, tofu);
        map(Allergen.OYSTER, shiitake, kelp);
        map(Allergen.ABALONE, mushroom, soyProtein);
        map(Allergen.MUSSEL, vegetable, mushroom);
        map(Allergen.PINE_NUT, sesame, sunflowerSeed);
        map(Allergen.EXTRACTED_INGREDIENTS, agar, pectin);
    }

    // =========================
    // 공통 메서드
    // =========================
    private AlternativeIngredient saveIngredient(String name) {
        return ingredientRepository.findByName(name)
                .orElseGet(() -> ingredientRepository.save(new AlternativeIngredient(name)));
    }

    private void map(Allergen allergen, AlternativeIngredient... ingredients) {
        for (AlternativeIngredient ingredient : ingredients) {
            if (!allergenAlternativeRepository.existsByAllergenAndIngredient(allergen, ingredient)) {
                allergenAlternativeRepository.save(
                        new AllergenAlternative(allergen, ingredient)
                );
            }
        }
    }
}

