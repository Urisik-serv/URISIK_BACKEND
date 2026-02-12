package com.urisik.backend.support;

import com.urisik.backend.global.apiPayload.handler.GeneralExceptionAdvice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestErrorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GeneralExceptionAdvice.class)
class ApiResponseAdviceTest {

    @Autowired
    MockMvc mockMvc;

    @TestConfiguration
    static class ValidatorTestConfig implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

        @Override
        public org.springframework.validation.Validator getValidator() {
            return new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        }
    }

    @Test
    @DisplayName("커스텀 GeneralException -> ApiResponse 실패 포맷 + 404")
    void generalException_404() throws Exception {
        mockMvc.perform(get("/test/general"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_404_001"))
                .andExpect(jsonPath("$.message").value("요청한 리소스를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.errorDetail").value("요청한 리소스를 찾을 수 없습니다."));

    }

    @Test
    @DisplayName("@Valid RequestBody 검증 실패 -> MethodArgumentNotValidException")
    void methodArgumentNotValid_400() throws Exception {
        String body = """
                { "name": "" }
                """;

        mockMvc.perform(post("/test/valid-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_400_002"))
                .andExpect(jsonPath("$.message").value("요청 값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errorDetail").isArray());
    }

    @Test
    @DisplayName("@ModelAttribute 검증 실패 -> BindException")
    void bindException_400() throws Exception {
        mockMvc.perform(get("/test/model-attr"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_400_002"))
                .andExpect(jsonPath("$.message").value("요청 값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errorDetail").isArray());
    }

    @Test
    @DisplayName("@Validated + RequestParam 제약 실패 -> ConstraintViolationException 처리")
    void constraintViolation_400() throws Exception {
        mockMvc.perform(get("/test/param").param("age", "1")) // @Min(10) 위반
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_400_002"))
                .andExpect(jsonPath("$.message").value("요청 값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errorDetail").isArray());
    }

    @Test
    @DisplayName("PathVariable 타입 미스매치 -> MethodArgumentTypeMismatchException 처리")
    void typeMismatch_400() throws Exception {
        mockMvc.perform(get("/test/type-mismatch/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_400_002"))
                .andExpect(jsonPath("$.message").value("요청 값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errorDetail").isArray());
    }

    @Test
    @DisplayName("필수 RequestParam 누락 -> MissingServletRequestParameterException 처리")
    void missingRequestParam_400() throws Exception {
        mockMvc.perform(get("/test/param")) // age 누락
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_400_002"))
                .andExpect(jsonPath("$.message").value("요청 값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errorDetail").isArray());
    }

    @Test
    @DisplayName("JSON 파싱 실패 -> HttpMessageNotReadableException 처리 + 400(BAD_REQUEST)")
    void httpMessageNotReadable_400() throws Exception {
        String brokenJson = "{ \"name\": ";

        mockMvc.perform(post("/test/valid-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_400_001"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.errorDetail").isArray());
    }

    @Test
    @DisplayName("지원하지 않는 HTTP Method -> 405 + METHOD_NOT_ALLOWED 코드")
    void methodNotSupported_405() throws Exception {
        mockMvc.perform(post("/test/only-get"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_405_001"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 HTTP 메서드입니다."))
                .andExpect(jsonPath("$.errorDetail").isArray());
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type -> 415 + UNSUPPORTED_MEDIA_TYPE 코드")
    void mediaTypeNotSupported_415() throws Exception {
        mockMvc.perform(post("/test/valid-body")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_415_001"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 Content-Type입니다."))
                .andExpect(jsonPath("$.errorDetail").isArray());
    }

    @Test
    @DisplayName("그 외 예외 -> 500 + INTERNAL_SERVER_ERROR 포맷")
    void unhandledException_500() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_500_001"))
                .andExpect(jsonPath("$.message").value("예기치 않은 서버 에러가 발생했습니다."))
                .andExpect(jsonPath("$.result").doesNotExist())
                .andExpect(jsonPath("$.errorDetail").doesNotExist());
    }

}

