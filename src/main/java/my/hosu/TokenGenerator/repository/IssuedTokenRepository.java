package my.hosu.TokenGenerator.repository;

import my.hosu.TokenGenerator.domain.IssuedToken;
import my.hosu.TokenGenerator.domain.ProductVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IssuedTokenRepository extends JpaRepository<IssuedToken, Long> {
    Optional<IssuedToken> findByTokenValue(String tokenValue);

    List<IssuedToken> findByVersion(ProductVersion version);

    long countByVersionAndVerificationCount(ProductVersion version, int verificationCount);
}
