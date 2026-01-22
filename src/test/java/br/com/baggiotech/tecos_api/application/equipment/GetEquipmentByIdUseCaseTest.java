package br.com.baggiotech.tecos_api.application.equipment;

import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.equipment.EquipmentRepository;
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
@DisplayName("GetEquipmentByIdUseCase Tests")
class GetEquipmentByIdUseCaseTest {

    @Mock
    private EquipmentRepository repository;

    @InjectMocks
    private GetEquipmentByIdUseCase useCase;

    private Equipment equipment;
    private UUID equipmentId;

    @BeforeEach
    void setUp() {
        equipmentId = UUID.randomUUID();
        equipment = new Equipment();
        equipment.setId(equipmentId);
        equipment.setType("Notebook");
    }

    @Test
    @DisplayName("Deve buscar equipment por ID com sucesso")
    void shouldGetEquipmentByIdSuccessfully() {
        when(repository.findById(equipmentId)).thenReturn(Optional.of(equipment));

        Equipment result = useCase.execute(equipmentId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(equipmentId);
        verify(repository).findById(equipmentId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando equipment não encontrado")
    void shouldThrowExceptionWhenEquipmentNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Equipment");

        verify(repository).findById(nonExistentId);
    }
}
