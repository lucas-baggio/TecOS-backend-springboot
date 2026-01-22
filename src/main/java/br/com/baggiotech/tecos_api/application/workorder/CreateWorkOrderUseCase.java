package br.com.baggiotech.tecos_api.application.workorder;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.equipment.EquipmentRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateWorkOrderUseCase {

    private final WorkOrderRepository repository;
    private final ClientRepository clientRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final WorkOrderHistoryRepository workOrderHistoryRepository;
    private final CustomMetrics metrics;

    public CreateWorkOrderUseCase(WorkOrderRepository repository, ClientRepository clientRepository,
                                  EquipmentRepository equipmentRepository, UserRepository userRepository,
                                  WorkOrderHistoryRepository workOrderHistoryRepository,
                                  CustomMetrics metrics) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
        this.workOrderHistoryRepository = workOrderHistoryRepository;
        this.metrics = metrics;
    }

    public WorkOrder execute(UUID companyId, UUID clientId, UUID equipmentId, UUID technicianId,
                            String reportedDefect, String internalObservations, Boolean returnOrder,
                            UUID originWorkOrderId, UUID createdByUserId) {
        Timer.Sample sample = metrics.startTimer();
        try {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("Client", clientId));

            Equipment equipment = equipmentRepository.findById(equipmentId)
                    .orElseThrow(() -> new EntityNotFoundException("Equipment", equipmentId));

            User technician = userRepository.findById(technicianId)
                    .orElseThrow(() -> new EntityNotFoundException("User", technicianId));

            // Verificar se client, equipment e technician pertencem à mesma company
            if (client.getCompany() == null || !client.getCompany().getId().equals(companyId)) {
                throw new IllegalArgumentException("O cliente selecionado não pertence à empresa especificada.");
            }

            if (equipment.getCompany() == null || !equipment.getCompany().getId().equals(companyId)) {
                throw new IllegalArgumentException("O equipamento selecionado não pertence à empresa especificada.");
            }

            if (technician.getCompany() == null || !technician.getCompany().getId().equals(companyId)) {
                throw new IllegalArgumentException("O técnico selecionado não pertence à empresa especificada.");
            }

            // Verificar se equipment pertence ao client
            if (equipment.getClient() == null || !equipment.getClient().getId().equals(clientId)) {
                throw new IllegalArgumentException("O equipamento selecionado não pertence ao cliente especificado.");
            }

            // Verificar se technician é TECNICO
            if (!"TECNICO".equals(technician.getType())) {
                throw new IllegalArgumentException("O usuário selecionado não é um técnico.");
            }

            // Lógica de RETORNO (RB-08)
            boolean isReturn = false;
            if (originWorkOrderId != null) {
                WorkOrder originWorkOrder = repository.findById(originWorkOrderId)
                        .orElseThrow(() -> new EntityNotFoundException("WorkOrder", originWorkOrderId));

                // Verificar se origem pertence à mesma company
                if (originWorkOrder.getCompany() == null || !originWorkOrder.getCompany().getId().equals(companyId)) {
                    throw new IllegalArgumentException("A ordem de serviço de origem não pertence à empresa especificada.");
                }

                // Verificar se origem está ENTREGUE
                if (originWorkOrder.getStatus() != OrderStatus.ENTREGUE) {
                    throw new IllegalArgumentException("A ordem de serviço de origem deve estar com status ENTREGUE.");
                }

                if (originWorkOrder.getDeliveredAt() == null) {
                    throw new IllegalArgumentException("A ordem de serviço de origem não possui data de entrega.");
                }

                // Verificar garantia (30 dias)
                int warrantyDays = 30; // TODO: Pode vir de configuração
                LocalDateTime warrantyEndDate = originWorkOrder.getDeliveredAt().plusDays(warrantyDays);
                
                if (LocalDateTime.now().isAfter(warrantyEndDate)) {
                    throw new IllegalArgumentException("A garantia da ordem de serviço original expirou.");
                }

                isReturn = true;
            }

            Company company = client.getCompany();

            WorkOrder workOrder = new WorkOrder();
            workOrder.setId(UUID.randomUUID());
            workOrder.setCompany(company);
            workOrder.setClient(client);
            workOrder.setEquipment(equipment);
            workOrder.setTechnician(technician);
            workOrder.setStatus(OrderStatus.RECEBIDO);
            workOrder.setReportedDefect(reportedDefect);
            workOrder.setInternalObservations(internalObservations);
            workOrder.setReturnOrder(isReturn);
            workOrder.setOriginWorkOrderId(originWorkOrderId);
            workOrder.setDeliveredAt(null);
            workOrder.setCreatedAt(LocalDateTime.now());
            workOrder.setUpdatedAt(LocalDateTime.now());
            workOrder.setDeletedAt(null);

            WorkOrder saved = repository.save(workOrder);
            
            // Criar histórico inicial (RB-03)
            if (createdByUserId != null) {
                br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory history = 
                    new br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory();
                history.setId(UUID.randomUUID());
                history.setWorkOrder(saved);
                User createdBy = userRepository.findById(createdByUserId)
                        .orElseThrow(() -> new EntityNotFoundException("User", createdByUserId));
                history.setUser(createdBy);
                history.setStatusBefore(null);
                history.setStatusAfter(OrderStatus.RECEBIDO);
                history.setObservation("Ordem de serviço criada");
                history.setCreatedAt(LocalDateTime.now());
                history.setUpdatedAt(LocalDateTime.now());
                workOrderHistoryRepository.save(history);
            }
            
            metrics.incrementWorkOrdersCreated();
            return saved;
        } finally {
            metrics.recordTimer(sample, "create");
        }
    }
}
