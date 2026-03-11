package my.hosu.TokenGenerator.web;

import lombok.RequiredArgsConstructor;
import my.hosu.TokenGenerator.domain.ProductVersion;
import my.hosu.TokenGenerator.dto.TokenIssueRequest;
import my.hosu.TokenGenerator.dto.TokenIssueResponse;
import my.hosu.TokenGenerator.service.ProductVersionService;
import my.hosu.TokenGenerator.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
public class TokenApiController {

    private final TokenService tokenService;
    private final ProductVersionService versionService;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping("/issue")
    public ResponseEntity<TokenIssueResponse> issueTokens(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TokenIssueRequest request) throws Exception {

        if (request.getVersionId() == null) {
            return ResponseEntity.badRequest().build();
        }

        ProductVersion version = versionService.getVersion(request.getVersionId());
        if (!version.getProduct().getIssuer().getUsername().equals(userDetails.getUsername())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        int count = request.getCount() != null ? request.getCount() : 1;
        int expiryDays = request.getExpiryDays() != null ? request.getExpiryDays() : 365;

        List<String> tokens = tokenService.generateTokensBulk(request.getVersionId(), count, expiryDays);

        String verificationUrl = baseUrl + "/verify?productId=" + version.getProduct().getId() + "&versionName="
                + version.getVersionName();
        String qrCodeBase64 = tokenService.generateQRCodeBase64(verificationUrl, 250, 250);

        return ResponseEntity.ok(TokenIssueResponse.builder()
                .tokens(tokens)
                .qrCodeBase64(qrCodeBase64)
                .build());
    }
}
