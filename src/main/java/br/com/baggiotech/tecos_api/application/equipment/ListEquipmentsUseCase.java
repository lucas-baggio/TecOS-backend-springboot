package br.com.baggiotech.tecos_api.application.equipment;

import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.equipment.EquipmentRepository;
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
public class ListEquipmentsUseCase {

    private final EquipmentRepository repository;

    public ListEquipmentsUseCase(EquipmentRepository repository) {
        this.repository = repository;
    }

    public Page<Equipment> execute(UUID companyId, UUID clientId, String search, 
                                   String sortBy, String sortOrder, int page, int size) {
        List<Equipment> equipments;

        if (companyId != null && clientId != null && search != null && !search.isBlank()) {
            List<Equipment> filteredByCompanyAndClient = repository.findByCompanyIdAndClientId(companyId, clientId);
            List<Equipment> searched = repository.findByClientIdAndSearch(clientId, search);
            var searchedIds = searched.stream()
                    .map(Equipment::getId)
                    .collect(Collectors.toSet());
            equipments = new ArrayList<>(filteredByCompanyAndClient.stream()
                    .filter(e -> searchedIds.contains(e.getId()))
                    .collect(Collectors.toList()));
        } else if (companyId != null && clientId != null) {
            equipments = new ArrayList<>(repository.findByCompanyIdAndClientId(companyId, clientId));
        } else if (clientId != null && search != null && !search.isBlank()) {
            equipments = new ArrayList<>(repository.findByClientIdAndSearch(clientId, search));
        } else if (clientId != null) {
            equipments = new ArrayList<>(repository.findByClientId(clientId));
        } else if (companyId != null && search != null && !search.isBlank()) {
            equipments = new ArrayList<>(repository.findByCompanyIdAndSearch(companyId, search));
        } else if (companyId != null) {
            equipments = new ArrayList<>(repository.findByCompanyId(companyId));
        } else if (search != null && !search.isBlank()) {
            equipments = new ArrayList<>(repository.searchByTypeOrBrandOrModelOrSerialNumber(search));
        } else {
            equipments = new ArrayList<>(repository.findAll());
        }

        String sortField = sortBy != null ? sortBy : "type";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        equipments.sort((e1, e2) -> {
            int result = 0;
            switch (sortField.toLowerCase()) {
                case "type":
                    result = (e1.getType() != null ? e1.getType() : "").compareToIgnoreCase(e2.getType() != null ? e2.getType() : "");
                    break;
                case "brand":
                    result = (e1.getBrand() != null ? e1.getBrand() : "").compareToIgnoreCase(e2.getBrand() != null ? e2.getBrand() : "");
                    break;
                case "model":
                    result = (e1.getModel() != null ? e1.getModel() : "").compareToIgnoreCase(e2.getModel() != null ? e2.getModel() : "");
                    break;
                case "serial_number":
                    result = (e1.getSerialNumber() != null ? e1.getSerialNumber() : "").compareToIgnoreCase(e2.getSerialNumber() != null ? e2.getSerialNumber() : "");
                    break;
                default:
                    result = (e1.getType() != null ? e1.getType() : "").compareToIgnoreCase(e2.getType() != null ? e2.getType() : "");
            }
            return direction == Sort.Direction.ASC ? result : -result;
        });

        int start = page * size;
        int end = Math.min(start + size, equipments.size());
        List<Equipment> pagedEquipments = start < equipments.size() 
                ? equipments.subList(start, end) 
                : List.of();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return new PageImpl<>(pagedEquipments, pageable, equipments.size());
    }
}
