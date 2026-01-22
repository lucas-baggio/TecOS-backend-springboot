package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.publiclink;

import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder.WorkOrderJpaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "public_links", uniqueConstraints = {
    @UniqueConstraint(columnNames = "token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicLinkJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrderJpaEntity workOrder;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
