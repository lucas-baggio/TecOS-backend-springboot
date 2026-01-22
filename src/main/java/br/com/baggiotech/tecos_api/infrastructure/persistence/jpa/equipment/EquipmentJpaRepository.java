package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.equipment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EquipmentJpaRepository extends JpaRepository<EquipmentJpaEntity, UUID> {
    List<EquipmentJpaEntity> findByCompanyId(UUID companyId);
    List<EquipmentJpaEntity> findByClientId(UUID clientId);
    
    @Query("SELECT e FROM EquipmentJpaEntity e WHERE e.company.id = :companyId AND e.client.id = :clientId")
    List<EquipmentJpaEntity> findByCompanyIdAndClientId(@Param("companyId") UUID companyId, @Param("clientId") UUID clientId);
    
    @Query("SELECT e FROM EquipmentJpaEntity e WHERE " +
           "LOWER(e.type) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.serialNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<EquipmentJpaEntity> searchByTypeOrBrandOrModelOrSerialNumber(@Param("search") String search);
    
    @Query("SELECT e FROM EquipmentJpaEntity e WHERE e.company.id = :companyId AND " +
           "(LOWER(e.type) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.serialNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<EquipmentJpaEntity> findByCompanyIdAndSearch(@Param("companyId") UUID companyId, @Param("search") String search);
    
    @Query("SELECT e FROM EquipmentJpaEntity e WHERE e.client.id = :clientId AND " +
           "(LOWER(e.type) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.serialNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<EquipmentJpaEntity> findByClientIdAndSearch(@Param("clientId") UUID clientId, @Param("search") String search);
    
    @Query("SELECT COUNT(w) FROM WorkOrderJpaEntity w WHERE w.equipment.id = :equipmentId")
    long countByEquipmentId(@Param("equipmentId") UUID equipmentId);
}
