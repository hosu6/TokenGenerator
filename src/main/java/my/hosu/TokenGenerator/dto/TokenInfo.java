package my.hosu.TokenGenerator.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenInfo {
    private String token;
    private Long productId;
    private String productName;
    private Map<String, String> fieldValues;
    private long expirySeconds;
    private Integer verificationCount;
    private String versionName;
}
