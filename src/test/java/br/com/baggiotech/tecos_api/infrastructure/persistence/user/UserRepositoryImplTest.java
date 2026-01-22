package br.com.baggiotech.tecos_api.infrastructure.persistence.user;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaRepository;
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
@Import({UserRepositoryImpl.class, br.com.baggiotech.tecos_api.infrastructure.persistence.company.CompanyRepositoryImpl.class})
@DisplayName("UserRepositoryImpl Integration Tests")
class UserRepositoryImplTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CompanyJpaRepository companyJpaRepository;

    @Autowired
    private UserRepository repository;

    private Company createCompany(String name, String email) {
        CompanyJpaEntity companyEntity = new CompanyJpaEntity();
        companyEntity.setName(name);
        companyEntity.setEmail(email);
        companyEntity.setIsActive(true);
        companyEntity.setCreatedAt(LocalDateTime.now());
        companyEntity.setUpdatedAt(LocalDateTime.now());
        CompanyJpaEntity saved = companyJpaRepository.save(companyEntity);
        
        Company company = new Company();
        company.setId(saved.getId());
        company.setName(saved.getName());
        company.setEmail(saved.getEmail());
        company.setIsActive(saved.getIsActive());
        company.setCreatedAt(saved.getCreatedAt());
        company.setUpdatedAt(saved.getUpdatedAt());
        return company;
    }

    private User createUser(Company company, String name, String email, String password, String type, Boolean isActive) {
        User user = new User();
        user.setCompany(company);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setType(type);
        user.setIsActive(isActive);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return repository.save(user);
    }

    @Test
    @DisplayName("Deve salvar usuário com sucesso")
    void shouldSaveUserSuccessfully() {
        Company company = createCompany("Test Company", "company@example.com");
        
        User newUser = new User();
        newUser.setCompany(company);
        newUser.setName("Test User");
        newUser.setEmail("user@example.com");
        newUser.setPassword("password123");
        newUser.setType("TECNICO");
        newUser.setIsActive(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        
        User saved = repository.save(newUser);
        
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test User");
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(userJpaRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void shouldFindUserById() {
        Company company = createCompany("Test Company", "company@example.com");
        User user1 = createUser(company, "User Alpha", "alpha@example.com", "pass123", "TECNICO", true);
        
        Optional<User> found = repository.findById(user1.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("User Alpha");
        assertThat(found.get().getEmail()).isEqualTo("alpha@example.com");
    }

    @Test
    @DisplayName("Deve retornar empty quando usuário não encontrado")
    void shouldReturnEmptyWhenUserNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        Optional<User> found = repository.findById(nonExistentId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void shouldFindAllUsers() {
        Company company = createCompany("Test Company", "company@example.com");
        createUser(company, "User Alpha", "alpha@example.com", "pass123", "TECNICO", true);
        createUser(company, "User Beta", "beta@example.com", "pass456", "ADMIN", false);
        
        List<User> users = repository.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("Deve deletar usuário por ID")
    void shouldDeleteUserById() {
        Company company = createCompany("Test Company", "company@example.com");
        User user1 = createUser(company, "User Alpha", "alpha@example.com", "pass123", "TECNICO", true);
        User user2 = createUser(company, "User Beta", "beta@example.com", "pass456", "ADMIN", false);
        
        repository.deleteById(user1.getId());
        
        assertThat(repository.existsById(user1.getId())).isFalse();
        assertThat(repository.existsById(user2.getId())).isTrue();
    }

    @Test
    @DisplayName("Deve verificar se usuário existe por ID")
    void shouldCheckIfUserExistsById() {
        Company company = createCompany("Test Company", "company@example.com");
        User user1 = createUser(company, "User Alpha", "alpha@example.com", "pass123", "TECNICO", true);
        
        assertThat(repository.existsById(user1.getId())).isTrue();
        assertThat(repository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve buscar usuário por email")
    void shouldFindUserByEmail() {
        Company company = createCompany("Test Company", "company@example.com");
        createUser(company, "User Alpha", "alpha@example.com", "pass123", "TECNICO", true);
        
        Optional<User> found = repository.findByEmail("alpha@example.com");
        
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("alpha@example.com");
    }

    @Test
    @DisplayName("Deve verificar se email existe")
    void shouldCheckIfEmailExists() {
        Company company = createCompany("Test Company", "company@example.com");
        createUser(company, "User Alpha", "alpha@example.com", "pass123", "TECNICO", true);
        
        assertThat(repository.existsByEmail("alpha@example.com")).isTrue();
        assertThat(repository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("Deve buscar usuários por companyId")
    void shouldFindUsersByCompanyId() {
        Company company1 = createCompany("Company 1", "company1@example.com");
        Company company2 = createCompany("Company 2", "company2@example.com");
        
        createUser(company1, "User 1", "user1@example.com", "pass123", "TECNICO", true);
        createUser(company1, "User 2", "user2@example.com", "pass456", "TECNICO", true);
        createUser(company2, "User 3", "user3@example.com", "pass789", "TECNICO", true);
        
        List<User> company1Users = repository.findByCompanyId(company1.getId());
        
        assertThat(company1Users).hasSize(2);
        assertThat(company1Users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    @DisplayName("Deve filtrar usuários por isActive")
    void shouldFindUsersByIsActive() {
        Company company = createCompany("Test Company", "company@example.com");
        createUser(company, "User Alpha", "alpha@example.com", "pass123", "TECNICO", true);
        createUser(company, "User Beta", "beta@example.com", "pass456", "ADMIN", false);
        
        List<User> activeUsers = repository.findByIsActive(true);
        List<User> inactiveUsers = repository.findByIsActive(false);
        
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getName()).isEqualTo("User Alpha");
        assertThat(inactiveUsers).hasSize(1);
        assertThat(inactiveUsers.get(0).getName()).isEqualTo("User Beta");
    }

    @Test
    @DisplayName("Deve filtrar usuários por tipo")
    void shouldFindUsersByType() {
        Company company = createCompany("Test Company", "company@example.com");
        createUser(company, "User Alpha", "alpha@example.com", "pass123", "TECNICO", true);
        createUser(company, "User Beta", "beta@example.com", "pass456", "ADMIN", true);
        
        List<User> tecnicos = repository.findByType("TECNICO");
        List<User> admins = repository.findByType("ADMIN");
        
        assertThat(tecnicos).hasSize(1);
        assertThat(tecnicos.get(0).getName()).isEqualTo("User Alpha");
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getName()).isEqualTo("User Beta");
    }

    @Test
    @DisplayName("Deve buscar usuários por nome ou email")
    void shouldSearchUsersByNameOrEmail() {
        Company company = createCompany("Test Company", "company@example.com");
        createUser(company, "User Alpha", "alpha@example.com", "pass123", "TECNICO", true);
        createUser(company, "User Beta", "beta@example.com", "pass456", "ADMIN", false);
        
        List<User> results1 = repository.searchByNameOrEmail("Alpha");
        List<User> results2 = repository.searchByNameOrEmail("alpha@example.com");
        List<User> results3 = repository.searchByNameOrEmail("Beta");
        
        assertThat(results1).hasSize(1);
        assertThat(results1.get(0).getName()).containsIgnoringCase("Alpha");
        assertThat(results2).hasSize(1);
        assertThat(results2.get(0).getEmail()).containsIgnoringCase("alpha");
        assertThat(results3).hasSize(1);
        assertThat(results3.get(0).getName()).containsIgnoringCase("Beta");
    }

    @Test
    @DisplayName("Deve verificar se email existe por companyId")
    void shouldCheckIfEmailExistsByCompanyId() {
        Company company1 = createCompany("Company 1", "company1@example.com");
        Company company2 = createCompany("Company 2", "company2@example.com");
        
        createUser(company1, "User 1", "user@example.com", "pass123", "TECNICO", true);
        createUser(company2, "User 2", "user@example.com", "pass456", "TECNICO", true);
        
        assertThat(repository.existsByEmailAndCompanyId("user@example.com", company1.getId())).isTrue();
        assertThat(repository.existsByEmailAndCompanyId("user@example.com", company2.getId())).isTrue();
        assertThat(repository.existsByEmailAndCompanyId("nonexistent@example.com", company1.getId())).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se email existe por companyId excluindo ID específico")
    void shouldCheckIfEmailExistsByCompanyIdAndIdNot() {
        Company company = createCompany("Test Company", "company@example.com");
        User user1 = createUser(company, "User 1", "user@example.com", "pass123", "TECNICO", true);
        User user2 = createUser(company, "User 2", "other@example.com", "pass456", "TECNICO", true);
        
        assertThat(repository.existsByEmailAndCompanyIdAndIdNot("user@example.com", company.getId(), user2.getId())).isTrue();
        assertThat(repository.existsByEmailAndCompanyIdAndIdNot("user@example.com", company.getId(), user1.getId())).isFalse();
    }
}
