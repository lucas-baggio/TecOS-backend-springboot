package br.com.baggiotech.tecos_api.domain.workorderhistory;

import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;

import java.time.LocalDateTime;
import java.util.UUID;

public class WorkOrderHistory {
    private UUID id;
    private WorkOrder workOrder;
    private User user;
    private OrderStatus statusBefore;
    private OrderStatus statusAfter;
    private String observation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkOrderHistory() {
    }

    public WorkOrderHistory(UUID id, WorkOrder workOrder, User user, OrderStatus statusBefore,
                           OrderStatus statusAfter, String observation, LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.id = id;
        this.workOrder = workOrder;
        this.user = user;
        this.statusBefore = statusBefore;
        this.statusAfter = statusAfter;
        this.observation = observation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OrderStatus getStatusBefore() {
        return statusBefore;
    }

    public void setStatusBefore(OrderStatus statusBefore) {
        this.statusBefore = statusBefore;
    }

    public OrderStatus getStatusAfter() {
        return statusAfter;
    }

    public void setStatusAfter(OrderStatus statusAfter) {
        this.statusAfter = statusAfter;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
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
}
