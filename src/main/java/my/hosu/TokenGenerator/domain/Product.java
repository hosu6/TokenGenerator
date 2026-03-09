package my.hosu.TokenGenerator.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account issuer;

    @ElementCollection
    @CollectionTable(name = "product_required_fields", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "field_name")
    private List<String> requiredFields;
}
