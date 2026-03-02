package my.hosu.TokenGenerator.service;

import lombok.RequiredArgsConstructor;
import my.hosu.TokenGenerator.domain.Account;
import my.hosu.TokenGenerator.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    private static final String CODE_PREFIX = "MAIL_CODE:";
    private static final String VERIFIED_PREFIX = "VERIFIED:";

    public void sendVerificationCode(String email) {
        if (accountRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(CODE_PREFIX + email, code, 5, TimeUnit.MINUTES);

        emailService.sendVerificationCode(email, code);
    }

    public void verifyCodeLocal(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (savedCode == null || !savedCode.equals(code)) {
            throw new RuntimeException("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        // 인증 성공 시 '인증됨' 플래그를 10분간 유지
        redisTemplate.opsForValue().set(VERIFIED_PREFIX + email, "true", 10, TimeUnit.MINUTES);
        redisTemplate.delete(CODE_PREFIX + email); // 코드 즉시 삭제
    }

    @Transactional
    public Account registerNewAccount(String username, String email, String password) {
        // 1. 아이디 중복 체크
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // 2. 인증 여부 확인
        String isVerified = redisTemplate.opsForValue().get(VERIFIED_PREFIX + email);
        if (isVerified == null || !isVerified.equals("true")) {
            throw new RuntimeException("이메일 인증이 필요합니다.");
        }

        Account account = Account.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role("USER")
                .enabled(true)
                .build();

        Account saved = accountRepository.save(account);
        redisTemplate.delete(VERIFIED_PREFIX + email); // 가입 완료 후 플래그 삭제

        return saved;
    }
}
