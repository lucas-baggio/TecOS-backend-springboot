package br.com.baggiotech.tecos_api.application.workorderhistory;

import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWorkOrderHistoryByIdUseCase Tests")
class GetWorkOrderHistoryByIdUseCaseTest {

    @Mock
    private WorkOrderHistoryRepository repository;

    @InjectMocks
    private GetWorkOrderHistoryByIdUseCase useCase;

    private WorkOrderHistory history;
    private UUID historyId;

    @BeforeEach
    void setUp() {
        historyId = UUID.randomUUID();
        history = new WorkOrderHistory();
        history.setId(historyId);
    }

    @Test
    @DisplayName("Deve buscar histórico por ID com sucesso")
    void shouldGetHistoryByIdSuccessfully() {
        when(repository.findById(historyId)).thenReturn(Optional.of(history));

        WorkOrderHistory result = useCase.execute(historyId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(historyId);
        verify(repository).findById(historyId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando histórico não encontrado")
    void shouldThrowExceptionWhenHistoryNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("WorkOrderHistory");

        verify(repository).findById(nonExistentId);
    }
}
