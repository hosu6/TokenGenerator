package my.hosu.TokenGenerator.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.hosu.TokenGenerator.dto.RegisterRequest;
import my.hosu.TokenGenerator.dto.VerificationRequest;
import my.hosu.TokenGenerator.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    @DisplayName("인증 코드 발송 API 테스트")
    @WithMockUser
    void sendCodeTest() throws Exception {
        RegistrationController.SendCodeRequest request = new RegistrationController.SendCodeRequest();
        request.setEmail("test@example.com");

        doNothing().when(accountService).sendVerificationCode(anyString());

        mockMvc.perform(post("/register/send-code")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(accountService).sendVerificationCode("test@example.com");
    }

    @Test
    @DisplayName("인증 코드 확인 API 테스트")
    @WithMockUser
    void verifyCodeTest() throws Exception {
        VerificationRequest request = new VerificationRequest();
        request.setEmail("test@example.com");
        request.setCode("123456");

        doNothing().when(accountService).verifyCodeLocal(anyString(), anyString());

        mockMvc.perform(post("/register/verify-code")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(accountService).verifyCodeLocal("test@example.com", "123456");
    }

    @Test
    @DisplayName("회원가입 API 테스트")
    @WithMockUser
    void registerUserTest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");

        mockMvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(accountService).registerNewAccount("testuser", "test@example.com", "password");
    }
}
