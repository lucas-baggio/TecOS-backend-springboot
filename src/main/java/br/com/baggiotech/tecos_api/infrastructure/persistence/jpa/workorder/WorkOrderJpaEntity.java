package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder;

import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.equipment.EquipmentJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SoftDelete
public class WorkOrderJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyJpaEntity company;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientJpaEntity client;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentJpaEntity equipment;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "technician_id", nullable = false)
    private UserJpaEntity technician;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private br.com.baggiotech.tecos_api.domain.workorder.OrderStatus status;
    
    @Column(name = "reported_defect", nullable = false, columnDefinition = "TEXT")
    private String reportedDefect;
    
    @Column(name = "internal_observations", columnDefinition = "TEXT")
    private String internalObservations;
    
    @Column(name = "return", nullable = false)
    private Boolean returnOrder = false;
    
    @Column(name = "origin_work_order_id")
    private UUID originWorkOrderId;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
