package my.hosu.TokenGenerator.web;

import lombok.RequiredArgsConstructor;
import my.hosu.TokenGenerator.dto.TokenInfo;
import my.hosu.TokenGenerator.service.TokenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/verify")
@RequiredArgsConstructor
public class VerificationController {

    private final my.hosu.TokenGenerator.service.ProductService productService;
    private final TokenService tokenService;

    @GetMapping
    public String verificationForm(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "versionName", required = false) String versionName,
            @RequestParam(value = "token", required = false) String token,
            Model model) {

        if (productId != null) {
            my.hosu.TokenGenerator.domain.Product product = productService.getProduct(productId);
            model.addAttribute("product", product);
            model.addAttribute("productId", productId);
        }

        if (versionName != null) {
            model.addAttribute("versionName", versionName);
        }

        if (token != null && !token.isEmpty()) {
            TokenInfo tokenInfo = tokenService.getTokenInfoDirect(token);
            if (tokenInfo != null) {
                model.addAttribute("token", token);
                model.addAttribute("tokenInfo", tokenInfo);
                if (productId == null) {
                    model.addAttribute("product", productService.getProduct(tokenInfo.getProductId()));
                }
            }
        }

        return "verify/form";
    }

    @PostMapping
    public String verify(
            @RequestParam("token") String tokenValue,
            @RequestParam java.util.Map<String, String> allParams,
            Model model) {

        TokenInfo initialInfo = tokenService.getTokenInfoDirect(tokenValue);
        if (initialInfo == null) {
            model.addAttribute("error", "Invalid Token");
            return "verify/form";
        }

        my.hosu.TokenGenerator.domain.Product product = productService.getProduct(initialInfo.getProductId());
        java.util.Map<String, String> providedAttributes = new java.util.HashMap<>();
        if (product.getRequiredFields() != null) {
            for (String field : product.getRequiredFields()) {
                providedAttributes.put(field, allParams.get("attr_" + field));
            }
        }

        TokenInfo tokenInfo = tokenService.verifyToken(tokenValue, providedAttributes);

        if (tokenInfo == null) {
            model.addAttribute("error", "Invalid verification details or Token");
            model.addAttribute("token", tokenValue);
            model.addAttribute("product", product);
            return "verify/form";
        }

        model.addAttribute("isValid", true);
        model.addAttribute("tokenInfo", tokenInfo);
        return "verify/result";
    }
}
