package my.hosu.TokenGenerator.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id")
    private ProductVersion version;

    @Column(unique = true, nullable = false)
    private String tokenValue;

    @Column(nullable = false)
    private Integer verificationCount;

    private LocalDateTime lastVerifiedAt;

    private LocalDateTime expiresAt;

    @PrePersist
    public void prePersist() {
        if (this.verificationCount == null) {
            this.verificationCount = 0;
        }
    }
}
