# Análise de Testes Backend - Sistema SGC

**Data:** 17 de dezembro de 2025  
**Total de Testes:** 97 arquivos de teste  
**Status Atual:** ✅ Todos os testes passando

---

## 1. Sumário Executivo

Esta análise examina a suite de testes do backend do Sistema de Gestão de Competências (SGC), identificando problemas, dívida técnica e oportunidades de melhoria. Os testes estão funcionais, mas apresentam inconsistências significativas em padrões, cobertura e manutenibilidade.

### Principais Achados

- ✅ **Positivo:** Boa cobertura de casos de sucesso e erro
- ✅ **Positivo:** Uso consistente de bibliotecas modernas (JUnit 5, Mockito, AssertJ)
- ⚠️ **Atenção:** Inconsistência em convenções de nomenclatura
- ⚠️ **Atenção:** Duplicação de código de setup entre testes
- ⚠️ **Atenção:** Testes de integração excessivamente dependentes de dados específicos
- ❌ **Problema:** Testes de DTO/Model sem valor agregado
- ❌ **Problema:** Uso inconsistente de `@DisplayName`
- ❌ **Problema:** `MockitoSettings(strictness = LENIENT)` mascarando problemas

---

## 2. Análise por Categoria

### 2.1. Consistência

#### 🔴 Problemas Críticos

**P1: Inconsistência na Nomenclatura de Métodos de Teste**

Existem três padrões diferentes sendo usados simultaneamente:

```java
// Padrão 1: DisplayName descritivo (RECOMENDADO)
@Test
@DisplayName("Criar processo deve persistir e publicar evento")
void criar() { }

// Padrão 2: Nome de método descritivo sem DisplayName
@Test
void criarProcessoComSucesso() { }

// Padrão 3: Nome de método em português com underscore
@Test
void obterPorCodigo_NaoEncontrado() { }
```

**Impacto:** Dificulta leitura, manutenção e geração de relatórios.

**Exemplos:**
- `ProcessoServiceTest.java`: Usa `@DisplayName` + nomes curtos
- `MapaServiceTest.java`: Usa nomes de método descritivos
- `AtividadeServiceTest.java`: Mistura os dois padrões

**Recomendação:**
```java
// PADRÃO RECOMENDADO: Nome descritivo + DisplayName em português
@Test
@DisplayName("Deve criar processo com sucesso e publicar evento")
void deveCriarProcessoComSucesso() { }

@Test
@DisplayName("Deve lançar ErroEntidadeNaoEncontrada quando processo não existir")
void deveLancarErroQuandoProcessoNaoExistir() { }
```

---

**P2: Uso Inconsistente de `@MockitoSettings(strictness = LENIENT)`**

```java
// Encontrado em 8 classes de teste
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessoServiceTest { }
```

**Problema:** O modo `LENIENT` desabilita warnings sobre stubs não utilizados, mascarando problemas de qualidade nos testes.

**Impacto:**
- Permite mocks configurados mas nunca chamados (código morto)
- Dificulta identificar testes que precisam de refatoração
- Indica configuração excessiva de mocks

**Recomendação:**
- **Remover** `LENIENT` e corrigir os warnings
- Usar `lenient()` apenas para casos específicos necessários
- Revisar testes com muitos mocks (possível violação de SRP)

---

**P3: Organização Inconsistente de Classes de Teste**

```java
// Alguns testes usam @Nested classes
@Nested
@DisplayName("Testes de fluxo de login completo")
class FluxoLoginTests { }

// Outros não usam, mas deveriam
class SubprocessoWorkflowServiceTest {
    // 20+ métodos de teste sem organização
}
```

**Recomendação:** Usar `@Nested` para agrupar testes relacionados:

```java
@Nested
@DisplayName("Disponibilizar Cadastro")
class DisponibilizarCadastro {
    @Test
    @DisplayName("Deve disponibilizar com sucesso")
    void deveDisponibilizarComSucesso() { }
    
    @Test
    @DisplayName("Deve lançar ErroAccessoNegado quando usuário não autorizado")
    void deveLancarErroAcessoNegadoQuandoNaoAutorizado() { }
}
```

---

#### 🟡 Problemas Moderados

**P4: Convenções de Nomes de Variáveis**

```java
// Inconsistente
Processo p = new Processo();     // Abreviado
Unidade u = new Unidade();       // Abreviado
Usuario user = new Usuario();    // Inglês
Subprocesso sp = new Subprocesso(); // Abreviado
Subprocesso subprocesso = new Subprocesso(); // Completo
```

**Recomendação:** Padronizar nomes descritivos em português:

```java
Processo processo = new Processo();
Unidade unidade = new Unidade();
Usuario usuario = new Usuario();
Subprocesso subprocesso = new Subprocesso();

// Para múltiplas instâncias
Unidade unidadeOrigem = new Unidade();
Unidade unidadeDestino = new Unidade();
```

---

### 2.2. Robustez

#### 🔴 Problemas Críticos

**P5: Testes Frágeis com Dados Hardcoded**

```java
// CDU01IntegrationTest.java
String tituloEleitoral = "111111111111"; // ADMIN hardcoded
String tituloEleitoral = "999999999999"; // GESTOR hardcoded

// ProcessoServiceTest.java
when(processoRepo.findById(99L)).thenReturn(Optional.empty()); // Magic number
```

**Problema:** Testes dependem de dados específicos do `data.sql` e magic numbers.

**Impacto:**
- Testes quebram se dados de teste mudarem
- Dificulta entendimento do teste
- Reduz portabilidade

**Recomendação:**

```java
// Usar constantes ou builders
private static final String ADMIN_TITULO_ELEITORAL = "111111111111";
private static final Long ID_PROCESSO_INEXISTENTE = 99L;

// Ou melhor ainda: criar fixtures/builders
public class ProcessoFixture {
    public static Processo criarProcessoValido() {
        return Processo.builder()
            .descricao("Processo de Teste")
            .tipo(TipoProcesso.MAPEAMENTO)
            .situacao(SituacaoProcesso.CRIADO)
            .build();
    }
}
```

---

**P6: Falta de Validação de Estado Completo**

```java
// Teste verifica apenas uma propriedade
processoService.atualizar(id, req);
assertThat(processo.getDescricao()).isEqualTo("Nova Desc");
verify(processoRepo).saveAndFlush(processo);

// Não verifica se outras propriedades foram mantidas
// Não verifica se situação ainda é válida
// Não verifica relacionamentos
```

**Recomendação:**

```java
processoService.atualizar(id, req);

// Verificações mais completas
assertThat(processo.getDescricao()).isEqualTo("Nova Desc");
assertThat(processo.getTipo()).isEqualTo(TipoProcesso.MAPEAMENTO);
assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.CRIADO);
assertThat(processo.getParticipantes()).hasSize(1);
verify(processoRepo).saveAndFlush(processo);
```

---

**P7: Testes de Exceção Incompletos**

```java
// Verifica apenas o tipo da exceção
assertThatThrownBy(() -> processoService.apagar(99L))
    .isInstanceOf(ErroEntidadeNaoEncontrada.class);

// Melhor: verificar também a mensagem
assertThatThrownBy(() -> processoService.apagar(99L))
    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
    .hasMessageContaining("Processo")
    .hasMessageContaining("99");
```

---

#### 🟡 Problemas Moderados

**P8: Configuração Excessiva de Mocks (Over-Mocking)**

```java
// ProcessoServiceTest.java - 12 mocks injetados!
@Mock private ProcessoRepo processoRepo;
@Mock private UnidadeRepo unidadeRepo;
@Mock private SubprocessoRepo subprocessoRepo;
@Mock private ApplicationEventPublisher publicadorEventos;
@Mock private ProcessoMapper processoMapper;
@Mock private sgc.processo.service.ProcessoDetalheBuilder processoDetalheBuilder;
@Mock private MapaRepo mapaRepo;
@Mock private SubprocessoMovimentacaoRepo movimentacaoRepo;
@Mock private SubprocessoMapper subprocessoMapper;
@Mock private CopiaMapaService servicoDeCopiaDeMapa;
@Mock private SgrhService sgrhService;
@Mock private UnidadeMapaRepo unidadeMapaRepo;

@InjectMocks private ProcessoService processoService;
```

**Problema:** Indica possível violação do Single Responsibility Principle.

**Impacto:**
- Testes complexos e difíceis de manter
- Service com muitas responsabilidades
- Acoplamento excessivo

**Recomendação:**
- Avaliar se o service precisa ser decomposto
- Considerar usar teste de integração em vez de tanto mocking
- Usar objetos reais quando possível (ex: mappers)

---

**P9: Falta de Testes de Casos Extremos**

```java
// Faltam testes para:
// - Strings vazias vs null
// - Listas vazias vs null
// - Valores limite (MAX_VALUE, MIN_VALUE)
// - Concorrência
// - Transações rollback
```

**Exemplos necessários:**

```java
@Test
@DisplayName("Deve tratar corretamente descrição com espaços em branco")
void deveTratarDescricaoComEspacos() { }

@Test
@DisplayName("Deve tratar corretamente lista nula de unidades")
void deveTratarListaNulaDeUnidades() { }

@Test
@DisplayName("Deve tratar corretamente quando processo atualizado concorrentemente")
void deveTratarAtualizacaoConcorrente() { }
```

---

### 2.3. Clareza

#### 🔴 Problemas Críticos

**P10: Testes de Getters/Setters Sem Valor Agregado**

```java
// ModeloTest.java, ProcessoDtoTest.java - testes inúteis
@Test
void subprocessoGettersAndSetters() {
    Subprocesso subprocesso = new Subprocesso();
    subprocesso.setCodigo(1L);
    assertEquals(1L, subprocesso.getCodigo());
    // ... 15 linhas de set/get
}

@Test
void testProcessoDtoBuilderAndAccessors() {
    var dto = ProcessoDto.builder().codigo(1L).build();
    assertEquals(1L, dto.getCodigo());
    // ... testa todos os getters
}
```

**Problema:**
- Não testam lógica de negócio
- Testam código gerado pelo Lombok
- Inflam métricas de cobertura artificialmente
- Consomem tempo de execução

**Recomendação:** **REMOVER** esses testes. Lombok garante que getters/setters funcionam.

---

**P11: Setup Duplicado Entre Testes**

```java
// Duplicado em múltiplos métodos de teste
@Test
void criar() {
    Unidade unidade = new Unidade();
    unidade.setCodigo(1L);
    when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
    // ...
}

@Test
void atualizar() {
    Unidade unidade = new Unidade(); // Duplicado!
    unidade.setCodigo(1L);
    when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
    // ...
}
```

**Recomendação:**

```java
private Unidade unidadePadrao;

@BeforeEach
void setUp() {
    unidadePadrao = new Unidade();
    unidadePadrao.setCodigo(1L);
    when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidadePadrao));
}
```

---

**P12: Falta de Documentação de Cenários Complexos**

```java
// Workflow complexo sem documentação
@Test
@DisplayName("aceitarValidacao homologado se nao houver proxima unidade")
void aceitarValidacaoHomologado() {
    // Setup complexo sem explicação
    Subprocesso sp = new Subprocesso();
    Unidade u = new Unidade();
    Unidade sup = new Unidade();
    sup.setSigla("SUP");
    u.setUnidadeSuperior(sup); // Por que sup não tem superior?
    sp.setUnidade(u);
    // ...
}
```

**Recomendação:**

```java
@Test
@DisplayName("Deve homologar quando não houver próxima unidade na hierarquia")
void deveHomologarQuandoNaoHouverProximaUnidade() {
    // Arrange: Configurar hierarquia onde unidade superior é o topo
    Subprocesso sp = new Subprocesso();
    Unidade unidade = new Unidade();
    Unidade unidadeSuperior = new Unidade(); // É o topo da hierarquia
    unidadeSuperior.setSigla("SEDOC");
    unidadeSuperior.setUnidadeSuperior(null); // Não tem superior
    unidade.setUnidadeSuperior(unidadeSuperior);
    sp.setUnidade(unidade);
    
    // Act
    service.aceitarValidacao(sp.getCodigo(), usuario);
    
    // Assert: Como não há próxima unidade, deve homologar diretamente
    assertThat(sp.getSituacao())
        .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
}
```

---

#### 🟡 Problemas Moderados

**P13: Mistura de Idiomas (Português/Inglês)**

```java
// Variáveis em inglês em código português
Usuario user = new Usuario();
Processo processo = new Processo();
CriarProcessoReq req = new CriarProcessoReq();

// Métodos em português com variáveis em inglês
service.disponibilizarCadastro(id, user);
```

**Recomendação:** Manter consistência com o resto do projeto (português):

```java
Usuario usuario = new Usuario();
Processo processo = new Processo();
CriarProcessoReq requisicao = new CriarProcessoReq();

service.disponibilizarCadastro(id, usuario);
```

---

**P14: Falta de Padrão AAA (Arrange-Act-Assert)**

```java
// Sem separação clara
@Test
void criar() {
    CriarProcessoReq req = new CriarProcessoReq("Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
    Unidade unidade = new Unidade();
    unidade.setCodigo(1L);
    when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
    when(processoRepo.saveAndFlush(any())).thenAnswer(i -> { /* ... */ });
    when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());
    processoService.criar(req);
    verify(processoRepo).saveAndFlush(any());
    verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
}
```

**Recomendação:**

```java
@Test
@DisplayName("Deve criar processo e publicar evento")
void deveCriarProcessoEPublicarEvento() {
    // Arrange
    CriarProcessoReq requisicao = new CriarProcessoReq(
        "Processo de Teste",
        TipoProcesso.MAPEAMENTO,
        LocalDateTime.now(),
        List.of(1L)
    );
    
    Unidade unidade = new Unidade();
    unidade.setCodigo(1L);
    
    when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
    when(processoRepo.saveAndFlush(any())).thenAnswer(i -> {
        Processo p = i.getArgument(0);
        p.setCodigo(100L);
        return p;
    });
    when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());
    
    // Act
    processoService.criar(requisicao);
    
    // Assert
    verify(processoRepo).saveAndFlush(any());
    verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
}
```

---

### 2.4. Dívida Técnica

#### 🔴 Alta Prioridade

**DT1: Testes de Integração Acoplados a Dados Específicos**

**Arquivos Afetados:** `CDU01IntegrationTest.java` a `CDU21IntegrationTest.java` (21 arquivos)

**Problema:**
```java
// Dependência de dados hardcoded do data.sql
String tituloEleitoral = "111111111111"; // ADMIN
mockMvc.perform(post("/api/usuarios/autorizar")
    .contentType(MediaType.APPLICATION_JSON)
    .content(tituloEleitoral))
    .andExpect(jsonPath("$[0].siglaUnidade").value("ADMIN-UNIT"));
```

**Impacto:**
- Testes quebram se `data.sql` mudar
- Dificulta criar ambientes de teste isolados
- Impossível rodar testes em paralelo

**Recomendação:**
- Criar fixtures programáticos usando `@BeforeEach`
- Usar `@Sql` para carregar dados específicos do teste
- Evitar dependência de dados globais

```java
@BeforeEach
void setUp() {
    // Criar dados específicos para este teste
    Unidade unidadeAdmin = unidadeRepo.save(
        Unidade.builder()
            .sigla("ADMIN-TEST")
            .nome("Unidade Admin Teste")
            .build()
    );
    
    Usuario admin = usuarioRepo.save(
        Usuario.builder()
            .tituloEleitoral("111111111111")
            .nome("Administrador Teste")
            .build()
    );
}
```

---

**DT2: Ausência de Testes de Performance**

**Problema:** Apenas um arquivo `ProcessoPerformanceTest.java`, mas sem testes reais de performance.

**Recomendação:**

```java
@Test
@Timeout(value = 5, unit = TimeUnit.SECONDS)
@DisplayName("Deve listar processos em menos de 5 segundos")
void deveListarProcessosRapidamente() {
    // Criar 1000 processos
    IntStream.range(0, 1000).forEach(i -> {
        processoRepo.save(criarProcesso("Processo " + i));
    });
    
    // Verificar que listagem é rápida
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    
    List<ProcessoDto> processos = processoService.listar();
    
    stopWatch.stop();
    assertThat(processos).hasSize(1000);
    assertThat(stopWatch.getTotalTimeMillis()).isLessThan(5000);
}
```

---

**DT3: Falta de Testes para Eventos Assíncronos**

**Problema:**
```java
// Apenas verifica se evento foi publicado
verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));

// Não verifica se os listeners processaram o evento
// Não verifica efeitos colaterais (notificações, alertas)
```

**Recomendação:**

```java
@Test
@DisplayName("Deve processar evento e criar alerta quando processo criado")
void deveProcessarEventoECriarAlerta() {
    // Arrange
    CriarProcessoReq requisicao = criarRequisicaoValida();
    
    // Act
    ProcessoDto resultado = processoService.criar(requisicao);
    
    // Assert: Verificar evento
    verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
    
    // Assert: Verificar efeitos do listener
    await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
        List<Alerta> alertas = alertaRepo.findByProcessoCodigo(resultado.getCodigo());
        assertThat(alertas)
            .isNotEmpty()
            .allMatch(a -> a.getTipo() == TipoAlerta.PROCESSO_CRIADO);
    });
}
```

---

#### 🟡 Média Prioridade

**DT4: Falta de Testes Paramétricos**

**Problema:** Testes repetitivos que poderiam ser paramétricos:

```java
@Test
void criarDescricaoVazia() {
    assertThatThrownBy(() -> service.criar(req("")))
        .isInstanceOf(ConstraintViolationException.class);
}

@Test
void criarSemUnidades() {
    assertThatThrownBy(() -> service.criar(reqSemUnidades()))
        .isInstanceOf(ConstraintViolationException.class);
}
```

**Recomendação:**

```java
@ParameterizedTest
@DisplayName("Deve lançar ConstraintViolationException para requisições inválidas")
@MethodSource("requisicoesInvalidas")
void deveLancarExcecaoParaRequisicoesInvalidas(
    CriarProcessoReq requisicao,
    String motivoInvalido
) {
    assertThatThrownBy(() -> processoService.criar(requisicao))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining(motivoInvalido);
}

static Stream<Arguments> requisicoesInvalidas() {
    return Stream.of(
        Arguments.of(reqComDescricaoVazia(), "descrição"),
        Arguments.of(reqSemUnidades(), "unidades"),
        Arguments.of(reqComDataInvalida(), "data")
    );
}
```

---

**DT5: Ausência de Testes de Segurança Específicos**

**Problema:** Apenas `ProcessoSegurancaTest.java` e `ActuatorSecurityTest.java`.

**Faltam testes para:**
- SQL Injection (se houver queries nativas)
- XSS (validação de inputs HTML)
- CSRF
- Autorização granular por role
- Rate limiting

---

**DT6: Cobertura de Código Não Medida/Reportada**

**Observação:** Existe um arquivo `analise-cobertura-testes.md` no backend, mas não há integração com ferramentas como JaCoCo.

**Recomendação:**

```gradle
// build.gradle.kts
plugins {
    jacoco
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

---

### 2.5. Boas Práticas Observadas ✅

#### Pontos Positivos

1. **Uso de AssertJ:** Assertions fluentes e legíveis
   ```java
   assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.CRIADO);
   ```

2. **Teste de Arquitetura:** `ArchConsistencyTest.java` valida regras arquiteturais
   ```java
   @ArchTest
   static final ArchRule controllers_should_not_access_repositories =
       noClasses()
           .that().haveNameMatching(".*Controller")
           .should().accessClassesThat().haveNameMatching(".*Repo");
   ```

3. **Separação de Testes Unitários e Integração:**
   - Unitários: `@ExtendWith(MockitoExtension.class)`
   - Integração: `@SpringBootTest` + `@Transactional`

4. **Uso de `@DisplayName`:** Muitos testes têm descrições legíveis

5. **BaseIntegrationTest:** Classe base para testes de integração evita duplicação

6. **Uso de Builders:** DTOs usam padrão Builder para clareza

---

## 3. Métricas e Estatísticas

### 3.1. Distribuição de Testes

| Categoria | Quantidade | Percentual |
|-----------|------------|------------|
| Testes de Service | 28 | 29% |
| Testes de Controller | 15 | 15% |
| Testes de Integração (CDU) | 21 | 22% |
| Testes de Mapper | 7 | 7% |
| Testes de DTO/Model | 12 | 12% |
| Testes de Eventos | 3 | 3% |
| Testes de Repositório (View) | 1 | 1% |
| Outros | 10 | 10% |
| **Total** | **97** | **100%** |

### 3.2. Problemas por Severidade

| Severidade | Quantidade | Arquivos Afetados |
|------------|------------|-------------------|
| 🔴 Crítico | 10 | ~60 arquivos |
| 🟡 Moderado | 4 | ~30 arquivos |
| 🟢 Baixo | 2 | ~10 arquivos |

### 3.3. Categorias de Dívida Técnica

| Categoria | Estimativa de Esforço |
|-----------|----------------------|
| Padronização de nomenclatura | 8 horas |
| Remoção de testes inúteis (DTO/Model) | 2 horas |
| Refatoração de testes frágeis | 16 horas |
| Implementação de testes paramétricos | 8 horas |
| Documentação e AAA | 12 horas |
| Configuração JaCoCo | 4 horas |
| **Total Estimado** | **50 horas** |

---

## 4. Recomendações Priorizadas

### 4.1. Ações Imediatas (Sprint Atual)

1. **Remover `@MockitoSettings(strictness = LENIENT)`**
   - Arquivos: 8 classes
   - Tempo: 2 horas
   - Benefício: Identificar mocks desnecessários

2. **Remover testes de getters/setters**
   - Arquivos: `ModeloTest.java`, `ProcessoDtoTest.java`, etc.
   - Tempo: 1 hora
   - Benefício: Reduzir ruído e tempo de execução

3. **Padronizar nomenclatura de testes**
   - Criar guia de estilo
   - Aplicar em novos testes
   - Tempo: 2 horas (guia) + contínuo

### 4.2. Ações de Curto Prazo (Próximo Sprint)

4. **Implementar fixtures/builders reutilizáveis**
   - Criar `ProcessoFixture.java`, `SubprocessoFixture.java`, etc.
   - Tempo: 8 horas
   - Benefício: Reduzir duplicação, melhorar manutenibilidade

5. **Refatorar testes de integração**
   - Remover dependências de dados hardcoded
   - Usar `@Sql` ou programatic setup
   - Tempo: 12 horas
   - Benefício: Testes mais robustos e isolados

6. **Adicionar testes de exceção completos**
   - Verificar mensagens de erro
   - Tempo: 4 horas
   - Benefício: Melhor cobertura de cenários de erro

### 4.3. Ações de Médio Prazo (2-3 Sprints)

7. **Implementar testes paramétricos**
   - Converter testes repetitivos
   - Tempo: 8 horas
   - Benefício: Cobertura de casos extremos

8. **Configurar JaCoCo e métricas**
   - Integrar no pipeline CI/CD
   - Definir thresholds mínimos
   - Tempo: 4 horas
   - Benefício: Visibilidade de cobertura

9. **Adicionar testes de eventos assíncronos**
   - Verificar efeitos colaterais
   - Tempo: 8 horas
   - Benefício: Garantir integridade do sistema de eventos

### 4.4. Ações de Longo Prazo (3+ Sprints)

10. **Revisar services com muitas dependências**
    - `ProcessoService` (12 mocks)
    - Avaliar decomposição
    - Tempo: 16 horas
    - Benefício: Melhor design, testes mais simples

11. **Implementar testes de performance**
    - Cenários de carga
    - Benchmarks
    - Tempo: 12 horas
    - Benefício: Garantir SLAs

12. **Expandir testes de segurança**
    - SQL Injection, XSS, etc.
    - Tempo: 8 horas
    - Benefício: Maior segurança

---

## 5. Guia de Estilo Proposto

### 5.1. Estrutura de Teste Padrão

```java
@ExtendWith(MockitoExtension.class) // Ou @SpringBootTest para integração
@DisplayName("ProcessoService - Operações CRUD")
class ProcessoServiceTest {
    
    @Mock
    private ProcessoRepo processoRepo;
    // ... outros mocks necessários (máximo 5)
    
    @InjectMocks
    private ProcessoService processoService;
    
    private Processo processoValido;
    
    @BeforeEach
    void setUp() {
        // Setup comum a todos os testes
        processoValido = ProcessoFixture.criarProcessoValido();
    }
    
    @Nested
    @DisplayName("Criar Processo")
    class CriarProcesso {
        
        @Test
        @DisplayName("Deve criar processo e publicar evento quando dados válidos")
        void deveCriarProcessoEPublicarEventoQuandoDadosValidos() {
            // Arrange: Preparação
            CriarProcessoReq requisicao = CriarProcessoReq.builder()
                .descricao("Processo de Teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                .unidades(List.of(1L))
                .build();
            
            when(unidadeRepo.findById(1L))
                .thenReturn(Optional.of(unidadeValida()));
            when(processoRepo.saveAndFlush(any(Processo.class)))
                .thenAnswer(invocation -> {
                    Processo p = invocation.getArgument(0);
                    p.setCodigo(100L);
                    return p;
                });
            
            // Act: Execução
            ProcessoDto resultado = processoService.criar(requisicao);
            
            // Assert: Verificação
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(100L);
            assertThat(resultado.getDescricao()).isEqualTo("Processo de Teste");
            
            verify(processoRepo).saveAndFlush(any(Processo.class));
            verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
        }
        
        @Test
        @DisplayName("Deve lançar ErroEntidadeNaoEncontrada quando unidade não existir")
        void deveLancarErroEntidadeNaoEncontradaQuandoUnidadeNaoExistir() {
            // Arrange
            Long unidadeInexistente = 999L;
            CriarProcessoReq requisicao = criarRequisicaoComUnidade(unidadeInexistente);
            
            when(unidadeRepo.findById(unidadeInexistente))
                .thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> processoService.criar(requisicao))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Unidade")
                .hasMessageContaining("999");
            
            verify(processoRepo, never()).saveAndFlush(any());
        }
    }
    
    // Métodos auxiliares privados
    private Unidade unidadeValida() {
        return Unidade.builder()
            .codigo(1L)
            .sigla("TEST")
            .nome("Unidade de Teste")
            .build();
    }
    
    private CriarProcessoReq criarRequisicaoComUnidade(Long unidadeCodigo) {
        return CriarProcessoReq.builder()
            .descricao("Processo Teste")
            .tipo(TipoProcesso.MAPEAMENTO)
            .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
            .unidades(List.of(unidadeCodigo))
            .build();
    }
}
```

### 5.2. Convenções de Nomenclatura

#### Métodos de Teste

```java
// PADRÃO: deve{Ação}Quando{Condição}
@Test
@DisplayName("Deve criar processo quando dados válidos")
void deveCriarProcessoQuandoDadosValidos() { }

@Test
@DisplayName("Deve lançar ErroValidacao quando descrição vazia")
void deveLancarErroValidacaoQuandoDescricaoVazia() { }
```

#### Variáveis

```java
// Português, nomes completos e descritivos
Processo processo = new Processo();
Usuario usuario = new Usuario();
CriarProcessoReq requisicao = new CriarProcessoReq();

// Para múltiplas instâncias, usar sufixos descritivos
Unidade unidadeOrigem = new Unidade();
Unidade unidadeDestino = new Unidade();
Usuario usuarioAdmin = criarAdmin();
Usuario usuarioGestor = criarGestor();
```

#### Classes de Teste

```java
// PADRÃO: {ClasseTested}Test
class ProcessoServiceTest { }
class MapaControllerTest { }

// Para integração: {Feature}IntegrationTest
class FluxoCompletoProcessoIntegrationTest { }
class CDU01IntegrationTest { } // Mantém padrão existente
```

### 5.3. Assertions

```java
// Preferir AssertJ para legibilidade
import static org.assertj.core.api.Assertions.*;

// ✅ BOM
assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.CRIADO);
assertThat(processos).hasSize(5).allMatch(p -> p.getTipo() == TipoProcesso.MAPEAMENTO);

// ❌ EVITAR JUnit básico
assertEquals(SituacaoProcesso.CRIADO, processo.getSituacao());
assertTrue(processos.size() == 5);
```

### 5.4. Mocks e Stubs

```java
// Minimizar número de mocks (idealmente <= 5)
// Usar when() apenas quando necessário
// Preferir objetos reais para ValueObjects e DTOs

// ✅ BOM
when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

// ❌ EVITAR mock desnecessário
when(processoMapper.toDto(any())).thenReturn(dto); // Usar mapper real se possível
```

---

## 6. Exemplos de Refatoração

### 6.1. Antes: Teste Confuso e Frágil

```java
@Test
void criar() {
    CriarProcessoReq req = new CriarProcessoReq("Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
    Unidade unidade = new Unidade();
    unidade.setCodigo(1L);
    when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
    when(processoRepo.saveAndFlush(any())).thenAnswer(i -> { Processo p = i.getArgument(0); p.setCodigo(100L); return p; });
    when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());
    processoService.criar(req);
    verify(processoRepo).saveAndFlush(any());
    verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
}
```

### 6.2. Depois: Teste Claro e Robusto

```java
@Test
@DisplayName("Deve criar processo, persistir no banco e publicar evento de criação")
void deveCriarProcessoPersistirEPublicarEvento() {
    // Arrange: Preparar dados de entrada e comportamento esperado
    CriarProcessoReq requisicao = CriarProcessoReq.builder()
        .descricao("Processo de Mapeamento de Competências 2025")
        .tipo(TipoProcesso.MAPEAMENTO)
        .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
        .unidades(List.of(1L))
        .build();
    
    Unidade unidadeParticipante = Unidade.builder()
        .codigo(1L)
        .sigla("SEDOC")
        .nome("Secretaria de Documentação")
        .build();
    
    when(unidadeRepo.findById(1L))
        .thenReturn(Optional.of(unidadeParticipante));
    
    when(processoRepo.saveAndFlush(any(Processo.class)))
        .thenAnswer(invocation -> {
            Processo processoSalvo = invocation.getArgument(0);
            processoSalvo.setCodigo(100L); // Simula ID gerado pelo banco
            return processoSalvo;
        });
    
    ProcessoDto dtoEsperado = ProcessoDto.builder()
        .codigo(100L)
        .descricao(requisicao.descricao())
        .tipo(requisicao.tipo().name())
        .situacao(SituacaoProcesso.CRIADO)
        .build();
    
    when(processoMapper.toDto(any(Processo.class)))
        .thenReturn(dtoEsperado);
    
    // Act: Executar método sob teste
    ProcessoDto resultado = processoService.criar(requisicao);
    
    // Assert: Verificar comportamento correto
    assertThat(resultado)
        .isNotNull()
        .extracting("codigo", "descricao", "situacao")
        .containsExactly(100L, requisicao.descricao(), SituacaoProcesso.CRIADO);
    
    // Verificar interações com dependências
    ArgumentCaptor<Processo> processoCaptor = ArgumentCaptor.forClass(Processo.class);
    verify(processoRepo).saveAndFlush(processoCaptor.capture());
    
    Processo processoSalvo = processoCaptor.getValue();
    assertThat(processoSalvo.getDescricao()).isEqualTo(requisicao.descricao());
    assertThat(processoSalvo.getTipo()).isEqualTo(TipoProcesso.MAPEAMENTO);
    assertThat(processoSalvo.getSituacao()).isEqualTo(SituacaoProcesso.CRIADO);
    
    verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
}
```

---

## 7. Checklist de Qualidade para Novos Testes

Antes de commitar um novo teste, verificar:

- [ ] **Nomenclatura**
  - [ ] Método segue padrão `deve{Ação}Quando{Condição}`
  - [ ] `@DisplayName` presente e descritivo em português
  - [ ] Variáveis em português com nomes completos

- [ ] **Estrutura**
  - [ ] Padrão AAA (Arrange-Act-Assert) claramente separado
  - [ ] Comentários explicam o "porquê", não o "o quê"
  - [ ] Testes agrupados com `@Nested` quando apropriado

- [ ] **Robustez**
  - [ ] Não depende de dados hardcoded ou mágicos
  - [ ] Não depende de ordem de execução
  - [ ] Verifica estado completo, não apenas uma propriedade
  - [ ] Exceções verificam tipo E mensagem

- [ ] **Manutenibilidade**
  - [ ] Número de mocks <= 5 (se mais, considerar teste de integração)
  - [ ] Setup comum em `@BeforeEach`, não duplicado
  - [ ] Usa fixtures/builders para dados de teste
  - [ ] Independente de outros testes

- [ ] **Valor**
  - [ ] Testa comportamento, não implementação
  - [ ] Não testa getters/setters simples
  - [ ] Não testa código de framework (Spring, Lombok)
  - [ ] Adiciona cobertura real, não artificial

---

## 8. Conclusão

Os testes do backend do SGC estão funcionais e cobrem os principais cenários, mas apresentam dívida técnica significativa que dificulta manutenção e evolução. As principais áreas de melhoria são:

1. **Consistência:** Padronizar nomenclatura e estrutura
2. **Robustez:** Eliminar dependências de dados específicos
3. **Clareza:** Melhorar legibilidade e documentação
4. **Eficiência:** Remover testes sem valor agregado

Seguindo as recomendações priorizadas, o time pode reduzir a dívida técnica de forma incremental e sustentável, melhorando significativamente a qualidade e manutenibilidade da suite de testes.

---

## Anexos

### A. Ferramentas Recomendadas

- **JaCoCo:** Cobertura de código
- **ArchUnit:** Testes de arquitetura (já em uso ✅)
- **Testcontainers:** Testes de integração com banco real
- **Awaitility:** Testes assíncronos (já presente ✅)
- **MockWebServer:** Testes de clientes HTTP
- **SonarQube:** Análise estática de qualidade de código

### B. Recursos de Aprendizado

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Test-Driven Development by Example - Kent Beck](https://www.amazon.com/Test-Driven-Development-Kent-Beck/dp/0321146530)
- [Growing Object-Oriented Software, Guided by Tests](https://www.amazon.com/Growing-Object-Oriented-Software-Guided-Tests/dp/0321503627)

### C. Arquivos Prioritários para Refatoração

1. `ProcessoServiceTest.java` - 12 mocks, falta AAA
2. `SubprocessoWorkflowServiceTest.java` - 20+ testes sem organização
3. `CDU*IntegrationTest.java` (21 arquivos) - Dados hardcoded
4. `ModeloTest.java` - Testes de getters/setters (remover)
5. `ProcessoDtoTest.java` - Testes de getters/setters (remover)
6. `AtividadeServiceTest.java` - Nomenclatura inconsistente
7. `MapaServiceTest.java` - Falta validação completa
8. `SubprocessoServiceTest.java` - Cobertura incompleta

---

**Documento gerado em:** 17/12/2025  
**Autor:** GitHub Copilot Agent  
**Versão:** 1.0
