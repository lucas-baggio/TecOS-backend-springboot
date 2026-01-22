package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.equipment.*;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.presentation.dto.equipment.EquipmentRequest;
import br.com.baggiotech.tecos_api.presentation.dto.equipment.EquipmentResponse;
import br.com.baggiotech.tecos_api.presentation.mapper.equipment.EquipmentMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentController {

    private final ListEquipmentsUseCase listEquipmentsUseCase;
    private final CreateEquipmentUseCase createEquipmentUseCase;
    private final GetEquipmentByIdUseCase getEquipmentByIdUseCase;
    private final UpdateEquipmentUseCase updateEquipmentUseCase;
    private final DeleteEquipmentUseCase deleteEquipmentUseCase;
    private final EquipmentMapper mapper;

    public EquipmentController(
            ListEquipmentsUseCase listEquipmentsUseCase,
            CreateEquipmentUseCase createEquipmentUseCase,
            GetEquipmentByIdUseCase getEquipmentByIdUseCase,
            UpdateEquipmentUseCase updateEquipmentUseCase,
            DeleteEquipmentUseCase deleteEquipmentUseCase,
            EquipmentMapper mapper) {
        this.listEquipmentsUseCase = listEquipmentsUseCase;
        this.createEquipmentUseCase = createEquipmentUseCase;
        this.getEquipmentByIdUseCase = getEquipmentByIdUseCase;
        this.updateEquipmentUseCase = updateEquipmentUseCase;
        this.deleteEquipmentUseCase = deleteEquipmentUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<Page<EquipmentResponse>> index(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "type") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 15;
        
        Page<Equipment> equipmentsPage = listEquipmentsUseCase.execute(
                companyId, clientId, search, sortBy, sortOrder, pageNumber, pageSize);
        Page<EquipmentResponse> responsePage = equipmentsPage.map(mapper::toResponse);
        
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping
    public ResponseEntity<EquipmentResponse> store(@Valid @RequestBody EquipmentRequest request) {
        Equipment equipment = createEquipmentUseCase.execute(
                request.companyId(),
                request.clientId(),
                request.type(),
                request.brand(),
                request.model(),
                request.serialNumber(),
                request.observations()
        );
        
        EquipmentResponse response = mapper.toResponse(equipment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> show(@PathVariable UUID id) {
        Equipment equipment = getEquipmentByIdUseCase.execute(id);
        EquipmentResponse response = mapper.toResponse(equipment);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody EquipmentRequest request) {
        
        Equipment equipment = updateEquipmentUseCase.execute(
                id,
                request.companyId(),
                request.clientId(),
                request.type(),
                request.brand(),
                request.model(),
                request.serialNumber(),
                request.observations()
        );
        
        EquipmentResponse response = mapper.toResponse(equipment);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> destroy(@PathVariable UUID id) {
        deleteEquipmentUseCase.execute(id);
        return ResponseEntity.ok(Map.of("message", "Equipamento excluído com sucesso."));
    }
}
