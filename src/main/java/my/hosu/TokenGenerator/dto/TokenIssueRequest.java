package my.hosu.TokenGenerator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenIssueRequest {
    private Long versionId;
    private Integer count;
    private Integer expiryDays;
}
