package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.client.*;
import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.presentation.dto.client.ClientRequest;
import br.com.baggiotech.tecos_api.presentation.dto.client.ClientResponse;
import br.com.baggiotech.tecos_api.presentation.exception.GlobalExceptionHandler;
import br.com.baggiotech.tecos_api.presentation.mapper.client.ClientMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientController Integration Tests")
class ClientControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ListClientsUseCase listClientsUseCase;

    @Mock
    private CreateClientUseCase createClientUseCase;

    @Mock
    private GetClientByIdUseCase getClientByIdUseCase;

    @Mock
    private UpdateClientUseCase updateClientUseCase;

    @Mock
    private DeleteClientUseCase deleteClientUseCase;

    @Mock
    private ClientMapper mapper;

    private ClientController controller;

    private Client client;
    private Company company;
    private UUID clientId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        controller = new ClientController(
                listClientsUseCase,
                createClientUseCase,
                getClientByIdUseCase,
                updateClientUseCase,
                deleteClientUseCase,
                mapper
        );
        
        org.springframework.validation.beanvalidation.LocalValidatorFactoryBean validator = 
            new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        companyId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);
        company.setName("Test Company");
        company.setEmail("company@example.com");
        company.setIsActive(true);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        clientId = UUID.randomUUID();
        client = new Client();
        client.setId(clientId);
        client.setCompany(company);
        client.setName("Test Client");
        client.setPhone("11999999999");
        client.setEmail("client@example.com");
        client.setCpf("123.456.789-00");
        client.setObservations("Test observations");
        client.setIsActive(true);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/clients - Deve listar clientes com sucesso")
    void shouldListClientsSuccessfully() throws Exception {
        List<Client> clients = Arrays.asList(client);
        Page<Client> page = new PageImpl<>(clients, PageRequest.of(0, 15), 1);
        
        when(listClientsUseCase.execute(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);
        when(mapper.toResponse(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            return new ClientResponse(
                    c.getId(),
                    c.getCompany() != null ? c.getCompany().getId() : null,
                    c.getCompany() != null ? c.getCompany().getName() : null,
                    c.getName(),
                    c.getPhone(),
                    c.getEmail(),
                    c.getCpf(),
                    c.getObservations(),
                    c.getIsActive(),
                    c.getCreatedAt(),
                    c.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/clients")
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Client"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/clients - Deve filtrar por companyId")
    void shouldFilterByCompanyId() throws Exception {
        List<Client> clients = Arrays.asList(client);
        Page<Client> page = new PageImpl<>(clients, PageRequest.of(0, 15), 1);
        
        when(listClientsUseCase.execute(eq(companyId), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);
        when(mapper.toResponse(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            return new ClientResponse(
                    c.getId(), c.getCompany() != null ? c.getCompany().getId() : null,
                    c.getCompany() != null ? c.getCompany().getName() : null,
                    c.getName(), c.getPhone(), c.getEmail(), c.getCpf(),
                    c.getObservations(), c.getIsActive(), c.getCreatedAt(), c.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/clients")
                        .param("companyId", companyId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/clients - Deve criar cliente com sucesso")
    void shouldCreateClientSuccessfully() throws Exception {
        ClientRequest request = new ClientRequest(
                companyId,
                "New Client",
                "11999999999",
                "newclient@example.com",
                "123.456.789-00",
                "Observations",
                true
        );

        when(createClientUseCase.execute(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(client);
        when(mapper.toResponse(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            return new ClientResponse(
                    c.getId(), c.getCompany() != null ? c.getCompany().getId() : null,
                    c.getCompany() != null ? c.getCompany().getName() : null,
                    c.getName(), c.getPhone(), c.getEmail(), c.getCpf(),
                    c.getObservations(), c.getIsActive(), c.getCreatedAt(), c.getUpdatedAt()
            );
        });

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Client"));
    }

    @Test
    @DisplayName("GET /api/clients/{id} - Deve buscar cliente por ID")
    void shouldGetClientById() throws Exception {
        when(getClientByIdUseCase.execute(clientId)).thenReturn(client);
        when(mapper.toResponse(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            return new ClientResponse(
                    c.getId(), c.getCompany() != null ? c.getCompany().getId() : null,
                    c.getCompany() != null ? c.getCompany().getName() : null,
                    c.getName(), c.getPhone(), c.getEmail(), c.getCpf(),
                    c.getObservations(), c.getIsActive(), c.getCreatedAt(), c.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId.toString()))
                .andExpect(jsonPath("$.name").value("Test Client"));
    }

    @Test
    @DisplayName("PUT /api/clients/{id} - Deve atualizar cliente com sucesso")
    void shouldUpdateClientSuccessfully() throws Exception {
        ClientRequest request = new ClientRequest(
                companyId,
                "Updated Client",
                "11888888888",
                "updated@example.com",
                "987.654.321-00",
                "Updated observations",
                false
        );

        Client updatedClient = new Client();
        updatedClient.setId(clientId);
        updatedClient.setCompany(company);
        updatedClient.setName("Updated Client");
        updatedClient.setPhone("11888888888");
        updatedClient.setEmail("updated@example.com");
        updatedClient.setCpf("987.654.321-00");
        updatedClient.setObservations("Updated observations");
        updatedClient.setIsActive(false);
        updatedClient.setCreatedAt(LocalDateTime.now());
        updatedClient.setUpdatedAt(LocalDateTime.now());

        when(updateClientUseCase.execute(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(updatedClient);
        when(mapper.toResponse(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            return new ClientResponse(
                    c.getId(), c.getCompany() != null ? c.getCompany().getId() : null,
                    c.getCompany() != null ? c.getCompany().getName() : null,
                    c.getName(), c.getPhone(), c.getEmail(), c.getCpf(),
                    c.getObservations(), c.getIsActive(), c.getCreatedAt(), c.getUpdatedAt()
            );
        });

        mockMvc.perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Client"));
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} - Deve deletar cliente com sucesso")
    void shouldDeleteClientSuccessfully() throws Exception {
        doNothing().when(deleteClientUseCase).execute(clientId);

        mockMvc.perform(delete("/api/clients/{id}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cliente excluído com sucesso."));

        verify(deleteClientUseCase).execute(clientId);
    }

    @Test
    @DisplayName("GET /api/clients/{id} - Deve retornar 404 quando cliente não encontrado")
    void shouldReturn404WhenClientNotFound() throws Exception {
        when(getClientByIdUseCase.execute(clientId))
                .thenThrow(new EntityNotFoundException("Client", clientId));

        mockMvc.perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isNotFound());
    }
}
