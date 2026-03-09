package my.hosu.TokenGenerator.web;

import lombok.RequiredArgsConstructor;
import my.hosu.TokenGenerator.domain.Account;
import my.hosu.TokenGenerator.domain.IssuedToken;
import my.hosu.TokenGenerator.domain.Product;
import my.hosu.TokenGenerator.domain.ProductVersion;
import my.hosu.TokenGenerator.repository.AccountRepository;
import my.hosu.TokenGenerator.service.ProductService;
import my.hosu.TokenGenerator.service.ProductVersionService;
import my.hosu.TokenGenerator.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductVersionService versionService;
    private final TokenService tokenService;
    private final AccountRepository accountRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping
    public String listProducts(Model model, Principal principal) {
        Account account = getAuthenticatedAccount(principal);
        model.addAttribute("products", productService.getProductsByIssuer(account));
        return "product/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return "product/create";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute Product product, @RequestParam("fields") String fields, Principal principal) {
        Account account = getAuthenticatedAccount(principal);
        product.setIssuer(account);

        if (fields != null && !fields.trim().isEmpty()) {
            List<String> requiredFields = Arrays.stream(fields.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            product.setRequiredFields(requiredFields);
        }

        productService.saveProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/{id}")
    public String versions(@PathVariable Long id, Model model) throws Exception {
        Product product = productService.getProduct(id);
        List<ProductVersion> versions = versionService.getVersionsForProduct(id);

        // Add counts to versions
        Map<Long, Long> totalCounts = new HashMap<>();
        Map<Long, Long> unverifiedCounts = new HashMap<>();
        Map<Long, String> qrCodes = new HashMap<>();
        Map<Long, List<IssuedToken>> versionTokens = new HashMap<>();
        Map<Long, String> qrLinks = new HashMap<>();

        for (ProductVersion v : versions) {
            totalCounts.put(v.getId(), tokenService.getTotalTokensCount(v));
            unverifiedCounts.put(v.getId(), tokenService.getUnverifiedTokensCount(v));

            // URL: /verify?productId=1&versionName=v1
            String verificationUrl = baseUrl + "/verify?productId=" + id + "&versionName=" + v.getVersionName();
            qrCodes.put(v.getId(), tokenService.generateQRCodeBase64(verificationUrl, 200, 200));
            qrLinks.put(v.getId(), verificationUrl);

            versionTokens.put(v.getId(), tokenService.getTokensByVersion(v));
        }

        model.addAttribute("product", product);
        model.addAttribute("versions", versions);
        model.addAttribute("totalCounts", totalCounts);
        model.addAttribute("unverifiedCounts", unverifiedCounts);
        model.addAttribute("qrCodes", qrCodes);
        model.addAttribute("qrLinks", qrLinks);
        model.addAttribute("versionTokens", versionTokens);
        return "product/versions";
    }

    @PostMapping("/{id}/versions/new")
    public String createVersion(@PathVariable Long id, @RequestParam String versionName,
            @RequestParam Map<String, String> allRequestParams) {

        Product product = productService.getProduct(id);
        Map<String, String> attributeValues = new HashMap<>();
        if (product.getRequiredFields() != null) {
            for (String field : product.getRequiredFields()) {
                attributeValues.put(field, allRequestParams.get("attr_" + field));
            }
        }

        versionService.createVersion(id, versionName, attributeValues);
        return "redirect:/products/" + id;
    }

    @PostMapping("/{id}/versions/{versionId}/tokens")
    public String issueTokens(@PathVariable Long id, @PathVariable Long versionId,
            @RequestParam int quantity, @RequestParam(defaultValue = "365") int expiryDays,
            Model model) throws Exception {
        List<String> tokens = tokenService.generateTokensBulk(versionId, quantity, expiryDays);
        ProductVersion version = versionService.getVersion(versionId);

        String verificationUrl = baseUrl + "/verify?productId=" + id + "&versionName=" + version.getVersionName();
        String qrCode = tokenService.generateQRCodeBase64(verificationUrl, 250, 250);

        model.addAttribute("tokens", tokens);
        model.addAttribute("qrCode", qrCode);
        model.addAttribute("verificationUrl", verificationUrl);
        model.addAttribute("product", productService.getProduct(id));
        model.addAttribute("version", version);

        return "product/token-result";
    }

    @GetMapping("/{id}/versions/{versionId}/tokens")
    public String listTokens(@PathVariable Long id, @PathVariable Long versionId, Model model) throws Exception {
        Product product = productService.getProduct(id);
        ProductVersion version = versionService.getVersion(versionId);
        List<IssuedToken> tokens = tokenService.getTokensByVersion(version);

        model.addAttribute("product", product);
        model.addAttribute("version", version);
        model.addAttribute("tokens", tokens);
        return "product/tokens";
    }

    private Account getAuthenticatedAccount(Principal principal) {
        return accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + principal.getName()));
    }
}
