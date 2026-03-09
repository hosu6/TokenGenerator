package my.hosu.TokenGenerator.service;

import lombok.RequiredArgsConstructor;
import my.hosu.TokenGenerator.domain.Product;
import my.hosu.TokenGenerator.domain.ProductVersion;
import my.hosu.TokenGenerator.repository.ProductRepository;
import my.hosu.TokenGenerator.repository.ProductVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVersionService {

    private final ProductVersionRepository versionRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductVersion createVersion(Long productId, String versionName,
            java.util.Map<String, String> attributeValues) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (versionRepository.findByProductAndVersionName(product, versionName).isPresent()) {
            throw new IllegalArgumentException("Version already exists for this product");
        }

        ProductVersion version = ProductVersion.builder()
                .product(product)
                .versionName(versionName)
                .attributeValues(attributeValues)
                .build();

        return versionRepository.save(version);
    }

    public List<ProductVersion> getVersionsForProduct(Long productId) {
        return versionRepository.findByProductId(productId);
    }

    public ProductVersion getVersion(Long id) {
        return versionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));
    }

    public ProductVersion getVersionByProductAndName(Product product, String versionName) {
        return versionRepository.findByProductAndVersionName(product, versionName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product or version name"));
    }
}
