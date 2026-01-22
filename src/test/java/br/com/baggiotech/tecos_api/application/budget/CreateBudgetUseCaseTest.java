package br.com.baggiotech.tecos_api.application.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateBudgetUseCase Tests")
class CreateBudgetUseCaseTest {

    @Mock
    private BudgetRepository repository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomMetrics metrics;

    @InjectMocks
    private CreateBudgetUseCase useCase;

    private Company company;
    private Client client;
    private Equipment equipment;
    private User technician;
    private User creator;
    private WorkOrder workOrder;
    private Budget savedBudget;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Test Company");
        company.setIsActive(true);

        client = new Client();
        client.setId(UUID.randomUUID());
        client.setCompany(company);

        equipment = new Equipment();
        equipment.setId(UUID.randomUUID());
        equipment.setCompany(company);
        equipment.setClient(client);

        technician = new User();
        technician.setId(UUID.randomUUID());
        technician.setCompany(company);
        technician.setType("TECNICO");

        creator = new User();
        creator.setId(UUID.randomUUID());
        creator.setCompany(company);

        workOrder = new WorkOrder();
        workOrder.setId(UUID.randomUUID());
        workOrder.setCompany(company);
        workOrder.setClient(client);
        workOrder.setEquipment(equipment);
        workOrder.setTechnician(technician);
        workOrder.setStatus(OrderStatus.RECEBIDO);

        savedBudget = new Budget();
        savedBudget.setId(UUID.randomUUID());
        savedBudget.setCompany(company);
        savedBudget.setWorkOrder(workOrder);
        savedBudget.setServiceValue(new BigDecimal("100.00"));
        savedBudget.setPartsValue(new BigDecimal("50.00"));
        savedBudget.setTotalValue(new BigDecimal("150.00"));
        savedBudget.setStatus(BudgetStatus.PENDENTE);
        savedBudget.setCreatedBy(creator);
    }

    @Test
    @DisplayName("Deve criar um orçamento com sucesso")
    void shouldCreateBudgetSuccessfully() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = 
            io.micrometer.core.instrument.Timer.start(meterRegistry);

        when(workOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(workOrder));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(creator));
        when(repository.save(any(Budget.class))).thenReturn(savedBudget);
        when(metrics.startTimer()).thenReturn(sample);

        Budget result = useCase.execute(
                company.getId(),
                workOrder.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                creator.getId()
        );

        assertThat(result).isNotNull();
        assertThat(result.getServiceValue()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getPartsValue()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getTotalValue()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.PENDENTE);

        verify(workOrderRepository).findById(workOrder.getId());
        verify(userRepository).findById(creator.getId());
        verify(repository).save(any(Budget.class));
        verify(metrics).incrementBudgetsCreated();
    }

    @Test
    @DisplayName("Deve calcular total_value automaticamente quando não fornecido")
    void shouldCalculateTotalValueAutomatically() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = 
            io.micrometer.core.instrument.Timer.start(meterRegistry);

        when(workOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(workOrder));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(creator));
        when(repository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget budget = invocation.getArgument(0);
            assertThat(budget.getTotalValue()).isEqualByComparingTo(new BigDecimal("150.00"));
            return savedBudget;
        });
        when(metrics.startTimer()).thenReturn(sample);

        useCase.execute(
                company.getId(),
                workOrder.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                null,
                creator.getId()
        );

        verify(repository).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve usar partsValue como zero quando não fornecido")
    void shouldUseZeroForPartsValueWhenNotProvided() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = 
            io.micrometer.core.instrument.Timer.start(meterRegistry);

        when(workOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(workOrder));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(creator));
        when(repository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget budget = invocation.getArgument(0);
            assertThat(budget.getPartsValue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(budget.getTotalValue()).isEqualByComparingTo(new BigDecimal("100.00"));
            return savedBudget;
        });
        when(metrics.startTimer()).thenReturn(sample);

        useCase.execute(
                company.getId(),
                workOrder.getId(),
                new BigDecimal("100.00"),
                null,
                new BigDecimal("100.00"),
                creator.getId()
        );

        verify(repository).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando work order não existe")
    void shouldThrowExceptionWhenWorkOrderNotFound() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = 
            io.micrometer.core.instrument.Timer.start(meterRegistry);

        UUID nonExistentWorkOrderId = UUID.randomUUID();
        when(workOrderRepository.findById(nonExistentWorkOrderId)).thenReturn(Optional.empty());
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                company.getId(),
                nonExistentWorkOrderId,
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                creator.getId()
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("WorkOrder");

        verify(workOrderRepository).findById(nonExistentWorkOrderId);
        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando work order não pertence à company")
    void shouldThrowExceptionWhenWorkOrderDoesNotBelongToCompany() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = 
            io.micrometer.core.instrument.Timer.start(meterRegistry);

        Company otherCompany = new Company();
        otherCompany.setId(UUID.randomUUID());
        workOrder.setCompany(otherCompany);

        when(workOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(workOrder));
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                company.getId(),
                workOrder.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                creator.getId()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pertence à sua empresa");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando work order está CANCELADA")
    void shouldThrowExceptionWhenWorkOrderIsCancelled() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = 
            io.micrometer.core.instrument.Timer.start(meterRegistry);

        workOrder.setStatus(OrderStatus.CANCELADO);

        when(workOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(workOrder));
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                company.getId(),
                workOrder.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                creator.getId()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CANCELADO");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando work order está ENTREGUE")
    void shouldThrowExceptionWhenWorkOrderIsDelivered() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = 
            io.micrometer.core.instrument.Timer.start(meterRegistry);

        workOrder.setStatus(OrderStatus.ENTREGUE);

        when(workOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(workOrder));
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                company.getId(),
                workOrder.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                creator.getId()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ENTREGUE");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando total_value não corresponde à soma")
    void shouldThrowExceptionWhenTotalValueDoesNotMatch() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = 
            io.micrometer.core.instrument.Timer.start(meterRegistry);

        when(workOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(workOrder));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(creator));
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                company.getId(),
                workOrder.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("200.00"), // Total incorreto
                creator.getId()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valor total deve ser igual à soma");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário criador não existe")
    void shouldThrowExceptionWhenCreatorNotFound() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = 
            io.micrometer.core.instrument.Timer.start(meterRegistry);

        UUID nonExistentUserId = UUID.randomUUID();
        when(workOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(workOrder));
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                company.getId(),
                workOrder.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                nonExistentUserId
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário criador não pertence à company")
    void shouldThrowExceptionWhenCreatorDoesNotBelongToCompany() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = 
            io.micrometer.core.instrument.Timer.start(meterRegistry);

        Company otherCompany = new Company();
        otherCompany.setId(UUID.randomUUID());
        creator.setCompany(otherCompany);

        when(workOrderRepository.findById(any(UUID.class))).thenReturn(Optional.of(workOrder));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(creator));
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                company.getId(),
                workOrder.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                creator.getId()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pertence à empresa");

        verify(repository, never()).save(any(Budget.class));
    }
}
