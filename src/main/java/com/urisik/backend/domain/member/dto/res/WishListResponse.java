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
        private List<String> wishItems; // foodName 리스트
    }


    @Getter
    @Builder
    public static class GetWishes {

        private Boolean isSuccess;
        private List<MemberWishList> items;
        private Long nextCursor;   // 다음 요청에 넣을 cursor(마지막 아이템 id)
        private Boolean hasNext;
    }

}
