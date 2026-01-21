# Guia de Testes JUnit - SGC

## 📋 Visão Geral

Este guia fornece recomendações e boas práticas para criar novos testes unitários e de integração com JUnit no Sistema de Gestão de Competências (SGC). 

## 🎯 Objetivos dos Testes

1. **Confiabilidade**: Testes devem ser determinísticos e não apresentar flakiness
2. **Manutenibilidade**: Código de teste claro, organizado e fácil de evoluir
3. **Isolamento**: Cada teste deve ser independente e autossuficiente
4. **Documentação Viva**: Testes devem documentar o comportamento esperado do sistema

## 🛠️ Stack de Testes

- **Framework**: JUnit
- **Mocking**: Mockito
- **Assertions**: AssertJ (preferencial) e JUnit Assertions
- **Spring Testing**: `@SpringBootTest`, `@Transactional`
- **Banco de Dados de Teste**: H2 em memória
- **Cobertura**: JaCoCo

## 📐 Padrões Fundamentais

### 1. Nomenclatura de Métodos

**Padrão Obrigatório**: `deve{Acao}Quando{Condicao}`

```java
@Test
@DisplayName("Deve criar processo quando dados válidos")
void deveCriarProcessoQuandoDadosValidos() {
    // ...
}

@Test
@DisplayName("Deve lançar exceção quando processo não encontrado")
void deveLancarExcecaoQuandoProcessoNaoEncontrado() {
    // ...
}
```

**❌ Evite:**
- `test{Acao}` (padrão antigo)
- `test_cenario_especifico` (com underscore)
- Nomes em inglês

### 2. Anotação @DisplayName

**Obrigatória** em todos os testes. Deve ser uma frase descritiva em português.

```java
@Test
@DisplayName("Deve retornar lista vazia quando não houver processos")
void deveRetornarListaVaziaQuandoNaoHouverProcessos() {
    // ...
}
```

### 3. Estrutura AAA (Arrange-Act-Assert)

Organize sempre seus testes em três seções claras:

```java
@Test
@DisplayName("Deve atualizar situação do processo")
void deveAtualizarSituacaoProcesso() {
    // Arrange
    Processo processo = ProcessoFixture.processoPadrao();
    when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
    
    // Act
    service.atualizarSituacao(1L, SituacaoProcesso.EM_ANDAMENTO);
    
    // Assert
    assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
    verify(processoRepo).save(processo);
}
```

### 4. Organização com @Nested

Para classes de teste com mais de 10 métodos, agrupe cenários relacionados:

```java
@DisplayName("ProcessoService")
class ProcessoServiceTest {
    
    @Nested
    @DisplayName("Criação de processo")
    class CriacaoProcesso {
        
        @Test
        @DisplayName("Deve criar processo quando dados válidos")
        void deveCriarProcessoQuandoDadosValidos() { }
        
        @Test
        @DisplayName("Deve lançar exceção quando dados inválidos")
        void deveLancarExcecaoQuandoDadosInvalidos() { }
    }
    
    @Nested
    @DisplayName("Atualização de processo")
    class AtualizacaoProcesso {
        
        @Test
        @DisplayName("Deve atualizar processo existente")
        void deveAtualizarProcessoExistente() { }
    }
}
```

## 🧪 Testes Unitários

### Configuração Básica

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService")
class ProcessoServiceTest {
    
    @Mock
    private ProcessoRepo processoRepo;
    
    @Mock
    private UsuarioRepo usuarioRepo;
    
    @InjectMocks
    private ProcessoService service;
    
    // Testes aqui
}
```

### Uso de Fixtures

**Sempre use fixtures** do pacote `sgc.fixture` para criar entidades de teste:

```java
@Test
@DisplayName("Deve criar processo com unidade associada")
void deveCriarProcessoComUnidadeAssociada() {
    // Arrange
    Unidade unidade = UnidadeFixture.unidadePadrao();
    Processo processo = ProcessoFixture.processoComUnidade(unidade);
    
    when(processoRepo.save(any())).thenReturn(processo);
    
    // Act
    Processo resultado = service.criar(processo);
    
    // Assert
    assertThat(resultado.getParticipantes()).contains(unidade);
}
```

**Fixtures disponíveis:**
- `ProcessoFixture`
- `SubprocessoFixture`
- `MapaFixture`
- `AtividadeFixture`
- `CompetenciaFixture`
- `UnidadeFixture`
- `UsuarioFixture`
- `AlertaFixture`

### Asserções com AssertJ

Prefira AssertJ para asserções mais expressivas:

```java
// ✅ Bom - AssertJ
assertThat(resultado).isNotNull();
assertThat(resultado.getNome()).isEqualTo("Esperado");
assertThat(resultado.getItens()).hasSize(3);

// ❌ Evite - JUnit assertions
assertTrue(resultado != null);
assertEquals("Esperado", resultado.getNome());
assertEquals(3, resultado.getItens().size());
```

### Verificação de Exceções

Sempre verifique o **tipo**, **mensagem** e **causa** da exceção:

```java
@Test
@DisplayName("Deve lançar ErroEntidadeNaoEncontrada quando processo não existe")
void deveLancarErroQuandoProcessoNaoExiste() {
    // Arrange
    when(processoRepo.findById(1L)).thenReturn(Optional.empty());
    
    // Act & Assert
    assertThatThrownBy(() -> service.buscarPorId(1L))
        .isInstanceOf(ErroEntidadeNaoEncontrada.class)
        .hasMessageContaining("Processo")
        .hasNoCause();
}
```

### Testes Parametrizados

Use `@ParameterizedTest` para testar múltiplos cenários similares:

```java
@ParameterizedTest
@CsvSource({
    "CRIADO, true",
    "EM_ANDAMENTO, true",
    "FINALIZADO, false",
    "CANCELADO, false"
})
@DisplayName("Deve verificar se processo pode ser editado por situação")
void deveVerificarSeProcessoPodeSerEditado(SituacaoProcesso situacao, boolean esperado) {
    // Arrange
    Processo processo = ProcessoFixture.processoPadrao();
    processo.setSituacao(situacao);
    
    // Act
    boolean resultado = service.podeEditar(processo);
    
    // Assert
    assertThat(resultado).isEqualTo(esperado);
}
```

### Verificações Múltiplas com assertAll

Para verificar múltiplos aspectos de um objeto:

```java
@Test
@DisplayName("Deve criar processo com todos os campos preenchidos")
void deveCriarProcessoComTodosCamposPreenchidos() {
    // Arrange & Act
    Processo resultado = service.criar(dados);
    
    // Assert
    assertAll(
        () -> assertThat(resultado.getCodigo()).isNotNull(),
        () -> assertThat(resultado.getDescricao()).isEqualTo("Mapeamento 2024"),
        () -> assertThat(resultado.getSituacao()).isEqualTo(SituacaoProcesso.CRIADO),
        () -> assertThat(resultado.getDataCriacao()).isNotNull()
    );
}
```

### Mockito - Boas Práticas

**❌ NUNCA use `Strictness.LENIENT`**

```java
// ❌ Proibido
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessoServiceTest {
    // ...
}
```

**✅ Configure stubs apenas quando necessário:**

```java
@Test
@DisplayName("Deve buscar processo por código")
void deveBuscarProcessoPorCodigo() {
    // Arrange - Apenas o stub necessário
    Processo processo = ProcessoFixture.processoPadrao();
    when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
    
    // Act
    Processo resultado = service.buscarPorId(1L);
    
    // Assert
    assertThat(resultado).isEqualTo(processo);
}
```

## 🔗 Testes de Integração

### Configuração Básica

```java
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-XX: Nome do Caso de Uso")
@Import(TestSecurityConfig.class)
class CDUXXIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private ProcessoRepo processoRepo;
    
    @Autowired
    private UnidadeRepo unidadeRepo;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // Testes aqui
}
```

### Princípio Fundamental: Setup Programático

**❌ NUNCA dependa de IDs hardcoded ou dados do `data.sql`**

```java
// ❌ Ruim - Dependência de seed global
@Test
void teste() {
    Processo processo = processoRepo.findById(99L).get(); // ID fixo do data.sql
    // ...
}
```

**✅ Sempre crie dados explicitamente no teste:**

```java
@BeforeEach
void setUp() {
    // Reiniciar sequências para evitar conflito
    jdbcTemplate.execute("ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 80000");
    
    // Criar dados explicitamente
    unidade = unidadeRepo.save(UnidadeFixture.unidadePadrao());
    usuario = usuarioRepo.save(UsuarioFixture.usuarioPadrao());
    processo = processoRepo.save(ProcessoFixture.processoComUnidade(unidade));
}

@Test
@DisplayName("Deve iniciar processo criado no setup")
void deveIniciarProcessoCriadoNoSetup() {
    // Arrange
    Long processoId = processo.getCodigo(); // ID dinâmico
    
    // Act
    service.iniciar(processoId);
    
    // Assert
    Processo atualizado = processoRepo.findById(processoId).get();
    assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
}
```

### Gestão de Sequências H2

Para evitar conflitos com dados imutáveis (Views como `VW_UNIDADE`), reinicie sequências:

```java
@BeforeEach
void setup() {
    try {
        jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 20000");
        jdbcTemplate.execute("ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 80000");
        jdbcTemplate.execute("ALTER TABLE SGC.ALERTA ALTER COLUMN CODIGO RESTART WITH 90000");
    } catch (Exception e) {
        System.err.println("Aviso: Não foi possível reiniciar sequências: " + e.getMessage());
    }
}
```

### Entidades Imutáveis e JdbcTemplate

Para dados de referência (usuários, perfis) em testes transacionais, use `JdbcTemplate`:

```java
@BeforeEach
void setUp() {
    // Inserir perfil via JDBC (evita conflitos de transação)
    jdbcTemplate.update(
        "INSERT INTO SGC.PERFIL (CODIGO, NOME) VALUES (?, ?)",
        1L, "ROLE_ADMIN"
    );
    
    // Inserir usuário via JDBC
    jdbcTemplate.update(
        "INSERT INTO SGC.USUARIO (CPF, NOME, EMAIL) VALUES (?, ?, ?)",
        "12345678901", "Usuario Teste", "teste@example.com"
    );
    
    // Agora pode usar entidades gerenciadas
    processo = processoRepo.save(ProcessoFixture.processoPadrao());
}
```

### Hierarquias de Entidades

Para testes que requerem hierarquia (unidades pai/filho):

```java
@BeforeEach
void setUp() {
    // Criar hierarquia
    Unidade raiz = UnidadeFixture.unidadePadrao();
    raiz.setCodigo(null); // Auto-increment
    raiz.setNome("Unidade Raiz");
    raiz = unidadeRepo.save(raiz);
    
    Unidade filha = UnidadeFixture.unidadePadrao();
    filha.setCodigo(null);
    filha.setNome("Unidade Filha");
    filha.setUnidadeSuperior(raiz);
    filha = unidadeRepo.save(filha);
}
```

### Testes de API REST

Use `MockMvc` para testar endpoints:

```java
@Test
@DisplayName("Deve listar processos via API")
void deveListarProcessosViaAPI() throws Exception {
    // Arrange
    processoRepo.save(ProcessoFixture.processoPadrao());
    
    // Act & Assert
    mockMvc.perform(get("/api/processos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
}
```

### Testes de Eventos

Para validar publicação e consumo de eventos:

```java
@SpringBootTest
@DisplayName("Testes de Eventos de Processo")
class ProcessoEventoIntegrationTest {
    
    @Autowired
    private ProcessoService processoService;
    
    @Autowired
    private AlertaRepo alertaRepo;
    
    @Test
    @DisplayName("Deve criar alerta quando processo iniciado")
    void deveCriarAlertaQuandoProcessoIniciado() {
        // Arrange
        Processo processo = processoRepo.save(ProcessoFixture.processoPadrao());
        Long contadorAntes = alertaRepo.count();
        
        // Act
        processoService.iniciar(processo.getCodigo());
        
        // Assert
        Long contadorDepois = alertaRepo.count();
        assertThat(contadorDepois).isGreaterThan(contadorAntes);
    }
}
```

## ❌ Anti-Padrões (O que EVITAR)

### 1. Magic Numbers

```java
// ❌ Ruim
Processo processo = processoRepo.findById(99L).get();

// ✅ Bom
Processo processo = processoRepo.save(ProcessoFixture.processoPadrao());
Long processoId = processo.getCodigo();
```

### 2. Thread.sleep (Flakiness)

```java
// ❌ Nunca faça isso
@Test
void teste() {
    service.executarAsync();
    Thread.sleep(1000); // PROIBIDO!
    // verificar resultado
}

// ✅ Use mecanismos adequados
@Test
void teste() {
    CompletableFuture<Void> future = service.executarAsync();
    future.join(); // Ou use awaitility
}
```

### 3. Testes Acoplados

```java
// ❌ Ruim - Dependência entre testes
static Long processoIdGlobal;

@Test
@Order(1)
void deveCriarProcesso() {
    processoIdGlobal = service.criar().getCodigo();
}

@Test
@Order(2)
void deveAtualizarProcesso() {
    service.atualizar(processoIdGlobal); // Depende do teste anterior
}

// ✅ Bom - Cada teste é independente
@Test
void deveCriarProcesso() {
    Long id = service.criar().getCodigo();
    assertThat(id).isNotNull();
}

@Test
void deveAtualizarProcesso() {
    Processo processo = processoRepo.save(ProcessoFixture.processoPadrao());
    service.atualizar(processo.getCodigo());
}
```

### 4. Testes de Getters/Setters Sem Valor

```java
// ❌ Remova testes boilerplate
@Test
void testGetNome() {
    processo.setNome("Teste");
    assertEquals("Teste", processo.getNome());
}

// ✅ Teste apenas lógica real de negócio
@Test
@DisplayName("Deve validar nome do processo")
void deveValidarNomeProcesso() {
    assertThatThrownBy(() -> processo.setNome(null))
        .isInstanceOf(ErroValidacao.class);
}
```

## 📊 Cobertura de Código

### Gerar Relatório

```bash
# Executar testes e gerar relatório
./gradlew :backend:test :backend:jacocoTestReport

# Visualizar no navegador
open backend/build/reports/jacoco/test/html/index.html
```

### Quality Gates

O projeto possui limites mínimos configurados:
- **Branches**: 60%
- **Linhas**: 80%

```bash
# Verificar quality gate
./gradlew :backend:jacocoTestCoverageVerification

# Quality gate é executado automaticamente no check
./gradlew :backend:check
```

### Métricas Atuais (Baseline)

- **Linhas**: 85.9%
- **Branches**: 62.1%
- **Instruções**: 84.6%
- **Métodos**: 83.5%
- **Classes**: 91.3%

## 🔍 Comandos Úteis

### Executar Testes

```bash
# Todos os testes do backend
./gradlew :backend:test

# Apenas testes de integração
./gradlew :backend:test --tests "sgc.integracao.*"

# Apenas testes de um módulo
./gradlew :backend:test --tests "sgc.processo.*"

# Teste específico
./gradlew :backend:test --tests "sgc.processo.service.ProcessoServiceTest"
```

### Verificações de Qualidade

```bash
# Quality check completo (Checkstyle, PMD, SpotBugs, JaCoCo)
./gradlew :backend:qualityCheck

# Quality check rápido (sem SpotBugs)
./gradlew :backend:qualityCheckFast
```

### Análise de Padrões

```bash
# Contar testes com @DisplayName
grep -R "@DisplayName" backend/src/test --include="*.java" | wc -l

# Contar testes com @Nested
grep -R "@Nested" backend/src/test --include="*.java" | wc -l

# Verificar uso de LENIENT (deve retornar 0)
grep -R "Strictness.LENIENT" backend/src/test --include="*.java"

# Listar fixtures disponíveis
ls -la backend/src/test/java/sgc/fixture/
```

## 📚 Checklist para Novos Testes

Ao criar um novo teste, verifique:

- [ ] Nome do método segue padrão `deve{Acao}Quando{Condicao}`
- [ ] Anotação `@DisplayName` presente e descritiva
- [ ] Estrutura AAA (Arrange-Act-Assert) explícita com comentários
- [ ] Usa fixtures do pacote `sgc.fixture`
- [ ] Não possui magic numbers ou IDs hardcoded
- [ ] Exceções verificadas incluem tipo, mensagem e causa
- [ ] Não usa `Thread.sleep` ou outros mecanismos de flakiness
- [ ] Não usa `Strictness.LENIENT`
- [ ] Asserções usam AssertJ quando possível
- [ ] Testes de integração não dependem de `data.sql`
- [ ] Classe com >10 testes organizada com `@Nested`
- [ ] Código roda com `./gradlew :backend:test`

### Documentos de Arquitetura

- [Padrões Backend](/etc/regrasgras/backend-padroes.md)
- [Backend README](/README.md)
- [AGENTS.md](/AGENTS.md)

## 💡 Dicas Práticas

### Para Testes Unitários

1. **Isole a unidade**: Mock todas as dependências
2. **Um conceito por teste**: Teste apenas uma coisa por método
3. **Nomes descritivos**: O nome do teste deve documentar o comportamento
4. **Fixtures para setup**: Reutilize builders para reduzir duplicação

### Para Testes de Integração

1. **Setup explícito**: Crie todos os dados no `@BeforeEach`
2. **Reinicie sequências**: Evite conflitos com entidades imutáveis
3. **Use JdbcTemplate para dados de referência**: Evita problemas transacionais
4. **Teste o fluxo completo**: Valide interações entre camadas

### Para Ambos

1. **Leia a mensagem de erro**: Testes devem falhar de forma clara
2. **Mantenha testes rápidos**: Evite operações desnecessárias
3. **Refatore testes**: Código de teste também precisa de manutenção
4. **Documente casos especiais**: Use comentários para situações não-óbvias
