package com.urisik.backend.domain.member.dto.res;

import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.recipe.enums.FoodSafety;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class WishListResponse {


    @Getter
    @Builder
    public static class PostWishes {
        private Boolean isPosted;
    }
    @Getter
    @Builder
    public static class DeleteWishes {
        private Boolean isDeleted;
        private long deletedNum;
        private long deletedTransNum;
    }


    @Getter
    @Builder
    public static class GetWishes {

        private List<WishItem> items;
        private Long nextCursor;   // 다음 요청에 넣을 cursor(마지막 아이템 id)
        private Boolean hasNext;
    }

    @Getter
    @Builder
    public static class GetTransWishes {

        private List<TransWishItem> items;
        private Long nextCursor;   // 다음 요청에 넣을 cursor(마지막 아이템 id)
        private Boolean hasNext;
    }

    @Getter
    @Builder
    public static class WishItem {
        private Long wishId;       // (선택) wish 자체 삭제/페이징에 필요하면 포함
        private Long recipeId;
        private String recipeName;
        private String category;
        private String foodImage;
        private double avgScore;
        private List<String> recipeIngredients;
        private FoodSafety foodSafety;
    }

    @Getter
    @Builder
    public static class TransWishItem {
        private Long wishId;       // (선택) wish 자체 삭제/페이징에 필요하면 포함
        private Long transformedRecipeId;
        private String transformedRecipeName;
        private String category;
        private String foodImage;
        private double avgScore;
        private List<String> recipeIngredients;
        private FoodSafety foodSafety;
    }
    @Getter
    @Builder
    public static class Recommendation {
        private List<String> recipeName;


    }



}
