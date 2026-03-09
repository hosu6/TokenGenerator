package my.hosu.TokenGenerator.repository;

import my.hosu.TokenGenerator.domain.Account;
import my.hosu.TokenGenerator.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIssuer(Account issuer);
}
