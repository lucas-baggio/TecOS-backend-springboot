package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.budget;

import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder.WorkOrderJpaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyJpaEntity company;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrderJpaEntity workOrder;
    
    @Column(name = "service_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceValue;
    
    @Column(name = "parts_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal partsValue;
    
    @Column(name = "total_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalValue;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private br.com.baggiotech.tecos_api.domain.budget.BudgetStatus status;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private UserJpaEntity createdBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approval_method")
    private String approvalMethod;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by_user_id")
    private UserJpaEntity approvedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
