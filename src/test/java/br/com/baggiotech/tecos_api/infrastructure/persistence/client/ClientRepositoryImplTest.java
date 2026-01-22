package br.com.baggiotech.tecos_api.infrastructure.persistence.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
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
@Import({ClientRepositoryImpl.class, br.com.baggiotech.tecos_api.infrastructure.persistence.company.CompanyRepositoryImpl.class})
@DisplayName("ClientRepositoryImpl Integration Tests")
class ClientRepositoryImplTest {

    @Autowired
    private ClientJpaRepository clientJpaRepository;

    @Autowired
    private CompanyJpaRepository companyJpaRepository;

    @Autowired
    private ClientRepository repository;

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

    private Client createClient(Company company, String name, String phone, String email, String cpf, Boolean isActive) {
        Client client = new Client();
        client.setCompany(company);
        client.setName(name);
        client.setPhone(phone);
        client.setEmail(email);
        client.setCpf(cpf);
        client.setIsActive(isActive);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        return repository.save(client);
    }

    @Test
    @DisplayName("Deve salvar cliente com sucesso")
    void shouldSaveClientSuccessfully() {
        Company company = createCompany("Test Company", "company@example.com");
        
        Client newClient = new Client();
        newClient.setCompany(company);
        newClient.setName("Test Client");
        newClient.setPhone("11999999999");
        newClient.setEmail("client@example.com");
        newClient.setCpf("123.456.789-00");
        newClient.setIsActive(true);
        newClient.setCreatedAt(LocalDateTime.now());
        newClient.setUpdatedAt(LocalDateTime.now());
        
        Client saved = repository.save(newClient);
        
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Client");
        assertThat(saved.getPhone()).isEqualTo("11999999999");
        assertThat(clientJpaRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("Deve buscar cliente por ID")
    void shouldFindClientById() {
        Company company = createCompany("Test Company", "company@example.com");
        Client client1 = createClient(company, "Client Alpha", "11999999999", "alpha@example.com", "123.456.789-00", true);
        
        Optional<Client> found = repository.findById(client1.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Client Alpha");
        assertThat(found.get().getPhone()).isEqualTo("11999999999");
    }

    @Test
    @DisplayName("Deve retornar empty quando cliente não existe")
    void shouldReturnEmptyWhenClientNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        Optional<Client> found = repository.findById(nonExistentId);
        
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar existência de cliente por ID")
    void shouldCheckClientExistsById() {
        Company company = createCompany("Test Company", "company@example.com");
        Client client = createClient(company, "Test Client", "11999999999", "test@example.com", null, true);
        
        assertThat(repository.existsById(client.getId())).isTrue();
        assertThat(repository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve deletar cliente (soft delete)")
    void shouldDeleteClient() {
        Company company = createCompany("Test Company", "company@example.com");
        Client client = createClient(company, "Test Client", "11999999999", "test@example.com", null, true);
        UUID clientId = client.getId();
        
        repository.delete(client);
        
        assertThat(repository.existsById(clientId)).isFalse();
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void shouldFindAllClients() {
        Company company = createCompany("Test Company", "company@example.com");
        createClient(company, "Client 1", "11999999999", "client1@example.com", null, true);
        createClient(company, "Client 2", "11888888888", "client2@example.com", null, true);
        
        List<Client> clients = repository.findAll();
        
        assertThat(clients).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Deve buscar clientes por companyId")
    void shouldFindClientsByCompanyId() {
        Company company1 = createCompany("Company 1", "company1@example.com");
        Company company2 = createCompany("Company 2", "company2@example.com");
        
        createClient(company1, "Client 1", "11999999999", "client1@example.com", null, true);
        createClient(company1, "Client 2", "11888888888", "client2@example.com", null, true);
        createClient(company2, "Client 3", "11777777777", "client3@example.com", null, true);
        
        List<Client> clients = repository.findByCompanyId(company1.getId());
        
        assertThat(clients).hasSize(2);
        assertThat(clients).allMatch(c -> c.getCompany().getId().equals(company1.getId()));
    }

    @Test
    @DisplayName("Deve buscar clientes por isActive")
    void shouldFindClientsByIsActive() {
        Company company = createCompany("Test Company", "company@example.com");
        createClient(company, "Active Client", "11999999999", "active@example.com", null, true);
        createClient(company, "Inactive Client", "11888888888", "inactive@example.com", null, false);
        
        List<Client> activeClients = repository.findByIsActive(true);
        List<Client> inactiveClients = repository.findByIsActive(false);
        
        assertThat(activeClients).isNotEmpty();
        assertThat(inactiveClients).isNotEmpty();
        assertThat(activeClients).allMatch(Client::getIsActive);
        assertThat(inactiveClients).allMatch(c -> !c.getIsActive());
    }

    @Test
    @DisplayName("Deve buscar clientes por companyId e isActive")
    void shouldFindClientsByCompanyIdAndIsActive() {
        Company company1 = createCompany("Company 1", "company1@example.com");
        Company company2 = createCompany("Company 2", "company2@example.com");
        
        createClient(company1, "Active Client 1", "11999999999", "active1@example.com", null, true);
        createClient(company1, "Inactive Client 1", "11888888888", "inactive1@example.com", null, false);
        createClient(company2, "Active Client 2", "11777777777", "active2@example.com", null, true);
        
        List<Client> clients = repository.findByCompanyIdAndIsActive(company1.getId(), true);
        
        assertThat(clients).hasSize(1);
        assertThat(clients.get(0).getCompany().getId()).isEqualTo(company1.getId());
        assertThat(clients.get(0).getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar clientes por nome, email, cpf ou telefone")
    void shouldSearchClientsByNameOrEmailOrCpfOrPhone() {
        Company company = createCompany("Test Company", "company@example.com");
        createClient(company, "John Doe", "11999999999", "john@example.com", "123.456.789-00", true);
        createClient(company, "Jane Smith", "11888888888", "jane@example.com", "987.654.321-00", true);
        
        List<Client> byName = repository.searchByNameOrEmailOrCpfOrPhone("John");
        List<Client> byEmail = repository.searchByNameOrEmailOrCpfOrPhone("jane@example.com");
        List<Client> byCpf = repository.searchByNameOrEmailOrCpfOrPhone("123.456.789-00");
        List<Client> byPhone = repository.searchByNameOrEmailOrCpfOrPhone("11888888888");
        
        assertThat(byName).isNotEmpty();
        assertThat(byEmail).isNotEmpty();
        assertThat(byCpf).isNotEmpty();
        assertThat(byPhone).isNotEmpty();
    }

    @Test
    @DisplayName("Deve buscar clientes por companyId e search")
    void shouldFindClientsByCompanyIdAndSearch() {
        Company company1 = createCompany("Company 1", "company1@example.com");
        Company company2 = createCompany("Company 2", "company2@example.com");
        
        createClient(company1, "Client Alpha", "11999999999", "alpha@example.com", "123.456.789-00", true);
        createClient(company1, "Client Beta", "11888888888", "beta@example.com", "987.654.321-00", true);
        createClient(company2, "Client Alpha", "11777777777", "alpha2@example.com", "111.222.333-44", true);
        
        List<Client> clients = repository.findByCompanyIdAndSearch(company1.getId(), "Alpha");
        
        assertThat(clients).hasSize(1);
        assertThat(clients.get(0).getCompany().getId()).isEqualTo(company1.getId());
        assertThat(clients.get(0).getName()).contains("Alpha");
    }

    @Test
    @DisplayName("Deve atualizar cliente existente")
    void shouldUpdateExistingClient() {
        Company company = createCompany("Test Company", "company@example.com");
        Client client = createClient(company, "Original Name", "11999999999", "original@example.com", null, true);
        
        client.setName("Updated Name");
        client.setPhone("11888888888");
        client.setUpdatedAt(LocalDateTime.now());
        Client updated = repository.save(client);
        
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getPhone()).isEqualTo("11888888888");
    }
}
