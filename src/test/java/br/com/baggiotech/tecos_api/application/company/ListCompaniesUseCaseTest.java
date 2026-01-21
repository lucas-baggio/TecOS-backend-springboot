package br.com.baggiotech.tecos_api.application.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListCompaniesUseCase Tests")
class ListCompaniesUseCaseTest {

    @Mock
    private CompanyRepository repository;

    @InjectMocks
    private ListCompaniesUseCase useCase;

    private Company company1;
    private Company company2;
    private Company company3;

    @BeforeEach
    void setUp() {
        company1 = createCompany(UUID.randomUUID(), "Alpha Company", "alpha@example.com", true);
        company2 = createCompany(UUID.randomUUID(), "Beta Company", "beta@example.com", true);
        company3 = createCompany(UUID.randomUUID(), "Gamma Company", "gamma@example.com", false);
    }

    private Company createCompany(UUID id, String name, String email, Boolean isActive) {
        Company company = new Company();
        company.setId(id);
        company.setName(name);
        company.setEmail(email);
        company.setIsActive(isActive);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());
        return company;
    }

    @Test
    @DisplayName("Deve listar todas as empresas")
    void shouldListAllCompanies() {

        List<Company> allCompanies = Arrays.asList(company1, company2, company3);
        when(repository.findAll()).thenReturn(allCompanies);
        Page<Company> result = useCase.execute(null, null, 0, 10);
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve filtrar por isActive")
    void shouldFilterByIsActive() {

        List<Company> activeCompanies = Arrays.asList(company1, company2);
        when(repository.findByIsActive(true)).thenReturn(activeCompanies);
        Page<Company> result = useCase.execute(true, null, 0, 10);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(Company::getIsActive);
        verify(repository).findByIsActive(true);
    }

    @Test
    @DisplayName("Deve buscar por nome ou email")
    void shouldSearchByNameOrEmail() {

        List<Company> searchedCompanies = Arrays.asList(company1);
        when(repository.searchByNameOrEmail("alpha")).thenReturn(searchedCompanies);
        Page<Company> result = useCase.execute(null, "alpha", 0, 10);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).containsIgnoringCase("alpha");
        verify(repository).searchByNameOrEmail("alpha");
    }

    @Test
    @DisplayName("Deve combinar filtro isActive e busca")
    void shouldCombineIsActiveAndSearch() {

        List<Company> activeCompanies = Arrays.asList(company1, company2);
        List<Company> searchedCompanies = Arrays.asList(company1);
        when(repository.findByIsActive(true)).thenReturn(activeCompanies);
        when(repository.searchByNameOrEmail("alpha")).thenReturn(searchedCompanies);
        Page<Company> result = useCase.execute(true, "alpha", 0, 10);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).containsIgnoringCase("alpha");
        verify(repository).findByIsActive(true);
        verify(repository).searchByNameOrEmail("alpha");
    }

    @Test
    @DisplayName("Deve ordenar por nome")
    void shouldSortByName() {

        List<Company> companies = Arrays.asList(company3, company1, company2);
        when(repository.findAll()).thenReturn(companies);
        Page<Company> result = useCase.execute(null, null, 0, 10);
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alpha Company");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Beta Company");
        assertThat(result.getContent().get(2).getName()).isEqualTo("Gamma Company");
    }

    @Test
    @DisplayName("Deve paginar resultados")
    void shouldPaginateResults() {

        List<Company> companies = Arrays.asList(company1, company2, company3);
        when(repository.findAll()).thenReturn(companies);
        Page<Company> result = useCase.execute(null, null, 0, 2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há resultados")
    void shouldReturnEmptyPageWhenNoResults() {

        when(repository.findAll()).thenReturn(List.of());
        Page<Company> result = useCase.execute(null, null, 0, 10);
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
}
