package br.com.baggiotech.tecos_api.domain.budget;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Budget {
    private UUID id;
    private Company company;
    private WorkOrder workOrder;
    private BigDecimal serviceValue;
    private BigDecimal partsValue;
    private BigDecimal totalValue;
    private BudgetStatus status;
    private String rejectionReason;
    private User createdBy;
    private LocalDateTime approvedAt;
    private String approvalMethod;
    private User approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Budget() {
    }

    public Budget(UUID id, Company company, WorkOrder workOrder, BigDecimal serviceValue,
                 BigDecimal partsValue, BigDecimal totalValue, BudgetStatus status,
                 String rejectionReason, User createdBy, LocalDateTime approvedAt,
                 String approvalMethod, User approvedBy, LocalDateTime createdAt,
                 LocalDateTime updatedAt) {
        this.id = id;
        this.company = company;
        this.workOrder = workOrder;
        this.serviceValue = serviceValue;
        this.partsValue = partsValue;
        this.totalValue = totalValue;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdBy = createdBy;
        this.approvedAt = approvedAt;
        this.approvalMethod = approvalMethod;
        this.approvedBy = approvedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public BigDecimal getServiceValue() {
        return serviceValue;
    }

    public void setServiceValue(BigDecimal serviceValue) {
        this.serviceValue = serviceValue;
    }

    public BigDecimal getPartsValue() {
        return partsValue;
    }

    public void setPartsValue(BigDecimal partsValue) {
        this.partsValue = partsValue;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BudgetStatus getStatus() {
        return status;
    }

    public void setStatus(BudgetStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getApprovalMethod() {
        return approvalMethod;
    }

    public void setApprovalMethod(String approvalMethod) {
        this.approvalMethod = approvalMethod;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
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
