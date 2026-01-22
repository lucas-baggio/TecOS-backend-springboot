package br.com.baggiotech.tecos_api.application.user;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
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
@DisplayName("ListUsersUseCase Tests")
class ListUsersUseCaseTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private ListUsersUseCase useCase;

    private Company company1;
    private Company company2;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        UUID companyId1 = UUID.randomUUID();
        UUID companyId2 = UUID.randomUUID();

        company1 = new Company();
        company1.setId(companyId1);
        company1.setName("Company 1");

        company2 = new Company();
        company2.setId(companyId2);
        company2.setName("Company 2");

        user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setCompany(company1);
        user1.setName("User Alpha");
        user1.setEmail("alpha@example.com");
        user1.setType("TECNICO");
        user1.setIsActive(true);

        user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setCompany(company1);
        user2.setName("User Beta");
        user2.setEmail("beta@example.com");
        user2.setType("ADMIN");
        user2.setIsActive(false);

        user3 = new User();
        user3.setId(UUID.randomUUID());
        user3.setCompany(company2);
        user3.setName("User Gamma");
        user3.setEmail("gamma@example.com");
        user3.setType("TECNICO");
        user3.setIsActive(true);
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void shouldListAllUsers() {
        when(repository.findAll()).thenReturn(new java.util.ArrayList<>(Arrays.asList(user1, user2, user3)));

        Page<User> result = useCase.execute(0, 15, null, null, null, null);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve filtrar por companyId")
    void shouldFilterByCompanyId() {
        when(repository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        Page<User> result = useCase.execute(0, 15, company1.getId(), null, null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("alpha@example.com", "beta@example.com");
    }

    @Test
    @DisplayName("Deve filtrar por isActive")
    void shouldFilterByIsActive() {
        when(repository.findAll()).thenReturn(new java.util.ArrayList<>(Arrays.asList(user1, user2, user3)));

        Page<User> result = useCase.execute(0, 15, null, true, null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("alpha@example.com", "gamma@example.com");
    }

    @Test
    @DisplayName("Deve filtrar por tipo")
    void shouldFilterByType() {
        when(repository.findAll()).thenReturn(new java.util.ArrayList<>(Arrays.asList(user1, user2, user3)));

        Page<User> result = useCase.execute(0, 15, null, null, "TECNICO", null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("alpha@example.com", "gamma@example.com");
    }

    @Test
    @DisplayName("Deve buscar por nome ou email")
    void shouldSearchByNameOrEmail() {
        when(repository.findAll()).thenReturn(new java.util.ArrayList<>(Arrays.asList(user1, user2, user3)));
        when(repository.searchByNameOrEmail("Alpha")).thenReturn(new java.util.ArrayList<>(Arrays.asList(user1)));

        Page<User> result = useCase.execute(0, 15, null, null, null, "Alpha");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).containsIgnoringCase("Alpha");
        verify(repository).searchByNameOrEmail("Alpha");
    }

    @Test
    @DisplayName("Deve aplicar paginação corretamente")
    void shouldApplyPaginationCorrectly() {
        when(repository.findAll()).thenReturn(new java.util.ArrayList<>(Arrays.asList(user1, user2, user3)));

        Page<User> result = useCase.execute(0, 2, null, null, null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve ordenar por nome")
    void shouldSortByName() {
        when(repository.findAll()).thenReturn(new java.util.ArrayList<>(Arrays.asList(user3, user1, user2)));

        Page<User> result = useCase.execute(0, 15, null, null, null, null);

        List<User> content = result.getContent();
        assertThat(content.get(0).getName()).isLessThanOrEqualTo(content.get(1).getName());
    }

    @Test
    @DisplayName("Deve combinar múltiplos filtros")
    void shouldCombineMultipleFilters() {
        when(repository.findAll()).thenReturn(new java.util.ArrayList<>(Arrays.asList(user1, user2, user3)));

        Page<User> result = useCase.execute(0, 15, company1.getId(), true, "TECNICO", null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("alpha@example.com");
    }
}
