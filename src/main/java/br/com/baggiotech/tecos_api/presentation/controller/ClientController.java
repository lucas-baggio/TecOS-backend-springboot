package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.client.*;
import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.presentation.dto.client.ClientRequest;
import br.com.baggiotech.tecos_api.presentation.dto.client.ClientResponse;
import br.com.baggiotech.tecos_api.presentation.mapper.client.ClientMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ListClientsUseCase listClientsUseCase;
    private final CreateClientUseCase createClientUseCase;
    private final GetClientByIdUseCase getClientByIdUseCase;
    private final UpdateClientUseCase updateClientUseCase;
    private final DeleteClientUseCase deleteClientUseCase;
    private final ClientMapper mapper;

    public ClientController(
            ListClientsUseCase listClientsUseCase,
            CreateClientUseCase createClientUseCase,
            GetClientByIdUseCase getClientByIdUseCase,
            UpdateClientUseCase updateClientUseCase,
            DeleteClientUseCase deleteClientUseCase,
            ClientMapper mapper) {
        this.listClientsUseCase = listClientsUseCase;
        this.createClientUseCase = createClientUseCase;
        this.getClientByIdUseCase = getClientByIdUseCase;
        this.updateClientUseCase = updateClientUseCase;
        this.deleteClientUseCase = deleteClientUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<Page<ClientResponse>> index(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 15;
        
        Page<Client> clientsPage = listClientsUseCase.execute(
                companyId, isActive, search, sortBy, sortOrder, pageNumber, pageSize);
        Page<ClientResponse> responsePage = clientsPage.map(mapper::toResponse);
        
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping
    public ResponseEntity<ClientResponse> store(@Valid @RequestBody ClientRequest request) {
        Client client = createClientUseCase.execute(
                request.companyId(),
                request.name(),
                request.phone(),
                request.email(),
                request.cpf(),
                request.observations(),
                request.isActive()
        );
        
        ClientResponse response = mapper.toResponse(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> show(@PathVariable UUID id) {
        Client client = getClientByIdUseCase.execute(id);
        ClientResponse response = mapper.toResponse(client);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ClientRequest request) {
        
        Client client = updateClientUseCase.execute(
                id,
                request.name(),
                request.phone(),
                request.email(),
                request.cpf(),
                request.observations(),
                request.isActive()
        );
        
        ClientResponse response = mapper.toResponse(client);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> destroy(@PathVariable UUID id) {
        deleteClientUseCase.execute(id);
        return ResponseEntity.ok(Map.of("message", "Cliente excluído com sucesso."));
    }
}
