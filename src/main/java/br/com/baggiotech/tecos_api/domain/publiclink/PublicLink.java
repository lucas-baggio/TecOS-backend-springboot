package br.com.baggiotech.tecos_api.domain.publiclink;

import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;

import java.time.LocalDateTime;
import java.util.UUID;

public class PublicLink {
    private UUID id;
    private WorkOrder workOrder;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PublicLink() {
    }

    public PublicLink(UUID id, WorkOrder workOrder, String token, LocalDateTime createdAt,
                     LocalDateTime updatedAt) {
        this.id = id;
        this.workOrder = workOrder;
        this.token = token;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
