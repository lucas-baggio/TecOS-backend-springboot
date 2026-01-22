package br.com.baggiotech.tecos_api.application.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.company.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListClientsUseCase Tests")
class ListClientsUseCaseTest {

    @Mock
    private ClientRepository repository;

    @InjectMocks
    private ListClientsUseCase useCase;

    private Company company1;
    private Company company2;
    private List<Client> clients;

    @BeforeEach
    void setUp() {
        company1 = new Company();
        company1.setId(UUID.randomUUID());
        company1.setName("Company 1");

        company2 = new Company();
        company2.setId(UUID.randomUUID());
        company2.setName("Company 2");

        clients = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Client client = new Client();
            client.setId(UUID.randomUUID());
            client.setCompany(i <= 3 ? company1 : company2);
            client.setName("Client " + i);
            client.setPhone("1199999999" + i);
            client.setIsActive(i <= 3);
            client.setCreatedAt(LocalDateTime.now());
            client.setUpdatedAt(LocalDateTime.now());
            clients.add(client);
        }
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void shouldListAllClients() {
        when(repository.findAll()).thenReturn(clients);
        
        Page<Client> result = useCase.execute(null, null, null, "name", "asc", 0, 15);
        
        assertThat(result.getContent()).hasSize(5);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve filtrar por companyId")
    void shouldFilterByCompanyId() {
        List<Client> company1Clients = clients.subList(0, 3);
        when(repository.findByCompanyId(company1.getId())).thenReturn(company1Clients);
        
        Page<Client> result = useCase.execute(company1.getId(), null, null, "name", "asc", 0, 15);
        
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(c -> c.getCompany().getId().equals(company1.getId()));
        verify(repository).findByCompanyId(company1.getId());
    }

    @Test
    @DisplayName("Deve filtrar por isActive")
    void shouldFilterByIsActive() {
        List<Client> activeClients = clients.subList(0, 3);
        when(repository.findByIsActive(true)).thenReturn(activeClients);
        
        Page<Client> result = useCase.execute(null, true, null, "name", "asc", 0, 15);
        
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(Client::getIsActive);
        verify(repository).findByIsActive(true);
    }

    @Test
    @DisplayName("Deve filtrar por companyId e isActive")
    void shouldFilterByCompanyIdAndIsActive() {
        List<Client> filteredClients = clients.subList(0, 3);
        when(repository.findByCompanyIdAndIsActive(company1.getId(), true)).thenReturn(filteredClients);
        
        Page<Client> result = useCase.execute(company1.getId(), true, null, "name", "asc", 0, 15);
        
        assertThat(result.getContent()).hasSize(3);
        verify(repository).findByCompanyIdAndIsActive(company1.getId(), true);
    }

    @Test
    @DisplayName("Deve buscar por search")
    void shouldSearchClients() {
        List<Client> searchedClients = List.of(clients.get(0));
        when(repository.searchByNameOrEmailOrCpfOrPhone("Client 1")).thenReturn(searchedClients);
        
        Page<Client> result = useCase.execute(null, null, "Client 1", "name", "asc", 0, 15);
        
        assertThat(result.getContent()).hasSize(1);
        verify(repository).searchByNameOrEmailOrCpfOrPhone("Client 1");
    }

    @Test
    @DisplayName("Deve buscar por companyId e search")
    void shouldSearchClientsByCompanyId() {
        List<Client> searchedClients = List.of(clients.get(0));
        when(repository.findByCompanyIdAndSearch(company1.getId(), "Client 1")).thenReturn(searchedClients);
        
        Page<Client> result = useCase.execute(company1.getId(), null, "Client 1", "name", "asc", 0, 15);
        
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByCompanyIdAndSearch(company1.getId(), "Client 1");
    }

    @Test
    @DisplayName("Deve paginar resultados")
    void shouldPaginateResults() {
        when(repository.findAll()).thenReturn(clients);
        
        Page<Client> page1 = useCase.execute(null, null, null, "name", "asc", 0, 2);
        Page<Client> page2 = useCase.execute(null, null, null, "name", "asc", 1, 2);
        
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page2.getTotalElements()).isEqualTo(5);
    }
}
