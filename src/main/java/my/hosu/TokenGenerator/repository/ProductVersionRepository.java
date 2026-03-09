package my.hosu.TokenGenerator.repository;

import my.hosu.TokenGenerator.domain.ProductVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import my.hosu.TokenGenerator.domain.Product;

public interface ProductVersionRepository extends JpaRepository<ProductVersion, Long> {
    Optional<ProductVersion> findByProductAndVersionName(Product product, String versionName);

    List<ProductVersion> findByProductId(Long productId);
}
