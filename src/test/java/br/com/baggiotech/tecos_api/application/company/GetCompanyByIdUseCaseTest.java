package br.com.baggiotech.tecos_api.application.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCompanyByIdUseCase Tests")
class GetCompanyByIdUseCaseTest {

    @Mock
    private CompanyRepository repository;

    @InjectMocks
    private GetCompanyByIdUseCase useCase;

    private Company company;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);
        company.setName("Test Company");
        company.setEmail("test@example.com");
        company.setIsActive(true);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve buscar empresa por ID com sucesso")
    void shouldGetCompanyByIdSuccessfully() {

        when(repository.findById(companyId)).thenReturn(Optional.of(company));
        Company result = useCase.execute(companyId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(companyId);
        assertThat(result.getName()).isEqualTo("Test Company");
        verify(repository).findById(companyId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando empresa não encontrada")
    void shouldThrowExceptionWhenCompanyNotFound() {

        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Company")
                .hasMessageContaining(nonExistentId.toString());

        verify(repository).findById(nonExistentId);
    }
}
