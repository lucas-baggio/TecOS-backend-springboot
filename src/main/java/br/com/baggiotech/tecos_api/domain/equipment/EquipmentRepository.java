package br.com.baggiotech.tecos_api.domain.equipment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentRepository {
    Equipment save(Equipment equipment);
    Optional<Equipment> findById(UUID id);
    boolean existsById(UUID id);
    void delete(Equipment equipment);
    List<Equipment> findAll();
    List<Equipment> findByCompanyId(UUID companyId);
    List<Equipment> findByClientId(UUID clientId);
    List<Equipment> findByCompanyIdAndClientId(UUID companyId, UUID clientId);
    List<Equipment> searchByTypeOrBrandOrModelOrSerialNumber(String search);
    List<Equipment> findByCompanyIdAndSearch(UUID companyId, String search);
    List<Equipment> findByClientIdAndSearch(UUID clientId, String search);
    long countByEquipmentId(UUID equipmentId);
}
