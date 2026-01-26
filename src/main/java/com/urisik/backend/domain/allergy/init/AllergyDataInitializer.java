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
        // 알레르기 ↔ 대체 식재료 매핑 (reason 포함)
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
                        new AllergenAlternative(
                                allergen,
                                ingredient,
                                reasonOf(allergen, ingredient)
                        )
                );
            }
        }
    }

    // =========================
    // 대체 이유 매핑
    // =========================
    private String reasonOf(Allergen allergen, AlternativeIngredient ingredient) {
        return switch (allergen) {

            case EGG -> switch (ingredient.getName()) {
                case "두부" -> "알류를 사용하지 않으면서도 부드러운 조직감으로 조리 시 결합 역할을 일부 대체할 수 있습니다.";
                case "병아리콩" -> "알류 성분 없이도 고소한 풍미와 단백질감을 더할 수 있는 대체 식재료입니다.";
                case "아마씨 대체제" -> "알류 대신 점성을 형성하여 반죽이나 조리 과정에서 결합 역할을 보완할 수 있습니다.";
                default -> "알류 알레르기 대체 식재료입니다.";
            };

            case MILK -> switch (ingredient.getName()) {
                case "두유" -> "유제품은 아니지만 액상 질감과 고소함을 유지하여 조리 시 우유의 역할을 대체할 수 있습니다.";
                case "오트밀크" -> "풍미는 다르지만 부드러운 질감과 지방감을 유지하여 음료나 조리에 활용할 수 있습니다.";
                case "코코넛밀크" -> "우유와는 다른 향을 가지지만 높은 지방감으로 조리 시 크리미한 식감을 유지할 수 있습니다.";
                default -> "우유 알레르기 대체 식재료입니다.";
            };

            case BUCKWHEAT -> switch (ingredient.getName()) {
                case "쌀국수" -> "메밀을 사용하지 않으면서도 면 요리에 적합한 탄력과 식감을 제공합니다.";
                case "옥수수면" -> "곡물 기반 면으로 메밀 없이도 면 요리의 구조를 유지할 수 있습니다.";
                default -> "메밀 알레르기 대체 식재료입니다.";
            };

            case PEANUT -> switch (ingredient.getName()) {
                case "해바라기씨버터" -> "땅콩은 아니지만 고소한 풍미와 부드러운 질감으로 소스나 페이스트 역할을 대체할 수 있습니다.";
                default -> "땅콩 알레르기 대체 식재료입니다.";
            };

            case SOY -> switch (ingredient.getName()) {
                case "병아리콩" -> "대두를 사용하지 않으면서도 단백질과 포만감을 보완할 수 있는 식재료입니다.";
                case "렌틸콩" -> "대두 성분 없이도 단백질 기반 요리에 활용할 수 있는 대체 식재료입니다.";
                default -> "대두 알레르기 대체 식재료입니다.";
            };

            case WHEAT -> switch (ingredient.getName()) {
                case "쌀가루" -> "밀가루와는 다른 특성이지만 반죽과 조리에 필요한 기본적인 구조를 형성할 수 있습니다.";
                case "타피오카" -> "글루텐 없이도 점성과 탄성을 부여하여 조리 시 질감을 보완할 수 있습니다.";
                default -> "밀 알레르기 대체 식재료입니다.";
            };

            case MACKEREL -> switch (ingredient.getName()) {
                case "닭고기" -> "고등어를 사용하지 않고도 단백질 요리의 중심 재료로 활용할 수 있습니다.";
                case "두부" -> "어류 성분 없이도 부드러운 식감으로 단백질 요리에 활용할 수 있습니다.";
                default -> "고등어 알레르기 대체 식재료입니다.";
            };

            case CRAB -> switch (ingredient.getName()) {
                case "버섯" -> "갑각류를 사용하지 않으면서도 씹는 식감과 감칠맛을 더할 수 있습니다.";
                case "콩단백" -> "게 대신 단백질 기반 식재료로 조리 시 질감을 보완할 수 있습니다.";
                default -> "게 알레르기 대체 식재료입니다.";
            };

            case SHRIMP -> switch (ingredient.getName()) {
                case "표고버섯" -> "해산물 풍미는 다르지만 쫄깃한 식감으로 요리의 식감을 유지할 수 있습니다.";
                case "곤약" -> "새우의 탄력감은 없지만 조리 시 씹는 질감을 대체할 수 있습니다.";
                default -> "새우 알레르기 대체 식재료입니다.";
            };

            case PORK -> switch (ingredient.getName()) {
                case "소고기" -> "돼지고기 대신 단백질과 육류 식감을 유지하며 요리에 활용할 수 있습니다.";
                case "닭고기" -> "지방감은 다르지만 담백한 육류 대체 재료로 사용할 수 있습니다.";
                default -> "돼지고기 알레르기 대체 식재료입니다.";
            };

            case PEACH -> switch (ingredient.getName()) {
                case "사과" -> "복숭아와는 다른 향이지만 산미와 단맛의 균형으로 과일 역할을 대체할 수 있습니다.";
                case "배" -> "부드러운 식감과 수분감을 유지하여 디저트나 샐러드에 활용할 수 있습니다.";
                default -> "복숭아 알레르기 대체 식재료입니다.";
            };

            case TOMATO -> switch (ingredient.getName()) {
                case "파프리카" -> "토마토의 산미는 없지만 색감과 아삭한 식감으로 요리에 활용할 수 있습니다.";
                case "비트" -> "산미는 다르지만 색감과 자연스러운 단맛으로 요리의 시각적 요소를 보완할 수 있습니다.";
                default -> "토마토 알레르기 대체 식재료입니다.";
            };

            case SULFITES -> switch (ingredient.getName()) {
                case "생과일" -> "아황산류 첨가물 없이도 자연적인 풍미를 유지할 수 있습니다.";
                case "무첨가 건과일" -> "보존료 없이 과일의 단맛과 식감을 활용할 수 있습니다.";
                default -> "아황산류 알레르기 대체 식재료입니다.";
            };

            case WALNUT -> switch (ingredient.getName()) {
                case "해바라기씨" -> "견과류는 아니지만 고소한 풍미와 씹는 식감으로 토핑 역할을 대체할 수 있습니다.";
                default -> "호두 알레르기 대체 식재료입니다.";
            };

            case CHICKEN -> switch (ingredient.getName()) {
                case "소고기" -> "닭고기 대신 육류 요리의 중심 재료로 활용할 수 있습니다.";
                case "콩단백" -> "육류는 아니지만 단백질 기반 식재료로 조리 구조를 유지할 수 있습니다.";
                default -> "닭고기 알레르기 대체 식재료입니다.";
            };

            case BEEF -> switch (ingredient.getName()) {
                case "닭고기" -> "풍미는 다르지만 단백질 식재료로 요리에 활용할 수 있습니다.";
                case "대체육" -> "쇠고기 성분 없이도 육류와 유사한 식감으로 조리에 사용할 수 있습니다.";
                default -> "쇠고기 알레르기 대체 식재료입니다.";
            };

            case SQUID -> switch (ingredient.getName()) {
                case "버섯" -> "해산물 특유의 풍미는 없지만 씹는 식감으로 요리를 구성할 수 있습니다.";
                case "두부" -> "부드러운 조직감으로 단백질 요리에 활용할 수 있습니다.";
                default -> "오징어 알레르기 대체 식재료입니다.";
            };

            case OYSTER -> switch (ingredient.getName()) {
                case "표고버섯" -> "굴 특유의 맛은 없지만 감칠맛을 보완할 수 있는 대체 식재료입니다.";
                case "다시마" -> "해조류 기반 재료로 국물이나 요리에 감칠맛을 더할 수 있습니다.";
                default -> "굴 알레르기 대체 식재료입니다.";
            };

            case ABALONE -> switch (ingredient.getName()) {
                case "버섯" -> "전복의 질감은 다르지만 씹는 식감과 감칠맛을 보완할 수 있습니다.";
                case "콩단백" -> "단백질 기반 재료로 조리 시 중심 재료 역할을 할 수 있습니다.";
                default -> "전복 알레르기 대체 식재료입니다.";
            };

            case MUSSEL -> switch (ingredient.getName()) {
                case "채소" -> "해산물은 아니지만 요리의 부피와 식감을 보완할 수 있습니다.";
                case "버섯" -> "쫄깃한 식감으로 요리의 구조를 유지할 수 있습니다.";
                default -> "홍합 알레르기 대체 식재료입니다.";
            };

            case PINE_NUT -> switch (ingredient.getName()) {
                case "참깨" -> "잣과는 다른 풍미지만 고소한 맛으로 토핑이나 소스에 활용할 수 있습니다.";
                case "해바라기씨" -> "견과류는 아니지만 씹는 식감과 고소함을 제공합니다.";
                default -> "잣 알레르기 대체 식재료입니다.";
            };

            case EXTRACTED_INGREDIENTS -> switch (ingredient.getName()) {
                case "한천" -> "동물성 성분 없이도 응고 특성을 제공하여 디저트 조리에 활용할 수 있습니다.";
                case "펙틴" -> "식물성 성분으로 점성과 응고감을 형성하여 조리에 사용할 수 있습니다.";
                default -> "추출성분 알레르기 대체 식재료입니다.";
            };
        };
    }
}


