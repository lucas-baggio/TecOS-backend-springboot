package br.com.baggiotech.tecos_api.infrastructure.persistence.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.budget.BudgetJpaRepository;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({BudgetRepositoryImpl.class, 
         br.com.baggiotech.tecos_api.infrastructure.persistence.company.CompanyRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.workorder.WorkOrderRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.user.UserRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.client.ClientRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.equipment.EquipmentRepositoryImpl.class})
@DisplayName("BudgetRepositoryImpl Integration Tests")
class BudgetRepositoryImplTest {

    @Autowired
    private BudgetJpaRepository budgetJpaRepository;

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
    private BudgetRepository repository;

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

    private Budget createBudget(Company company, WorkOrder workOrder, User creator, BudgetStatus status) {
        Budget budget = new Budget();
        budget.setCompany(company);
        budget.setWorkOrder(workOrder);
        budget.setServiceValue(new BigDecimal("100.00"));
        budget.setPartsValue(new BigDecimal("50.00"));
        budget.setTotalValue(new BigDecimal("150.00"));
        budget.setStatus(status);
        budget.setCreatedBy(creator);
        budget.setCreatedAt(LocalDateTime.now());
        budget.setUpdatedAt(LocalDateTime.now());
        return repository.save(budget);
    }

    @Test
    @DisplayName("Deve salvar orçamento com sucesso")
    void shouldSaveBudgetSuccessfully() {
        Company company = createCompany("Test Company", "company@example.com");
        User creator = createUser(company, "Creator", "creator@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        
        Budget newBudget = new Budget();
        newBudget.setCompany(company);
        newBudget.setWorkOrder(workOrder);
        newBudget.setServiceValue(new BigDecimal("100.00"));
        newBudget.setPartsValue(new BigDecimal("50.00"));
        newBudget.setTotalValue(new BigDecimal("150.00"));
        newBudget.setStatus(BudgetStatus.PENDENTE);
        newBudget.setCreatedBy(creator);
        newBudget.setCreatedAt(LocalDateTime.now());
        newBudget.setUpdatedAt(LocalDateTime.now());
        
        Budget saved = repository.save(newBudget);
        
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getServiceValue()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(saved.getStatus()).isEqualTo(BudgetStatus.PENDENTE);
        assertThat(budgetJpaRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("Deve buscar orçamento por ID")
    void shouldFindBudgetById() {
        Company company = createCompany("Test Company", "company@example.com");
        User creator = createUser(company, "Creator", "creator@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        Budget budget1 = createBudget(company, workOrder, creator, BudgetStatus.PENDENTE);
        
        Optional<Budget> found = repository.findById(budget1.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(BudgetStatus.PENDENTE);
        assertThat(found.get().getTotalValue()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Deve retornar empty quando orçamento não existe")
    void shouldReturnEmptyWhenBudgetNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        Optional<Budget> found = repository.findById(nonExistentId);
        
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar existência de orçamento por ID")
    void shouldCheckBudgetExistsById() {
        Company company = createCompany("Test Company", "company@example.com");
        User creator = createUser(company, "Creator", "creator@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        Budget budget = createBudget(company, workOrder, creator, BudgetStatus.PENDENTE);
        
        assertThat(repository.existsById(budget.getId())).isTrue();
        assertThat(repository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve listar todos os orçamentos")
    void shouldFindAllBudgets() {
        Company company = createCompany("Test Company", "company@example.com");
        User creator = createUser(company, "Creator", "creator@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        createBudget(company, workOrder, creator, BudgetStatus.PENDENTE);
        createBudget(company, workOrder, creator, BudgetStatus.APROVADO);
        
        List<Budget> budgets = repository.findAll();
        
        assertThat(budgets).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Deve buscar orçamentos por companyId")
    void shouldFindBudgetsByCompanyId() {
        Company company1 = createCompany("Company 1", "company1@example.com");
        Company company2 = createCompany("Company 2", "company2@example.com");
        User creator1 = createUser(company1, "Creator 1", "creator1@example.com");
        User creator2 = createUser(company2, "Creator 2", "creator2@example.com");
        User technician1 = createUser(company1, "Technician 1", "tech1@example.com");
        User technician2 = createUser(company2, "Technician 2", "tech2@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client1 = createClient(company1, "Client 1", "11999999999");
        br.com.baggiotech.tecos_api.domain.client.Client client2 = createClient(company2, "Client 2", "11888888888");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment1 = createEquipment(company1, client1, "Notebook");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment2 = createEquipment(company2, client2, "Desktop");
        WorkOrder workOrder1 = createWorkOrder(company1, client1, equipment1, technician1);
        WorkOrder workOrder2 = createWorkOrder(company2, client2, equipment2, technician2);
        
        createBudget(company1, workOrder1, creator1, BudgetStatus.PENDENTE);
        createBudget(company1, workOrder1, creator1, BudgetStatus.APROVADO);
        createBudget(company2, workOrder2, creator2, BudgetStatus.PENDENTE);
        
        List<Budget> budgets = repository.findByCompanyId(company1.getId());
        
        assertThat(budgets).hasSize(2);
        assertThat(budgets).allMatch(b -> b.getCompany().getId().equals(company1.getId()));
    }

    @Test
    @DisplayName("Deve buscar orçamentos por workOrderId")
    void shouldFindBudgetsByWorkOrderId() {
        Company company = createCompany("Test Company", "company@example.com");
        User creator = createUser(company, "Creator", "creator@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment1 = createEquipment(company, client, "Notebook");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment2 = createEquipment(company, client, "Desktop");
        WorkOrder workOrder1 = createWorkOrder(company, client, equipment1, technician);
        WorkOrder workOrder2 = createWorkOrder(company, client, equipment2, technician);
        
        createBudget(company, workOrder1, creator, BudgetStatus.PENDENTE);
        createBudget(company, workOrder1, creator, BudgetStatus.APROVADO);
        createBudget(company, workOrder2, creator, BudgetStatus.PENDENTE);
        
        List<Budget> budgets = repository.findByWorkOrderId(workOrder1.getId());
        
        assertThat(budgets).hasSize(2);
        assertThat(budgets).allMatch(b -> b.getWorkOrder().getId().equals(workOrder1.getId()));
    }

    @Test
    @DisplayName("Deve buscar orçamentos por status")
    void shouldFindBudgetsByStatus() {
        Company company = createCompany("Test Company", "company@example.com");
        User creator = createUser(company, "Creator", "creator@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        
        createBudget(company, workOrder, creator, BudgetStatus.PENDENTE);
        createBudget(company, workOrder, creator, BudgetStatus.APROVADO);
        createBudget(company, workOrder, creator, BudgetStatus.REJEITADO);
        
        List<Budget> pendingBudgets = repository.findByStatus(BudgetStatus.PENDENTE);
        List<Budget> approvedBudgets = repository.findByStatus(BudgetStatus.APROVADO);
        
        assertThat(pendingBudgets).isNotEmpty();
        assertThat(approvedBudgets).isNotEmpty();
        assertThat(pendingBudgets).allMatch(b -> b.getStatus() == BudgetStatus.PENDENTE);
        assertThat(approvedBudgets).allMatch(b -> b.getStatus() == BudgetStatus.APROVADO);
    }

    @Test
    @DisplayName("Deve buscar orçamentos por workOrderId e status")
    void shouldFindBudgetsByWorkOrderIdAndStatus() {
        Company company = createCompany("Test Company", "company@example.com");
        User creator = createUser(company, "Creator", "creator@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment1 = createEquipment(company, client, "Notebook");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment2 = createEquipment(company, client, "Desktop");
        WorkOrder workOrder1 = createWorkOrder(company, client, equipment1, technician);
        WorkOrder workOrder2 = createWorkOrder(company, client, equipment2, technician);
        
        createBudget(company, workOrder1, creator, BudgetStatus.PENDENTE);
        createBudget(company, workOrder1, creator, BudgetStatus.APROVADO);
        createBudget(company, workOrder2, creator, BudgetStatus.PENDENTE);
        
        List<Budget> budgets = repository.findByWorkOrderIdAndStatus(workOrder1.getId(), BudgetStatus.APROVADO);
        
        assertThat(budgets).hasSize(1);
        assertThat(budgets.get(0).getWorkOrder().getId()).isEqualTo(workOrder1.getId());
        assertThat(budgets.get(0).getStatus()).isEqualTo(BudgetStatus.APROVADO);
    }

    @Test
    @DisplayName("Deve buscar orçamentos por companyId e status")
    void shouldFindBudgetsByCompanyIdAndStatus() {
        Company company1 = createCompany("Company 1", "company1@example.com");
        Company company2 = createCompany("Company 2", "company2@example.com");
        User creator1 = createUser(company1, "Creator 1", "creator1@example.com");
        User creator2 = createUser(company2, "Creator 2", "creator2@example.com");
        User technician1 = createUser(company1, "Technician 1", "tech1@example.com");
        User technician2 = createUser(company2, "Technician 2", "tech2@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client1 = createClient(company1, "Client 1", "11999999999");
        br.com.baggiotech.tecos_api.domain.client.Client client2 = createClient(company2, "Client 2", "11888888888");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment1 = createEquipment(company1, client1, "Notebook");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment2 = createEquipment(company2, client2, "Desktop");
        WorkOrder workOrder1 = createWorkOrder(company1, client1, equipment1, technician1);
        WorkOrder workOrder2 = createWorkOrder(company2, client2, equipment2, technician2);
        
        createBudget(company1, workOrder1, creator1, BudgetStatus.PENDENTE);
        createBudget(company1, workOrder1, creator1, BudgetStatus.APROVADO);
        createBudget(company2, workOrder2, creator2, BudgetStatus.PENDENTE);
        
        List<Budget> budgets = repository.findByCompanyIdAndStatus(company1.getId(), BudgetStatus.APROVADO);
        
        assertThat(budgets).hasSize(1);
        assertThat(budgets.get(0).getCompany().getId()).isEqualTo(company1.getId());
        assertThat(budgets.get(0).getStatus()).isEqualTo(BudgetStatus.APROVADO);
    }

    @Test
    @DisplayName("Deve atualizar orçamento existente")
    void shouldUpdateExistingBudget() {
        Company company = createCompany("Test Company", "company@example.com");
        User creator = createUser(company, "Creator", "creator@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        Budget budget = createBudget(company, workOrder, creator, BudgetStatus.PENDENTE);
        
        budget.setStatus(BudgetStatus.APROVADO);
        budget.setApprovalMethod("presential");
        budget.setUpdatedAt(LocalDateTime.now());
        Budget updated = repository.save(budget);
        
        assertThat(updated.getStatus()).isEqualTo(BudgetStatus.APROVADO);
        assertThat(updated.getApprovalMethod()).isEqualTo("presential");
    }
}
