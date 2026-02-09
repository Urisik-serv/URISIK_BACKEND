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
            case NONE -> switch (ingredient.getName()) {
                default -> "일반 음식입니다";};

            case EGG -> switch (ingredient.getName()) {
                case "두부" ->
                        "달걀을 두부로 대체하여 부드러운 조직감으로 조리 시 결합 역할을 일부 대체했어요.";
                case "병아리콩" ->
                        "달걀을 병아리콩으로 대체하여 고소한 풍미와 단백질감을 더했어요.";
                case "아마씨 대체제" ->
                        "달걀을 아마씨 대체제로 대체해 점성을 형성하여 반죽이나 조리 과정에서 결합 역할을 보완했어요.";
                default ->
                        "알류 알레르기 대체 식재료입니다.";
            };

            case MILK -> switch (ingredient.getName()) {
                case "두유" ->
                        "우유를 두유로 대체하여 액상 질감과 고소한 풍미를 유지했어요.";
                case "오트밀크" ->
                        "우유를 오트밀크로 대체하여 부드러운 질감과 은은한 곡물 풍미를 더했어요.";
                case "코코넛밀크" ->
                        "우유를 코코넛밀크로 대체하여 높은 지방감으로 크리미한 식감을 유지했어요.";
                default ->
                        "우유 알레르기 대체 식재료입니다.";
            };

            case BUCKWHEAT -> switch (ingredient.getName()) {
                case "쌀국수" ->
                        "메밀을 쌀국수로 대체하여 메밀 없이도 면 요리에 적합한 식감과 탄력을 유지했어요.";
                case "옥수수면" ->
                        "메밀을 옥수수면으로 대체하여 곡물 기반 면 요리의 구조를 유지했어요.";
                default ->
                        "메밀 알레르기 대체 식재료입니다.";
            };

            case PEANUT -> switch (ingredient.getName()) {
                case "해바라기씨버터" ->
                        "땅콩을 해바라기씨버터로 대체하여 고소한 풍미와 부드러운 질감을 유지했어요.";
                default ->
                        "땅콩 알레르기 대체 식재료입니다.";
            };

            case SOY -> switch (ingredient.getName()) {
                case "병아리콩" ->
                        "대두를 병아리콩으로 대체하여 단백질과 포만감을 보완했어요.";
                case "렌틸콩" ->
                        "대두를 렌틸콩으로 대체하여 대두 성분 없이도 단백질 요리에 활용할 수 있어요.";
                default ->
                        "대두 알레르기 대체 식재료입니다.";
            };

            case WHEAT -> switch (ingredient.getName()) {
                case "쌀가루" ->
                        "밀가루를 쌀가루로 대체하여 글루텐 없이도 반죽의 기본 구조를 형성했어요.";
                case "타피오카" ->
                        "밀가루를 타피오카로 대체하여 점성과 탄성을 보완했어요.";
                default ->
                        "밀 알레르기 대체 식재료입니다.";
            };

            case MACKEREL -> switch (ingredient.getName()) {
                case "닭고기" ->
                        "고등어를 닭고기로 대체하여 어류 없이도 단백질 요리의 중심 재료로 활용했어요.";
                case "두부" ->
                        "고등어를 두부로 대체하여 어류 성분 없이도 부드러운 식감의 단백질 요리를 만들었어요.";
                default ->
                        "고등어 알레르기 대체 식재료입니다.";
            };

            case CRAB -> switch (ingredient.getName()) {
                case "버섯" ->
                        "게를 버섯으로 대체하여 갑각류 없이도 씹는 식감과 감칠맛을 더했어요.";
                case "콩단백" ->
                        "게를 콩단백으로 대체하여 단백질 기반 재료로 조리 시 질감을 보완했어요.";
                default ->
                        "게 알레르기 대체 식재료입니다.";
            };

            case SHRIMP -> switch (ingredient.getName()) {
                case "표고버섯" ->
                        "새우를 표고버섯으로 대체하여 해산물 없이도 쫄깃한 식감을 유지했어요.";
                case "곤약" ->
                        "새우를 곤약으로 대체하여 씹는 질감을 보완했어요.";
                default ->
                        "새우 알레르기 대체 식재료입니다.";
            };

            case PORK -> switch (ingredient.getName()) {
                case "소고기" ->
                        "돼지고기를 소고기로 대체하여 육류 요리의 단백질과 식감을 유지했어요.";
                case "닭고기" ->
                        "돼지고기를 닭고기로 대체하여 담백한 육류 대체 재료로 활용했어요.";
                default ->
                        "돼지고기 알레르기 대체 식재료입니다.";
            };

            case PEACH -> switch (ingredient.getName()) {
                case "사과" ->
                        "복숭아를 사과로 대체하여 산미와 단맛의 균형을 유지했어요.";
                case "배" ->
                        "복숭아를 배로 대체하여 부드러운 식감과 수분감을 살렸어요.";
                default ->
                        "복숭아 알레르기 대체 식재료입니다.";
            };

            case TOMATO -> switch (ingredient.getName()) {
                case "파프리카" ->
                        "토마토를 파프리카로 대체하여 색감과 아삭한 식감을 더했어요.";
                case "비트" ->
                        "토마토를 비트로 대체하여 색감과 자연스러운 단맛을 보완했어요.";
                default ->
                        "토마토 알레르기 대체 식재료입니다.";
            };

            case SULFITES -> switch (ingredient.getName()) {
                case "생과일" ->
                        "아황산류가 포함된 재료를 생과일로 대체하여 첨가물 없이 자연스러운 풍미를 유지했어요.";
                case "무첨가 건과일" ->
                        "아황산류를 무첨가 건과일로 대체하여 보존료 없이 단맛을 살렸어요.";
                default ->
                        "아황산류 알레르기 대체 식재료입니다.";
            };

            case WALNUT -> switch (ingredient.getName()) {
                case "해바라기씨" ->
                        "호두를 해바라기씨로 대체하여 고소한 풍미와 씹는 식감을 유지했어요.";
                default ->
                        "호두 알레르기 대체 식재료입니다.";
            };

            case CHICKEN -> switch (ingredient.getName()) {
                case "소고기" ->
                        "닭고기를 소고기로 대체하여 육류 요리의 중심 재료로 활용했어요.";
                case "콩단백" ->
                        "닭고기를 콩단백으로 대체하여 식물성 단백질 요리로 구성했어요.";
                default ->
                        "닭고기 알레르기 대체 식재료입니다.";
            };

            case BEEF -> switch (ingredient.getName()) {
                case "닭고기" ->
                        "소고기를 닭고기로 대체하여 단백질 요리로 활용했어요.";
                case "대체육" ->
                        "소고기를 대체육으로 대체하여 육류와 유사한 식감을 구현했어요.";
                default ->
                        "쇠고기 알레르기 대체 식재료입니다.";
            };

            case SQUID -> switch (ingredient.getName()) {
                case "버섯" ->
                        "오징어를 버섯으로 대체하여 씹는 식감 위주의 요리 구조를 유지했어요.";
                case "두부" ->
                        "오징어를 두부로 대체하여 부드러운 단백질 요리로 구성했어요.";
                default ->
                        "오징어 알레르기 대체 식재료입니다.";
            };

            case OYSTER -> switch (ingredient.getName()) {
                case "표고버섯" ->
                        "굴을 표고버섯으로 대체하여 감칠맛을 보완했어요.";
                case "다시마" ->
                        "굴을 다시마로 대체하여 국물 요리에 해조류 기반 감칠맛을 더했어요.";
                default ->
                        "굴 알레르기 대체 식재료입니다.";
            };

            case ABALONE -> switch (ingredient.getName()) {
                case "버섯" ->
                        "전복을 버섯으로 대체하여 씹는 식감과 감칠맛을 보완했어요.";
                case "콩단백" ->
                        "전복을 콩단백으로 대체하여 단백질 중심 요리로 활용했어요.";
                default ->
                        "전복 알레르기 대체 식재료입니다.";
            };

            case MUSSEL -> switch (ingredient.getName()) {
                case "채소" ->
                        "홍합을 채소로 대체하여 요리의 부피와 식감을 보완했어요.";
                case "버섯" ->
                        "홍합을 버섯으로 대체하여 쫄깃한 식감을 유지했어요.";
                default ->
                        "홍합 알레르기 대체 식재료입니다.";
            };

            case PINE_NUT -> switch (ingredient.getName()) {
                case "참깨" ->
                        "잣을 참깨로 대체하여 고소한 풍미를 더했어요.";
                case "해바라기씨" ->
                        "잣을 해바라기씨로 대체하여 씹는 식감과 고소함을 유지했어요.";
                default ->
                        "잣 알레르기 대체 식재료입니다.";
            };

            case EXTRACTED_INGREDIENTS -> switch (ingredient.getName()) {
                case "한천" ->
                        "동물성 추출 성분을 한천으로 대체하여 식물성 응고 특성을 활용했어요.";
                case "펙틴" ->
                        "동물성 추출 성분을 펙틴으로 대체하여 점성과 응고감을 형성했어요.";
                default ->
                        "추출 성분 알레르기 대체 식재료입니다.";
            };
        };
    }
}


