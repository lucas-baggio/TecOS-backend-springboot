package br.com.baggiotech.tecos_api.application.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ListClientsUseCase {

    private final ClientRepository repository;

    public ListClientsUseCase(ClientRepository repository) {
        this.repository = repository;
    }

    public Page<Client> execute(UUID companyId, Boolean isActive, String search, 
                                String sortBy, String sortOrder, int page, int size) {
        List<Client> clients;

        if (companyId != null && isActive != null && search != null && !search.isBlank()) {
            List<Client> filteredByCompanyAndActive = repository.findByCompanyIdAndIsActive(companyId, isActive);
            List<Client> searched = repository.findByCompanyIdAndSearch(companyId, search);
            var searchedIds = searched.stream()
                    .map(Client::getId)
                    .collect(Collectors.toSet());
            clients = new ArrayList<>(filteredByCompanyAndActive.stream()
                    .filter(c -> searchedIds.contains(c.getId()))
                    .collect(Collectors.toList()));
        } else if (companyId != null && isActive != null) {
            clients = new ArrayList<>(repository.findByCompanyIdAndIsActive(companyId, isActive));
        } else if (companyId != null && search != null && !search.isBlank()) {
            clients = new ArrayList<>(repository.findByCompanyIdAndSearch(companyId, search));
        } else if (companyId != null) {
            clients = new ArrayList<>(repository.findByCompanyId(companyId));
        } else if (isActive != null && search != null && !search.isBlank()) {
            List<Client> filteredByActive = repository.findByIsActive(isActive);
            List<Client> searched = repository.searchByNameOrEmailOrCpfOrPhone(search);
            var searchedIds = searched.stream()
                    .map(Client::getId)
                    .collect(Collectors.toSet());
            clients = new ArrayList<>(filteredByActive.stream()
                    .filter(c -> searchedIds.contains(c.getId()))
                    .collect(Collectors.toList()));
        } else if (isActive != null) {
            clients = new ArrayList<>(repository.findByIsActive(isActive));
        } else if (search != null && !search.isBlank()) {
            clients = new ArrayList<>(repository.searchByNameOrEmailOrCpfOrPhone(search));
        } else {
            clients = new ArrayList<>(repository.findAll());
        }

        String sortField = sortBy != null ? sortBy : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        clients.sort((c1, c2) -> {
            int result = 0;
            switch (sortField.toLowerCase()) {
                case "name":
                    result = c1.getName().compareToIgnoreCase(c2.getName());
                    break;
                case "phone":
                    result = (c1.getPhone() != null ? c1.getPhone() : "").compareToIgnoreCase(c2.getPhone() != null ? c2.getPhone() : "");
                    break;
                case "email":
                    result = (c1.getEmail() != null ? c1.getEmail() : "").compareToIgnoreCase(c2.getEmail() != null ? c2.getEmail() : "");
                    break;
                case "cpf":
                    result = (c1.getCpf() != null ? c1.getCpf() : "").compareToIgnoreCase(c2.getCpf() != null ? c2.getCpf() : "");
                    break;
                default:
                    result = c1.getName().compareToIgnoreCase(c2.getName());
            }
            return direction == Sort.Direction.ASC ? result : -result;
        });

        int start = page * size;
        int end = Math.min(start + size, clients.size());
        List<Client> pagedClients = start < clients.size() 
                ? clients.subList(start, end) 
                : List.of();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return new PageImpl<>(pagedClients, pageable, clients.size());
    }
}
