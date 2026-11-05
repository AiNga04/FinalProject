package org.vti.jamie.com.testingsystem.controller;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.vti.jamie.com.testingsystem.config.SecurityConfig;
import org.vti.jamie.com.testingsystem.form.department.DepartmentSearchForm;
import org.vti.jamie.com.testingsystem.form.department.UpdateDepartmentForm;
import org.vti.jamie.com.testingsystem.dto.department.DepartmentResponse;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.exception.GlobalExceptionHandler;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.repository.IRevokedSessionRepository;
import org.vti.jamie.com.testingsystem.service.IDepartmentService;
import org.vti.jamie.com.testingsystem.util.common.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DepartmentSecurityTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private IDepartmentService service;
    private IAccountRepository accounts;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestConfig.class);
        context.refresh();
        service = context.getBean(IDepartmentService.class);
        accounts = context.getBean(IAccountRepository.class);
        decoder = context.getBean(JwtDecoder.class);
        when(service.getDepartments(any(DepartmentSearchForm.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(get("/api/v1/").contextPath("/api/v1"))
                .apply(springSecurity()).build();
    }

    @AfterEach
    void tearDown() { context.close(); }

    @Test
    void onlyAdminCanAccessEveryDepartmentEndpoint() throws Exception {
        for (EN_AccountType type : EN_AccountType.values()) {
            stubToken(type);
            var requests = List.of(
                    get("/api/v1/departments"), get("/api/v1/departments/10"),
                    post("/api/v1/departments").content("{\"name\":\"Engineering\",\"type\":\"DEV\"}"),
                    put("/api/v1/departments/10").content("{\"name\":\"New name\"}"),
                    patch("/api/v1/departments/10/status").content("{\"status\":\"DELETED\"}"),
                    patch("/api/v1/departments/updateStatusList")
                            .content("{\"ids\":[10,11],\"status\":\"ACTIVE\"}")
            );
            for (var request : requests) {
                var result = mvc.perform(request.header("Authorization", "Bearer " + type.name())
                        .contentType(MediaType.APPLICATION_JSON));
                if (type == EN_AccountType.ADMIN) {
                    result.andExpect(status().is2xxSuccessful());
                } else {
                    result.andExpect(status().isForbidden());
                }
            }
        }
    }

    @Test
    void noTokenCannotAccessDepartments() throws Exception {
        mvc.perform(get("/api/v1/departments")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void listBindsQueryFiltersAndReturnsArrayWithTotal() throws Exception {
        stubToken(EN_AccountType.ADMIN);
        DepartmentResponse department = response();
        when(service.getDepartments(any(DepartmentSearchForm.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(department),
                PageRequest.of(0, 10), 16));
        mvc.perform(get("/api/v1/departments").header("Authorization", "Bearer ADMIN")
                        .param("pageNumber", "2").param("size", "10").param("search", "Engineering")
                        .param("type", "DEV").param("status", "ACTIVE")
                        .param("minCreatedDate", "2026-10-01").param("maxCreatedDate", "2026-10-29"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data2nd").value(16));
        var request = ArgumentCaptor.forClass(DepartmentSearchForm.class);
        var paging = ArgumentCaptor.forClass(Pageable.class);
        verify(service).getDepartments(request.capture(), paging.capture());
        assertEquals(1, paging.getValue().getPageNumber());
        assertEquals(10, paging.getValue().getPageSize());
        assertEquals("Engineering", request.getValue().getSearch());
        assertEquals("DEV", request.getValue().getType());
        assertEquals(EN_Status.ACTIVE, request.getValue().getStatus());
        assertEquals(LocalDate.of(2026, 10, 1), request.getValue().getMinCreatedDate());
        assertEquals(LocalDate.of(2026, 10, 29), request.getValue().getMaxCreatedDate());
    }

    @Test
    void detailReturnsSingleObjectWithNullData2nd() throws Exception {
        stubToken(EN_AccountType.ADMIN);
        when(service.getDepartment(10L)).thenReturn(response());
        mvc.perform(get("/api/v1/departments/10").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data2nd").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void partialUpdateAllowsOmittedType() throws Exception {
        stubToken(EN_AccountType.ADMIN);
        mvc.perform(put("/api/v1/departments/10").header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"New name\"}"))
                .andExpect(status().isOk());
        var request = ArgumentCaptor.forClass(UpdateDepartmentForm.class);
        verify(service).update(eq(10L), request.capture(), eq("3"));
        assertEquals("New name", request.getValue().getName());
        assertNull(request.getValue().getType());
    }

    @Test
    void validationRejectsBlankFieldsInvalidIdsAndMissingStatus() throws Exception {
        stubToken(EN_AccountType.ADMIN);
        mvc.perform(post("/api/v1/departments").header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"type\":\"DEV\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/departments/10").header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"type\":null}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/v1/departments/10/status").header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/v1/departments/updateStatusList").header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[],\"status\":\"DELETED\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void nonPositivePageSizeFallsBackToDefault() throws Exception {
        stubToken(EN_AccountType.ADMIN);
        mvc.perform(get("/api/v1/departments?size=0").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isOk());
        var paging = ArgumentCaptor.forClass(Pageable.class);
        verify(service).getDepartments(any(DepartmentSearchForm.class), paging.capture());
        assertEquals(10, paging.getValue().getPageSize());
    }

    private DepartmentResponse response() {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(10L);
        response.setName("Engineering");
        response.setType("DEV");
        response.setStatus(EN_Status.ACTIVE);
        return response;
    }

    private void stubToken(EN_AccountType type) {
        Account account = new Account();
        account.setId(3L);
        account.setAccountType(type);
        when(accounts.findById(3L)).thenReturn(Optional.of(account));
        when(decoder.decode(type.name())).thenReturn(Jwt.withTokenValue(type.name())
                .header("alg", "HS256").subject("3").claim("sid", "department-session")
                .claim("accountType", type.name()).build());
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, DepartmentController.class, GlobalExceptionHandler.class})
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            var pageable = new PageableHandlerMethodArgumentResolver();
            pageable.setPageParameterName("pageNumber");
            pageable.setSizeParameterName("size");
            pageable.setOneIndexedParameters(true);
            pageable.setFallbackPageable(PageRequest.of(0, 10));
            pageable.setMaxPageSize(100);
            resolvers.add(pageable);
        }

        @Bean IAccountRepository accountRepository() { return mock(IAccountRepository.class); }
        @Bean IDepartmentService departmentService() { return mock(IDepartmentService.class); }
        @Bean JwtDecoder jwtDecoder() { return mock(JwtDecoder.class); }
        @Bean IRevokedSessionRepository revokedSessions() { return mock(IRevokedSessionRepository.class); }
    }
}
