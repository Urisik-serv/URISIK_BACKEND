package com.urisik.backend.domain.member.dto.res;

import com.urisik.backend.domain.member.entity.MemberWishList;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class WishListResponse {


    @Getter
    @Builder
    public static class PostWishes {
        private Boolean isSuccess;
    }
    @Getter
    @Builder
    public static class DeleteWishes {
        private Boolean isSuccess;
        private long deletedNum;
        private long deletedTransNum;
    }


    @Getter
    @Builder
    public static class GetWishes {

        private Boolean isSuccess;
        private List<WishItem> items;
        private Long nextCursor;   // 다음 요청에 넣을 cursor(마지막 아이템 id)
        private Boolean hasNext;
    }

    @Getter
    @Builder
    public static class WishItem {
        private Long wishId;       // (선택) wish 자체 삭제/페이징에 필요하면 포함
        private Long recipeId;
        private String recipeName;
    }

}
