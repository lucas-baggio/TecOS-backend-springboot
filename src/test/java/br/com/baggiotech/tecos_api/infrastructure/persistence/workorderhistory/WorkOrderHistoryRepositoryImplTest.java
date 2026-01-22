package br.com.baggiotech.tecos_api.infrastructure.persistence.workorderhistory;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.equipment.EquipmentJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.equipment.EquipmentJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder.WorkOrderJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder.WorkOrderJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorderhistory.WorkOrderHistoryJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({WorkOrderHistoryRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.company.CompanyRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.workorder.WorkOrderRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.user.UserRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.client.ClientRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.equipment.EquipmentRepositoryImpl.class})
@DisplayName("WorkOrderHistoryRepositoryImpl Integration Tests")
class WorkOrderHistoryRepositoryImplTest {

    @Autowired
    private WorkOrderHistoryJpaRepository workOrderHistoryJpaRepository;

    @Autowired
    private CompanyJpaRepository companyJpaRepository;

    @Autowired
    private ClientJpaRepository clientJpaRepository;

    @Autowired
    private EquipmentJpaRepository equipmentJpaRepository;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private WorkOrderHistoryRepository repository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    private Company createCompany(String name, String email) {
        CompanyJpaEntity companyEntity = new CompanyJpaEntity();
        companyEntity.setName(name);
        companyEntity.setEmail(email);
        companyEntity.setIsActive(true);
        companyEntity.setCreatedAt(LocalDateTime.now());
        companyEntity.setUpdatedAt(LocalDateTime.now());
        CompanyJpaEntity saved = companyJpaRepository.save(companyEntity);
        
        Company company = new Company();
        company.setId(saved.getId());
        company.setName(saved.getName());
        company.setEmail(saved.getEmail());
        company.setIsActive(saved.getIsActive());
        company.setCreatedAt(saved.getCreatedAt());
        company.setUpdatedAt(saved.getUpdatedAt());
        return company;
    }

    private User createUser(Company company, String name, String email) {
        UserJpaEntity userEntity = new UserJpaEntity();
        userEntity.setCompany(companyJpaRepository.findById(company.getId()).orElseThrow());
        userEntity.setName(name);
        userEntity.setEmail(email);
        userEntity.setPassword("password");
        userEntity.setType("TECNICO");
        userEntity.setIsActive(true);
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());
        UserJpaEntity saved = userJpaRepository.save(userEntity);
        
        User user = new User();
        user.setId(saved.getId());
        user.setCompany(company);
        user.setName(saved.getName());
        user.setEmail(saved.getEmail());
        user.setType(saved.getType());
        user.setIsActive(saved.getIsActive());
        user.setCreatedAt(saved.getCreatedAt());
        user.setUpdatedAt(saved.getUpdatedAt());
        return user;
    }

    private br.com.baggiotech.tecos_api.domain.client.Client createClient(Company company, String name, String phone) {
        ClientJpaEntity clientEntity = new ClientJpaEntity();
        clientEntity.setCompany(companyJpaRepository.findById(company.getId()).orElseThrow());
        clientEntity.setName(name);
        clientEntity.setPhone(phone);
        clientEntity.setIsActive(true);
        clientEntity.setCreatedAt(LocalDateTime.now());
        clientEntity.setUpdatedAt(LocalDateTime.now());
        ClientJpaEntity saved = clientJpaRepository.save(clientEntity);
        
        br.com.baggiotech.tecos_api.domain.client.Client client = new br.com.baggiotech.tecos_api.domain.client.Client();
        client.setId(saved.getId());
        client.setCompany(company);
        client.setName(saved.getName());
        client.setPhone(saved.getPhone());
        client.setIsActive(saved.getIsActive());
        client.setCreatedAt(saved.getCreatedAt());
        client.setUpdatedAt(saved.getUpdatedAt());
        return client;
    }

    private br.com.baggiotech.tecos_api.domain.equipment.Equipment createEquipment(Company company, br.com.baggiotech.tecos_api.domain.client.Client client, String type) {
        EquipmentJpaEntity equipmentEntity = new EquipmentJpaEntity();
        equipmentEntity.setCompany(companyJpaRepository.findById(company.getId()).orElseThrow());
        equipmentEntity.setClient(clientJpaRepository.findById(client.getId()).orElseThrow());
        equipmentEntity.setType(type);
        equipmentEntity.setCreatedAt(LocalDateTime.now());
        equipmentEntity.setUpdatedAt(LocalDateTime.now());
        EquipmentJpaEntity saved = equipmentJpaRepository.save(equipmentEntity);
        
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = new br.com.baggiotech.tecos_api.domain.equipment.Equipment();
        equipment.setId(saved.getId());
        equipment.setCompany(company);
        equipment.setClient(client);
        equipment.setType(saved.getType());
        equipment.setCreatedAt(saved.getCreatedAt());
        equipment.setUpdatedAt(saved.getUpdatedAt());
        return equipment;
    }

    private WorkOrder createWorkOrder(Company company, br.com.baggiotech.tecos_api.domain.client.Client client,
                                     br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment, User technician) {
        WorkOrderJpaEntity workOrderEntity = new WorkOrderJpaEntity();
        workOrderEntity.setCompany(companyJpaRepository.findById(company.getId()).orElseThrow());
        workOrderEntity.setClient(clientJpaRepository.findById(client.getId()).orElseThrow());
        workOrderEntity.setEquipment(equipmentJpaRepository.findById(equipment.getId()).orElseThrow());
        workOrderEntity.setTechnician(userJpaRepository.findById(technician.getId()).orElseThrow());
        workOrderEntity.setStatus(OrderStatus.RECEBIDO);
        workOrderEntity.setReportedDefect("Test defect");
        workOrderEntity.setReturnOrder(false);
        workOrderEntity.setCreatedAt(LocalDateTime.now());
        workOrderEntity.setUpdatedAt(LocalDateTime.now());
        WorkOrderJpaEntity saved = workOrderJpaRepository.save(workOrderEntity);
        
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(saved.getId());
        workOrder.setCompany(company);
        workOrder.setStatus(OrderStatus.RECEBIDO);
        return workOrder;
    }

    private WorkOrderHistory createHistory(WorkOrder workOrder, User user, OrderStatus statusBefore,
                                          OrderStatus statusAfter, String observation) {
        WorkOrderHistory history = new WorkOrderHistory();
        history.setWorkOrder(workOrder);
        history.setUser(user);
        history.setStatusBefore(statusBefore);
        history.setStatusAfter(statusAfter);
        history.setObservation(observation);
        history.setCreatedAt(LocalDateTime.now());
        history.setUpdatedAt(LocalDateTime.now());
        return repository.save(history);
    }

    @Test
    @DisplayName("Deve salvar histórico com sucesso")
    void shouldSaveHistorySuccessfully() {
        Company company = createCompany("Test Company", "company@example.com");
        User user = createUser(company, "Test User", "user@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        
        WorkOrderHistory newHistory = new WorkOrderHistory();
        newHistory.setWorkOrder(workOrder);
        newHistory.setUser(user);
        newHistory.setStatusBefore(null);
        newHistory.setStatusAfter(OrderStatus.RECEBIDO);
        newHistory.setObservation("Ordem de serviço criada");
        newHistory.setCreatedAt(LocalDateTime.now());
        newHistory.setUpdatedAt(LocalDateTime.now());
        
        WorkOrderHistory saved = repository.save(newHistory);
        
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatusAfter()).isEqualTo(OrderStatus.RECEBIDO);
        assertThat(saved.getObservation()).isEqualTo("Ordem de serviço criada");
        assertThat(workOrderHistoryJpaRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("Deve buscar histórico por ID")
    void shouldFindHistoryById() {
        Company company = createCompany("Test Company", "company@example.com");
        User user = createUser(company, "Test User", "user@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        WorkOrderHistory history1 = createHistory(workOrder, user, null, OrderStatus.RECEBIDO, "Created");
        
        Optional<WorkOrderHistory> found = repository.findById(history1.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getStatusAfter()).isEqualTo(OrderStatus.RECEBIDO);
        assertThat(found.get().getObservation()).isEqualTo("Created");
    }

    @Test
    @DisplayName("Deve retornar empty quando histórico não existe")
    void shouldReturnEmptyWhenHistoryNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        Optional<WorkOrderHistory> found = repository.findById(nonExistentId);
        
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar existência de histórico por ID")
    void shouldCheckHistoryExistsById() {
        Company company = createCompany("Test Company", "company@example.com");
        User user = createUser(company, "Test User", "user@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        WorkOrderHistory history = createHistory(workOrder, user, null, OrderStatus.RECEBIDO, "Test");
        
        assertThat(repository.existsById(history.getId())).isTrue();
        assertThat(repository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve listar todos os históricos")
    void shouldFindAllHistories() {
        Company company = createCompany("Test Company", "company@example.com");
        User user = createUser(company, "Test User", "user@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        createHistory(workOrder, user, null, OrderStatus.RECEBIDO, "Created");
        createHistory(workOrder, user, OrderStatus.RECEBIDO, OrderStatus.EM_ANALISE, "In analysis");
        
        List<WorkOrderHistory> histories = repository.findAll();
        
        assertThat(histories).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Deve buscar históricos por workOrderId")
    void shouldFindHistoriesByWorkOrderId() {
        Company company = createCompany("Test Company", "company@example.com");
        User user = createUser(company, "Test User", "user@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder1 = createWorkOrder(company, client, equipment, technician);
        WorkOrder workOrder2 = createWorkOrder(company, client, equipment, technician);
        
        createHistory(workOrder1, user, null, OrderStatus.RECEBIDO, "Created 1");
        createHistory(workOrder1, user, OrderStatus.RECEBIDO, OrderStatus.EM_ANALISE, "In analysis 1");
        createHistory(workOrder2, user, null, OrderStatus.RECEBIDO, "Created 2");
        
        List<WorkOrderHistory> histories = repository.findByWorkOrderId(workOrder1.getId());
        
        assertThat(histories).hasSize(2);
        assertThat(histories).allMatch(h -> h.getWorkOrder().getId().equals(workOrder1.getId()));
    }

    @Test
    @DisplayName("Deve buscar históricos por userId")
    void shouldFindHistoriesByUserId() {
        Company company = createCompany("Test Company", "company@example.com");
        User user1 = createUser(company, "User 1", "user1@example.com");
        User user2 = createUser(company, "User 2", "user2@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        
        createHistory(workOrder, user1, null, OrderStatus.RECEBIDO, "Created by user1");
        createHistory(workOrder, user2, OrderStatus.RECEBIDO, OrderStatus.EM_ANALISE, "Updated by user2");
        createHistory(workOrder, user1, OrderStatus.EM_ANALISE, OrderStatus.EM_CONSERTO, "Updated by user1");
        
        List<WorkOrderHistory> histories = repository.findByUserId(user1.getId());
        
        assertThat(histories).hasSize(2);
        assertThat(histories).allMatch(h -> h.getUser().getId().equals(user1.getId()));
    }

    @Test
    @DisplayName("Deve buscar históricos por workOrderId ordenados por createdAt desc")
    void shouldFindHistoriesByWorkOrderIdOrderedByCreatedAtDesc() {
        Company company = createCompany("Test Company", "company@example.com");
        User user = createUser(company, "Test User", "user@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        
        WorkOrderHistory history1 = createHistory(workOrder, user, null, OrderStatus.RECEBIDO, "First");
        try {
            Thread.sleep(10); // Pequeno delay para garantir timestamps diferentes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        WorkOrderHistory history2 = createHistory(workOrder, user, OrderStatus.RECEBIDO, OrderStatus.EM_ANALISE, "Second");
        
        List<WorkOrderHistory> histories = repository.findByWorkOrderIdOrderByCreatedAtDesc(workOrder.getId());
        
        assertThat(histories).hasSize(2);
        // O mais recente deve vir primeiro
        assertThat(histories.get(0).getId()).isEqualTo(history2.getId());
        assertThat(histories.get(1).getId()).isEqualTo(history1.getId());
    }

    @Test
    @DisplayName("Deve atualizar histórico existente")
    void shouldUpdateExistingHistory() {
        Company company = createCompany("Test Company", "company@example.com");
        User user = createUser(company, "Test User", "user@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        WorkOrderHistory history = createHistory(workOrder, user, null, OrderStatus.RECEBIDO, "Original");
        
        history.setObservation("Updated observation");
        history.setUpdatedAt(LocalDateTime.now());
        WorkOrderHistory updated = repository.save(history);
        
        assertThat(updated.getObservation()).isEqualTo("Updated observation");
    }
}
