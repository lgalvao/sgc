# Test Improvement Tracking

**Project:** SGC - Sistema de Gestão de Competências  
**Started:** 2026-01-11  
**Current Quality Score:** 6.5/10  
**Target Quality Score:** 8.5/10  

---

## 📊 Executive Summary

### Key Findings from Analysis
1. ⚠️ **SEVERE REDUNDANCY:** Multiple test files per service (3 files for UsuarioService, 4 for PainelService)
2. ⚠️ **BLOATED TEST FILES:** ProcessoFacadeTest has 1,239 lines
3. ⚠️ **ANTI-PATTERN:** 359 mock usages in integration tests (defeats integration testing purpose)
4. ⚠️ **INCONSISTENT NAMING:** Mix of test naming patterns
5. ⚠️ **LOW ENTITY COVERAGE:** Only 6 entity tests for 54 entity classes

### Current Statistics
- **Total test files:** 173
- **Total test methods:** 1,151
- **Integration tests:** 52 (@SpringBootTest)
- **Unit tests:** 65 (@ExtendWith(MockitoExtension))
- **Lines of test code:** ~31,867

### Quality Metrics
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Test Organization | 5/10 | 8/10 | ⚠️ Major redundancy |
| Naming Consistency | 8/10 | 9/10 | ✅ Mostly consistent |
| Test Isolation | 6/10 | 9/10 | ⚠️ Mock usage in integration tests |
| Coverage (Services) | 10/10 | 10/10 | ✅ All services tested |
| Coverage (DTOs/Entities) | 2/10 | 7/10 | ❌ Critical gap |
| Maintainability | 4/10 | 8/10 | ❌ Large files, redundancy |
| Architectural Tests | 10/10 | 10/10 | ✅ Excellent ArchUnit usage |
| Integration Tests | 7/10 | 9/10 | ⚠️ Mock anti-pattern |

---

## 🎯 Phase 1: Test Consolidation (High Priority)

**Goal:** Reduce redundancy and improve maintainability  
**Estimated Time:** 2 weeks  
**Status:** 🔴 Not Started

### Task 1.1: Consolidate UsuarioService Tests ⬜

**Problem:** 3 test files with 68 tests across 912 lines, significant overlap

**Files to Consolidate:**
- `UsuarioServiceTest.java` (365 lines, 24 tests) - Integration with @SpringBootTest
- `UsuarioServiceCoverageTest.java` (403 lines, 38 tests) - Unit tests for coverage
- `UsuarioServiceGapsTest.java` (144 lines, 6 tests) - Gap-filling unit tests

**Target Output:**
- `UsuarioServiceTest.java` - Unit tests with mocks (<600 lines)
- `UsuarioControllerIntegrationTest.java` - Integration tests (<400 lines)

**Success Criteria:**
- [ ] All 68 tests still pass
- [ ] No duplicate test cases
- [ ] Each file <600 lines
- [ ] Clear @Nested organization
- [ ] Run `./gradlew :backend:test --tests "sgc.organizacao.usuario.*"`

**Notes:**
- Review each test to identify true duplicates vs. similar tests with different assertions
- Use @Nested classes: Criacao, Atualizacao, Consultas, Validacao, Seguranca
- Preserve all unique validations

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

### Task 1.2: Split ProcessoFacadeTest ⬜

**Problem:** Single file with 1,239 lines + 316 line CoverageTest = 1,555 total lines

**Current Files:**
- `ProcessoFacadeTest.java` (1,239 lines, 59 tests) - Massive comprehensive suite
- `ProcessoFacadeCoverageTest.java` (316 lines, 16 tests) - Additional coverage

**Target Output (4 focused files):**
- `ProcessoFacadeCrudTest.java` (~300 lines) - criar, atualizar, apagar
- `ProcessoFacadeWorkflowTest.java` (~300 lines) - iniciar, finalizar, transições
- `ProcessoFacadeQueryTest.java` (~300 lines) - consultas, listagens, buscas
- `ProcessoFacadeSecurityTest.java` (~250 lines) - checarAcesso, permissões

**Existing @Nested Classes to Distribute:**
- Criacao → CrudTest
- Atualizacao → CrudTest
- Exclusao → CrudTest
- Consultas → QueryTest
- Workflow → WorkflowTest
- Seguranca → SecurityTest
- Lembretes → WorkflowTest or QueryTest

**Success Criteria:**
- [ ] All 75 tests (59 + 16) still pass
- [ ] Each file <400 lines
- [ ] Clear single responsibility per file
- [ ] Preserve all @Nested organization
- [ ] Delete original ProcessoFacadeTest.java
- [ ] Delete ProcessoFacadeCoverageTest.java
- [ ] Run `./gradlew :backend:test --tests "sgc.processo.*ProcessoFacade*"`

**Notes:**
- This is the most complex consolidation task
- Take extra care with setup/teardown methods
- Ensure shared fixtures are properly extracted
- Document the split strategy in commit message

**Status:** ⬜ Not Started  
**Priority:** HIGH - Biggest maintainability issue  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

### Task 1.3: Consolidate PainelService Tests ⬜

**Problem:** 4 test files for a single service (worst redundancy case)

**Files to Consolidate:**
- `PainelServiceTest.java` (355 lines, 19 tests) - Main test suite
- `PainelServiceCoverageTest.java` (219 lines, 8 tests) - Coverage gap filling
- `PainelServiceUpdateTest.java` (129 lines, 4 tests) - Update-specific tests
- `PainelServiceIntegrationTest.java` (? lines, ? tests) - Integration tests

**Target Output:**
- `PainelServiceTest.java` - Consolidated unit tests (<400 lines)
- `PainelServiceIntegrationTest.java` - Keep separate, unchanged

**Success Criteria:**
- [ ] All 31+ tests still pass
- [ ] Clear separation: unit vs integration
- [ ] No test duplication
- [ ] Unit test file <400 lines
- [ ] Delete PainelServiceCoverageTest.java
- [ ] Delete PainelServiceUpdateTest.java
- [ ] Run `./gradlew :backend:test --tests "sgc.painel.*"`

**Notes:**
- Review if UpdateTest tests are truly specific or just normal CRUD
- Keep integration tests completely separate
- Integration tests should use real dependencies, not mocks

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

### Task 1.4: Consolidate AlertaService Tests ⬜

**Problem:** 2 test files with unclear separation

**Files to Consolidate:**
- `AlertaServiceTest.java` (349 lines, 15 tests) - Main test suite
- `AlertaServiceUpdateTest.java` (154 lines, 4 tests) - Update-specific tests

**Target Output:**
- `AlertaServiceTest.java` - Single consolidated file (<400 lines)

**Success Criteria:**
- [ ] All 19 tests still pass
- [ ] Single cohesive test file
- [ ] File <400 lines
- [ ] Delete AlertaServiceUpdateTest.java
- [ ] Run `./gradlew :backend:test --tests "sgc.alerta.*"`

**Notes:**
- Simple consolidation - just merge UpdateTest into main test
- Use @Nested class for update operations if needed

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

### Task 1.5: Review Other Redundancies ⬜

**Review List:**

1. **LoginService (2 files):**
   - `LoginServiceTest.java`
   - `LoginServiceMemoryLeakTest.java` - **KEEP SEPARATE** (specific concern)
   - Decision: No action needed, separation is justified

2. **GerenciadorJwt (2 files):**
   - Identify the two files
   - Determine if consolidation is beneficial
   - Action: TBD after investigation

3. **SubprocessoMapaWorkflowService (2 files):**
   - Check if CoverageTest can be merged
   - Action: TBD after investigation

**Success Criteria:**
- [ ] All redundancies reviewed
- [ ] Consolidation decisions documented
- [ ] Justified separations documented

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

## 🏷️ Phase 2: Test Naming Standardization (Medium Priority)

**Goal:** Ensure consistent naming and categorization  
**Estimated Time:** 1 week  
**Status:** 🔴 Not Started

### Task 2.1: Standardize Test Method Names ⬜

**Problem:** 697 tests use "deve..." (60%), 1 uses "should..." (needs conversion)

**Current Patterns:**
- Portuguese "deve...": 697 tests ✅
- English "should...": 1 test ❌
- Other patterns: 23 tests ⚠️

**Target Pattern:** `deve{Acao}Quando{Condicao}`

**Examples:**
```java
// ✅ Correct
void deveBuscarUsuarioPorTitulo()
void deveLancarExcecaoQuandoUsuarioNaoEncontrado()

// ❌ Convert
void shouldFindUserByTitle() → deveBuscarUsuarioPorTitulo()
```

**Action Items:**
- [ ] Find all tests NOT using "deve..." pattern
- [ ] Convert English patterns to Portuguese
- [ ] Standardize the 23 "other pattern" tests
- [ ] Verify all @DisplayName annotations are in Portuguese
- [ ] Run full test suite to ensure nothing broke

**Success Criteria:**
- [ ] 100% of tests use Portuguese "deve..." pattern
- [ ] All @DisplayName annotations in Portuguese
- [ ] All tests still pass

**Commands:**
```bash
# Find non-Portuguese test methods
grep -rn "void should" backend/src/test --include="*.java"

# Find tests not using deve pattern
grep -rn "@Test" backend/src/test --include="*.java" -A 1 | grep "void" | grep -v "deve"
```

**Status:** ✅ COMPLETE  
**Assigned:** AI Agent  
**Started:** 2026-01-11  
**Completed:** 2026-01-11  

**Results:**
- ✅ Found 1 English test method: `ConfigCorsTest.shouldConfigureCorsSource()`
- ✅ Converted to Portuguese: `deveConfigurarOrigemCorsComOrigensPermitidas()`
- ✅ Updated @DisplayName to Portuguese
- ✅ All tests now use 100% Portuguese naming
- ✅ No "should..." pattern tests remaining

---

### Task 2.2: Add Test Tags ⬜

**Problem:** Only 9 integration tests tagged, no unit test tags

**Current State:**
- @Tag('integration'): 9 tests
- @Tag('unit'): 0 tests ❌
- Other tags: Minimal usage

**Target Tags:**
- `@Tag("unit")` - All @ExtendWith(MockitoExtension.class) tests
- `@Tag("integration")` - All @SpringBootTest tests
- `@Tag("architecture")` - ArchConsistencyTest
- `@Tag("performance")` - ProcessoPerformanceTest
- `@Tag("security")` - Security-specific tests
- `@Tag("e2e")` - End-to-end tests

**Benefits:**
- Selective test execution: `./gradlew test --tests *unit*`
- CI/CD pipeline optimization
- Better test organization

**Action Items:**
- [ ] Add @Tag("unit") to all MockitoExtension tests (~65 files)
- [ ] Add @Tag("integration") to all SpringBootTest tests (~52 files)
- [ ] Add @Tag("architecture") to ArchConsistencyTest
- [ ] Add @Tag("performance") to ProcessoPerformanceTest
- [ ] Add @Tag("security") to security module tests (~14 files)
- [ ] Verify tags don't break test execution
- [ ] Document tag usage in test guide

**Success Criteria:**
- [ ] All unit tests tagged
- [ ] All integration tests tagged
- [ ] Specialized tags applied
- [ ] Run: `./gradlew test --tests "*unit*"` works
- [ ] Run: `./gradlew test --tests "*integration*"` works

**Status:** 🟡 In Progress (22/~120 files tagged)  
**Assigned:** AI Agent  
**Started:** 2026-01-11  
**Completed:** -  

**Progress:**
- ✅ Session 1: Tagged ConfigCorsTest (1 file)
- ✅ Session 2: Tagged all security module tests (12 files)
- ✅ Session 2: Tagged all organizacao module tests (9 files)
- ⏳ Remaining: ~50 unit test files
- ⏳ Remaining: ~48 integration test files
- ⏳ Specialized tags to add

**Files Tagged (22 total):**
- Security Module (13 files): ConfigCorsTest, LoginServiceTest, GerenciadorJwtTest, AccessControlServiceTest, SubprocessoAccessPolicyTest, AccessAuditServiceTest, MapaAccessPolicyTest, ProcessoAccessPolicyTest, AtividadeAccessPolicyTest, LimitadorTentativasLoginTest, GerenciadorJwtSecurityTest, AutenticarReqValidationTest, LoginControllerTest, LoginServiceMemoryLeakTest (already tagged)
- Organizacao Module (11 files, 2 already tagged): UnidadeServiceTest, UnidadeServiceHierarchyTest, ValidadorDadosOrganizacionaisTest, ServicoHierarquiaTest, UsuarioMapperTest, UnidadeTest, UsuarioTest, UnidadeControllerTest, UsuarioControllerTest, UsuarioServiceTest (already tagged), UsuarioServiceUnitTest (already tagged)

**Next Modules:** Processo, Subprocesso, Mapa, Painel, Alerta

---

## 🔒 Phase 3: Test Isolation Improvements (High Priority)

**Goal:** Fix integration test anti-patterns and improve test isolation  
**Estimated Time:** 1-2 weeks  
**Status:** 🔴 Not Started

### Task 3.1: Remove Mocks from Integration Tests ⬜

**Problem:** 359 mock usages in integration tests (defeats purpose of integration testing)

**Anti-Pattern:**
```java
// ❌ Integration test with mocks
@SpringBootTest
class ProcessoIntegrationTest {
    @MockBean
    private ProcessoRepo processoRepo; // Should use real repo!
    
    @Test
    void test() {
        when(processoRepo.findById(1L))... // Not testing real integration
    }
}
```

**Correct Pattern:**
```java
// ✅ Real integration test
@SpringBootTest
class ProcessoIntegrationTest {
    @Autowired
    private ProcessoRepo processoRepo; // Real repo
    
    @Test
    void test() {
        processoRepo.save(...); // Real database interaction
    }
}
```

**Action Items:**
- [ ] Identify all integration tests in `sgc.integracao` package
- [ ] For each mock usage, decide:
  - Testing real integration? → Remove mock, use real dependency
  - Mock is necessary? → Move test to unit test file
- [ ] Focus on critical integration paths first
- [ ] Run integration tests after each change
- [ ] Update BaseIntegrationTest if needed

**Success Criteria:**
- [ ] Zero @MockBean usage in integration tests
- [ ] Zero Mockito usage in @SpringBootTest tests
- [ ] All integration tests use real Spring components
- [ ] Integration tests still pass
- [ ] Run: `./gradlew test --tests "sgc.integracao.*"`

**Complexity:** HIGH - Requires understanding test intent

**Notes:**
- Some tests may need database setup changes
- May require fixture enhancements
- Document decisions for each conversion

**Status:** ⬜ Not Started  
**Priority:** HIGH - Critical anti-pattern  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

### Task 3.2: Fix Timing Dependencies ⬜

**Problem:** 6 tests use Thread.sleep (causes flakiness and slow execution)

**Anti-Pattern:**
```java
// ❌ Flaky timing test
@Test
void test() {
    service.executarAsync();
    Thread.sleep(1000); // Unreliable!
    verify(mock).wasCalled();
}
```

**Correct Patterns:**
```java
// ✅ Use CompletableFuture
@Test
void test() {
    CompletableFuture<Void> future = service.executarAsync();
    future.join(); // Wait for completion
}

// ✅ Use Awaitility
@Test
void test() {
    service.executarAsync();
    await().atMost(5, SECONDS)
        .untilAsserted(() -> verify(mock).wasCalled());
}
```

**Action Items:**
- [ ] Find all Thread.sleep usages in tests
- [ ] Replace with Awaitility or proper async handling
- [ ] Add Awaitility dependency if needed
- [ ] Verify tests are more reliable
- [ ] Measure test execution time improvement

**Success Criteria:**
- [ ] Zero Thread.sleep calls in tests
- [ ] All async tests use proper wait mechanisms
- [ ] Tests are more reliable (no random failures)
- [ ] Test execution time same or improved

**Commands:**
```bash
# Find Thread.sleep usage
grep -rn "Thread.sleep" backend/src/test --include="*.java"
```

**Status:** ✅ NOT NEEDED - Verified no Thread.sleep usages  
**Assigned:** AI Agent  
**Started:** 2026-01-11  
**Completed:** 2026-01-11  

**Results:**
- ✅ Searched entire test codebase for Thread.sleep
- ✅ ZERO usages found
- ✅ Report finding outdated or already fixed
- ✅ No flaky timing dependencies to fix

---

### Task 3.3: Eliminate Static State ⬜

**Problem:** 3 test files use non-final static fields (potential shared state)

**Anti-Pattern:**
```java
// ❌ Shared mutable state
class MyTest {
    static Long processoIdGlobal; // BAD!
    
    @Test
    @Order(1)
    void test1() {
        processoIdGlobal = create();
    }
    
    @Test
    @Order(2)
    void test2() {
        use(processoIdGlobal); // Depends on test1!
    }
}
```

**Correct Pattern:**
```java
// ✅ Instance state with proper setup
class MyTest {
    private Long processoId;
    
    @BeforeEach
    void setup() {
        processoId = create();
    }
    
    @Test
    void test1() {
        use(processoId); // Independent
    }
    
    @Test
    void test2() {
        use(processoId); // Independent
    }
}
```

**Action Items:**
- [ ] Find all static non-final fields in tests
- [ ] Convert to instance fields with @BeforeEach setup
- [ ] If truly needed, use @DirtiesContext and document why
- [ ] Verify tests are independent

**Success Criteria:**
- [ ] Zero static mutable fields in tests
- [ ] All tests are independent (can run in any order)
- [ ] Document any necessary shared state

**Commands:**
```bash
# Find static fields in tests
grep -rn "static.*[^final]" backend/src/test --include="*.java" | grep -v "final"
```

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

## 📈 Phase 4: Coverage Gap Filling (Medium Priority)

**Goal:** Address critical coverage gaps in DTOs, Entities, and Mappers  
**Estimated Time:** 1-2 weeks  
**Status:** 🔴 Not Started

### Task 4.1: Add DTO Validation Tests ⬜

**Problem:** Only 1 test for 66 DTO classes (1.5% coverage)

**Current State:**
- Total DTOs: 66
- DTOs with tests: 1 (AutenticarReqValidationTest)
- Coverage: 1.5% ❌

**Target:** 30-40% coverage (20-30 DTOs with tests)

**Critical DTOs to Test:**
1. **Processo Module:**
   - `CriarProcessoReq` - Bean Validation
   - `AtualizarProcessoReq` - Bean Validation
   
2. **Subprocesso Module:**
   - `CriarSubprocessoReq`
   - `AtualizarSubprocessoReq`
   
3. **Mapa Module:**
   - Critical mapping DTOs with validation

4. **Other Modules:**
   - DTOs with complex validation rules

**Test Pattern:**
```java
@DisplayName("CriarProcessoReq - Validação")
class CriarProcessoReqValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Deve validar quando todos campos obrigatórios preenchidos")
    void deveValidarQuandoTodosCamposObrigatoriosPreenchidos() {
        CriarProcessoReq req = new CriarProcessoReq();
        req.setDescricao("Mapeamento 2024");
        req.setUnidadeCodigo(1L);
        
        Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);
        
        assertThat(violations).isEmpty();
    }
    
    @Test
    @DisplayName("Deve rejeitar quando descrição nula")
    void deveRejeitarQuandoDescricaoNula() {
        CriarProcessoReq req = new CriarProcessoReq();
        
        Set<ConstraintViolation<CriarProcessoReq>> violations = validator.validate(req);
        
        assertThat(violations)
            .extracting(v -> v.getPropertyPath().toString())
            .contains("descricao");
    }
}
```

**Action Items:**
- [ ] List all DTOs with Bean Validation annotations
- [ ] Prioritize DTOs in critical flows (Processo, Subprocesso, Mapa)
- [ ] Create validation test files for 20-30 DTOs
- [ ] Test: @NotNull, @NotBlank, @Size, @Pattern, custom validators
- [ ] Run tests to ensure validation works correctly

**Success Criteria:**
- [ ] 20-30 new DTO validation test files created
- [ ] Coverage: 30-40% of DTOs have tests
- [ ] All validation annotations tested
- [ ] All tests pass

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

### Task 4.2: Add Entity Tests ⬜

**Problem:** Only 6 tests for 54 entity classes (11% coverage)

**Current State:**
- Total Entities: 54
- Entities with tests: 6
- Coverage: 11% ❌

**Target:** 25-30% coverage (15-20 entities with tests)

**Entities with Complex Logic to Test:**

1. **Processo**
   - State machine logic
   - Transition validations
   - Business rule methods

2. **Subprocesso**
   - State transitions
   - Workflow methods

3. **Usuario**
   - Authorities calculation
   - Role management

4. **Unidade**
   - Hierarchy logic
   - Path calculations

5. **Other entities with business logic**

**Test Pattern:**
```java
@DisplayName("Processo - Lógica de Negócio")
class ProcessoTest {
    
    @Test
    @DisplayName("Deve permitir edição quando em situação CRIADO")
    void devePermitirEdicaoQuandoEmSituacaoCriado() {
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.CRIADO);
        
        boolean podeEditar = processo.podeEditar();
        
        assertThat(podeEditar).isTrue();
    }
    
    @Test
    @DisplayName("Deve bloquear edição quando FINALIZADO")
    void deveBloquerEdicaoQuandoFinalizado() {
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        
        boolean podeEditar = processo.podeEditar();
        
        assertThat(podeEditar).isFalse();
    }
    
    @Test
    @DisplayName("Deve adicionar participante quando unidade válida")
    void deveAdicionarParticipanteQuandoUnidadeValida() {
        Processo processo = new Processo();
        Unidade unidade = UnidadeFixture.unidadePadrao();
        
        processo.adicionarParticipante(unidade);
        
        assertThat(processo.getParticipantes()).contains(unidade);
    }
}
```

**Action Items:**
- [ ] Identify entities with business logic (not just getters/setters)
- [ ] Create test files for 15-20 entities with complex logic
- [ ] Test state machines, validations, calculations
- [ ] Focus on non-trivial methods
- [ ] Run tests

**Success Criteria:**
- [ ] 15-20 new entity test files created
- [ ] Coverage: 25-30% of entities have tests
- [ ] All business logic methods tested
- [ ] No trivial getter/setter tests
- [ ] All tests pass

**Notes:**
- Skip pure POJO entities with only getters/setters
- Focus on entities with business rules
- Use fixtures for entity creation

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

### Task 4.3: Add Mapper Tests ⬜

**Problem:** 12 mappers, only 7 tested (58% coverage)

**Current State:**
- Total Mappers: 12
- Mappers with tests: 7
- Coverage: 58% ⚠️

**Target:** 80%+ coverage (10-11 mappers with tests)

**Missing Mapper Tests:**
- Identify the 5 untested mappers
- Create comprehensive test suites

**Test Pattern:**
```java
@DisplayName("ProcessoMapper")
class ProcessoMapperTest {
    
    private ProcessoMapper mapper = Mappers.getMapper(ProcessoMapper.class);
    
    @Test
    @DisplayName("Deve mapear entidade para DTO com todos campos")
    void devemapearEntidadeParaDtoComTodosCampos() {
        Processo processo = ProcessoFixture.processoPadrao();
        
        ProcessoDto dto = mapper.toDto(processo);
        
        assertAll(
            () -> assertThat(dto.getCodigo()).isEqualTo(processo.getCodigo()),
            () -> assertThat(dto.getDescricao()).isEqualTo(processo.getDescricao()),
            () -> assertThat(dto.getSituacao()).isEqualTo(processo.getSituacao())
        );
    }
    
    @Test
    @DisplayName("Deve retornar null quando entidade null")
    void deveRetornarNullQuandoEntidadeNull() {
        ProcessoDto dto = mapper.toDto(null);
        
        assertThat(dto).isNull();
    }
    
    @Test
    @DisplayName("Deve mapear coleção de entidades")
    void devemapearColecaoDeEntidades() {
        List<Processo> processos = Arrays.asList(
            ProcessoFixture.processoPadrao(),
            ProcessoFixture.processoPadrao()
        );
        
        List<ProcessoDto> dtos = mapper.toDtoList(processos);
        
        assertThat(dtos).hasSize(2);
    }
    
    @Test
    @DisplayName("Deve mapear objetos nested corretamente")
    void devemapearObjetosNestedCorretamente() {
        Processo processo = ProcessoFixture.processoComUnidade(UnidadeFixture.unidadePadrao());
        
        ProcessoDto dto = mapper.toDto(processo);
        
        assertThat(dto.getUnidadeCodigo()).isNotNull();
    }
}
```

**Action Items:**
- [ ] List all mappers in the project
- [ ] Identify the 5 mappers without tests
- [ ] Create test files following MapStruct testing pattern
- [ ] Test null handling
- [ ] Test collection mapping
- [ ] Test nested object mapping
- [ ] Test bidirectional mapping (toDto + toEntity)
- [ ] Run tests

**Success Criteria:**
- [ ] 5 new mapper test files created
- [ ] Coverage: 80%+ of mappers have tests
- [ ] Null handling tested
- [ ] Collection mapping tested
- [ ] Nested objects tested
- [ ] All tests pass

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

## 🔧 Phase 5: Test Maintainability (Low Priority)

**Goal:** Clean up technical debt in test code  
**Estimated Time:** 1 week  
**Status:** 🔴 Not Started

### Task 5.1: Resolve TODOs ⬜

**Problem:** 4 test files contain TODO/FIXME comments

**TODO Examples:**
- `// TODO esse teste esta muito complexo e pesado e tem asserts demais`
- `// TODO não gosto dessa exceção!`
- `// TODO esse teste está muito lento`

**Action Items:**
- [ ] Find all TODO/FIXME comments in test files
- [ ] For each TODO:
  - Fix the issue, OR
  - Document why it can't be fixed, OR
  - Remove if no longer relevant
- [ ] Update tests after fixing TODOs
- [ ] Run affected tests

**Commands:**
```bash
# Find all TODOs in test files
grep -rn "TODO\|FIXME" backend/src/test --include="*.java"
```

**Success Criteria:**
- [ ] All TODOs reviewed
- [ ] Complex tests simplified or justified
- [ ] Slow tests optimized or marked @Disabled with reason
- [ ] All tests still pass

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

### Task 5.2: Extract Long Test Methods ⬜

**Problem:** Some test methods exceed 100 lines (CDU20IntegrationTest has 117-line test)

**Anti-Pattern:**
```java
// ❌ Very long test method
@Test
void testComplexScenario() {
    // 117 lines of setup, execution, assertions...
}
```

**Correct Pattern:**
```java
// ✅ Split into smaller focused tests
@Nested
class ComplexScenario {
    
    @BeforeEach
    void setupScenario() {
        // Common setup
    }
    
    @Test
    void deveValidarPrimeiraEtapa() {
        // 20-30 lines
    }
    
    @Test
    void deveValidarSegundaEtapa() {
        // 20-30 lines
    }
}
```

**Action Items:**
- [ ] Find all test methods >100 lines
- [ ] Split into multiple smaller test methods
- [ ] Extract common setup to @BeforeEach
- [ ] Use @Nested for related tests
- [ ] Target: Max 50 lines per test method
- [ ] Run tests to ensure behavior preserved

**Commands:**
```bash
# Find long test methods (manual review needed)
find backend/src/test -name "*.java" -exec wc -l {} \; | sort -rn | head -20
```

**Success Criteria:**
- [ ] No test methods >100 lines
- [ ] Most test methods <50 lines
- [ ] Tests more focused and readable
- [ ] All tests still pass

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

### Task 5.3: Reduce Test Duplication with Utilities ⬜

**Problem:** Common test setup patterns repeated across many tests

**Solution:** Create shared test helper utilities

**Proposed Helpers:**
- `ProcessoTestHelper` - Common processo setup/assertions
- `SubprocessoTestHelper` - Common subprocesso operations
- `MapaTestHelper` - Common mapa setup
- `SecurityTestHelper` - Common security setup
- `AssertionHelper` - Custom assertions

**Example:**
```java
// Before: Repeated in many tests
@BeforeEach
void setup() {
    unidade = unidadeRepo.save(UnidadeFixture.unidadePadrao());
    usuario = usuarioRepo.save(UsuarioFixture.usuarioPadrao());
    processo = processoRepo.save(ProcessoFixture.processoComUnidade(unidade));
}

// After: Use helper
@BeforeEach
void setup() {
    ProcessoTestHelper helper = new ProcessoTestHelper(unidadeRepo, usuarioRepo, processoRepo);
    processo = helper.criarProcessoCompleto();
}
```

**Action Items:**
- [ ] Identify common test setup patterns
- [ ] Create helper classes in `sgc/util` test package
- [ ] Refactor tests to use helpers
- [ ] Document helper usage
- [ ] Verify tests still pass

**Success Criteria:**
- [ ] 3-5 test helper classes created
- [ ] Reduced duplication in test setup
- [ ] Tests more concise and readable
- [ ] All tests still pass

**Status:** ⬜ Not Started  
**Assigned:** -  
**Started:** -  
**Completed:** -

---

## 📚 Documentation & Policies

### Create Test Organization Policy ⬜

**Goal:** Establish clear guidelines for test organization

**Content:** Create `regras/test-organization-policy.md` with:
1. Test file naming conventions
2. When to split tests into multiple files
3. Unit vs Integration test separation
4. Coverage targets by layer
5. Test review checklist
6. Guidelines on @Nested usage
7. When to use @Tag

**Status:** ⬜ Not Started

---

### Update Test Guide ⬜

**Goal:** Update `regras/guia-testes-junit.md` with learnings

**Updates:**
- Add anti-patterns discovered
- Add examples from consolidation work
- Update best practices based on improvements
- Add section on test organization

**Status:** ⬜ Not Started

---

## 📊 Success Metrics & Validation

### Phase 1 Metrics
- [ ] Test files reduced from 173 to ~140 (-20%)
- [ ] Average test file size <500 lines
- [ ] Zero test files >800 lines
- [ ] Test execution time unchanged or improved
- [ ] Run: `./gradlew :backend:test` - All pass

### Phase 3 Metrics
- [ ] Zero @MockBean in integration tests
- [ ] Zero Mockito in @SpringBootTest tests
- [ ] Zero Thread.sleep calls
- [ ] Zero static mutable fields
- [ ] Run: `./gradlew test --tests "sgc.integracao.*"` - All pass

### Phase 4 Metrics
- [ ] DTO coverage: 30-40% (from 1.5%)
- [ ] Entity coverage: 25-30% (from 11%)
- [ ] Mapper coverage: 80%+ (from 58%)
- [ ] Run: `./gradlew :backend:jacocoTestReport` - Verify coverage

### Final Validation
- [ ] Overall Quality Score: 8.5/10 (from 6.5/10)
- [ ] Test Organization: 9/10 (from 5/10)
- [ ] Maintainability: 8/10 (from 4/10)
- [ ] Coverage: 8/10 (from varies)
- [ ] All 1,151+ tests still pass
- [ ] No regression in code coverage
- [ ] CI/CD pipeline passes

---

## 🚀 Next Steps

### Immediate Actions (Start Now)
1. **Begin Phase 1, Task 1.1:** Consolidate UsuarioService tests
   - This is a good starting point - moderate complexity
   - Clear success criteria
   - Good learning for other consolidations

2. **Investigate mock usage:** Run analysis to understand the 359 mock usages
   ```bash
   grep -rn "@MockBean\|@Mock" backend/src/test/java/sgc/integracao --include="*.java"
   ```

3. **Set up tracking system:** Review this file weekly

### Weekly Review Schedule
- **Monday:** Review progress, plan week's tasks
- **Wednesday:** Mid-week check-in, adjust if needed
- **Friday:** Document learnings, update metrics

### Risk Management
- **Before Each Task:** Run full test suite, record baseline
- **During Task:** Make incremental changes, test frequently
- **After Task:** Verify all tests pass, no coverage loss
- **Rollback Plan:** One task = one commit, revert if needed

---

## 📝 Notes & Learnings

### Discoveries During Implementation
_Document unexpected findings, edge cases, and lessons learned here_

### Challenges Faced
_Track difficulties and how they were resolved_

### Decisions Made
_Record key decisions and rationale_

---

## 📞 Contacts & Resources

### Key Resources
- [Test Quality Report](test-quality-report.md) - Original analysis
- [Test Guide](regras/guia-testes-junit.md) - Testing standards
- [Backend Patterns](regras/backend-padroes.md) - Code patterns
- [Architecture Decisions](/docs/adrs/) - ADR documents

### Commands Reference
```bash
# Run all backend tests
./gradlew :backend:test

# Run specific module tests
./gradlew :backend:test --tests "sgc.processo.*"

# Run integration tests only
./gradlew :backend:test --tests "sgc.integracao.*"

# Generate coverage report
./gradlew :backend:jacocoTestReport

# Quality checks
./gradlew :backend:qualityCheck
./gradlew :backend:qualityCheckFast

# Find test patterns
grep -rn "@DisplayName" backend/src/test --include="*.java" | wc -l
grep -rn "@Nested" backend/src/test --include="*.java" | wc -l
grep -rn "Thread.sleep" backend/src/test --include="*.java"
```

---

**Last Updated:** 2026-01-11  
**Next Review:** 2026-01-18  
**Status:** 🔴 Planning Phase - Ready to start Phase 1
