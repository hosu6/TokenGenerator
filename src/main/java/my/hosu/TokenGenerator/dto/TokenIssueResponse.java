package my.hosu.TokenGenerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class TokenIssueResponse {
    private List<String> tokens;
    private String qrCodeBase64;
}
