# 🏗️ Sprint 3 - Refatoração Backend (God Objects)

**Duração Estimada:** 5-10 dias  
**Objetivo:** Arquitetura mais clara, SRP respeitado, arquivos menores  
**Foco:** Decomposição de God Objects, melhor organização de responsabilidades

---

## 📋 Sumário de Ações

| #  | Ação                                              | Prioridade | Esforço  | Impacto  | Arquivos                            |
|----|---------------------------------------------------|------------|----------|----------|-------------------------------------|
| 6  | Decompor `UnidadeFacade` em 3 services            | 🟡 Média   | 🔴 Alto  | 🟠 Médio | 1 arquivo (384 linhas) → 4 arquivos |
| 8  | Dividir `SubprocessoWorkflowService` (775 linhas) | 🟡 Média   | 🔴 Alto  | 🟠 Médio | 1 arquivo → 3 arquivos              |
| 10 | Consolidar AtividadeService + CompetenciaService  | 🟡 Média   | 🟡 Médio | 🟠 Médio | 3 arquivos → 1 arquivo              |

**Resultado Esperado:** Classes < 500 linhas, SRP respeitado, melhor testabilidade e manutenibilidade.

---

## 🎯 Ação #6: Decompor UnidadeFacade em 3 Services

### Contexto

`UnidadeFacade` é uma classe de **384 linhas** com **6 responsabilidades distintas**, violando o Single Responsibility
Principle (SRP). Apesar de cada método ser coeso individualmente, o arquivo como um todo é difícil de navegar e manter.

### Problema Identificado

**Arquivo:** `/backend/src/main/java/sgc/organizacao/facade/UnidadeFacade.java`

**Responsabilidades Atuais (Misturadas):**

1. 🌳 **Hierarquia de unidades** - Árvore, descendentes, ancestrais (cache incluído)
2. 🗺️ **Mapa vigente** - Verificação e busca de unidades com mapa vigente
3. 👤 **Gestão de responsáveis** - Chefe, chefe hierárquico, gestores
4. 📋 **Atribuições temporárias** - Criar, listar, remover atribuições
5. ✅ **Elegibilidade** - Verificar se unidade é elegível para processos
6. 💾 **Cache** - Sistema de cache para hierarquia (já removido na Sprint 1)

**Indicadores de Problema:**

- ❌ Arquivo muito grande (384 linhas)
- ❌ Múltiplas responsabilidades (SRP violation)
- ❌ Difícil navegação e compreensão
- ❌ Testes complexos (muitos mocks necessários)

### Solução

**Estrutura Proposta:**

```
sgc.organizacao/
├── facade/
│   └── UnidadeFacade.java                    (Orquestrador - ~60 linhas)
└── service/
    ├── UnidadeHierarquiaService.java         (~150 linhas)
    ├── UnidadeMapaService.java               (~100 linhas)
    └── UnidadeResponsavelService.java        (~100 linhas)
```

**Decomposição Detalhada:**

#### UnidadeHierarquiaService (~150 linhas)

```java
@Service
class UnidadeHierarquiaService {
    
    private final UnidadeRepo unidadeRepo;
    private final UnidadeMapper mapper;
    
    /**
     * Busca a árvore hierárquica completa de unidades.
     */
    public List<UnidadeDto> buscarArvoreHierarquica() {
        // Lógica de montagem da árvore
    }
    
    /**
     * Busca todos os IDs de unidades descendentes.
     */
    public List<Long> buscarIdsDescendentes(Long codigoUnidade) {
        // Lógica de busca recursiva
    }
    
    /**
     * Busca ancestral de tipo específico.
     */
    public Optional<Unidade> buscarAncestral(Long codigoUnidade, TipoUnidade tipo) {
        // Lógica de busca de ancestral
    }
    
    /**
     * Monta hierarquia de unidades a partir de lista plana.
     */
    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades) {
        // Lógica de montagem
    }
}
```

#### UnidadeMapaService (~100 linhas)

```java
@Service
class UnidadeMapaService {
    
    private final UnidadeRepo unidadeRepo;
    private final MapaRepo mapaRepo;
    
    /**
     * Verifica se unidade tem mapa vigente.
     */
    public boolean verificarMapaVigente(Long codigoUnidade) {
        // Lógica de verificação
    }
    
    /**
     * Busca todas as unidades com mapa vigente.
     */
    public List<UnidadeDto> buscarUnidadesComMapaVigente() {
        // Lógica de busca
    }
    
    /**
     * Busca mapa vigente de uma unidade.
     */
    public Optional<MapaDto> buscarMapaVigente(Long codigoUnidade) {
        // Lógica de busca
    }
}
```

#### UnidadeResponsavelService (~100 linhas)

```java
@Service
class UnidadeResponsavelService {
    
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final AtribuicaoTemporariaRepo atribuicaoRepo;
    
    /**
     * Busca responsável atual da unidade.
     */
    public Optional<UsuarioDto> buscarResponsavelAtual(Long codigoUnidade) {
        // Lógica considerando atribuições temporárias
    }
    
    /**
     * Busca chefe da unidade.
     */
    public Optional<UsuarioDto> buscarChefePorUnidade(Long codigoUnidade) {
        // Lógica de busca de chefe
    }
    
    /**
     * Cria atribuição temporária de responsável.
     */
    public AtribuicaoTemporaria criarAtribuicaoTemporaria(
        Long codigoUnidade,
        String tituloUsuario,
        LocalDate dataInicio,
        LocalDate dataFim
    ) {
        // Lógica de criação
    }
    
    /**
     * Verifica elegibilidade de unidade para processos.
     */
    public boolean verificarElegibilidade(Long codigoUnidade) {
        // Lógica de verificação
    }
}
```

#### UnidadeFacade (~60 linhas - Orquestrador)

```java
@Service
public class UnidadeFacade {
    
    private final UnidadeHierarquiaService hierarquiaService;
    private final UnidadeMapaService mapaService;
    private final UnidadeResponsavelService responsavelService;
    
    // Delega para services especializados
    
    public List<UnidadeDto> buscarTodasEntidadesComHierarquia() {
        return hierarquiaService.buscarArvoreHierarquica();
    }
    
    public boolean verificarMapaVigente(Long codigoUnidade) {
        return mapaService.verificarMapaVigente(codigoUnidade);
    }
    
    public Optional<UsuarioDto> buscarResponsavelAtual(Long codigoUnidade) {
        return responsavelService.buscarResponsavelAtual(codigoUnidade);
    }
    
    // ... outros métodos que apenas delegam
}
```

### Passos para Execução por IA

#### Fase 1: Análise e Planejamento

1. **Ler o arquivo completo:**

   ```bash
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/facade/UnidadeFacade.java
   ```

2. **Identificar métodos por responsabilidade:**

   ```bash
   grep -n "public.*buscar\|public.*verificar\|public.*criar" /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/facade/UnidadeFacade.java
   ```

3. **Mapear dependências:**

   ```bash
   grep -n "private final\|@Autowired" /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/facade/UnidadeFacade.java
   ```

#### Fase 2: Criar Services Especializados

1. **Criar pasta service (se não existir):**

   ```bash
   mkdir -p /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/service
   ```

2. **Criar UnidadeHierarquiaService:**

   ```bash
   create /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java
   ```

    - Copiar métodos relacionados a hierarquia
    - Adicionar anotação `@Service`
    - Injetar dependências necessárias

3. **Criar UnidadeMapaService:**

   ```bash
   create /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/service/UnidadeMapaService.java
   ```

    - Copiar métodos relacionados a mapas
    - Adicionar anotação `@Service`

4. **Criar UnidadeResponsavelService:**

   ```bash
   create /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/service/UnidadeResponsavelService.java
   ```

    - Copiar métodos relacionados a responsáveis
    - Adicionar anotação `@Service`

#### Fase 3: Refatorar UnidadeFacade

1. **Simplificar UnidadeFacade:**

   ```bash
   edit /home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/facade/UnidadeFacade.java
   ```

    - Remover implementações
    - Injetar os 3 novos services
    - Delegar chamadas para services especializados

2. **Exemplo de refatoração:**

   ```diff
   - public List<UnidadeDto> buscarTodasEntidadesComHierarquia() {
   -     List<Unidade> unidades = unidadeRepo.findAll();
   -     return montarHierarquia(unidades);
   - }
   
   + public List<UnidadeDto> buscarTodasEntidadesComHierarquia() {
   +     return hierarquiaService.buscarArvoreHierarquica();
   + }
   ```

#### Fase 4: Atualizar Testes

1. **Verificar testes existentes:**

   ```bash
   find backend/src/test -name "*UnidadeFacade*" -type f
   ```

2. **Criar testes para novos services:**

   ```bash
   create backend/src/test/java/sgc/organizacao/service/UnidadeHierarquiaServiceTest.java
   create backend/src/test/java/sgc/organizacao/service/UnidadeMapaServiceTest.java
   create backend/src/test/java/sgc/organizacao/service/UnidadeResponsavelServiceTest.java
   ```

3. **Atualizar testes de UnidadeFacade:**

   ```bash
   edit backend/src/test/java/sgc/organizacao/facade/UnidadeFacadeTest.java
   # Simplificar testes - agora apenas mocks dos 3 services
   ```

#### Fase 5: Validação

1. **Executar testes:**

   ```bash
   cd /home/runner/work/sgc/sgc
   ./gradlew :backend:test --tests "*Unidade*"
   ```

2. **Verificar compilação:**

   ```bash
   ./gradlew :backend:build
   ```

3. **Executar testes E2E (se houver):**

   ```bash
   npm run test:e2e
   ```

### Critérios de Validação

- ✅ UnidadeFacade < 100 linhas (orquestrador apenas)
- ✅ 3 services especializados criados
- ✅ Cada service < 200 linhas
- ✅ SRP respeitado (uma responsabilidade por service)
- ✅ Testes passam (100%)
- ✅ Nenhuma regressão de funcionalidade
- ✅ Documentação (JavaDoc) em cada service

### Benefícios

- 🟢 **Manutenibilidade:** Arquivos menores, mais fáceis de entender
- 🟢 **Testabilidade:** Services isolados, menos mocks necessários
- 🟢 **Reusabilidade:** Services podem ser usados independentemente
- 🟢 **SRP:** Cada classe tem uma única responsabilidade
- 🟢 **Navegabilidade:** Estrutura mais clara e organizada

---

## 🎯 Ação #8: Dividir SubprocessoWorkflowService (775 linhas)

### Contexto

`SubprocessoWorkflowService` é o **maior arquivo** do backend com **775 linhas** e **17 dependências injetadas**. Foi
criado com boa intenção (consolidar 4 serviços), mas resultou em um God Object difícil de navegar e manter.

### Problema Identificado

**Arquivo:** `/backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoWorkflowService.java`

**Documentação Interna:**

```java
/**
 * Serviço unificado responsável por todos os workflows de subprocesso.
 *
 * <p>Consolidação dos serviços:
 * <ul>
 *   <li>SubprocessoCadastroWorkflowService - Workflow de cadastro de atividades</li>
 *   <li>SubprocessoMapaWorkflowService - Workflow de mapa de competências</li>
 *   <li>SubprocessoTransicaoService - Transições e movimentações</li>
 *   <li>SubprocessoWorkflowService (root) - Operações administrativas</li>
 * </ul>
 */
```

**Dependências (17 injetadas!):**

```java
private final SubprocessoRepo subprocessoRepo;
private final SubprocessoCrudService crudService;
private final AlertaFacade alertaService;
private final UnidadeFacade unidadeService;
private final MovimentacaoRepo repositorioMovimentacao;
private final SubprocessoTransicaoService transicaoService;
private final AnaliseFacade analiseFacade;
@Lazy private final SubprocessoValidacaoService validacaoService;  // ❌ Quebra ciclo
@Lazy private final ImpactoMapaService impactoMapaService;         // ❌ Quebra ciclo
private final MapaFacade mapaService;
// ... mais 7 dependências
```

**Indicadores de Problema:**

- ❌ 775 linhas (arquivo muito grande)
- ❌ 17 dependências (God Object)
- ❌ `@Lazy` para quebrar ciclos (code smell)
- ❌ Múltiplas responsabilidades (4 workflows distintos)

### Solução

**Estrutura Proposta:**

```
sgc.subprocesso.service.workflow/
├── SubprocessoWorkflowFacade.java          (Orquestrador - ~100 linhas)
├── SubprocessoCadastroWorkflowService.java (~250 linhas)
├── SubprocessoMapaWorkflowService.java     (~250 linhas)
└── SubprocessoAdminWorkflowService.java    (~200 linhas)
```

**Decomposição por Responsabilidade:**

#### SubprocessoCadastroWorkflowService (~250 linhas)

**Responsabilidade:** Workflow de cadastro de atividades

```java
@Service
class SubprocessoCadastroWorkflowService {
    
    private final SubprocessoRepo subprocessoRepo;
    private final AtividadeFacade atividadeFacade;
    private final SubprocessoValidacaoService validacaoService;
    
    /**
     * Inicia cadastro de atividades para subprocesso.
     */
    public void iniciarCadastroAtividades(Long codigoSubprocesso) {
        // Lógica de iniciar cadastro
    }
    
    /**
     * Finaliza cadastro de atividades.
     */
    public void finalizarCadastroAtividades(Long codigoSubprocesso) {
        // Lógica de finalizar cadastro
        // Validações específicas
    }
    
    /**
     * Valida se cadastro pode ser finalizado.
     */
    public void validarFinalizacaoCadastro(Long codigoSubprocesso) {
        // Regras de negócio
    }
}
```

#### SubprocessoMapaWorkflowService (~250 linhas)

**Responsabilidade:** Workflow de mapa de competências

```java
@Service
class SubprocessoMapaWorkflowService {
    
    private final SubprocessoRepo subprocessoRepo;
    private final MapaFacade mapaService;
    private final ImpactoMapaService impactoMapaService;
    private final AlertaFacade alertaService;
    
    /**
     * Inicia preenchimento de mapa de competências.
     */
    public void iniciarPreenchimentoMapa(Long codigoSubprocesso) {
        // Lógica de iniciar mapa
    }
    
    /**
     * Finaliza preenchimento de mapa.
     */
    public void finalizarPreenchimentoMapa(Long codigoSubprocesso) {
        // Lógica de finalizar mapa
        // Validações de completude
    }
    
    /**
     * Calcula impacto de mudanças no mapa.
     */
    public ImpactoMapaDto calcularImpactoMudancas(Long codigoSubprocesso) {
        return impactoMapaService.calcularImpacto(codigoSubprocesso);
    }
}
```

#### SubprocessoAdminWorkflowService (~200 linhas)

**Responsabilidade:** Operações administrativas (transições, movimentações)

```java
@Service
class SubprocessoAdminWorkflowService {
    
    private final SubprocessoRepo subprocessoRepo;
    private final SubprocessoTransicaoService transicaoService;
    private final MovimentacaoRepo movimentacaoRepo;
    private final AlertaFacade alertaService;
    
    /**
     * Solicita movimentação de subprocesso.
     */
    public void solicitarMovimentacao(
        Long codigoSubprocesso,
        Long codigoUnidadeDestino
    ) {
        // Lógica de movimentação
    }
    
    /**
     * Cancela subprocesso.
     */
    public void cancelar(Long codigoSubprocesso, String motivo) {
        // Lógica de cancelamento
        // Transições de estado
    }
    
    /**
     * Arquiva subprocesso finalizado.
     */
    public void arquivar(Long codigoSubprocesso) {
        // Lógica de arquivamento
    }
}
```

#### SubprocessoWorkflowFacade (~100 linhas - Orquestrador)

```java
@Service
public class SubprocessoWorkflowFacade {
    
    private final SubprocessoCadastroWorkflowService cadastroService;
    private final SubprocessoMapaWorkflowService mapaService;
    private final SubprocessoAdminWorkflowService adminService;
    
    // Delega para services especializados
    
    public void iniciarCadastroAtividades(Long codigo) {
        cadastroService.iniciarCadastroAtividades(codigo);
    }
    
    public void iniciarPreenchimentoMapa(Long codigo) {
        mapaService.iniciarPreenchimentoMapa(codigo);
    }
    
    public void solicitarMovimentacao(Long codigo, Long unidadeDestino) {
        adminService.solicitarMovimentacao(codigo, unidadeDestino);
    }
    
    // ... métodos de orquestração quando necessário
}
```

### Passos para Execução por IA

#### Fase 1: Análise

1. **Ler arquivo completo:**

   ```bash
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoWorkflowService.java
   ```

2. **Mapear métodos por workflow:**

   ```bash
   grep -n "public void\|public.*Dto" backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoWorkflowService.java
   ```

3. **Identificar dependências de cada grupo:**
    - Anotar quais dependências são usadas por quais métodos
    - Identificar dependências compartilhadas

#### Fase 2: Criar Services Especializados

1. **Criar SubprocessoCadastroWorkflowService:**

   ```bash
   create backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoCadastroWorkflowService.java
   ```

    - Copiar métodos relacionados a cadastro
    - Injetar apenas dependências necessárias

2. **Criar SubprocessoMapaWorkflowService:**

   ```bash
   create backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoMapaWorkflowService.java
   ```

    - Copiar métodos relacionados a mapa

3. **Criar SubprocessoAdminWorkflowService:**

   ```bash
   create backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoAdminWorkflowService.java
   ```

    - Copiar métodos administrativos

#### Fase 3: Refatorar para Facade

1. **Renomear arquivo original:**

   ```bash
   # Backup do original
   mv backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoWorkflowService.java \
      backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoWorkflowFacade.java
   ```

2. **Simplificar Facade:**

   ```bash
   edit backend/src/main/java/sgc/subprocesso/service/workflow/SubprocessoWorkflowFacade.java
   # Remover implementações, apenas delegação
   ```

#### Fase 4: Atualizar Referências

1. **Buscar usos de SubprocessoWorkflowService:**

   ```bash
   grep -r "SubprocessoWorkflowService" backend/src/main/java/sgc/ --include="*.java"
   ```

2. **Atualizar imports e referências:**

   ```bash
   # Substituir SubprocessoWorkflowService por SubprocessoWorkflowFacade
   ```

#### Fase 5: Validação

1. **Compilar:**

   ```bash
   ./gradlew :backend:build
   ```

2. **Executar testes:**

   ```bash
   ./gradlew :backend:test --tests "*Subprocesso*"
   ```

### Critérios de Validação

- ✅ SubprocessoWorkflowService renomeado para SubprocessoWorkflowFacade
- ✅ Facade < 150 linhas
- ✅ 3 services especializados criados
- ✅ Cada service < 300 linhas
- ✅ Nenhum `@Lazy` necessário (ciclos eliminados)
- ✅ Testes passam
- ✅ Nenhuma regressão

---

## 🎯 Ação #10: Consolidar AtividadeService + CompetenciaService

### Contexto

`AtividadeService` e `CompetenciaService` são services separados, mas operam sobre o mesmo contexto de domínio (Mapa de
Competências). Frequentemente, operações em atividades requerem operações em competências e vice-versa. Consolidar em um
único service **MapaManutencaoService** reduz acoplamento e melhora coesão.

### Problema Identificado

**Arquivos Atuais:**

- `/backend/src/main/java/sgc/mapa/service/AtividadeService.java` (~200 linhas)
- `/backend/src/main/java/sgc/mapa/service/CompetenciaService.java` (~150 linhas)
- Ambos chamam um ao outro (acoplamento circular)

**Acoplamento Circular:**

```java
// AtividadeService.java
private final CompetenciaService competenciaService;  // ❌ Depende de Competencia

public void vincularCompetencia(Long atividadeId, Long competenciaId) {
    competenciaService.atualizarAtividades(...);  // ❌ Chamada cruzada
}

// CompetenciaService.java
private final AtividadeService atividadeService;  // ❌ Depende de Atividade

public void adicionarAtividade(Long competenciaId, Long atividadeId) {
    atividadeService.atualizar(...);  // ❌ Chamada cruzada
}
```

### Solução

**Consolidar em MapaManutencaoService:**

```
sgc.mapa.service/
├── MapaManutencaoService.java    (~350 linhas - consolidado)
├── AtividadeRepo.java             (Repository)
└── CompetenciaRepo.java           (Repository)
```

**MapaManutencaoService (~350 linhas):**

```java
@Service
public class MapaManutencaoService {
    
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo competenciaRepo;
    private final MapaRepo mapaRepo;
    private final AtividadeMapper atividadeMapper;
    private final CompetenciaMapper competenciaMapper;
    
    // === MÉTODOS DE ATIVIDADE ===
    
    public AtividadeDto criarAtividade(Long codigoMapa, CriarAtividadeRequest request) {
        // Lógica de criação
    }
    
    public void removerAtividade(Long codigoAtividade) {
        // Remove atividade e atualiza competências relacionadas
        // SEM chamadas cruzadas!
    }
    
    // === MÉTODOS DE COMPETÊNCIA ===
    
    public CompetenciaDto criarCompetencia(Long codigoMapa, CriarCompetenciaRequest request) {
        // Lógica de criação
    }
    
    public void removerCompetencia(Long codigoCompetencia) {
        // Remove competência e atualiza atividades relacionadas
        // SEM chamadas cruzadas!
    }
    
    // === MÉTODOS DE VÍNCULO (Atividade ↔ Competência) ===
    
    public void vincularAtividadeCompetencia(Long atividadeId, Long competenciaId) {
        // Operação transacional em ambas as entidades
        // Coesão natural!
    }
    
    public void desvincularAtividadeCompetencia(Long atividadeId, Long competenciaId) {
        // Operação transacional em ambas as entidades
    }
}
```

### Passos para Execução por IA

1. **Criar MapaManutencaoService:**

   ```bash
   create backend/src/main/java/sgc/mapa/service/MapaManutencaoService.java
   ```

2. **Migrar métodos de AtividadeService:**

   ```bash
   view backend/src/main/java/sgc/mapa/service/AtividadeService.java
   # Copiar métodos para MapaManutencaoService
   ```

3. **Migrar métodos de CompetenciaService:**

   ```bash
   view backend/src/main/java/sgc/mapa/service/CompetenciaService.java
   # Copiar métodos para MapaManutencaoService
   ```

4. **Eliminar chamadas cruzadas:**

   ```bash
   # Refatorar métodos que antes chamavam o outro service
   # Agora estão no mesmo service!
   ```

5. **Atualizar Facades:**

   ```bash
   edit backend/src/main/java/sgc/mapa/facade/AtividadeFacade.java
   # Injetar MapaManutencaoService em vez de AtividadeService
   
   edit backend/src/main/java/sgc/mapa/facade/CompetenciaFacade.java
   # Injetar MapaManutencaoService em vez de CompetenciaService
   ```

6. **Deletar services antigos:**

   ```bash
   rm backend/src/main/java/sgc/mapa/service/AtividadeService.java
   rm backend/src/main/java/sgc/mapa/service/CompetenciaService.java
   ```

7. **Executar testes:**

   ```bash
   ./gradlew :backend:test --tests "*Atividade*"
   ./gradlew :backend:test --tests "*Competencia*"
   ```

### Critérios de Validação

- ✅ MapaManutencaoService criado
- ✅ AtividadeService e CompetenciaService removidos
- ✅ Nenhuma chamada cruzada entre services
- ✅ Testes passam
- ✅ Lógica transacional preservada
- ✅ Coesão melhorada

---

## 📊 Checklist de Validação da Sprint 3

Após implementar todas as 3 ações, validar:

### Testes Automatizados

- [ ] ✅ Testes unitários backend passam: `./gradlew :backend:test`
- [ ] ✅ Compilação sem erros: `./gradlew :backend:build`
- [ ] ✅ Testes E2E passam: `npm run test:e2e` (crítico)

### Validação Manual

- [ ] ✅ Aplicação inicia sem erros
- [ ] ✅ Funcionalidades de unidade funcionam
- [ ] ✅ Workflows de subprocesso funcionam
- [ ] ✅ CRUD de atividades e competências funciona
- [ ] ✅ Nenhuma regressão de funcionalidade

### Qualidade de Código

- [ ] ✅ Nenhum arquivo > 500 linhas
- [ ] ✅ SRP respeitado em todos os services
- [ ] ✅ Nenhuma dependência circular (`@Lazy` removido)
- [ ] ✅ Estrutura de pacotes clara e organizada
- [ ] ✅ JavaDoc completo em services públicos

### Métricas

- [ ] ✅ Redução de classes > 500 linhas: 2 → 0
- [ ] ✅ Aumento de arquivos menores e coesos
- [ ] ✅ Redução de dependências por classe

---

## 📈 Métricas de Sucesso

**Antes da Sprint 3:**

- Arquivos > 500 linhas: 2 (SubprocessoWorkflowService 775L, UnidadeFacade 384L)
- God Objects: 2
- Dependências circulares: 1 (Atividade ↔ Competência)
- Uso de @Lazy: 2 ocorrências

**Após a Sprint 3:**

- ✅ Arquivos > 500 linhas: 0
- ✅ God Objects: 0
- ✅ Dependências circulares: 0
- ✅ Uso de @Lazy: 0 ocorrências

**Estimativa de Impacto:**

- 🟢 **Manutenibilidade:** Melhoria significativa (arquivos menores)
- 🟢 **Testabilidade:** Melhoria (services isolados, menos mocks)
- 🟢 **Legibilidade:** Melhoria (estrutura clara, SRP)
- 🟢 **Reusabilidade:** Melhoria (services coesos)

---

## 🚀 Próximos Passos

Após conclusão da Sprint 3, considerar:

- **Sprint 4:** [otimizacoes-sprint-4.md](./otimizacoes-sprint-4.md) - Otimizações Opcionais
- **Documentação:** Atualizar ADRs com decisões arquiteturais
- **Code Review:** Revisar estrutura com equipe

---

**Versão:** 1.0  
**Data de Criação:** 26 de Janeiro de 2026  
**Status:** 🔵 Planejada
