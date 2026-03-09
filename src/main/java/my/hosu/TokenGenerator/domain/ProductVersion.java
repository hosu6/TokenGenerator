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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "product_id", "version_name" })
})
public class ProductVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String versionName;

    @ElementCollection
    @CollectionTable(name = "product_version_attributes", joinColumns = @JoinColumn(name = "version_id"))
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_value")
    private java.util.Map<String, String> attributeValues;

    @Column(nullable = false)
    private java.time.LocalDateTime createdAt;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<IssuedToken> issuedTokens;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
