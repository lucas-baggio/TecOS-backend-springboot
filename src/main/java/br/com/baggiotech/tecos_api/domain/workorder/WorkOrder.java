package br.com.baggiotech.tecos_api.domain.workorder;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class WorkOrder {
    private UUID id;
    private Company company;
    private Client client;
    private Equipment equipment;
    private User technician;
    private OrderStatus status;
    private String reportedDefect;
    private String internalObservations;
    private Boolean returnOrder;
    private UUID originWorkOrderId;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public WorkOrder() {
    }

    public WorkOrder(UUID id, Company company, Client client, Equipment equipment, User technician,
                    OrderStatus status, String reportedDefect, String internalObservations,
                    Boolean returnOrder, UUID originWorkOrderId, LocalDateTime deliveredAt,
                    LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.company = company;
        this.client = client;
        this.equipment = equipment;
        this.technician = technician;
        this.status = status;
        this.reportedDefect = reportedDefect;
        this.internalObservations = internalObservations;
        this.returnOrder = returnOrder;
        this.originWorkOrderId = originWorkOrderId;
        this.deliveredAt = deliveredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void transitionTo(OrderStatus newStatus) {
        if (!OrderStatus.isStatusTransitionAllowed(this.status, newStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Transição de status de %s para %s não é permitida.", this.status, newStatus)
            );
        }
        this.status = newStatus;
        
        if (newStatus == OrderStatus.ENTREGUE) {
            this.deliveredAt = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public User getTechnician() {
        return technician;
    }

    public void setTechnician(User technician) {
        this.technician = technician;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getReportedDefect() {
        return reportedDefect;
    }

    public void setReportedDefect(String reportedDefect) {
        this.reportedDefect = reportedDefect;
    }

    public String getInternalObservations() {
        return internalObservations;
    }

    public void setInternalObservations(String internalObservations) {
        this.internalObservations = internalObservations;
    }

    public Boolean getReturnOrder() {
        return returnOrder;
    }

    public void setReturnOrder(Boolean returnOrder) {
        this.returnOrder = returnOrder;
    }

    public UUID getOriginWorkOrderId() {
        return originWorkOrderId;
    }

    public void setOriginWorkOrderId(UUID originWorkOrderId) {
        this.originWorkOrderId = originWorkOrderId;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
