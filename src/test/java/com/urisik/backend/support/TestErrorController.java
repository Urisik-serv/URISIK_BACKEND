package com.urisik.backend.support;

import com.urisik.backend.global.apiPayload.exception.GeneralException;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/test")
public class TestErrorController {

    // 1) 커스텀 예외
    @GetMapping("/general")
    public void general() {
        throw new GeneralException(GeneralErrorCode.NOT_FOUND);
    }

    // 2) @Valid RequestBody 검증 실패 (MethodArgumentNotValidException)
    @PostMapping(value = "/valid-body", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void validBody(@RequestBody @Valid CreateReq req) {
        // no-op
    }

    // 3) @ModelAttribute 검증 실패 (BindException)
    @GetMapping("/model-attr")
    public void modelAttr(@Valid ModelAttrReq req) {
        // no-op
    }

    // 4) @Validated + RequestParam 제약 실패 (ConstraintViolationException)
    @GetMapping("/param")
    public void param(@RequestParam @Min(10) int age) {
        // no-op
    }

    // 5) 타입 미스매치 (MethodArgumentTypeMismatchException)
    @GetMapping("/type-mismatch/{id}")
    public void typeMismatch(@PathVariable Long id) {
        // no-op
    }

    // 6) 405 유도용 (GET만 존재)
    @GetMapping("/only-get")
    public String onlyGet() {
        return "ok";
    }

    // 7) 500 유도용
    @GetMapping("/boom")
    public void boom() {
        throw new RuntimeException("BOOM");
    }

    public static class CreateReq {

        @NotBlank
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class ModelAttrReq {

        @NotBlank
        private String q;

        public String getQ() {
            return q;
        }

        public void setQ(String q) {
            this.q = q;
        }
    }
}

