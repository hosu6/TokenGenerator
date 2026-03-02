package my.hosu.TokenGenerator.web;

import lombok.RequiredArgsConstructor;
import my.hosu.TokenGenerator.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final AccountService accountService;

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestBody Map<String, String> request) {
        try {
            accountService.sendVerificationCode(request.get("email"));
            return ResponseEntity.ok("인증 코드가 전송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody Map<String, String> request) {
        try {
            accountService.verifyCodeLocal(request.get("email"), request.get("code"));
            return ResponseEntity.ok("인증되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> request) {
        try {
            accountService.registerNewAccount(
                    request.get("username"),
                    request.get("email"),
                    request.get("password"),
                    request.get("code"));
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
