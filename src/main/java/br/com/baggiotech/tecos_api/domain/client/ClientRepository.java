package br.com.baggiotech.tecos_api.domain.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(UUID id);
    boolean existsById(UUID id);
    void delete(Client client);
    List<Client> findAll();
    List<Client> findByCompanyId(UUID companyId);
    List<Client> findByIsActive(Boolean isActive);
    List<Client> findByCompanyIdAndIsActive(UUID companyId, Boolean isActive);
    List<Client> searchByNameOrEmailOrCpfOrPhone(String search);
    List<Client> findByCompanyIdAndSearch(UUID companyId, String search);
}
