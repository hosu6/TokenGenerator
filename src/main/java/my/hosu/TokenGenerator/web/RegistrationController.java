package my.hosu.TokenGenerator.web;

import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import my.hosu.TokenGenerator.dto.RegisterRequest;
import my.hosu.TokenGenerator.dto.VerificationRequest;
import my.hosu.TokenGenerator.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    @Getter
    @Setter
    public static class SendCodeRequest {
        private String email;
    }

    private final AccountService accountService;

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestBody SendCodeRequest request) {
        accountService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok("인증 코드가 전송되었습니다.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody VerificationRequest request) {
        accountService.verifyCodeLocal(request.getEmail(), request.getCode());
        return ResponseEntity.ok("인증되었습니다.");
    }

    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request) {
        accountService.registerNewAccount(
                request.getUsername(),
                request.getEmail(),
                request.getPassword());
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}
