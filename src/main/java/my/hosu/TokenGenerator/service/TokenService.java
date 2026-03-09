package my.hosu.TokenGenerator.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import my.hosu.TokenGenerator.domain.IssuedToken;
import my.hosu.TokenGenerator.domain.ProductVersion;
import my.hosu.TokenGenerator.dto.TokenInfo;
import my.hosu.TokenGenerator.repository.IssuedTokenRepository;
import my.hosu.TokenGenerator.repository.ProductVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final IssuedTokenRepository issuedTokenRepository;
    private final ProductVersionRepository productVersionRepository;

    @Transactional
    public List<String> generateTokensBulk(Long versionId, int quantity, int expiryDays) {
        ProductVersion version = productVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(expiryDays);

        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            String tokenValue = UUID.randomUUID().toString();
            IssuedToken issuedToken = IssuedToken.builder()
                    .version(version)
                    .tokenValue(tokenValue)
                    .verificationCount(0)
                    .expiresAt(expiresAt)
                    .build();
            issuedTokenRepository.save(issuedToken);
            tokens.add(tokenValue);
        }
        return tokens;
    }

    @Transactional
    public TokenInfo verifyToken(String tokenValue, Map<String, String> providedAttributes) {
        IssuedToken issuedToken = issuedTokenRepository.findByTokenValue(tokenValue)
                .orElse(null);

        if (issuedToken == null)
            return null;

        // Check if attributes match
        ProductVersion version = issuedToken.getVersion();
        Map<String, String> requiredAttributes = version.getAttributeValues();

        for (String key : requiredAttributes.keySet()) {
            String requiredValue = requiredAttributes.get(key);
            String providedValue = providedAttributes.get(key);
            if (providedValue == null || !providedValue.equals(requiredValue)) {
                return null; // Attribute mismatch
            }
        }

        // Check expiry
        if (issuedToken.getExpiresAt() != null && issuedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }

        issuedToken.setVerificationCount(issuedToken.getVerificationCount() + 1);
        issuedToken.setLastVerifiedAt(LocalDateTime.now());
        issuedTokenRepository.save(issuedToken);

        return TokenInfo.builder()
                .token(tokenValue)
                .productId(version.getProduct().getId())
                .productName(version.getProduct().getName())
                .fieldValues(providedAttributes)
                .verificationCount(issuedToken.getVerificationCount())
                .versionName(version.getVersionName())
                .build();
    }

    public List<IssuedToken> getTokensByVersion(ProductVersion version) {
        return issuedTokenRepository.findByVersion(version);
    }

    public TokenInfo getTokenInfoDirect(String tokenValue) {
        IssuedToken issuedToken = issuedTokenRepository.findByTokenValue(tokenValue)
                .orElse(null);
        if (issuedToken == null)
            return null;

        ProductVersion version = issuedToken.getVersion();
        return TokenInfo.builder()
                .token(tokenValue)
                .productId(version.getProduct().getId())
                .productName(version.getProduct().getName())
                .verificationCount(issuedToken.getVerificationCount())
                .versionName(version.getVersionName())
                .build();
    }

    public String generateQRCodeBase64(String content, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        return Base64.getEncoder().encodeToString(pngData);
    }

    public long getTotalTokensCount(ProductVersion version) {
        return issuedTokenRepository.findByVersion(version).size();
    }

    public long getUnverifiedTokensCount(ProductVersion version) {
        return issuedTokenRepository.countByVersionAndVerificationCount(version, 0);
    }
}
