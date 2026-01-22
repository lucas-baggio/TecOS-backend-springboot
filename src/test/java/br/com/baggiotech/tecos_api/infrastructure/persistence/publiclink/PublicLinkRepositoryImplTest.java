package br.com.baggiotech.tecos_api.infrastructure.persistence.publiclink;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.equipment.EquipmentJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.equipment.EquipmentJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.publiclink.PublicLinkJpaRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({PublicLinkRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.company.CompanyRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.workorder.WorkOrderRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.user.UserRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.client.ClientRepositoryImpl.class,
         br.com.baggiotech.tecos_api.infrastructure.persistence.equipment.EquipmentRepositoryImpl.class})
@DisplayName("PublicLinkRepositoryImpl Integration Tests")
class PublicLinkRepositoryImplTest {

    @Autowired
    private PublicLinkJpaRepository publicLinkJpaRepository;

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
    private PublicLinkRepository repository;

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

    private PublicLink createPublicLink(WorkOrder workOrder, String token) {
        PublicLink publicLink = new PublicLink();
        publicLink.setWorkOrder(workOrder);
        publicLink.setToken(token);
        publicLink.setCreatedAt(LocalDateTime.now());
        publicLink.setUpdatedAt(LocalDateTime.now());
        return repository.save(publicLink);
    }

    @Test
    @DisplayName("Deve salvar link público com sucesso")
    void shouldSavePublicLinkSuccessfully() {
        Company company = createCompany("Test Company", "company@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        
        PublicLink newLink = new PublicLink();
        newLink.setWorkOrder(workOrder);
        newLink.setToken("unique-token-123");
        newLink.setCreatedAt(LocalDateTime.now());
        newLink.setUpdatedAt(LocalDateTime.now());
        
        PublicLink saved = repository.save(newLink);
        
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getToken()).isEqualTo("unique-token-123");
        assertThat(publicLinkJpaRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("Deve buscar link público por ID")
    void shouldFindPublicLinkById() {
        Company company = createCompany("Test Company", "company@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        PublicLink link1 = createPublicLink(workOrder, "token-1");
        
        Optional<PublicLink> found = repository.findById(link1.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo("token-1");
    }

    @Test
    @DisplayName("Deve retornar empty quando link não existe")
    void shouldReturnEmptyWhenLinkNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        Optional<PublicLink> found = repository.findById(nonExistentId);
        
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar existência de link por ID")
    void shouldCheckLinkExistsById() {
        Company company = createCompany("Test Company", "company@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        PublicLink link = createPublicLink(workOrder, "token-test");
        
        assertThat(repository.existsById(link.getId())).isTrue();
        assertThat(repository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Deve listar todos os links públicos")
    void shouldFindAllPublicLinks() {
        Company company = createCompany("Test Company", "company@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        createPublicLink(workOrder, "token-1");
        createPublicLink(workOrder, "token-2");
        
        List<PublicLink> links = repository.findAll();
        
        assertThat(links).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Deve buscar links públicos por workOrderId")
    void shouldFindPublicLinksByWorkOrderId() {
        Company company = createCompany("Test Company", "company@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder1 = createWorkOrder(company, client, equipment, technician);
        WorkOrder workOrder2 = createWorkOrder(company, client, equipment, technician);
        
        createPublicLink(workOrder1, "token-1");
        createPublicLink(workOrder1, "token-2");
        createPublicLink(workOrder2, "token-3");
        
        List<PublicLink> links = repository.findByWorkOrderId(workOrder1.getId());
        
        assertThat(links).hasSize(2);
        assertThat(links).allMatch(l -> l.getWorkOrder().getId().equals(workOrder1.getId()));
    }

    @Test
    @DisplayName("Deve buscar link público por token")
    void shouldFindPublicLinkByToken() {
        Company company = createCompany("Test Company", "company@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        PublicLink link = createPublicLink(workOrder, "unique-token-123");
        
        Optional<PublicLink> found = repository.findByToken("unique-token-123");
        
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(link.getId());
    }

    @Test
    @DisplayName("Deve verificar existência de token")
    void shouldCheckTokenExists() {
        Company company = createCompany("Test Company", "company@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        PublicLink link = createPublicLink(workOrder, "existing-token");
        
        assertThat(repository.existsByToken("existing-token")).isTrue();
        assertThat(repository.existsByToken("non-existing-token")).isFalse();
    }

    @Test
    @DisplayName("Deve deletar link público")
    void shouldDeletePublicLink() {
        Company company = createCompany("Test Company", "company@example.com");
        User technician = createUser(company, "Technician", "tech@example.com");
        br.com.baggiotech.tecos_api.domain.client.Client client = createClient(company, "Test Client", "11999999999");
        br.com.baggiotech.tecos_api.domain.equipment.Equipment equipment = createEquipment(company, client, "Notebook");
        WorkOrder workOrder = createWorkOrder(company, client, equipment, technician);
        PublicLink link = createPublicLink(workOrder, "token-to-delete");
        UUID linkId = link.getId();
        
        repository.delete(link);
        
        assertThat(repository.existsById(linkId)).isFalse();
    }
}
