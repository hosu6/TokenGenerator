package my.hosu.TokenGenerator.web;

import my.hosu.TokenGenerator.dto.TokenInfo;
import my.hosu.TokenGenerator.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VerificationController.class)
class VerificationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private TokenService tokenService;

        @MockBean
        private my.hosu.TokenGenerator.service.ProductService productService;

        @MockBean
        private my.hosu.TokenGenerator.service.ProductVersionService versionService;

        @Test
        @DisplayName("인증 폼 조회 테스트 - 기본")
        @WithMockUser
        void verificationFormTest() throws Exception {
                mockMvc.perform(get("/verify"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("verify/form"));
        }

        @Test
        @DisplayName("인증 폼 조회 테스트 - 토큰 포함")
        @WithMockUser
        void verificationFormWithTokenTest() throws Exception {
                TokenInfo tokenInfo = TokenInfo.builder()
                                .token("valid-token")
                                .productId(1L)
                                .productName("Test Product")
                                .versionName("v1.0")
                                .build();
                when(tokenService.getTokenInfoDirect("valid-token")).thenReturn(tokenInfo);
                when(productService.getProduct(1L)).thenReturn(
                                my.hosu.TokenGenerator.domain.Product.builder().id(1L).name("Test Product").build());

                mockMvc.perform(get("/verify").param("token", "valid-token"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("verify/form"))
                                .andExpect(model().attribute("tokenInfo", tokenInfo));
        }

        @Test
        @DisplayName("토큰 검증 테스트 - 성공")
        @WithMockUser
        void verifySuccessTest() throws Exception {
                TokenInfo tokenInfo = TokenInfo.builder()
                                .token("valid-token")
                                .productId(1L)
                                .productName("Test Product")
                                .versionName("v1.0")
                                .verificationCount(1)
                                .build();
                when(tokenService.getTokenInfoDirect("valid-token")).thenReturn(tokenInfo);
                when(productService.getProduct(1L))
                                .thenReturn(my.hosu.TokenGenerator.domain.Product.builder().id(1L).build());
                when(tokenService.verifyToken(eq("valid-token"), any())).thenReturn(tokenInfo);

                mockMvc.perform(post("/verify")
                                .with(csrf())
                                .param("token", "valid-token"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("verify/result"))
                                .andExpect(model().attribute("isValid", true))
                                .andExpect(model().attributeExists("tokenInfo"));
        }

        @Test
        @DisplayName("토큰 검증 테스트 - 실패 (정보 불일치)")
        @WithMockUser
        void verifyFailureMismatchTest() throws Exception {
                TokenInfo tokenInfo = TokenInfo.builder()
                                .token("valid-token")
                                .productId(1L)
                                .productName("Test Product")
                                .versionName("v1.0")
                                .build();
                when(tokenService.getTokenInfoDirect("valid-token")).thenReturn(tokenInfo);
                when(productService.getProduct(1L))
                                .thenReturn(my.hosu.TokenGenerator.domain.Product.builder().id(1L).build());
                when(tokenService.verifyToken(eq("valid-token"), any())).thenReturn(null);

                mockMvc.perform(post("/verify")
                                .with(csrf())
                                .param("token", "valid-token"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("verify/form"))
                                .andExpect(model().attributeExists("error"));
        }
}
