package br.com.baggiotech.tecos_api.application.equipment;

import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.equipment.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListEquipmentsUseCase Tests")
class ListEquipmentsUseCaseTest {

    @Mock
    private EquipmentRepository repository;

    @InjectMocks
    private ListEquipmentsUseCase useCase;

    private Equipment equipment1;
    private Equipment equipment2;
    private UUID companyId;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        equipment1 = new Equipment();
        equipment1.setId(UUID.randomUUID());
        equipment1.setType("Notebook");

        equipment2 = new Equipment();
        equipment2.setId(UUID.randomUUID());
        equipment2.setType("Desktop");
    }

    @Test
    @DisplayName("Deve listar todos os equipments")
    void shouldListAllEquipments() {
        List<Equipment> equipments = Arrays.asList(equipment1, equipment2);
        when(repository.findAll()).thenReturn(equipments);

        Page<Equipment> result = useCase.execute(null, null, null, "type", "asc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve filtrar por companyId")
    void shouldFilterByCompanyId() {
        List<Equipment> equipments = Arrays.asList(equipment1);
        when(repository.findByCompanyId(companyId)).thenReturn(equipments);

        Page<Equipment> result = useCase.execute(companyId, null, null, "type", "asc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByCompanyId(companyId);
    }

    @Test
    @DisplayName("Deve filtrar por clientId")
    void shouldFilterByClientId() {
        List<Equipment> equipments = Arrays.asList(equipment1);
        when(repository.findByClientId(clientId)).thenReturn(equipments);

        Page<Equipment> result = useCase.execute(null, clientId, null, "type", "asc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByClientId(clientId);
    }

    @Test
    @DisplayName("Deve filtrar por companyId e clientId")
    void shouldFilterByCompanyIdAndClientId() {
        List<Equipment> equipments = Arrays.asList(equipment1);
        when(repository.findByCompanyIdAndClientId(companyId, clientId)).thenReturn(equipments);

        Page<Equipment> result = useCase.execute(companyId, clientId, null, "type", "asc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByCompanyIdAndClientId(companyId, clientId);
    }

    @Test
    @DisplayName("Deve aplicar paginação")
    void shouldApplyPagination() {
        List<Equipment> equipments = Arrays.asList(equipment1, equipment2);
        when(repository.findAll()).thenReturn(equipments);

        Page<Equipment> result = useCase.execute(null, null, null, "type", "asc", 0, 1);

        assertThat(result).isNotNull();
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}
