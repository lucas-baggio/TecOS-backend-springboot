package br.com.baggiotech.tecos_api.application.workorder;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.equipment.EquipmentRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateWorkOrderUseCase {

    private final WorkOrderRepository repository;
    private final ClientRepository clientRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final CustomMetrics metrics;

    public UpdateWorkOrderUseCase(WorkOrderRepository repository, ClientRepository clientRepository,
                                  EquipmentRepository equipmentRepository, UserRepository userRepository,
                                  CustomMetrics metrics) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
        this.metrics = metrics;
    }

    public WorkOrder execute(UUID id, UUID companyId, UUID clientId, UUID equipmentId, UUID technicianId,
                            String reportedDefect, String internalObservations, Boolean returnOrder,
                            UUID originWorkOrderId) {
        Timer.Sample sample = metrics.startTimer();
        try {
            WorkOrder workOrder = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("WorkOrder", id));

            // RB-03: Não pode editar OS CANCELADA ou ENTREGUE
            if (workOrder.getStatus() == OrderStatus.CANCELADO || workOrder.getStatus() == OrderStatus.ENTREGUE) {
                throw new IllegalArgumentException(
                        "Não é possível editar uma ordem de serviço com status " + workOrder.getStatus() + "."
                );
            }

            // Se client_id foi alterado, verificar se pertence à mesma company
            if (clientId != null && !clientId.equals(workOrder.getClient().getId())) {
                Client client = clientRepository.findById(clientId)
                        .orElseThrow(() -> new EntityNotFoundException("Client", clientId));
                
                if (client.getCompany() == null || !client.getCompany().getId().equals(companyId)) {
                    throw new IllegalArgumentException("O cliente selecionado não pertence à empresa especificada.");
                }
                workOrder.setClient(client);
            }

            // Se equipment_id foi alterado, verificar se pertence à mesma company e ao client
            if (equipmentId != null && !equipmentId.equals(workOrder.getEquipment().getId())) {
                Equipment equipment = equipmentRepository.findById(equipmentId)
                        .orElseThrow(() -> new EntityNotFoundException("Equipment", equipmentId));
                
                if (equipment.getCompany() == null || !equipment.getCompany().getId().equals(companyId)) {
                    throw new IllegalArgumentException("O equipamento selecionado não pertence à empresa especificada.");
                }
                
                if (equipment.getClient() == null || !equipment.getClient().getId().equals(workOrder.getClient().getId())) {
                    throw new IllegalArgumentException("O equipamento selecionado não pertence ao cliente especificado.");
                }
                workOrder.setEquipment(equipment);
            }

            // Se technician_id foi alterado, verificar se pertence à mesma company
            if (technicianId != null && !technicianId.equals(workOrder.getTechnician().getId())) {
                User technician = userRepository.findById(technicianId)
                        .orElseThrow(() -> new EntityNotFoundException("User", technicianId));
                
                if (technician.getCompany() == null || !technician.getCompany().getId().equals(companyId)) {
                    throw new IllegalArgumentException("O técnico selecionado não pertence à empresa especificada.");
                }
                workOrder.setTechnician(technician);
            }

            if (reportedDefect != null) {
                workOrder.setReportedDefect(reportedDefect);
            }
            if (internalObservations != null) {
                workOrder.setInternalObservations(internalObservations);
            }
            if (returnOrder != null) {
                workOrder.setReturnOrder(returnOrder);
            }
            if (originWorkOrderId != null) {
                workOrder.setOriginWorkOrderId(originWorkOrderId);
            }
            workOrder.setUpdatedAt(LocalDateTime.now());

            WorkOrder updated = repository.save(workOrder);
            metrics.incrementWorkOrdersUpdated();
            return updated;
        } finally {
            metrics.recordTimer(sample, "update");
        }
    }
}
