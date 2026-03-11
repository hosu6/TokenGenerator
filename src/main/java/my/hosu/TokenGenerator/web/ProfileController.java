package my.hosu.TokenGenerator.web;

import lombok.RequiredArgsConstructor;
import my.hosu.TokenGenerator.domain.Account;
import my.hosu.TokenGenerator.repository.AccountRepository;
import my.hosu.TokenGenerator.service.AccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @GetMapping
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Account account = accountRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // Ensure API Key exists for existing users
        if (account.getApiKey() == null || account.getApiKey().isEmpty()) {
            accountService.regenerateApiKey(account.getUsername());
            account = accountRepository.findByUsername(userDetails.getUsername()).get();
        }

        model.addAttribute("account", account);
        return "profile/index";
    }

    @PostMapping("/api-key/regenerate")
    public String regenerateApiKey(@AuthenticationPrincipal UserDetails userDetails) {
        accountService.regenerateApiKey(userDetails.getUsername());
        return "redirect:/profile";
    }
}
