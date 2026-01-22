package br.com.baggiotech.tecos_api.presentation.mapper.publiclink;

import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.presentation.dto.publiclink.PublicLinkResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
    "app.public.base-url=http://test.example.com"
})
@DisplayName("PublicLinkMapper Tests")
class PublicLinkMapperTest {

    private PublicLinkMapper mapper;
    private PublicLink publicLink;
    private WorkOrder workOrder;
    private UUID linkId;
    private UUID workOrderId;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @BeforeEach
    void setUp() {
        mapper = new PublicLinkMapper();
        ReflectionTestUtils.setField(mapper, "baseUrl", "http://test.example.com");
        
        linkId = UUID.randomUUID();
        workOrderId = UUID.randomUUID();
        token = "test-token-123";
        createdAt = LocalDateTime.now().minusHours(1);
        updatedAt = LocalDateTime.now();

        workOrder = new WorkOrder();
        workOrder.setId(workOrderId);

        publicLink = new PublicLink();
        publicLink.setId(linkId);
        publicLink.setWorkOrder(workOrder);
        publicLink.setToken(token);
        publicLink.setCreatedAt(createdAt);
        publicLink.setUpdatedAt(updatedAt);
    }

    @Test
    @DisplayName("Deve mapear PublicLink para PublicLinkResponse com sucesso")
    void shouldMapPublicLinkToResponseSuccessfully() {
        PublicLinkResponse response = mapper.toResponse(publicLink);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(linkId);
        assertThat(response.workOrderId()).isEqualTo(workOrderId);
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.publicUrl()).isEqualTo("http://test.example.com/api/public/" + token);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Deve mapear PublicLink sem WorkOrder")
    void shouldMapPublicLinkWithoutWorkOrder() {
        publicLink.setWorkOrder(null);

        PublicLinkResponse response = mapper.toResponse(publicLink);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(linkId);
        assertThat(response.workOrderId()).isNull();
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.publicUrl()).isEqualTo("http://test.example.com/api/public/" + token);
    }

    @Test
    @DisplayName("Deve usar baseUrl configurado corretamente")
    void shouldUseConfiguredBaseUrl() {
        ReflectionTestUtils.setField(mapper, "baseUrl", "https://custom.example.com");

        PublicLinkResponse response = mapper.toResponse(publicLink);

        assertThat(response.publicUrl()).isEqualTo("https://custom.example.com/api/public/" + token);
    }

    @Test
    @DisplayName("Deve usar default baseUrl quando não configurado")
    void shouldUseDefaultBaseUrlWhenNotConfigured() {
        ReflectionTestUtils.setField(mapper, "baseUrl", "http://localhost:8080");

        PublicLinkResponse response = mapper.toResponse(publicLink);

        assertThat(response.publicUrl()).startsWith("http://localhost:8080/api/public/");
        assertThat(response.publicUrl()).endsWith(token);
    }
}
