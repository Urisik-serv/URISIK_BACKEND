package com.urisik.backend.domain.member.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class WishListRequest {

    @Getter
    @NoArgsConstructor
    public static class PostWishes {

        @NotEmpty
        private List<@NotNull(message = "recipeId 안에는 null이 올 수 없습니다.") Long> recipeId; // foodName 리스트

        @NotEmpty
        private List<@NotNull(message = "transformedRecipeId 안에는 null이 올 수 없습니다.") Long> transformedRecipeId; // foodName 리스트


    }
    @Getter
    @NoArgsConstructor
    public static class DeleteWishes {

        @NotEmpty
        private List<@NotNull(message = "recipeId 안에는 null이 올 수 없습니다.") Long> recipeId; // foodName 리스트

        @NotEmpty
        private List<@NotNull(message = "transformedRecipeId 안에는 null이 올 수 없습니다.") Long> transformedRecipeId; // foodName 리스트


    }

    @Getter
    @NoArgsConstructor
    public static class GetWishes {
        /**
         * cursor: 다음 페이지를 가져올 때 기준이 되는 wishList id
         * - null이면 첫 페이지
         * - cursor가 있으면 "id < cursor" 기준으로 다음 데이터 조회 (DESC)
         */
        private Long cursor;
        /**
         * size: 한 번에 가져올 개수 (기본 10)
         */
        private Integer size;

        public int getSizeOrDefault(int defaultSize) {
            return (size == null || size <= 0) ? defaultSize : size;
        }

    }



}
