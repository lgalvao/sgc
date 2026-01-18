# Plano de Redução de Complexidade Desnecessária - SGC Backend

## Sumário Executivo

Este plano detalha a remoção sistemática de complexidade desnecessária no backend do SGC, baseado na análise do `complexity-ranking.md`. O foco está em eliminar:

1. **Verificações de nulo redundantes** (código usa `@NullMarked` em todos os pacotes)
2. **Validações defensivas** sobre dados garantidos por views do banco (VW_UNIDADE, VW_USUARIO, etc.)
3. **Validações duplicadas** já realizadas no frontend
4. **Padrões defensivos** que violam o princípio de confiar em invariantes do sistema

### Contexto Arquitetural

- **Total de classes:** 303 arquivos Java
- **Complexidade ciclomática total:** 1600
- **Uso de @NullMarked:** Todos os pacotes principais (exceto valores do cliente)
- **Views do banco:** Garantem integridade referencial e NOT NULL constraints
- **Validação frontend:** Componentes Vue já validam entrada antes de enviar ao backend

### Impacto Esperado

- **Redução estimada de complexidade:** 15-25%
- **Linhas de código removidas:** ~300-500 (checks defensivos)
- **Melhoria de manutenibilidade:** Alta
- **Risco de regressão:** Baixo (mudanças são simplificações com testes existentes)

---

## Sprint 1: Análise e Preparação (3-5 dias) - CONCLUÍDO

### Objetivo
Estabelecer baseline, documentar padrões problemáticos e criar ferramentas de análise automatizada.

### Tarefas

#### Task 1.1: Auditoria Completa de Null Checks
**Contexto:** O projeto usa `@NullMarked` em todos os pacotes (verificar `package-info.java`), então verificações de nulo são necessárias APENAS para:
- Valores vindos do cliente (Request DTOs)
- Campos marcados explicitamente com `@Nullable`

**Ações:**
1. Executar busca por padrões de null check:
   ```bash
   grep -r "!= null" backend/src/main/java/sgc --include="*.java" > null-checks-audit.txt
   grep -r "== null" backend/src/main/java/sgc --include="*.java" >> null-checks-audit.txt
   ```

2. Categorizar cada ocorrência:
   - ✅ **Legítima:** Em métodos que recebem parâmetros `@Nullable`
   - ❌ **Redundante:** Em campos garantidos por `@NullMarked`
   - ❌ **Defensiva:** Em entidades após fetch do banco

3. Gerar relatório por classe (usar script Python/bash):
   ```
   Classe | Total Checks | Legítimos | Redundantes | % Redundância
   ```

4. Identificar top 20 classes com maior % de redundância

**Entregável:** `null-checks-analysis.md` com lista completa e classificação

**Ferramentas IA:**
- Usar `task agent (explore)` para localizar padrões
- Script automatizado para categorização inicial

---

#### Task 1.2: Auditoria de Validações sobre Views do Banco
**Contexto:** As views `VW_UNIDADE`, `VW_USUARIO`, `VW_RESPONSABILIDADE`, `VW_USUARIO_PERFIL_UNIDADE` garantem:
- Constraints NOT NULL em campos obrigatórios
- Integridade referencial via JOINs
- Dados validados no startup via `ValidadorDadosOrgService`

**Ações:**
1. Mapear campos garantidos pelas views:
   ```sql
   VW_UNIDADE:
     - codigo (PK, NOT NULL)
     - sigla (NOT NULL para unidades ativas)
     - nome (NOT NULL)
     - tipo (enum validado, NOT NULL)
   
   VW_USUARIO:
     - titulo (PK, NOT NULL)
     - unidade_lot_codigo (FK VW_UNIDADE, NOT NULL)
     - unidade_comp_codigo (derivado de lógica, NOT NULL)
   ```

2. Buscar validações no código que verificam esses campos:
   ```bash
   grep -r "getSigla.*null" backend/src/main/java/sgc
   grep -r "getUnidadeLotacao.*null" backend/src/main/java/sgc
   grep -r "getTituloTitular.*null" backend/src/main/java/sgc
   ```

3. Analisar cada validação:
   - Se campo vem de view → **Redundante** (exceto em `ValidadorDadosOrgService` que roda no startup)
   - Se campo é FK obrigatório → **Redundante** (JPA garante)

**Entregável:** `view-validations-audit.md` com mapeamento campo-a-campo

**Exceção:** Manter validações em `ValidadorDadosOrgService` - é o guardião do startup

---

#### Task 1.3: Auditoria de Validações Duplicadas Frontend/Backend
**Contexto:** O frontend usa componentes controlados (select lists, date pickers) que já validam:
- Listas fechadas (siglas de unidade, perfis)
- Datas futuras (date pickers com validação)
- Campos obrigatórios (disabled submit buttons)

**Ações:**
1. Mapear validações frontend:
   ```bash
   # Buscar por disabled conditions
   grep -r ":disabled=" frontend/src/components --include="*.vue"
   
   # Buscar por validações de formulário
   grep -r "v-model" frontend/src/components --include="*.vue" | grep -E "(required|validate)"
   ```

2. Comparar com validações backend:
   - Bean Validation annotations (`@NotNull`, `@NotBlank`, `@NotEmpty`)
   - Validações manuais em Services

3. Classificar:
   - **Manter:** Validações de segurança (mesmo que frontend valide)
   - **Manter:** Validações de negócio (regras complexas)
   - **Simplificar:** Validações puramente sintáticas já cobertas pelo frontend

**Entregável:** `frontend-backend-validation-comparison.md`

**Nota:** NÃO remover Bean Validation do backend - são proteção contra uso direto da API

---

#### Task 1.4: Criar Suite de Testes de Regressão
**Contexto:** Antes de simplificar, garantir que testes existentes cobrem os caminhos felizes.

**Ações:**
1. Executar coverage report:
   ```bash
   ./gradlew :backend:test :backend:jacocoTestReport
   ```

2. Identificar classes top 10 de complexidade com coverage < 80%:
   - Se baixa cobertura → Adicionar testes antes de refatorar
   - Se alta cobertura → Proceder com confiança

3. Adicionar testes focados em invariantes:
   ```java
   // Exemplo: Testar que Unidade sempre tem sigla após fetch
   @Test
   void unidadeDeveTerSiglaNaoNula() {
       Unidade u = unidadeRepo.findById(1L).orElseThrow();
       assertThat(u.getSigla()).isNotNull();
   }
   ```

**Entregável:** Coverage report + lista de classes que precisam de testes adicionais

---

## Sprint 2: Refatoração das Top 5 Classes Mais Complexas (5-7 dias) - EM ANDAMENTO

### Objetivo
Reduzir complexidade das 5 classes com maior score, focando em null checks e validações redundantes.

---

### Task 2.1: SubprocessoWorkflowService (Score 78.5)
**Arquivo:** `backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoWorkflowService.java`

**Problemas Identificados:**
1. Line 165: `if (sp.getUnidade() != null)` - Unidade sempre existe após persistência
2. Line 178: `if (subprocesso.getProcesso() == null)` - Processo existe se situação != NAO_INICIADO
3. Line 311: `if (unidadeSubprocesso == null)` - Após `buscarSubprocesso()`, unidade garantida

**Ações:**
1. **Remover null check de Unidade (Line 165):**
   ```java
   // ANTES
   if (sp.getUnidade() != null) {
       var siglaUnidade = sp.getUnidade().getSigla();
       // ...
   }
   
   // DEPOIS
   var siglaUnidade = sp.getUnidade().getSigla();
   // ...
   ```
   - **Justificativa:** `SubprocessoCrudService.criar()` sempre seta a Unidade
   - **Teste:** Verificar que teste `criarSubprocessoTest` passa

2. **Substituir null check por assertion (Line 178):**
   ```java
   // ANTES
   if (subprocesso.getProcesso() == null) {
       throw new ErroEstadoImpossivel("Subprocesso deve ter processo");
   }
   
   // DEPOIS
   assert subprocesso.getProcesso() != null : 
       "Invariante violada: Subprocesso deve ter processo quando situação != NAO_INICIADO";
   ```
   - **Justificativa:** Se violado, é bug de programação, não erro de negócio
   - **Alternativa:** Remover completamente se não houver relatos de violação

3. **Marcar getUnidade() como @NonNull na entidade:**
   ```java
   // Em Subprocesso.java
   public @NonNull Unidade getUnidade() {
       return unidade;
   }
   ```

**Complexidade Esperada:**
- Antes: 102 (ciclomática)
- Depois: ~95 (-7%)
- Branches: 98 → ~92

**Validação:**
```bash
./gradlew :backend:test --tests "*SubprocessoWorkflowServiceTest"
```

**Entregável:** PR com diff mostrando simplificação + testes passando

---

### Task 2.2: SubprocessoAccessPolicy (Score 74.1)
**Arquivo:** `backend/src/main/java/sgc/seguranca/acesso/SubprocessoAccessPolicy.java`

**Problemas Identificados:**
1. Line 316: `if (unidadeSubprocesso == null)` - Vem de VW_UNIDADE (garantido)
2. Lines 325, 330, 338: `a.getUnidade() != null` - UsuarioPerfil sempre tem unidade_codigo
3. Lines 290-301: Lógica duplicada de verificação de perfil

**Ações:**
1. **Marcar campos de UsuarioPerfil como @NonNull:**
   ```java
   // Em UsuarioPerfil.java
   public record UsuarioPerfil(
       @NonNull Perfil perfil,
       @NonNull Unidade unidade  // Era nullable antes
   ) {}
   ```

2. **Remover checks redundantes (Lines 325, 330, 338):**
   ```java
   // ANTES
   boolean isGestorOuChefe = atribuicoes.stream()
       .anyMatch(a -> a.getUnidade() != null && 
                     (a.getPerfil() == GESTOR || a.getPerfil() == CHEFE));
   
   // DEPOIS
   boolean isGestorOuChefe = atribuicoes.stream()
       .anyMatch(a -> a.getPerfil() == GESTOR || a.getPerfil() == CHEFE);
   ```

3. **Consolidar switch duplicado (Lines 290-301):**
   ```java
   // ANTES: Dois métodos com switch similar
   private void definirMotivoNegacao1(...) {
       switch (perfil) {
           case ADMIN -> ...
           case GESTOR -> ...
       }
   }
   private void definirMotivoNegacao2(...) {
       switch (perfil) {
           case ADMIN -> ...  // Duplicado!
           case GESTOR -> ...
       }
   }
   
   // DEPOIS: Extrair para método comum
   private String getMotivoNegacaoPorPerfil(Perfil perfil, Acao acao) {
       return switch (perfil) {
           case ADMIN -> "Administradores não podem " + acao.getDescricao();
           case GESTOR -> "Gestores só podem " + acao.getDescricao() + " em sua hierarquia";
           // ...
       };
   }
   ```

**Complexidade Esperada:**
- Antes: 78 (ciclomática)
- Depois: ~68 (-13%)
- Branches: 121 → ~105

**Validação:**
```bash
./gradlew :backend:test --tests "*SubprocessoAccessPolicyTest"
```

**Entregável:** PR + atualização de `UsuarioPerfil` schema

---

### Task 2.3: SubprocessoFacade (Score 57.9)
**Arquivo:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoFacade.java`

**Problemas Identificados:**
1. Line 395-397: `if (subprocesso.getMapa() == null)` - Mapa sempre criado em `SubprocessoCrudService.criar()`
2. Lines 424-432: Null checks em titular/responsavel de VW_USUARIO

**Ações:**
1. **Documentar invariante de Mapa:**
   ```java
   // Em Subprocesso.java
   /**
    * Retorna o mapa de competências.
    * 
    * @return Mapa sempre não-nulo (criado no construtor ou em criar())
    */
   public @NonNull Mapa getMapa() {
       return mapa;
   }
   ```

2. **Remover null check (Line 395):**
   ```java
   // ANTES
   if (subprocesso.getMapa() == null) {
       return emptyList();
   }
   return subprocesso.getMapa().getAtividades();
   
   // DEPOIS
   return subprocesso.getMapa().getAtividades();
   ```

3. **Simplificar checks de titular (Lines 424-432):**
   ```java
   // ANTES
   Usuario titular = subprocesso.getUnidade().getTitular();
   if (titular == null || titular.getEmail() == null) {
       logger.warn("Titular sem email: {}", subprocesso.getCodigo());
       return emptyList();
   }
   
   // DEPOIS (confiar em VW_USUARIO)
   Usuario titular = subprocesso.getUnidade().getTitular();
   // Email garantido por VW_USUARIO - se null, é erro de configuração
   return List.of(titular.getEmail());
   ```
   - **Nota:** Manter log de warning se email for crítico para operação

**Complexidade Esperada:**
- Antes: 97 (ciclomática)
- Depois: ~90 (-7%)

**Validação:**
```bash
./gradlew :backend:test --tests "*SubprocessoFacadeTest"
```

**Entregável:** PR + documentação JavaDoc atualizada

---

### Task 2.4: UsuarioFacade (Score 48.6)
**Arquivo:** `backend/src/main/java/sgc/organizacao/UsuarioFacade.java`

**Problemas Identificados:**
1. Lines 235-241, 317-324: `unidadeLotacao` null checks - VW_USUARIO garante unidade_lot_codigo

**Ações:**
1. **Marcar getUnidadeLotacao() como @NonNull:**
   ```java
   // Em Usuario.java
   public @NonNull Unidade getUnidadeLotacao() {
       return unidadeLotacao;
   }
   ```

2. **Remover checks redundantes:**
   ```java
   // ANTES
   if (usuario.getUnidadeLotacao() == null) {
       throw new ErroValidacao("Usuário sem lotação");
   }
   var unidade = usuario.getUnidadeLotacao();
   
   // DEPOIS
   var unidade = usuario.getUnidadeLotacao();
   ```

**Complexidade Esperada:**
- Antes: 74 (ciclomática)
- Depois: ~70 (-5%)

**Validação:**
```bash
./gradlew :backend:test --tests "*UsuarioFacadeTest"
```

---

### Task 2.5: UnidadeFacade (Score 47.6)
**Arquivo:** `backend/src/main/java/sgc/organizacao/UnidadeFacade.java`

**Problemas Identificados:**
1. Lines 126-131, 138-141: Null checks recursivos em `unidadeSuperior`
2. Line 288-292: `if (unidade.getSubunidades() != null)`

**Ações:**
1. **Marcar unidadeSuperior como @Nullable explícito:**
   ```java
   // Em Unidade.java
   public @Nullable Unidade getUnidadeSuperior() {
       return unidadeSuperior;
   }
   ```
   - **Justificativa:** Raiz da hierarquia não tem superior - null é legítimo

2. **Usar Optional para hierarquia:**
   ```java
   // ANTES
   Unidade superior = unidade.getUnidadeSuperior();
   if (superior != null) {
       processarSuperior(superior);
       if (superior.getUnidadeSuperior() != null) {
           // ...
       }
   }
   
   // DEPOIS
   Optional.ofNullable(unidade.getUnidadeSuperior())
       .ifPresent(superior -> {
           processarSuperior(superior);
           processarHierarquia(superior);  // Recursivo
       });
   ```

3. **Inicializar coleções vazias (Line 288):**
   ```java
   // Em Unidade.java (construtor/default)
   @OneToMany
   private List<Unidade> subunidades = new ArrayList<>();
   
   // Então não precisa de null check
   public List<Unidade> getSubunidades() {
       return subunidades;  // Nunca null
   }
   ```

**Complexidade Esperada:**
- Antes: 68 (ciclomática)
- Depois: ~62 (-9%)

**Validação:**
```bash
./gradlew :backend:test --tests "*UnidadeFacadeTest"
```

---

## Sprint 3: Refatoração de Validações de DTOs (3-4 dias)

### Objetivo
Corrigir anotações de validação para evitar validações manuais redundantes.

---

### Task 3.1: Atualizar DTOs com @NotEmpty
**Contexto:** Muitos DTOs usam `@NotNull` em listas quando deveriam usar `@NotEmpty`.

**Ações:**
1. Identificar todos os DTOs com listas:
   ```bash
   grep -r "@NotNull.*List<" backend/src/main/java/sgc/*/dto
   ```

2. Para cada ocorrência, avaliar:
   - Lista vazia é inválida? → Usar `@NotEmpty`
   - Lista vazia é válida? → Manter `@NotNull`

3. **Classes prioritárias:**
   - `CompetenciaMapaDto.atividadesCodigos` → `@NotEmpty`
   - `SalvarMapaRequest.competencias` → `@NotEmpty`

**Exemplo:**
```java
// ANTES
@NotNull(message = "Lista de atividades não pode ser nula")
private final List<Long> atividadesCodigos;

// DEPOIS
@NotEmpty(message = "Pelo menos uma atividade deve ser associada")
private final List<Long> atividadesCodigos;
```

**Entregável:** PR com DTOs corrigidos + testes de validação atualizados

---

### Task 3.2: Adicionar @NotBlank em Campos de Observação
**Contexto:** `DevolverCadastroRequest` e `AceitarCadastroRequest` usam apenas `@Size`, permitindo strings vazias.

**Ações:**
```java
// ANTES
@Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
private String observacoes;

// DEPOIS
@NotBlank(message = "As observações são obrigatórias")
@Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
private String observacoes;
```

**Validação:**
- Testar que API rejeita `observacoes: ""` com HTTP 400
- Verificar testes de controller

---

### Task 3.3: Remover Validações Manuais em Services
**Contexto:** Após corrigir DTOs, remover validações manuais que duplicam Bean Validation.

**Buscar por:**
```bash
grep -r "isEmpty()" backend/src/main/java/sgc/*/service | grep "throw new"
```

**Exemplo:**
```java
// ANTES (em Service)
public void salvarMapa(SalvarMapaRequest req) {
    if (req.getCompetencias() == null || req.getCompetencias().isEmpty()) {
        throw new ErroValidacao("Competências obrigatórias");
    }
    // ...
}

// DEPOIS
public void salvarMapa(SalvarMapaRequest req) {
    // Bean Validation já garantiu que lista não é vazia
    // ...
}
```

**Entregável:** PR com Services simplificados

---

## Sprint 4: Simplificação de ValidadorDadosOrgService (2-3 dias)

### Objetivo
Separar validações de startup (manter) de validações de runtime (remover).

---

### Task 4.1: Documentar Papel de ValidadorDadosOrgService
**Contexto:** Esse service valida integridade das views no startup - é o único lugar onde validações sobre VW_* são legítimas.

**Ações:**
1. Adicionar JavaDoc explicativo:
   ```java
   /**
    * Validador de integridade de dados organizacionais.
    * 
    * <p>RESPONSABILIDADE: Verificar no STARTUP da aplicação que as views
    * do banco (VW_UNIDADE, VW_USUARIO) contêm dados íntegros e consistentes.
    * 
    * <p>Se validação falhar, aplicação NÃO deve iniciar (fail-fast).
    * 
    * <p>IMPORTANTE: Services de negócio NÃO devem duplicar essas validações.
    * Uma vez que o startup passou, dados são considerados íntegros.
    */
   @Service
   public class ValidadorDadosOrgService {
   ```

2. Criar ADR documentando decisão:
   - Por que validar no startup?
   - Por que confiar após startup?
   - Quando adicionar nova validação?

**Entregável:** `docs/ADR-006-startup-validation.md`

---

### Task 4.2: Extrair Validações de Runtime para Services
**Contexto:** Se `ValidadorDadosOrgService` contém lógica de negócio (não apenas validação de dados), mover para services apropriados.

**Ações:**
1. Analisar cada método:
   - É validação de integridade de dados? → Manter
   - É regra de negócio? → Mover para UsuarioFacade/UnidadeFacade

2. Exemplo de extração:
   ```java
   // ANTES (ValidadorDadosOrgService)
   public void validarPermissaoUsuario(Usuario u, Acao a) {
       if (!temPermissao(u, a)) {
           throw new ErroAccessoNegado("Sem permissão");
       }
   }
   
   // DEPOIS (mover para AccessControlService)
   // ValidadorDadosOrgService agora só valida dados no startup
   ```

**Entregável:** PR com responsabilidades claramente separadas

---

## Sprint 5: Refatoração de Classes Médias (5-7 dias)

### Objetivo
Aplicar aprendizados das sprints anteriores nas classes ranqueadas 6-20.

---

### Task 5.1: Batch Refactoring - PainelFacade, ImpactoMapaService, ProcessoFacade
**Abordagem:** Aplicar mesmos padrões de Sprint 2.

**Padrões a aplicar:**
1. Remover null checks em campos de views
2. Substituir `!= null` chains por Optional
3. Marcar métodos com @NonNull/@Nullable
4. Consolidar lógica duplicada

**Ações por classe:**

#### PainelFacade (Score 43.7)
- Line 173: Remover `unidade != null && unidade.getSigla() != null`
- Line 162-177: Simplificar cadeia de checks em `formatarUnidadesParticipantes()`

#### ImpactoMapaService (Score 32.3)
- Lines 279-280, 299-301: Remover `.filter(dto -> dto.getCodigo() != null)`
- Marcar campos DTO como @NonNull

#### ProcessoFacade (Score 30.9)
- Line 81-84: Mover validação de tipo INTERMEDIARIA para frontend

**Entregável:** 3 PRs (um por classe) ou 1 PR com commits separados

---

### Task 5.2: Batch Refactoring - SubprocessoCrudService, MapaSalvamentoService
**Foco:** Remover checks de Mapa e relacionamentos sempre presentes.

**Ações:**

#### SubprocessoCrudService (Score 28.1)
- Line 81-85: Remover throw de "Mapa não associado"
- Documentar que Mapa sempre existe após criação

#### MapaSalvamentoService (Score 26.5)
- Analisar null checks em salvamento de Mapa
- Garantir que cascata de salvamento não precisa de validações defensivas

**Entregável:** PR consolidado

---

### Task 5.3: EventoProcessoListener - Simplificação de Lógica Condicional
**Arquivo:** `backend/src/main/java/sgc/processo/listener/EventoProcessoListener.java`

**Problemas Identificados:**
- Score: 26.8 com 35 branches
- Listener assíncrono com muitas condicionais

**Ações:**
1. Extrair cada handler de evento para método privado:
   ```java
   // ANTES
   @EventListener
   public void onProcessoCriado(EventoProcessoCriado evt) {
       if (condicao1) {
           if (condicao2) {
               // lógica
           }
       }
   }
   
   // DEPOIS
   @EventListener
   public void onProcessoCriado(EventoProcessoCriado evt) {
       handleProcessoCriado(evt);
   }
   
   private void handleProcessoCriado(EventoProcessoCriado evt) {
       // Guard clauses no topo
       if (!condicao1) return;
       if (!condicao2) return;
       
       // Lógica principal sem nesting
   }
   ```

2. Usar guard clauses para reduzir nesting

**Complexidade Esperada:**
- Antes: 32 (ciclomática)
- Depois: ~25 (-22%)

---

## Sprint 6: Consolidação e Documentação (3-4 dias)

### Objetivo
Consolidar mudanças, atualizar documentação e criar guias para evitar regressão.

---

### Task 6.1: Atualizar Guias de Desenvolvimento

**Arquivos a atualizar:**

1. **regras/backend-padroes.md:**
   ```markdown
   ## Quando NÃO fazer null checks
   
   ❌ **NÃO** verificar nulo em:
   - Campos de entidades após fetch do banco
   - Valores de views (VW_UNIDADE, VW_USUARIO)
   - Relacionamentos obrigatórios (FK NOT NULL)
   - Campos garantidos por @NullMarked
   
   ✅ **SEMPRE** verificar nulo em:
   - Parâmetros marcados com @Nullable
   - Valores vindos de Request DTOs (antes de Bean Validation)
   - Campos opcionais de hierarquia (ex: unidadeSuperior)
   ```

2. **regras/guia-validacao.md:**
   - Adicionar seção sobre validações redundantes
   - Documentar quando usar @NotEmpty vs @NotNull

3. **Novo arquivo: regras/guia-nullability.md:**
   - Explicar @NullMarked
   - Quando usar @Nullable
   - Padrões de Optional vs null

**Entregável:** 3 arquivos atualizados + 1 novo

---

### Task 6.2: Criar ArchUnit Rules para Prevenir Regressão

**Contexto:** Prevenir que futuras mudanças reintroduzam padrões problemáticos.

**Ações:**
1. Criar `backend/src/test/java/sgc/arquitetura/NullCheckRulesTest.java`:
   ```java
   @AnalyzeClasses(packages = "sgc")
   public class NullCheckRulesTest {
       
       @ArchTest
       static final ArchRule services_nao_devem_validar_campos_de_views = 
           noClasses()
               .that().resideInAPackage("..service..")
               .should().callMethod(Unidade.class, "getSigla")
               .andThen().callMethod(String.class, "isBlank")
               .because("Sigla é garantida por VW_UNIDADE");
       
       @ArchTest
       static final ArchRule dtos_de_lista_devem_usar_notEmpty =
           fields()
               .that().areAnnotatedWith(NotNull.class)
               .and().haveRawType(List.class)
               .should().beAnnotatedWith(NotEmpty.class)
               .because("Listas vazias geralmente são inválidas");
   }
   ```

2. Executar e corrigir violações:
   ```bash
   ./gradlew :backend:test --tests "*NullCheckRulesTest"
   ```

**Entregável:** Suite de ArchUnit rules + CI integration

---

### Task 6.3: Métricas de Impacto

**Ações:**
1. Re-executar análise de complexidade:
   ```bash
   ./gradlew :backend:jacocoTestReport
   # Gerar novo complexity-ranking.md
   ```

2. Comparar métricas antes/depois:
   ```
   Métrica                    | Antes  | Depois | Redução
   ---------------------------|--------|--------|--------
   Complexidade Total         | 1600   | ~1300  | -18%
   Média por Classe           | 9.88   | ~8.00  | -19%
   Branches Total             | 1272   | ~1050  | -17%
   SubprocessoWorkflowService | 102    | ~95    | -7%
   SubprocessoAccessPolicy    | 78     | ~68    | -13%
   ```

3. Calcular LOC removidas:
   ```bash
   git diff main --stat
   ```

**Entregável:** `complexity-reduction-report.md` com antes/depois

---

### Task 6.4: Atualizar Complexity Ranking

**Ações:**
1. Executar script que gerou `complexity-ranking.md` original
2. Gerar novo ranking pós-refatoração
3. Comparar top 50:
   - Quantas classes saíram do top 10?
   - Qual nova média de complexidade?

**Entregável:** `complexity-ranking-v2.md` + comparativo

---

## Sprint 7: Validação Final e Rollout (2-3 dias)

### Objetivo
Garantir que todas as mudanças foram validadas e sistema está estável.

---

### Task 7.1: Execução Completa da Suite de Testes

**Ações:**
1. Executar TODOS os testes:
   ```bash
   ./gradlew :backend:clean :backend:test
   ```

2. Executar testes E2E:
   ```bash
   npm run test:e2e
   ```

3. Verificar logs de CI/CD:
   - Nenhum teste quebrado
   - Coverage mantido ou melhorado
   - Build time não aumentou significativamente

**Critério de Sucesso:**
- ✅ 100% dos testes passando
- ✅ Coverage >= baseline anterior
- ✅ Nenhum erro de NullPointerException introduzido

---

### Task 7.2: Code Review por Pares

**Contexto:** Mudanças estruturais requerem revisão humana além de IA.

**Ações:**
1. Para cada PR de Sprint:
   - Revisar mudanças linha a linha
   - Verificar que testes cobrem casos de null
   - Confirmar que simplificação não introduziu bugs

2. Checklist de revisão:
   - [ ] Removeu apenas null checks redundantes?
   - [ ] Manteve checks em campos @Nullable?
   - [ ] JavaDoc atualizado?
   - [ ] Testes passando?

**Entregável:** PRs aprovados e merged

---

### Task 7.3: Monitoramento Pós-Deploy

**Contexto:** Validar em produção que simplificações não causaram regressão.

**Ações:**
1. Configurar alertas para NPE:
   ```java
   // Em GlobalExceptionHandler
   @ExceptionHandler(NullPointerException.class)
   public ResponseEntity<ErroApi> handleNPE(NullPointerException ex) {
       logger.error("NPE após refatoração de null checks", ex);
       // Enviar alerta crítico
       return ResponseEntity.status(500).body(erro);
   }
   ```

2. Monitorar por 1 semana:
   - Logs de erro
   - Métricas de performance
   - Feedback de usuários

**Critério de Rollback:**
- Se > 5 NPEs em produção → Rollback e revisar

---

## Resumo de Entregáveis

### Documentação
- [ ] `null-checks-analysis.md` - Auditoria completa
- [ ] `view-validations-audit.md` - Mapeamento de views
- [ ] `frontend-backend-validation-comparison.md` - Comparativo
- [ ] `complexity-reduction-report.md` - Relatório de impacto
- [ ] `complexity-ranking-v2.md` - Novo ranking
- [ ] `docs/ADR-006-startup-validation.md` - Decisão arquitetural
- [ ] `regras/guia-nullability.md` - Novo guia
- [ ] Updates em `backend-padroes.md`, `guia-validacao.md`

### Código
- [ ] 5 PRs principais (Top 5 classes complexas)
- [ ] 3 PRs de DTOs e validações
- [ ] 1 PR de ArchUnit rules
- [ ] 2-3 PRs de classes médias (batch)
- [ ] **Total estimado:** 12-15 PRs

### Testes
- [ ] Coverage report mantido/melhorado
- [ ] ArchUnit suite para prevenção de regressão
- [ ] Testes E2E passando 100%

---

## Cronograma Sugerido

| Sprint | Duração | Início | Término | Entregas Principais |
|--------|---------|--------|---------|---------------------|
| Sprint 1 | 5 dias | D+0 | D+5 | 3 auditorias + suite testes |
| Sprint 2 | 7 dias | D+5 | D+12 | Top 5 classes refatoradas |
| Sprint 3 | 4 dias | D+12 | D+16 | DTOs corrigidos |
| Sprint 4 | 3 dias | D+16 | D+19 | ValidadorDadosOrg separado |
| Sprint 5 | 7 dias | D+19 | D+26 | Classes 6-20 refatoradas |
| Sprint 6 | 4 dias | D+26 | D+30 | Docs + ArchUnit + métricas |
| Sprint 7 | 3 dias | D+30 | D+33 | Validação final + deploy |
| **TOTAL** | **33 dias (~7 semanas)** |

---

## Riscos e Mitigações

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| NPE em produção após remover checks | Média | Alto | 1. Testes extensivos<br>2. Deploy gradual<br>3. Monitoramento ativo |
| Quebra de testes existentes | Alta | Médio | 1. Executar testes a cada mudança<br>2. Manter coverage |
| Regressão em regras de negócio | Baixa | Alto | 1. Code review rigoroso<br>2. Testes E2E |
| Overhead de ArchUnit | Baixa | Baixo | 1. Rules bem focadas<br>2. CI rápido |

---

## Métricas de Sucesso

### Quantitativas
- ✅ Redução de complexidade: >= 15%
- ✅ LOC removidas: >= 300
- ✅ Branches reduzidos: >= 15%
- ✅ Testes mantidos: 100%
- ✅ Coverage: >= baseline

### Qualitativas
- ✅ Código mais legível (feedback de code review)
- ✅ Menos "código defensivo" desnecessário
- ✅ Documentação clara sobre nullability
- ✅ Padrões estabelecidos para novas features

---

## Notas Finais para Execução por IA

### Princípios
1. **Confiança em Invariantes:** Se @NullMarked está ativo e campo não é @Nullable, confie que não é null
2. **Views são Contratos:** Dados de VW_* são garantidos pelo banco
3. **Bean Validation é Suficiente:** Não duplicar em Services
4. **Fail-Fast no Startup:** ValidadorDadosOrgService é guardião, não Services

### Abordagem Incremental
- Cada tarefa é independente
- Testes devem passar após cada commit
- PRs pequenos (< 500 LOC alteradas)
- Review automatizado com ArchUnit

### Ferramentas Sugeridas
- **IDE:** IntelliJ IDEA (análise de nullability integrada)
- **Testes:** JUnit 5 + AssertJ
- **Coverage:** JaCoCo
- **Análise:** ArchUnit + SonarQube (opcional)

---

**Versão:** 1.0  
**Autor:** Análise Automatizada SGC  
**Data:** 2026-01-18  
**Status:** Pronto para Execução
