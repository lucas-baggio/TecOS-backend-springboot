package br.com.baggiotech.tecos_api.application.company;

import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCompanyUseCase Tests")
class DeleteCompanyUseCaseTest {

    @Mock
    private CompanyRepository repository;

    @Mock
    private CustomMetrics metrics;

    @InjectMocks
    private DeleteCompanyUseCase useCase;

    @Test
    @DisplayName("Deve deletar empresa com sucesso")
    void shouldDeleteCompanySuccessfully() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        UUID companyId = UUID.randomUUID();
        when(repository.existsById(companyId)).thenReturn(true);
        doNothing().when(repository).deleteById(companyId);
        when(metrics.startTimer()).thenReturn(sample);
        
        useCase.execute(companyId);
        verify(repository).existsById(companyId);
        verify(repository).deleteById(companyId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando empresa não encontrada")
    void shouldThrowExceptionWhenCompanyNotFound() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        UUID nonExistentId = UUID.randomUUID();
        when(repository.existsById(nonExistentId)).thenReturn(false);
        when(metrics.startTimer()).thenReturn(sample);
        
        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Company")
                .hasMessageContaining(nonExistentId.toString());

        verify(repository).existsById(nonExistentId);
        verify(repository, never()).deleteById(any(UUID.class));
    }
}
