package my.hosu.TokenGenerator.web;

import my.hosu.TokenGenerator.domain.Account;
import my.hosu.TokenGenerator.domain.Product;
import my.hosu.TokenGenerator.domain.ProductVersion;
import my.hosu.TokenGenerator.repository.AccountRepository;
import my.hosu.TokenGenerator.service.ProductService;
import my.hosu.TokenGenerator.service.ProductVersionService;
import my.hosu.TokenGenerator.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductVersionService versionService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private AccountRepository accountRepository;

    @Test
    @DisplayName("상품 목록 조회 테스트")
    @WithMockUser(username = "testuser")
    void listProductsTest() throws Exception {
        Account account = Account.builder().username("testuser").build();
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(productService.getProductsByIssuer(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    @DisplayName("버전 목록 조회 테스트")
    @WithMockUser(username = "testuser")
    void versionsTest() throws Exception {
        Product product = Product.builder().id(1L).name("Test Product").build();
        when(productService.getProduct(1L)).thenReturn(product);
        when(versionService.getVersionsForProduct(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/versions"))
                .andExpect(model().attributeExists("product", "versions"));
    }

    @Test
    @DisplayName("버전 생성 테스트")
    @WithMockUser
    void createVersionTest() throws Exception {
        Product product = Product.builder().id(1L).name("Test Product").build();
        when(productService.getProduct(1L)).thenReturn(product);

        mockMvc.perform(post("/products/1/versions/new")
                .with(csrf())
                .param("versionName", "v1.0")
                .param("attr_Serial", "SN123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/1"));
    }

    @Test
    @DisplayName("대량 토큰 발행 테스트")
    @WithMockUser
    void issueTokensBulkTest() throws Exception {
        Product product = Product.builder().id(1L).name("Test Product").build();
        ProductVersion version = ProductVersion.builder().id(10L).versionName("v1.0").product(product).build();

        when(productService.getProduct(1L)).thenReturn(product);
        when(versionService.getVersion(10L)).thenReturn(version);
        when(tokenService.generateTokensBulk(eq(10L), anyInt(), anyInt())).thenReturn(Arrays.asList("t1", "t2"));
        when(tokenService.generateQRCodeBase64(anyString(), anyInt(), anyInt())).thenReturn("mock-qr");

        mockMvc.perform(post("/products/1/versions/10/tokens")
                .with(csrf())
                .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/token-result"))
                .andExpect(model().attributeExists("tokens", "qrCode", "product", "version"));
    }
}
