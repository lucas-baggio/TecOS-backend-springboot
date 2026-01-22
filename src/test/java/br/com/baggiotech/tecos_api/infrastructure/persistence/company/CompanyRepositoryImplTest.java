package br.com.baggiotech.tecos_api.infrastructure.persistence.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(CompanyRepositoryImpl.class)
@DisplayName("CompanyRepositoryImpl Integration Tests")
class CompanyRepositoryImplTest {

    @Autowired
    private CompanyJpaRepository jpaRepository;

    @Autowired
    private CompanyRepository repository;

    private Company createCompany(String name, String email, Boolean isActive) {
        Company company = new Company();

        company.setName(name);
        company.setEmail(email);
        company.setIsActive(isActive);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());
        return repository.save(company);
    }

    @Test
    @DisplayName("Deve salvar empresa com sucesso")
    void shouldSaveCompanySuccessfully() {

        Company newCompany = new Company();

        newCompany.setName("New Company");
        newCompany.setEmail("new@example.com");
        newCompany.setIsActive(true);
        newCompany.setCreatedAt(LocalDateTime.now());
        newCompany.setUpdatedAt(LocalDateTime.now());
        Company saved = repository.save(newCompany);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Company");
        assertThat(jpaRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("Deve buscar empresa por ID")
    void shouldFindCompanyById() {

        Company company1 = createCompany("Company Alpha", "alpha@example.com", true);
        

        Optional<Company> found = repository.findById(company1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Company Alpha");
        assertThat(found.get().getEmail()).isEqualTo("alpha@example.com");
    }

    @Test
    @DisplayName("Deve retornar empty quando empresa não encontrada")
    void shouldReturnEmptyWhenCompanyNotFound() {

        UUID nonExistentId = UUID.randomUUID();
        Optional<Company> found = repository.findById(nonExistentId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todas as empresas")
    void shouldFindAllCompanies() {
        createCompany("Company Alpha", "alpha@example.com", true);
        createCompany("Company Beta", "beta@example.com", false);

        List<Company> companies = repository.findAll();
        assertThat(companies).hasSize(2);
    }

    @Test
    @DisplayName("Deve deletar empresa por ID")
    void shouldDeleteCompanyById() {
        Company company1 = createCompany("Company Alpha", "alpha@example.com", true);
        Company company2 = createCompany("Company Beta", "beta@example.com", false);

        repository.deleteById(company1.getId());
        assertThat(repository.existsById(company1.getId())).isFalse();
        assertThat(repository.existsById(company2.getId())).isTrue();
    }

    @Test
    @DisplayName("Deve verificar se empresa existe por ID")
    void shouldCheckIfCompanyExistsById() {
        Company company1 = createCompany("Company Alpha", "alpha@example.com", true);

        assertThat(repository.existsById(company1.getId())).isTrue();
        assertThat(repository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve buscar empresa por email")
    void shouldFindCompanyByEmail() {
        createCompany("Company Alpha", "alpha@example.com", true);

        Optional<Company> found = repository.findByEmail("alpha@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("alpha@example.com");
    }

    @Test
    @DisplayName("Deve verificar se email existe")
    void shouldCheckIfEmailExists() {
        createCompany("Company Alpha", "alpha@example.com", true);

        assertThat(repository.existsByEmail("alpha@example.com")).isTrue();
        assertThat(repository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se email existe em outra empresa")
    void shouldCheckIfEmailExistsInAnotherCompany() {

        Company company1 = createCompany("Company Alpha", "alpha@example.com", true);
        Company company2 = createCompany("Company Beta", "beta@example.com", false);
        

        assertThat(repository.existsByEmailAndIdNot("alpha@example.com", company2.getId())).isTrue();
        assertThat(repository.existsByEmailAndIdNot("alpha@example.com", company1.getId())).isFalse();
    }

    @Test
    @DisplayName("Deve filtrar empresas por isActive")
    void shouldFindCompaniesByIsActive() {
        createCompany("Company Alpha", "alpha@example.com", true);
        createCompany("Company Beta", "beta@example.com", false);

        List<Company> activeCompanies = repository.findByIsActive(true);
        List<Company> inactiveCompanies = repository.findByIsActive(false);
        assertThat(activeCompanies).hasSize(1);
        assertThat(activeCompanies.get(0).getName()).isEqualTo("Company Alpha");
        assertThat(inactiveCompanies).hasSize(1);
        assertThat(inactiveCompanies.get(0).getName()).isEqualTo("Company Beta");
    }

    @Test
    @DisplayName("Deve buscar empresas por nome ou email")
    void shouldSearchCompaniesByNameOrEmail() {
        createCompany("Company Alpha", "alpha@example.com", true);
        createCompany("Company Beta", "beta@example.com", false);

        List<Company> results1 = repository.searchByNameOrEmail("Alpha");
        List<Company> results2 = repository.searchByNameOrEmail("alpha@example.com");
        List<Company> results3 = repository.searchByNameOrEmail("Beta");
        assertThat(results1).hasSize(1);
        assertThat(results1.get(0).getName()).containsIgnoringCase("Alpha");
        assertThat(results2).hasSize(1);
        assertThat(results2.get(0).getEmail()).containsIgnoringCase("alpha");
        assertThat(results3).hasSize(1);
        assertThat(results3.get(0).getName()).containsIgnoringCase("Beta");
    }
}
