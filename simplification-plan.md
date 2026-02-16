# 📋 Plano de Simplificação - SGC

**Data de Criação:** 16 de Fevereiro de 2026  
**Status:** 🟡 Em Execução (Fase 1 parcial, Fase 2 iniciada)  
**Versão:** 1.0 (Consolidado)

---

## 🎯 Objetivo

Este documento consolida o **plano completo de simplificação** do sistema SGC, integrando todas as análises, decisões e estratégias de implementação para reduzir a complexidade técnica desnecessária mantendo a qualidade e funcionalidade do sistema.

---

## 📊 Contexto e Justificativa

### Sistema Atual

O SGC é um sistema interno para tribunal eleitoral com:
- **Escala Real:** ~200-300 servidores, 10-20 usuários simultâneos, ~100-150 unidades organizacionais
- **Complexidade de Negócio Legítima:** Workflow de estados complexo (18 estados), hierarquia de unidades, integração com sistemas externos (SGRH, CORAU)
- **Sobre-engenharia Identificada:** 60-70% acima do necessário para a escala atual

### Problema Identificado

**Complexidade Técnica Excessiva:**
- **Backend:** 35 services (muitos com <3 métodos), 12 facades (8 são pass-through), 78 DTOs (53 com estrutura duplicada)
- **Frontend:** 16 stores (fragmentados), 18 composables (10 são view-specific)
- **Impacto:** Adicionar um campo simples requer alterar 15-17 arquivos

### Evidências Concretas

**Métricas Medidas:**
- **Backend:** ~250 arquivos Java, ~35.000 linhas
- **Frontend:** ~180 arquivos TS/Vue, ~18.000 linhas
- **Testes:** 206 arquivos de teste backend, ~3000 testes
- **Stack trace:** 7 camadas de profundidade
- **Onboarding:** 2-3 semanas para novo desenvolvedor

### Meta de Simplificação

**Reduzir complexidade em 15-25% mantendo:**
- ✅ Todas as funcionalidades de negócio
- ✅ Segurança e controle de acesso
- ✅ Cobertura de testes ≥70%
- ✅ Padrões arquiteturais fundamentais

---

## 🚦 Estratégia de Execução

### Abordagem: Incremental e Conservadora

**Princípios:**
1. **Sem perda funcional** - Todas as features mantidas
2. **Testável** - Validação contínua a cada mudança
3. **Reversível** - Possibilidade de rollback
4. **Documentado** - Decisões registradas com justificativa

### Fases de Implementação

**🟢 Fase 1: Quick Wins (7 dias, BAIXO risco)**
- Consolidar services pequenos/fragmentados
- Consolidar stores frontend
- Eliminar composables view-specific
- Arquivar documentação obsoleta

**🟡 Fase 2: Simplificação Estrutural (12 dias, MÉDIO risco)**
- Consolidar módulos mantendo Facades como fronteira
- Introduzir @JsonView para DTOs simples
- Atualizar testes de arquitetura (ArchUnit)
- Atualizar ADRs afetados

**🔴 Fase 3: Simplificação Avançada (15+ dias, ALTO risco) - OPCIONAL**
- Simplificar arquitetura de segurança (AccessPolicies)
- Avaliar remoção de Event System
- **POSTERGAR** até evidência clara de necessidade

---

## 📋 Detalhamento das Ações

### 🟢 FASE 1: Quick Wins

#### Backend (3 dias)

##### 1.1. Consolidar OrganizacaoServices (1 dia)
**Estado Atual:** 9 services pequenos (909 LOC total)
```
organizacao/service/
├── AdministradorService.java (52 LOC, 2 métodos)
├── HierarquiaService.java (60 LOC, 3 métodos)
├── UnidadeConsultaService.java (40 LOC) ← WRAPPER PURO
├── UnidadeHierarquiaService.java (253 LOC)
├── UnidadeMapaService.java (64 LOC)
├── UnidadeResponsavelService.java (187 LOC)
├── UsuarioConsultaService.java (51 LOC) ← WRAPPER PURO
├── UsuarioPerfilService.java (32 LOC, 2 métodos)
└── ValidadorDadosOrgService.java (170 LOC)
```

**Estado Desejado:** 3 services coesos (~600 LOC total)
```
organizacao/service/
├── OrganizacaoService.java (~300 LOC)
│   // Unidades + hierarquia + dados SGRH + validação
├── GestaoUsuariosService.java (~200 LOC)
│   // Usuários + perfis + administradores
└── ResponsabilidadeService.java (~100 LOC)
    // Responsáveis + substitutos (renomear UnidadeResponsavelService)
```

**Justificativa:**
- ✅ Elimina wrappers puros (UnidadeConsulta, UsuarioConsulta)
- ✅ Agrupa responsabilidades relacionadas (Organização vs Usuários vs Responsabilidades)
- ✅ Reduz número de mocks em testes
- ✅ Sem perda funcional - todos os métodos preservados

**Impacto:**
- Redução: -6 arquivos, -300 LOC de indireção
- Testes afetados: ~15
- Regras ArchUnit: Nenhuma

##### 1.2. Consolidar SubprocessoServices (1 dia)
**Estado Atual:** 8 services (1.624 LOC total)
```
subprocesso/service/
├── crud/
│   ├── SubprocessoCrudService.java (156 LOC)
│   └── SubprocessoValidacaoService.java (226 LOC)
├── workflow/
│   ├── SubprocessoMapaWorkflowService.java (422 LOC)
│   ├── SubprocessoCadastroWorkflowService.java (338 LOC)
│   ├── SubprocessoAdminWorkflowService.java (106 LOC)
│   └── SubprocessoTransicaoService.java (111 LOC)
├── query/
│   └── ConsultasSubprocessoService.java (118 LOC)
└── notificacao/
    └── SubprocessoEmailService.java (147 LOC) ← WRAPPER
```

**Estado Desejado:** 3 services (~1.400 LOC total)
```
subprocesso/service/
├── SubprocessoService.java (~350 LOC)
│   // CRUD + Consultas + Validação
├── SubprocessoWorkflowService.java (~900 LOC)
│   // Todas as transições de estado (complexidade legítima)
└── (SubprocessoEmailService eliminado → NotificacaoService global)
```

**Justificativa:**
- ✅ Elimina separação CQRS desnecessária (sistema sem carga para justificar)
- ✅ Elimina wrapper de notificação (lógica já está em NotificacaoService)
- ✅ WorkflowService com 900 LOC é justificado (18 estados, transições complexas)
- ✅ Validação como métodos privados, não service separado

**Impacto:**
- Redução: -5 arquivos, -224 LOC
- Testes afetados: ~12
- Regras ArchUnit: Nenhuma

##### 1.3. Atualizar Testes de Arquitetura (0.5 dia)
**Ação:** Generalizar regras #2 e #3 de controllers específicos

**Antes:**
```java
// Regra #2: mapa_controller_should_only_access_mapa_service
// Regra #3: processo_controller_should_only_access_processo_service
```

**Depois:**
```java
// Nova regra genérica: controllers_should_only_access_own_module_services
classes()
    .that().resideInAPackage("..controller..")
    .should().onlyAccessClassesThat()
    .resideInAnyPackage("..controller..", "..service..", "..facade..", "..dto..", "..comum..")
```

**Justificativa:**
- ✅ Mantém proteção contra acesso cruzado de módulos
- ✅ Mais flexível e genérica
- ✅ Reduz número de regras específicas

##### 1.4. Arquivar Documentação Obsoleta (0.5 dia)
**Arquivos a mover para `backend/etc/docs/archive/complexity-v1/`:**
- `LEIA-ME-COMPLEXIDADE.md` (versão 1, obsoleta)
- `complexity-report.md` (detalhes técnicos v1)
- `complexity-v1-vs-v2-comparison.md` (comparação histórica)

**Arquivos a remover:**
- `complexity-summary.txt` (substituído)

**Justificativa:**
- ✅ Reduz confusão sobre qual versão é atual
- ✅ Preserva histórico para referência
- ✅ Mantém documentação limpa

#### Frontend (2 dias)

##### 1.5. Consolidar Store de Processos (0.5 dia)
**Estado Atual:** 4 arquivos (261 LOC total)
```
stores/
├── processos.ts (agregador, re-exporta tudo)
├── processos/core.ts (97 LOC)
├── processos/workflow.ts (120 LOC)
└── processos/context.ts (44 LOC)
```

**Estado Desejado:** 1 arquivo (~250 LOC)
```
stores/
└── processos.ts (consolidado)
```

**Justificativa:**
- ✅ Navegação mais fácil (Cmd+F encontra tudo)
- ✅ Estado único (sem coordenação de lastError entre 3 stores)
- ✅ Padrão Vue recomendado (setup stores podem ter 300-400 linhas)
- ✅ Menos imports (1 import vs 4 possíveis)

**Impacto:**
- Redução: -3 arquivos
- Testes afetados: ~8

##### 1.6. Eliminar Composables View-Specific (1 dia)
**Composables a eliminar (10 arquivos):**
- `useProcessoView.ts` → lógica para ProcessoView.vue
- `useUnidadeView.ts` → lógica para UnidadeView.vue
- `useVisAtividades.ts` → lógica para view
- `useVisMapa.ts` → lógica para view
- `useAtividadeForm.ts` → lógica para form component
- `useProcessoForm.ts` → lógica para form component
- `useCadAtividades.ts` → lógica para view
- `useModalManager.ts` → substituir por useModal genérico
- `useLoadingManager.ts` → usar reactive do Vue
- `useApi.ts` → desnecessário

**Composables GENÉRICOS a manter/criar (6 arquivos):**
- `useForm.ts` - Validação + submit genérico
- `useModal.ts` - Gerenciamento de modais
- `usePagination.ts` - Paginação reutilizável
- `useLocalStorage.ts` - Persistência
- `useValidation.ts` - Validações comuns
- `useBreadcrumbs.ts` - Navegação

**Justificativa:**
- ✅ View-specific composables são anti-padrão
- ✅ Lógica deve estar na View onde é usada
- ✅ Composables devem ser reutilizáveis entre múltiplas views

**Impacto:**
- Redução: -10 arquivos
- Testes afetados: ~10

#### Validação Fase 1 (1 dia)
- [ ] Suite completa de testes backend passa
- [ ] Suite completa de testes frontend passa
- [ ] Todas as regras ArchUnit passam
- [ ] Code review aprovado
- [ ] Sem degradação de performance

**Resultado Fase 1:**
- ✅ **-19 arquivos** (services + stores + composables)
- ✅ **~45 testes ajustados**
- ✅ **2 regras ArchUnit** generalizadas
- ✅ **Documentação limpa**
- ✅ **Risco:** BAIXO

---

### 🟡 FASE 2: Simplificação Estrutural

#### Backend (7 dias)

##### 2.1. Consolidar Módulos mantendo Facades (2 dias)
**Objetivo:** Consolidar módulos relacionados mantendo Facades como fronteira arquitetural

**Candidatos à consolidação:**
- **Acompanhamento:** `AlertaFacade` + `AnaliseFacade` + `PainelFacade` → `AcompanhamentoFacade`
- **Configuração:** `ConfiguracaoFacade` pode ser eliminada (service direto)
- **Autenticação:** `LoginFacade` → lógica para `AutenticacaoService`

**Estratégia:**
- ✅ Manter Controllers chamando Facades
- ✅ Consolidar services especializados dentro do módulo
- ✅ Facades orquestram operações complexas

**Justificativa:**
- ✅ Reduz número de facades mantendo padrão arquitetural
- ✅ Elimina facades que são apenas pass-through
- ✅ Mantém complexidade de orquestração onde necessário
- ⚠️ Alinhado com ADR-001 (Facade Pattern) - reforça uso adequado

**Impacto:**
- Redução: ~5 facades (~600 LOC)
- Testes afetados: ~20
- Regras ArchUnit: #7 (REFORÇAR), #15 (REFORÇAR)

##### 2.2. Introduzir @JsonView (3 dias)
**Objetivo:** Substituir DTOs simples por @JsonView do Jackson

**DTOs candidatos à eliminação (15 arquivos):**
- Responses simples que duplicam estrutura de Entities
- Sem agregações (dados de única entity)
- Sem transformações (campos derivados, cálculos)

**Exemplo de conversão:**

**Antes:**
```java
// Processo.java (Entity)
@Entity
public class Processo {
    @Id private Long codigo;
    private String nome;
    private TipoProcesso tipo;
    private String observacoesInternas; // campo sensível
}

// ProcessoResponse.java (DTO)
public record ProcessoResponse(Long codigo, String nome, String tipo) {}

// ProcessoMapper.java (MapStruct)
@Mapper
public interface ProcessoMapper {
    ProcessoResponse toResponse(Processo processo);
}
```

**Depois:**
```java
// Processo.java (Entity com @JsonView)
@Entity
public class Processo {
    public static class Views {
        public interface Public {}
        public interface Admin extends Public {}
    }
    
    @JsonView(Views.Public.class)
    @Id private Long codigo;
    
    @JsonView(Views.Public.class)
    private String nome;
    
    @JsonView(Views.Public.class)
    private TipoProcesso tipo;
    
    @JsonView(Views.Admin.class) // Só ADMIN vê
    private String observacoesInternas;
}

// Controller
@GetMapping("/{codigo}")
@JsonView(Processo.Views.Public.class)
public Processo buscar(@PathVariable Long codigo) {
    return processoService.buscar(codigo); // Retorna entity direto
}
```

**DTOs a MANTER (25 arquivos):**
- Agregações (dados de múltiplas entities)
- Transformações (campos derivados)
- Requests com validações complexas

**Justificativa:**
- ✅ @JsonView é padrão Spring (amplamente usado, bem testado)
- ✅ Redução significativa de código (-2.650 LOC estimadas)
- ✅ Manutenção mais simples (1 arquivo vs 3)
- ⚠️ Requer testes de serialização (crítico para segurança)

**Impacto:**
- Redução: -15 DTOs (~750 LOC)
- Testes afetados: ~25 (ajustar) + ~15 novos (serialização)
- Regras ArchUnit: #10 (ADAPTAR)

##### 2.3. Atualizar Testes de Arquitetura (1 dia)
**Regra #7:** Reforçar uso de Facades
```java
// REFORÇAR: Controllers devem usar Facades de seu módulo
classes()
    .that().resideInAPackage("..controller..")
    .should().onlyDependOnClassesThat()
    .resideInAnyPackage("..facade..", "..dto..", "..comum..", "java..", "org.springframework..")
```

**Regra #10:** Adaptar para permitir @JsonView
```java
// ADAPTAR: Controllers podem retornar entities com @JsonView
classes()
    .that().resideInAPackage("..controller..")
    .and().areAnnotatedWith(RestController.class)
    .should(returnEntitiesOnlyWithJsonView())
```

**Regra #15:** Reforçar separação Facade/Repository
```java
// REFORÇAR: Facades não acessam repositories diretamente
classes()
    .that().resideInAPackage("..facade..")
    .should().onlyAccessClassesThat()
    .resideOutsideOfPackage("..repository..")
```

##### 2.4. Atualizar ADRs (1 dia)
**ADRs a atualizar:**

**ADR-001 (Facade Pattern):**
- Reforçar Facade como fronteira por módulo consolidado
- Documentar critérios para quando usar Facade vs Service direto
- Exemplos de consolidação bem-sucedida

**ADR-004 (DTO Pattern):**
- Adicionar @JsonView como alternativa válida
- Documentar quando usar DTO vs @JsonView
- Exemplos de uso seguro de @JsonView

**ADR-008 (NOVO - Simplification Decisions):**
- Criar novo ADR documentando processo de simplificação
- Justificativas, métricas, decisões tomadas
- Lições aprendidas

#### Validação Fase 2 (3 dias)
- [ ] Suite completa de testes (backend + frontend)
- [ ] Todas as regras ArchUnit passam
- [ ] Testes de serialização JSON (100% coverage)
- [ ] Testes E2E principais (smoke tests)
- [ ] Performance não degradou (±5%)
- [ ] Security: @JsonView não vaza dados sensíveis
- [ ] Code review com foco em segurança

**Resultado Fase 2:**
- ✅ **-23 classes/arquivos**
- ✅ **~65 testes ajustados**
- ✅ **4 regras ArchUnit** adaptadas
- ✅ **3 ADRs atualizados** + 1 novo
- ✅ **Risco:** MÉDIO (reversível)

---

### 🔴 FASE 3: Simplificação Avançada (OPCIONAL)

**⚠️ DECISÃO:** POSTERGAR até evidência clara de necessidade

**Justificativa:**
- 🔴 **Alto risco:** Mexe em segurança (AccessPolicies) e workflow (Eventos)
- ⚠️ **Benefício marginal:** Ganho de ~20 classes vs risco alto
- ✅ **Fases 1+2 já entregam 80% do valor** com 30% do risco
- ⏸️ **Sem evidência de problema atual:** Sistema funciona bem

**Critérios para reconsiderar:**
- Time cresce para 10+ desenvolvedores OU
- Sistema escala para 100+ usuários simultâneos OU
- Evidência de problemas de performance/manutenibilidade OU
- Aprovação explícita de CTO + Security Officer

**Se executada (15+ dias):**
- Consolidar 4 AccessPolicies em métodos de SecurityService
- Converter para @PreAuthorize onde possível
- Avaliar remoção de Event System (substituir por chamadas diretas)
- **Testes afetados:** ~35 (segurança crítica)
- **Risco:** 🔴 ALTO

---

## 📊 Métricas e Resultados Esperados

### Antes da Simplificação
| Métrica | Valor Atual |
|---------|-------------|
| Arquivos Java | 250 |
| Arquivos TS/Vue | 180 |
| Services | 35 |
| Facades | 12 |
| DTOs | 78 |
| Stores | 16 |
| Composables | 18 |
| Tempo adicionar campo | 15-17 arquivos |
| Tempo onboarding | 2-3 semanas |
| Camadas stack trace | 7 |

### Após Fases 1+2 (Meta)
| Métrica | Valor Alvo | Melhoria |
|---------|------------|----------|
| Arquivos Java | ~210 | **-16%** |
| Arquivos TS/Vue | ~160 | **-11%** |
| Services | ~20 | **-43%** |
| Facades | 4-6 | **-50%** |
| DTOs | ~25 | **-68%** |
| Stores | 15 | **-6%** |
| Composables | 6 | **-67%** |
| Tempo adicionar campo | 5-7 arquivos | **-65%** ⭐ |
| Tempo onboarding | 1 semana | **-60%** ⭐ |
| Camadas stack trace | 4 | **-43%** ⭐ |

### KPIs de Qualidade (Não podem degradar)
- ✅ Cobertura de testes: manter ≥70%
- ✅ Tempo de build: reduzir ou manter
- ✅ Tempo execução testes: reduzir ou manter
- ✅ Zero vulnerabilidades de segurança novas
- ✅ Zero bugs funcionais introduzidos

---

## 🎯 Padrões Arquiteturais Mantidos

### ✅ O que MANTER (Fundamental)

**1. Separation of Concerns**
- Módulos de domínio (processo, subprocesso, mapa, organizacao)
- Separação Controller/Service/Repository
- Pacotes por funcionalidade, não por camada

**2. Dependency Injection**
- Spring @Service, @Component, constructor injection
- Injeção de dependências no frontend (Pinia)

**3. Workflow State Machines**
- WorkflowServices para transições de estado
- Complexidade de SubprocessoWorkflowService (~900 LOC) é legítima

**4. Security**
- Spring Security com @PreAuthorize
- HierarchyService para verificação de subordinação
- Controle de acesso centralizado

**5. Bean Validation**
- @NotNull, @Valid, @Min, @Max em Requests
- Validações customizadas quando necessário

**6. Facade Pattern (adaptado)**
- Facades como fronteira de módulos consolidados
- Facades para orquestração complexa
- Controllers usam Facades (não services especializados diretamente)

**7. Event System**
- Spring Events para desacoplamento entre módulos
- Facilita extensão sem quebrar código existente

### ❌ O que SIMPLIFICAR

**1. Services Fragmentados**
- 9 services de Organização → 3 services coesos
- 8 services de Subprocesso → 3 services coesos

**2. Facades Pass-Through**
- 8 facades que apenas delegam → eliminar
- 4 facades com orquestração real → manter

**3. DTOs Duplicados**
- 53 DTOs com estrutura duplicada → @JsonView
- 25 DTOs com transformação real → manter

**4. Stores Fragmentadas**
- 4 arquivos de processos → 1 arquivo consolidado

**5. Composables View-Specific**
- 10 composables específicos → lógica nas Views
- 6 composables genéricos → manter/criar

---

## 🚨 Gestão de Riscos

### Mitigação de Riscos

**Para Fase 1 (BAIXO risco):**
- ✅ Testes extensivos antes de merge
- ✅ Code review obrigatório
- ✅ Validação incremental (consolidar 1 módulo por vez)
- ✅ Rollback fácil (git revert)

**Para Fase 2 (MÉDIO risco):**
- ✅ Testes de serialização JSON (crítico)
- ✅ Testes E2E de smoke
- ✅ Deploy gradual (dev → staging → produção)
- ✅ Monitoramento de erros (primeiras 48h)
- ✅ Plano de rollback documentado

**Para Fase 3 (ALTO risco):**
- 🔴 Testes de segurança manual
- 🔴 Testes de penetração básicos
- 🔴 Aprovação de security officer
- 🔴 Janela de manutenção (baixo tráfego)
- 🔴 Rollback testado previamente

### Critérios de Interrupção

**Interromper imediatamente se:**
- ❌ Cobertura de testes cai abaixo de 70%
- ❌ Testes E2E críticos falham
- ❌ Vulnerabilidade de segurança identificada
- ❌ Bug funcional em produção
- ❌ Performance degrada >10%

**Reconsiderar abordagem se:**
- ⚠️ Mais de 20% dos testes precisam de ajuste
- ⚠️ Esforço de implementação >50% acima do estimado
- ⚠️ Resistência significativa do time
- ⚠️ Stakeholders solicitam pausa

---

## 📚 Referências

### Documentos Base
- [LEIA-ME-COMPLEXIDADE-V2.md](LEIA-ME-COMPLEXIDADE-V2.md) - Análise completa com evidências
- [DECISOES-SIMPLIFICACAO.md](DECISOES-SIMPLIFICACAO.md) - Registro de decisões
- [guia-implementacao-simplificacao-v2.md](guia-implementacao-simplificacao-v2.md) - Guia prático

### Documentação Técnica
- [backend-padroes.md](backend/etc/docs/backend-padroes.md) - Padrões de código backend
- [frontend-padroes.md](frontend/etc/docs/frontend-padroes.md) - Padrões de código frontend
- [guia-dtos.md](backend/etc/docs/guia-dtos.md) - Taxonomia e convenções de DTOs

### ADRs Relevantes
- [ADR-001: Facade Pattern](backend/etc/docs/adr/ADR-001-facade-pattern.md)
- [ADR-003: Security Architecture](backend/etc/docs/adr/ADR-003-security-architecture.md)
- [ADR-004: DTO Pattern](backend/etc/docs/adr/ADR-004-dto-pattern.md)
- [ADR-006: Domain Aggregates](backend/etc/docs/adr/ADR-006-domain-aggregates-organization.md)

### Testes de Arquitetura
- [ArchConsistencyTest.java](backend/src/test/java/sgc/ArchConsistencyTest.java) - Regras ArchUnit
- [PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md](backend/etc/docs/PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md)

---

## 📅 Cronograma Proposto

### Fase 1: Quick Wins
**Duração:** 7 dias  
**Risco:** 🟢 BAIXO  
**Início:** [A definir]  
**Fim:** [A definir]

### Fase 2: Simplificação Estrutural
**Duração:** 12 dias  
**Risco:** 🟡 MÉDIO  
**Início:** [Após Fase 1]  
**Fim:** [A definir]

### Fase 3: Simplificação Avançada (OPCIONAL)
**Duração:** 15+ dias  
**Risco:** 🔴 ALTO  
**Status:** ⏸️ POSTERGAR

**Total Fases 1+2:** ~20 dias úteis (~4 semanas)

---

## ✅ Critérios de Sucesso

### Quantitativos (Obrigatórios)
- [ ] Todos os testes passam (100%)
- [ ] Cobertura mantém ≥70%
- [ ] Performance não degrada (±5%)
- [ ] Zero vulnerabilidades novas
- [ ] Redução de 15-20% em arquivos/classes

### Qualitativos (Desejados)
- [ ] Feedback positivo do time (>80%)
- [ ] Onboarding mais rápido (medido)
- [ ] Menos bugs em produção (próximos 3 meses)
- [ ] Tempo de desenvolvimento de features reduzido

---

**Elaborado por:** Agente de Consolidação  
**Data:** 16 de Fevereiro de 2026  
**Versão:** 1.0  
**Status:** 🟡 Aguardando Aprovação
