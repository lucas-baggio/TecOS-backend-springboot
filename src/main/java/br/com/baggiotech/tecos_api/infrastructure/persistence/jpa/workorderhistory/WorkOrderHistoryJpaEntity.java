package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorderhistory;

import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder.WorkOrderJpaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "work_order_histories")
@FilterDef(name = "companyThroughWorkOrderFilter", parameters = @ParamDef(name = "companyId", type = java.util.UUID.class))
@Filter(name = "companyThroughWorkOrderFilter", condition = "EXISTS (SELECT 1 FROM work_orders wo WHERE wo.id = work_order_id AND wo.company_id = :companyId)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderHistoryJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrderJpaEntity workOrder;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;
    
    @Column(name = "status_before")
    @Enumerated(EnumType.STRING)
    private br.com.baggiotech.tecos_api.domain.workorder.OrderStatus statusBefore;
    
    @Column(name = "status_after", nullable = false)
    @Enumerated(EnumType.STRING)
    private br.com.baggiotech.tecos_api.domain.workorder.OrderStatus statusAfter;
    
    @Column(columnDefinition = "TEXT")
    private String observation;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
