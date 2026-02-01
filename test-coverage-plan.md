# 📊 Plano de Restauração de Cobertura de Testes - SGC

**Versão:** 1.0  
**Data:** 2026-02-01  
**Objetivo:** Restaurar cobertura de testes para **>90% (branches)** e **>99% (lines/instructions)**

---

## 📋 Índice

1. [Contexto e Motivação](#contexto-e-motivação)
2. [Estado Atual](#estado-atual)
3. [Preparação: Limpeza de Código](#preparação-limpeza-de-código)
4. [Exclusões de Cobertura](#exclusões-de-cobertura)
5. [Estratégia de Teste](#estratégia-de-teste)
6. [Faseamento da Execução](#faseamento-da-execução)
7. [Priorização por Módulo](#priorização-por-módulo)
8. [Ferramentas e Scripts](#ferramentas-e-scripts)
9. [Definição de Pronto](#definição-de-pronto)
10. [Métricas de Sucesso](#métricas-de-sucesso)

---

## 🎯 Contexto e Motivação

### O Que Aconteceu

Durante uma grande refatoração simplificadora, centenas de testes backend foram removidos por supostamente agregarem pouco valor. Essa decisão teve como consequências:

- ❌ **Redução drástica da cobertura de testes**
- ❌ **Perda de confiança na qualidade do código**
- ❌ **Aumento do risco de regressões**
- ❌ **Dificuldade em validar refatorações futuras**

### Por Que Restaurar

- ✅ **Confiança:** Garantir que mudanças não quebrem comportamento existente
- ✅ **Documentação Viva:** Testes servem como especificação executável
- ✅ **Refatoração Segura:** Permite mudanças internas com segurança
- ✅ **Detecção Precoce:** Identifica bugs antes de chegarem à produção
- ✅ **Padrão de Qualidade:** O projeto já tinha meta de >90% de cobertura

### Princípios Orientadores

1. **Testes Valiosos, Não Apenas Cobertura:** Focar em testes que validam comportamento real
2. **Qualidade sobre Quantidade:** Preferir 100 testes excelentes a 500 mediocres
3. **Manutenibilidade:** Testes devem ser fáceis de entender e manter
4. **Execução Orientada por IA:** Plano estruturado para permitir execução por agentes AI
5. **Incremental:** Entregar valor continuamente, fase a fase

---

## 📊 Estado Atual

### Infraestrutura de Testes

**✅ Bem Estabelecida:**
- JUnit 5 com suporte a `@Nested` e `@DisplayName`
- Mockito para mocks
- AssertJ para assertions fluentes
- Test builders (`UnidadeTestBuilder`, `UsuarioTestBuilder`)
- H2 in-memory para testes de integração
- Jacoco para cobertura
- Gradle tasks: `test`, `unitTest`, `integrationTest`, `jacocoTestReport`

**Arquivos de Teste Existentes:** ~199 arquivos de teste

**Arquivos de Código-Fonte:** ~319 arquivos Java

### Metas de Cobertura (build.gradle.kts)

```kotlin
jacocoTestCoverageVerification {
    violationRules {
        rule { limit { counter = "BRANCH"; minimum = "0.90" } }
        rule { limit { counter = "LINE"; minimum = "0.99" } }
        rule { limit { counter = "INSTRUCTION"; minimum = "0.99" } }
    }
}
```

### Exclusões Atuais

```kotlin
classDirectories.setFrom(
    files(classDirectories.files.map {
        fileTree(it) {
            exclude(
                "**/*MapperImpl*",
                "sgc/Sgc.class",
                "sgc/**/*Config.class",
                "sgc/**/*Dto.class",
                "sgc/**/*Request.class",
                "sgc/**/*Response.class",
                "sgc/**/Erro*.class",
                "sgc/notificacao/NotificacaoModelosServiceMock.class"
            )
        }
    })
)
```

---

## 🧹 Preparação: Limpeza de Código

**ANTES de medir cobertura**, devemos remover código desnecessário que polui as métricas.

### Fase 0.1: Auditoria de Null Checks

**Objetivo:** Identificar e remover verificações null redundantes e defensivas desnecessárias.

**Script:** `backend/etc/scripts/auditar-verificacoes-null.js`

**Processo:**

1. **Executar auditoria:**
   ```bash
   cd backend
   node etc/scripts/auditar-verificacoes-null.js
   ```

2. **Analisar relatório gerado:**
   - `null-checks-audit.txt` - Lista completa de verificações
   - `null-checks-analysis.md` - Resumo por arquivo

3. **Classificação:**
   - `POTENTIALLY_REDUNDANT` - Candidatos à remoção
   - `MAYBE_LEGIT` - Verificar se `@Nullable` justifica

4. **Critérios de Remoção:**
   - ❌ Null checks em parâmetros sempre fornecidos pelo Spring
   - ❌ Null checks em retornos de `findByCodigo()` seguidos de `orElseThrow()`
   - ❌ Null checks em objetos recém-criados com `new`
   - ✅ **MANTER:** Null checks em dados externos (API, banco)
   - ✅ **MANTER:** Null checks com `@Nullable` explícito

5. **Validação Pós-Remoção:**
   ```bash
   ./gradlew :backend:test
   ```
   - **TODOS os testes devem continuar passando**
   - Se algum teste quebrar, a verificação null era legítima

**Estimativa:** 50-100 verificações redundantes, resultando em 100-200 branches a menos

---

### Fase 0.2: Atualizar Exclusões de Cobertura

**Objetivo:** Garantir que classes sem lógica testável sejam excluídas.

**Arquivos a Excluir (além dos já excluídos):**

1. **Entidades JPA Simples** (~19 classes)
   - Apenas getters/setters gerados por Lombok
   - Construtores básicos
   - **Pattern:** `@Entity` sem lógica de negócio

2. **Enums de Domínio**
   - Valores constantes sem lógica
   - **Exceção:** Enums com métodos de negócio devem ser testados

3. **Listeners de Eventos Simples**
   - Apenas delegação direta sem lógica

**Ação:**

Atualizar `backend/build.gradle.kts`:

```kotlin
classDirectories.setFrom(
    files(classDirectories.files.map {
        fileTree(it) {
            exclude(
                // Já existentes
                "**/*MapperImpl*",
                "sgc/Sgc.class",
                "sgc/**/*Config.class",
                "sgc/**/*Dto.class",
                "sgc/**/*Request.class",
                "sgc/**/*Response.class",
                "sgc/**/Erro*.class",
                "sgc/notificacao/NotificacaoModelosServiceMock.class",
                
                // Novos
                "sgc/**/model/*Entidade.class",      // Se existirem
                "sgc/**/Status*.class",               // Enums de status simples
                "sgc/**/Tipo*.class",                 // Enums de tipo simples
                "sgc/**/evento/listener/*DelegacaoListener.class" // Listeners só com delegação
            )
        }
    })
)
```

**Validação:**
```bash
./gradlew :backend:jacocoTestReport
node backend/etc/scripts/analisar-cobertura.js
```

---

## 📦 Exclusões de Cobertura

### Classes Automaticamente Excluídas

| Categoria              | Pattern                  | Motivo                                      | Qtd Aprox |
|------------------------|--------------------------|---------------------------------------------|-----------|
| Mappers (MapStruct)    | `**/*MapperImpl*`        | Gerado automaticamente                      | ~20       |
| Aplicação Principal    | `sgc/Sgc.class`          | Bootstrap da aplicação                      | 1         |
| Configurações Spring   | `sgc/**/*Config.class`   | Bean definitions sem lógica                 | ~15       |
| DTOs                   | `sgc/**/*Dto.class`      | Transporte de dados (records/simples)       | ~40       |
| Request/Response       | `sgc/**/*Request.class`  | DTOs de API                                 | ~36       |
| Exceções               | `sgc/**/Erro*.class`     | Classes de erro (maioria simples)           | ~10       |
| Mock de Teste          | `NotificacaoModelosServiceMock` | Apenas para testes              | 1         |

**Total Excluído Automaticamente:** ~123 classes

### Classes a Considerar para Exclusão Manual

Durante a execução, avaliar caso a caso:

1. **Entities JPA sem lógica:** Se apenas getters/setters Lombok
2. **Enums triviais:** Se apenas valores constantes
3. **Builders triviais:** Se apenas encadeamento de setters

---

## 🎯 Estratégia de Teste

### Pirâmide de Testes

```
         /\
        /E2E\         ← Poucos (Playwright - já existem)
       /------\
      /  API  \       ← Alguns (REST Assured - existem alguns)
     /----------\
    / Integração \    ← Médio (Spring Test Context)
   /--------------\
  /  Unit Tests   \   ← Muitos (Mockito + JUnit 5)
 /------------------\
```

**Distribuição Alvo:**
- **70%** Testes Unitários (mocks)
- **25%** Testes de Integração (Spring Context)
- **5%** Testes E2E/API (já cobertos em outro esforço)

### O Que Testar vs O Que NÃO Testar

#### ✅ O QUE TESTAR

**1. Lógica de Negócio (ALTA PRIORIDADE)**
- Facades (`*Facade`)
- Services especializados (`*Service`)
- Validators (`Validador*`)
- Cálculos e transformações
- Regras de transição de estado

**2. Controle de Acesso (ALTA PRIORIDADE)**
- `AccessControlService`
- `AccessPolicy` implementações
- `HierarchyService`
- Verificações de permissão

**3. Eventos e Comunicação (MÉDIA PRIORIDADE)**
- Publicação de eventos (`EventoProcesso*`, etc.)
- Event listeners com lógica

**4. Conversões e Mapeamentos (MÉDIA PRIORIDADE)**
- Mappers customizados (não gerados)
- Conversões complexas

**5. Edge Cases e Erros (ALTA PRIORIDADE)**
- Validações de entrada
- Tratamento de estados inválidos
- Exceções de negócio

#### ❌ O QUE NÃO TESTAR

1. **Código Gerado**
   - MapStruct `*MapperImpl`
   - Lombok getters/setters
   - JPA proxies

2. **Configuração Spring**
   - `@Configuration` classes
   - Bean definitions

3. **DTOs Simples**
   - Records sem lógica
   - POJOs apenas com dados

4. **Infraestrutura**
   - Controllers (teste por integração REST)
   - Repositories (teste por integração JPA)

5. **Código Trivial**
   - Construtores vazios
   - Getters/setters simples
   - Delegação pura

### Padrões de Teste a Seguir

#### Pattern 1: Testes Unitários de Service

```java
@DisplayName("ProcessoService")
class ProcessoServiceTest {
    
    @Mock private ProcessoRepo repo;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private ProcessoService service;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Nested
    @DisplayName("Iniciar Processo")
    class IniciarTest {
        
        @Test
        @DisplayName("deve alterar status para INICIADO")
        void deveAlterarStatusParaIniciado() {
            // Arrange
            Processo processo = criarProcessoPendente();
            when(repo.findByCodigo(CODIGO)).thenReturn(Optional.of(processo));
            
            // Act
            service.iniciar(CODIGO);
            
            // Assert
            assertThat(processo.getStatus()).isEqualTo(StatusProcesso.INICIADO);
        }
        
        @Test
        @DisplayName("deve lançar ErroNegocio quando processo não existe")
        void deveLancarErroQuandoNaoExiste() {
            // Arrange
            when(repo.findByCodigo(CODIGO)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> service.iniciar(CODIGO))
                .isInstanceOf(ErroNegocio.class)
                .hasMessageContaining("não encontrado");
        }
    }
}
```

**Características:**
- `@Nested` para agrupar testes relacionados
- `@DisplayName` em português descrevendo comportamento
- Um assert por teste (ou `assertAll` para mesmo conceito)
- Teste estado final, não implementação
- Sem `verify()` desnecessários

#### Pattern 2: Testes de Facade

```java
@DisplayName("ProcessoFacade")
class ProcessoFacadeTest {
    
    @Mock private ProcessoService processoService;
    @Mock private SubprocessoService subprocessoService;
    @Mock private AccessControlService accessControl;
    @InjectMocks private ProcessoFacade facade;
    
    @Test
    @DisplayName("deve orquestrar criação completa de processo")
    void deveOrquestrarCriacaoCompleta() {
        // Arrange
        ProcessoCriarRequest request = criarRequest();
        Processo processo = criarProcesso();
        when(processoService.criar(any())).thenReturn(processo);
        
        // Act
        ProcessoResponse response = facade.criar(request);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCodigo()).isEqualTo(CODIGO);
        verify(accessControl).verificarPermissao(any(), eq(Acao.CRIAR_PROCESSO), any());
    }
}
```

#### Pattern 3: Testes de Integração

```java
@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("Integração: Fluxo de Processo")
class ProcessoFluxoIntegrationTest {
    
    @Autowired private ProcessoFacade facade;
    @Autowired private ProcessoRepo repo;
    
    @Test
    @DisplayName("deve criar e iniciar processo com sucesso")
    void deveCriarEIniciarProcesso() {
        // Arrange
        ProcessoCriarRequest request = criarRequest();
        
        // Act
        ProcessoResponse criado = facade.criar(request);
        facade.iniciar(criado.getCodigo());
        
        // Assert
        Processo processo = repo.findByCodigo(criado.getCodigo()).orElseThrow();
        assertThat(processo.getStatus()).isEqualTo(StatusProcesso.INICIADO);
    }
}
```

#### Pattern 4: Testes de Validação

```java
@DisplayName("ValidadorProcessoRequest")
class ValidadorProcessoRequestTest {
    
    private ValidadorProcessoRequest validador = new ValidadorProcessoRequest();
    
    @Nested
    @DisplayName("Validar Título")
    class ValidarTituloTest {
        
        @Test
        @DisplayName("deve aceitar título válido")
        void deveAceitarTituloValido() {
            ProcessoCriarRequest request = criarRequestComTitulo("Título Válido");
            assertThatCode(() -> validador.validar(request)).doesNotThrowAnyException();
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "ab"})
        @DisplayName("deve rejeitar título inválido")
        void deveRejeitarTituloInvalido(String titulo) {
            ProcessoCriarRequest request = criarRequestComTitulo(titulo);
            assertThatThrownBy(() -> validador.validar(request))
                .isInstanceOf(ErroValidacao.class);
        }
    }
}
```

### Anti-Patterns a Evitar

❌ **Múltiplos Asserts Não Relacionados**
```java
// RUIM
@Test
void testCriar() {
    ProcessoResponse r = service.criar(req);
    assertNotNull(r);
    assertEquals("T", r.getTitulo());
    assertEquals("D", r.getDesc());
    assertEquals(StatusProcesso.PENDENTE, r.getStatus());
    assertNotNull(r.getDataCriacao());
    // Se primeiro falhar, não sabemos sobre os outros
}
```

✅ **Um Conceito Por Teste**
```java
// BOM
@Test void deveRetornarProcessoNaoNulo() { assertNotNull(service.criar(req)); }
@Test void deveDefinirTituloCorreto() { assertEquals("T", service.criar(req).getTitulo()); }
```

❌ **Testar Implementação**
```java
// RUIM - quebra com refatoração interna
@Test
void testIniciar() {
    service.iniciar(codigo);
    verify(repo, times(1)).findByCodigo(codigo);
    verify(repo, times(1)).save(any());
}
```

✅ **Testar Comportamento**
```java
// BOM - quebra apenas se comportamento mudar
@Test
void deveAlterarStatusAoIniciar() {
    Processo p = criarProcessoPendente();
    when(repo.findByCodigo(codigo)).thenReturn(Optional.of(p));
    service.iniciar(codigo);
    assertThat(p.getStatus()).isEqualTo(StatusProcesso.INICIADO);
}
```

---

## 🚀 Faseamento da Execução

### Visão Geral das Fases

```
Fase 0: Preparação (1-2 dias) ────────────┐
                                          ├─→ Baseline estabelecido
Fase 1: Foundation (3-5 dias) ───────────┘
        ├─→ Services
        ├─→ Facades
        └─→ Validators

Fase 2: Integração (2-3 dias)
        ├─→ Fluxos completos
        └─→ Persistência

Fase 3: Edge Cases (2-3 dias)
        ├─→ Erros
        ├─→ Validações
        └─→ Permissões

Fase 4: Verificação (1-2 dias)
        ├─→ Coverage > 90%
        └─→ Qualidade
```

**Total Estimado:** 9-15 dias (depende de complexidade descoberta)

---

### Fase 0: Preparação (OBRIGATÓRIA)

**Objetivo:** Estabelecer baseline limpo para medição de cobertura.

#### Checklist Fase 0

- [ ] **0.1 Auditoria de Null Checks**
  - [ ] Executar `auditar-verificacoes-null.js`
  - [ ] Analisar relatórios gerados
  - [ ] Remover verificações redundantes em batches pequenos
  - [ ] Validar testes após cada batch (`./gradlew :backend:test`)
  - [ ] Documentar verificações mantidas com justificativa

- [ ] **0.2 Atualizar Exclusões**
  - [ ] Identificar entidades JPA sem lógica
  - [ ] Identificar enums triviais
  - [ ] Atualizar `build.gradle.kts` com exclusões
  - [ ] Validar build

- [ ] **0.3 Baseline de Cobertura**
  - [ ] Executar `./gradlew :backend:jacocoTestReport`
  - [ ] Executar `node backend/etc/scripts/super-cobertura.js --run`
  - [ ] Documentar cobertura inicial em `coverage-tracking.md`
  - [ ] Identificar top 20 arquivos com menor cobertura

**Entregáveis:**
- `null-checks-audit.txt` (atualizado)
- `build.gradle.kts` (com novas exclusões)
- `coverage-tracking.md` (baseline documentado)
- `cobertura_lacunas.json` (priorização)

**Critério de Saída:** Cobertura baseline conhecida, código limpo, exclusões corretas.

---

### Fase 1: Foundation - Unit Tests (ALTA PRIORIDADE)

**Objetivo:** Cobertura de lógica de negócio core (Services, Facades, Validators).

#### Módulos Priorizados

1. **`processo`** - Orquestrador central
2. **`subprocesso`** - Máquina de estados
3. **`seguranca.acesso`** - Controle de acesso
4. **`mapa`** - Domínio core
5. **`organizacao`** - Estrutura hierárquica

#### Checklist Fase 1

Para cada módulo:

- [ ] **Services Especializados**
  - [ ] Identificar todos os `*Service.java` sem teste ou com teste incompleto
  - [ ] Para cada Service:
    - [ ] Criar `@Nested` classes por operação (criar, atualizar, buscar, etc.)
    - [ ] Testar caminhos felizes
    - [ ] Testar validações de entrada
    - [ ] Testar transições de estado
  - [ ] Executar testes: `./gradlew :backend:test --tests "sgc.<modulo>.*ServiceTest"`
  - [ ] Verificar cobertura: `node backend/etc/scripts/analisar-cobertura.js | grep <modulo>`

- [ ] **Facades**
  - [ ] Testar orquestração entre services
  - [ ] Testar chamadas de controle de acesso
  - [ ] Testar publicação de eventos
  - [ ] **NÃO** testar detalhes de implementação (evitar `verify()` excessivos)

- [ ] **Validators**
  - [ ] Usar `@ParameterizedTest` para múltiplos casos inválidos
  - [ ] Testar mensagens de erro específicas
  - [ ] Testar validações de regras de negócio

**Exemplo de Priorização (módulo `processo`):**

```
processo/
├── ProcessoService.java ───────────► Criar ProcessoServiceTest (ALTA)
├── ProcessoFacade.java ────────────► Criar ProcessoFacadeTest (ALTA)
├── ProcessoValidadorService.java ──► Criar ProcessoValidadorServiceTest (MÉDIA)
└── ProcessoNotificadorService.java ► Criar ProcessoNotificadorServiceTest (BAIXA)
```

**Critério de Saída:** 
- Todos os Services core com >80% cobertura
- Todas as Facades com >75% cobertura
- Testes executando em <30s (unitários)

---

### Fase 2: Integration Tests (MÉDIA PRIORIDADE)

**Objetivo:** Validar fluxos completos com Spring Context e banco H2.

#### Checklist Fase 2

- [ ] **Fluxos de Processo**
  - [ ] Criar → Iniciar → Finalizar
  - [ ] Validar persistência em cada etapa
  - [ ] Validar eventos publicados

- [ ] **Fluxos de Subprocesso**
  - [ ] Criar → Processar → Encerrar
  - [ ] Validar transições de estado
  - [ ] Validar relação com Processo pai

- [ ] **Hierarquia de Unidades**
  - [ ] Criar hierarquia completa
  - [ ] Testar queries hierárquicas
  - [ ] Validar herança de permissões

- [ ] **Controle de Acesso Integrado**
  - [ ] Testar políticas em contexto real
  - [ ] Validar auditoria de acesso
  - [ ] Testar hierarquia de unidades

**Pattern:**

```java
@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("Integração: Fluxo Completo de Processo")
class ProcessoFluxoCompletoIntegrationTest {
    
    @Autowired private ProcessoFacade processoFacade;
    @Autowired private SubprocessoFacade subprocessoFacade;
    @Autowired private ProcessoRepo processoRepo;
    
    @Test
    @DisplayName("deve executar fluxo completo de criação até finalização")
    void deveExecutarFluxoCompleto() {
        // ... teste completo com múltiplas etapas
    }
}
```

**Critério de Saída:**
- 5-10 testes de integração por módulo core
- Fluxos principais cobertos end-to-end
- Testes executando em <2min (integração)

---

### Fase 3: Edge Cases & Error Handling (ALTA PRIORIDADE)

**Objetivo:** Garantir robustez com casos extremos e tratamento de erros.

#### Checklist Fase 3

- [ ] **Validações de Entrada**
  - [ ] Campos obrigatórios ausentes
  - [ ] Formatos inválidos
  - [ ] Valores fora de range
  - [ ] Strings muito longas/curtas

- [ ] **Estados Inválidos**
  - [ ] Transições ilegais (ex: FINALIZADO → INICIADO)
  - [ ] Operações em estados incorretos
  - [ ] Validar mensagens de erro específicas

- [ ] **Recursos Não Encontrados**
  - [ ] Buscar por código inexistente
  - [ ] Validar tipo de exceção (`ErroNegocio`)
  - [ ] Validar mensagem de erro

- [ ] **Permissões Negadas**
  - [ ] Usuário sem permissão para ação
  - [ ] Unidade fora da hierarquia permitida
  - [ ] Validar auditoria de tentativa negada

- [ ] **Concorrência (se aplicável)**
  - [ ] Atualização simultânea do mesmo recurso
  - [ ] Validar `@Version` em entidades JPA

**Pattern:**

```java
@Nested
@DisplayName("Casos de Erro")
class CasosDeErroTest {
    
    @Test
    @DisplayName("deve lançar ErroNegocio ao iniciar processo já iniciado")
    void deveLancarErroAoIniciarProcessoJaIniciado() {
        Processo processo = criarProcessoIniciado();
        when(repo.findByCodigo(CODIGO)).thenReturn(Optional.of(processo));
        
        assertThatThrownBy(() -> service.iniciar(CODIGO))
            .isInstanceOf(ErroNegocio.class)
            .hasMessageContaining("já iniciado");
    }
    
    @ParameterizedTest
    @EnumSource(value = StatusProcesso.class, names = {"INICIADO", "FINALIZADO"})
    @DisplayName("deve rejeitar transição de estados finais")
    void deveRejeitarTransicaoDeEstadosFinais(StatusProcesso status) {
        // ...
    }
}
```

**Critério de Saída:**
- Todos os `throw new ErroNegocio()` cobertos
- Todos os `throw new ErroValidacao()` cobertos
- Cobertura de branches >85%

---

### Fase 4: Verificação & Polish (OBRIGATÓRIA)

**Objetivo:** Atingir meta de cobertura e garantir qualidade dos testes.

#### Checklist Fase 4

- [ ] **Análise de Cobertura**
  - [ ] Executar `./gradlew :backend:jacocoTestReport`
  - [ ] Executar `node backend/etc/scripts/super-cobertura.js`
  - [ ] Identificar gaps remanescentes
  - [ ] Priorizar gaps críticos (lógica de negócio)

- [ ] **Qualidade dos Testes**
  - [ ] Revisar testes com múltiplos asserts não relacionados
  - [ ] Revisar testes testando implementação (`verify()` excessivos)
  - [ ] Garantir naming em português com `@DisplayName`
  - [ ] Garantir uso de `@Nested` para organização

- [ ] **Performance dos Testes**
  - [ ] Identificar testes >1s
  - [ ] Otimizar ou marcar como `@Tag("slow")`
  - [ ] Meta: suíte completa em <5min

- [ ] **Documentação**
  - [ ] Atualizar `coverage-tracking.md` com cobertura final
  - [ ] Documentar decisões de não testar (se aplicável)
  - [ ] Atualizar `GUIA-MELHORIAS-TESTES.md` com aprendizados

- [ ] **Validação Final**
  - [ ] `./gradlew :backend:check` ✅
  - [ ] Cobertura BRANCH >90% ✅
  - [ ] Cobertura LINE >99% ✅
  - [ ] Cobertura INSTRUCTION >99% ✅

**Critério de Saída:**
- Meta de cobertura atingida
- Todos os testes passando
- Build de verificação bem-sucedido

---

## 📦 Priorização por Módulo

### Prioridade CRÍTICA (Fazer Primeiro)

| Módulo          | Motivo                                      | Arquivos | Testes | Gap Estimado |
|-----------------|---------------------------------------------|----------|--------|--------------|
| **processo**    | Orquestrador central de todo o sistema      | ~35      | ~25    | ALTO         |
| **subprocesso** | Máquina de estados core                     | ~40      | ~30    | ALTO         |
| **seguranca**   | Controle de acesso é crítico                | ~25      | ~15    | MÉDIO        |
| **mapa**        | Domínio principal (competências)            | ~30      | ~20    | MÉDIO        |

### Prioridade ALTA (Fazer em Seguida)

| Módulo          | Motivo                                      | Arquivos | Testes | Gap Estimado |
|-----------------|---------------------------------------------|----------|--------|--------------|
| **organizacao** | Estrutura hierárquica base                  | ~30      | ~20    | MÉDIO        |
| **comum**       | Utilitários compartilhados                  | ~25      | ~15    | BAIXO        |
| **alerta**      | Sistema de notificações                     | ~15      | ~10    | BAIXO        |

### Prioridade MÉDIA (Fazer se Tempo Permitir)

| Módulo          | Motivo                                      | Arquivos | Testes | Gap Estimado |
|-----------------|---------------------------------------------|----------|--------|--------------|
| **analise**     | Auditoria e histórico                       | ~20      | ~12    | MÉDIO        |
| **notificacao** | Sistema de emails (maioria mock)            | ~15      | ~8     | BAIXO        |
| **painel**      | Dashboard (maioria queries)                 | ~12      | ~6     | BAIXO        |

### Prioridade BAIXA (Apenas se Necessário)

| Módulo          | Motivo                                      | Arquivos | Testes | Gap Estimado |
|-----------------|---------------------------------------------|----------|--------|--------------|
| **relatorio**   | Geração de PDFs (maioria template)          | ~10      | ~5     | BAIXO        |
| **configuracao**| Parâmetros do sistema                       | ~8       | ~5     | BAIXO        |
| **e2e**         | Apenas helpers para testes E2E              | ~5       | ~3     | N/A          |

---

## 🛠 Ferramentas e Scripts

### Scripts Disponíveis

**Localização:** `/backend/etc/scripts/`

#### 1. `analisar-cobertura.js`

**Uso:**
```bash
cd backend
node etc/scripts/analisar-cobertura.js
```

**Saída:**
- Tabela detalhada de cobertura por arquivo
- Linhas não cobertas (lista)
- Branches não cobertos (lista)
- Complexidade ciclomática

**Quando Usar:** Após executar testes, para análise detalhada.

---

#### 2. `super-cobertura.js`

**Uso:**
```bash
cd backend
node etc/scripts/super-cobertura.js --run
```

**Opções:**
- `--run` - Executa testes antes de analisar

**Saída:**
- `cobertura_lacunas.json` - Arquivo JSON com gaps
- Relatório focado em gaps (>100% objetivo)
- Arquivos ordenados por "gravidade" (linhas + branches perdidos)

**Quando Usar:** Para priorizar o que testar a seguir.

---

#### 3. `auditar-verificacoes-null.js`

**Uso:**
```bash
cd backend
node etc/scripts/auditar-verificacoes-null.js
```

**Saída:**
- `null-checks-audit.txt` - Lista completa com linha e contexto
- `null-checks-analysis.md` - Tabela resumida por arquivo

**Quando Usar:** Fase 0.1 (preparação).

---

#### 4. `analisar-complexidade.js`

**Uso:**
```bash
cd backend
node etc/scripts/analisar-complexidade.js
```

**Saída:**
- Tabela de complexidade ciclomática por arquivo
- Identifica métodos complexos (>10)

**Quando Usar:** Para identificar código que mais precisa de testes.

---

#### 5. `prioritize_tests.py`

**Uso:**
```bash
cd backend
python3 etc/scripts/prioritize_tests.py
```

**Saída:**
- Priorização baseada em complexidade + falta de testes

**Quando Usar:** Planejamento inicial.

---

#### 6. `analyze_tests.py`

**Uso:**
```bash
cd backend
python3 etc/scripts/analyze_tests.py
```

**Saída:**
- Análise de padrões de teste existentes
- Identifica anti-patterns

**Quando Usar:** Auditoria de qualidade.

---

### Comandos Gradle Relevantes

```bash
# Executar todos os testes
./gradlew :backend:test

# Apenas testes unitários (exclui tag 'integration')
./gradlew :backend:unitTest

# Apenas testes de integração (tag 'integration')
./gradlew :backend:integrationTest

# Gerar relatório de cobertura (após testes)
./gradlew :backend:jacocoTestReport

# Verificar se cobertura atinge metas
./gradlew :backend:jacocoTestCoverageVerification

# Executar verificações de qualidade completas
./gradlew :backend:check
```

---

## ✅ Definição de Pronto

### Por Fase

**Fase 0 - Preparação:**
- [ ] Null checks auditados e redundantes removidos
- [ ] Exclusões de cobertura configuradas
- [ ] Baseline de cobertura documentado
- [ ] Todos os testes passando

**Fase 1 - Foundation:**
- [ ] Services core com >80% cobertura de linhas
- [ ] Facades com >75% cobertura de linhas
- [ ] Validators com >90% cobertura de linhas
- [ ] Testes unitários executando em <30s

**Fase 2 - Integração:**
- [ ] Fluxos principais testados end-to-end
- [ ] 5-10 testes de integração por módulo core
- [ ] Persistência validada
- [ ] Eventos validados

**Fase 3 - Edge Cases:**
- [ ] Todos os lançamentos de exceção cobertos
- [ ] Validações de entrada testadas
- [ ] Transições inválidas testadas
- [ ] Cobertura de branches >85%

**Fase 4 - Verificação:**
- [ ] Cobertura BRANCH ≥90%
- [ ] Cobertura LINE ≥99%
- [ ] Cobertura INSTRUCTION ≥99%
- [ ] `./gradlew :backend:check` ✅
- [ ] Documentação atualizada

### Por Teste Criado

Um teste está "pronto" quando:

1. ✅ **Naming:** Usa `@DisplayName` em português descrevendo comportamento
2. ✅ **Organização:** Usa `@Nested` para agrupar testes relacionados
3. ✅ **Foco:** Testa um único conceito/comportamento
4. ✅ **Independência:** Pode executar isoladamente (não depende de ordem)
5. ✅ **Assertions:** Usa AssertJ ou JUnit 5 assertions modernas
6. ✅ **Comportamento:** Testa "o que" não "como" (evita `verify()` excessivos)
7. ✅ **Legibilidade:** Estrutura Arrange-Act-Assert clara
8. ✅ **Performance:** Executa em <1s (unitário) ou <5s (integração)

---

## 📊 Métricas de Sucesso

### Métricas Primárias (Obrigatórias)

| Métrica               | Meta      | Baseline | Status |
|-----------------------|-----------|----------|--------|
| **BRANCH Coverage**   | ≥90%      | TBD      | 🔴 TBD |
| **LINE Coverage**     | ≥99%      | TBD      | 🔴 TBD |
| **INSTRUCTION Coverage** | ≥99%   | TBD      | 🔴 TBD |

### Métricas Secundárias (Desejáveis)

| Métrica                        | Meta      | Baseline | Status |
|--------------------------------|-----------|----------|--------|
| Tempo execução unitários       | <30s      | TBD      | 🔴 TBD |
| Tempo execução integração      | <2min     | TBD      | 🔴 TBD |
| Tempo execução total           | <5min     | TBD      | 🔴 TBD |
| Testes com >5 asserts          | 0         | TBD      | 🔴 TBD |
| Testes sem `@DisplayName`      | 0         | TBD      | 🔴 TBD |
| Código duplicado em testes     | <5%       | TBD      | 🔴 TBD |

### Métricas por Módulo (Top Priority)

| Módulo       | Target LINE | Target BRANCH | Status |
|--------------|-------------|---------------|--------|
| processo     | ≥99%        | ≥90%          | 🔴 TBD |
| subprocesso  | ≥99%        | ≥90%          | 🔴 TBD |
| seguranca    | ≥99%        | ≥95%          | 🔴 TBD |
| mapa         | ≥99%        | ≥90%          | 🔴 TBD |
| organizacao  | ≥95%        | ≥85%          | 🔴 TBD |

---

## 📝 Notas de Execução para Agentes AI

### Contexto do Projeto

Este é um projeto **Spring Boot 4.0** com **Java 21**. Principais características:

- **Arquitetura:** Facade Pattern (ADR-001), Services especializados
- **Eventos:** Unified Events Pattern (ADR-002)
- **Segurança:** Arquitetura de 3 camadas (ADR-003)
- **DTOs:** Taxonomia bem definida (ADR-004)
- **Controllers:** Organizados por workflow phase (ADR-005)

### Convenções a Seguir

1. **Idioma:** Todo código, comentários e testes em **Português Brasileiro**
2. **Nomenclatura:** 
   - Classes `PascalCase`
   - Métodos `camelCase`
   - Usar `codigo` não `id`
3. **Testes:**
   - `@DisplayName` em português
   - `@Nested` para organização
   - Um assert por teste (ou `assertAll` para mesmo conceito)
   - Testar comportamento, não implementação

### Estrutura de Pacotes

```
sgc/
├── processo/          # Orquestrador central
│   ├── ProcessoFacade.java
│   ├── ProcessoService.java
│   └── dto/
├── subprocesso/       # Máquina de estados
├── mapa/              # Domínio core
├── organizacao/       # Hierarquia
├── seguranca/
│   └── acesso/        # Controle de acesso
└── comum/             # Compartilhado
```

### Fluxo de Trabalho Recomendado

Para cada módulo:

1. **Análise:**
   ```bash
   node backend/etc/scripts/super-cobertura.js --run | grep <modulo>
   ```

2. **Identificar gaps** no JSON gerado

3. **Criar testes** seguindo patterns deste documento

4. **Validar:**
   ```bash
   ./gradlew :backend:test --tests "sgc.<modulo>.*Test"
   ./gradlew :backend:jacocoTestReport
   ```

5. **Iterar** até meta atingida

6. **Documentar** progresso em `coverage-tracking.md`

### Quando Pedir Ajuda

- **Complexidade >15:** Método pode precisar refatoração antes de testar
- **Lógica não clara:** Perguntar sobre regra de negócio
- **Múltiplas dependências:** Considerar teste de integração ao invés de unitário
- **Mock complexo demais:** Pode indicar violação SRP

---

## 🔄 Processo de Revisão

Ao final de cada fase:

1. **Auto-Revisão:**
   - Executar `./gradlew :backend:check`
   - Revisar relatório de cobertura
   - Identificar testes com anti-patterns

2. **Peer Review (se disponível):**
   - Revisar testes de alto impacto (Services, Facades)
   - Validar compreensão de regras de negócio

3. **Atualizar Tracking:**
   - Documentar cobertura atingida
   - Documentar dívidas técnicas remanescentes
   - Documentar decisões de não testar

---

## 📚 Referências

- **[GUIA-MELHORIAS-TESTES.md](backend/etc/docs/GUIA-MELHORIAS-TESTES.md)** - Diretrizes de qualidade
- **[backend-padroes.md](backend/etc/regras/backend-padroes.md)** - Padrões do projeto
- **[ADR-001](backend/etc/docs/adr/ADR-001-facade-pattern.md)** - Facade Pattern
- **[ADR-002](backend/etc/docs/adr/ADR-002-unified-events.md)** - Events Pattern
- **[ADR-003](backend/etc/docs/adr/ADR-003-security-architecture.md)** - Security Architecture
- **[guia-dtos.md](backend/etc/regras/guia-dtos.md)** - DTOs Taxonomy

---

## ✨ Conclusão

Este plano fornece um roadmap estruturado para restaurar a cobertura de testes do SGC de forma incremental, com foco em **testes valiosos** que realmente validam comportamento de negócio.

A execução deve ser **iterativa**, **medida** e **documentada**, permitindo ajustes conforme aprendizados durante o processo.

**Princípio Guia:** _"Melhor ter 200 testes excelentes que cobrem comportamento crítico do que 1000 testes medíocres que testam implementação."_

---

**Última Atualização:** 2026-02-01  
**Versão do Plano:** 1.0  
**Status:** 🟡 Aguardando Execução
