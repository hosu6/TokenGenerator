package my.hosu.TokenGenerator.service;

import lombok.RequiredArgsConstructor;
import my.hosu.TokenGenerator.domain.Account;
import my.hosu.TokenGenerator.domain.Product;
import my.hosu.TokenGenerator.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public List<Product> getProductsByIssuer(Account issuer) {
        return productRepository.findByIssuer(issuer);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
