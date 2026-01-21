package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.company.*;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityAlreadyExistsException;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.presentation.exception.GlobalExceptionHandler;
import br.com.baggiotech.tecos_api.presentation.mapper.company.CompanyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyController Integration Tests")
class CompanyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ListCompaniesUseCase listCompaniesUseCase;

    @Mock
    private CreateCompanyUseCase createCompanyUseCase;

    @Mock
    private GetCompanyByIdUseCase getCompanyByIdUseCase;

    @Mock
    private UpdateCompanyUseCase updateCompanyUseCase;

    @Mock
    private DeleteCompanyUseCase deleteCompanyUseCase;

    @Mock
    private CompanyMapper mapper;

    private CompanyController controller;

    private Company company;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        controller = new CompanyController(
                listCompaniesUseCase,
                createCompanyUseCase,
                getCompanyByIdUseCase,
                updateCompanyUseCase,
                deleteCompanyUseCase,
                mapper
        );
        
        org.springframework.validation.beanvalidation.LocalValidatorFactoryBean validator = 
            new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        companyId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);
        company.setName("Test Company");
        company.setEmail("test@example.com");
        company.setWhatsapp("11999999999");
        company.setLogoUrl("https://example.com/logo.png");
        company.setIsActive(true);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/companies - Deve listar empresas com sucesso")
    void shouldListCompaniesSuccessfully() throws Exception {

        List<Company> companies = Arrays.asList(company);
        Page<Company> page = new PageImpl<>(companies);
        when(listCompaniesUseCase.execute(isNull(), isNull(), eq(0), eq(15))).thenReturn(page);
        
        br.com.baggiotech.tecos_api.presentation.dto.company.CompanyResponse response = 
            new br.com.baggiotech.tecos_api.presentation.dto.company.CompanyResponse(
                company.getId(), company.getName(), company.getEmail(), company.getWhatsapp(),
                company.getLogoUrl(), company.getIsActive(), company.getCreatedAt(), company.getUpdatedAt()
            );
        when(mapper.toResponse(any(Company.class))).thenReturn(response);
        mockMvc.perform(get("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    if (result.getResponse().getStatus() != 200) {
                        System.out.println("Response body: " + result.getResponse().getContentAsString());
                        System.out.println("Response status: " + result.getResponse().getStatus());
                        if (result.getResolvedException() != null) {
                            result.getResolvedException().printStackTrace();
                        }
                    }
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Company"));

        verify(listCompaniesUseCase).execute(null, null, 0, 15);
        verify(mapper, atLeastOnce()).toResponse(any(Company.class));
    }

    @Test
    @DisplayName("GET /api/companies - Deve filtrar por isActive")
    void shouldFilterByIsActive() throws Exception {

        Page<Company> page = new PageImpl<>(Arrays.asList(company));
        when(listCompaniesUseCase.execute(eq(true), isNull(), eq(0), eq(15))).thenReturn(page);
        
        br.com.baggiotech.tecos_api.presentation.dto.company.CompanyResponse response = 
            new br.com.baggiotech.tecos_api.presentation.dto.company.CompanyResponse(
                company.getId(), company.getName(), company.getEmail(), company.getWhatsapp(),
                company.getLogoUrl(), company.getIsActive(), company.getCreatedAt(), company.getUpdatedAt()
            );
        when(mapper.toResponse(any(Company.class))).thenReturn(response);
        mockMvc.perform(get("/api/companies")
                        .param("isActive", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(listCompaniesUseCase).execute(true, null, 0, 15);
        verify(mapper, atLeastOnce()).toResponse(any(Company.class));
    }

    @Test
    @DisplayName("POST /api/companies - Deve criar empresa com sucesso")
    void shouldCreateCompanySuccessfully() throws Exception {

        String requestBody = """
                {
                    "name": "New Company",
                    "email": "new@example.com",
                    "whatsapp": "11999999999",
                    "logoUrl": "https://example.com/logo.png",
                    "isActive": true
                }
                """;

        when(createCompanyUseCase.execute(anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(company);
        when(mapper.toResponse(any(Company.class))).thenAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            return new br.com.baggiotech.tecos_api.presentation.dto.company.CompanyResponse(
                    c.getId(), c.getName(), c.getEmail(), c.getWhatsapp(),
                    c.getLogoUrl(), c.getIsActive(), c.getCreatedAt(), c.getUpdatedAt()
            );
        });
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Company"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(createCompanyUseCase).execute(
                "New Company",
                "new@example.com",
                "11999999999",
                "https://example.com/logo.png",
                true
        );
    }

    @Test
    @DisplayName("POST /api/companies - Deve retornar erro de validação quando nome está vazio")
    void shouldReturnValidationErrorWhenNameIsEmpty() throws Exception {

        String requestBody = """
                {
                    "name": "",
                    "email": "test@example.com"
                }
                """;
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("name"));

        verify(createCompanyUseCase, never()).execute(anyString(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/companies - Deve retornar erro quando email já existe")
    void shouldReturnErrorWhenEmailAlreadyExists() throws Exception {

        String requestBody = """
                {
                    "name": "New Company",
                    "email": "existing@example.com"
                }
                """;

        when(createCompanyUseCase.execute(eq("New Company"), eq("existing@example.com"), isNull(), isNull(), any()))
                .thenThrow(new EntityAlreadyExistsException("Company", "email", "existing@example.com"));
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("já existe")));
    }

    @Test
    @DisplayName("GET /api/companies/{id} - Deve buscar empresa por ID")
    void shouldGetCompanyById() throws Exception {

        when(getCompanyByIdUseCase.execute(companyId)).thenReturn(company);
        when(mapper.toResponse(any(Company.class))).thenAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            return new br.com.baggiotech.tecos_api.presentation.dto.company.CompanyResponse(
                    c.getId(), c.getName(), c.getEmail(), c.getWhatsapp(),
                    c.getLogoUrl(), c.getIsActive(), c.getCreatedAt(), c.getUpdatedAt()
            );
        });
        mockMvc.perform(get("/api/companies/{id}", companyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyId.toString()))
                .andExpect(jsonPath("$.name").value("Test Company"));

        verify(getCompanyByIdUseCase).execute(companyId);
    }

    @Test
    @DisplayName("GET /api/companies/{id} - Deve retornar 404 quando empresa não encontrada")
    void shouldReturn404WhenCompanyNotFound() throws Exception {

        UUID nonExistentId = UUID.randomUUID();
        when(getCompanyByIdUseCase.execute(nonExistentId))
                .thenThrow(new EntityNotFoundException("Company", nonExistentId));
        mockMvc.perform(get("/api/companies/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("não encontrado")));
    }

    @Test
    @DisplayName("PUT /api/companies/{id} - Deve atualizar empresa com sucesso")
    void shouldUpdateCompanySuccessfully() throws Exception {

        String requestBody = """
                {
                    "name": "Updated Company",
                    "email": "updated@example.com"
                }
                """;

        company.setName("Updated Company");
        company.setEmail("updated@example.com");
        when(updateCompanyUseCase.execute(any(UUID.class), anyString(), anyString(), any(), any(), any()))
                .thenReturn(company);
        when(mapper.toResponse(any(Company.class))).thenAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            return new br.com.baggiotech.tecos_api.presentation.dto.company.CompanyResponse(
                    c.getId(), c.getName(), c.getEmail(), c.getWhatsapp(),
                    c.getLogoUrl(), c.getIsActive(), c.getCreatedAt(), c.getUpdatedAt()
            );
        });
        mockMvc.perform(put("/api/companies/{id}", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Company"));

        verify(updateCompanyUseCase).execute(
                eq(companyId),
                eq("Updated Company"),
                eq("updated@example.com"),
                any(),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} - Deve deletar empresa com sucesso")
    void shouldDeleteCompanySuccessfully() throws Exception {

        doNothing().when(deleteCompanyUseCase).execute(companyId);
        mockMvc.perform(delete("/api/companies/{id}", companyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Empresa excluída com sucesso."));

        verify(deleteCompanyUseCase).execute(companyId);
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} - Deve retornar 404 quando empresa não encontrada")
    void shouldReturn404WhenDeletingNonExistentCompany() throws Exception {

        UUID nonExistentId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Company", nonExistentId))
                .when(deleteCompanyUseCase).execute(nonExistentId);
        mockMvc.perform(delete("/api/companies/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
