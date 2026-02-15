# 📊 Plano Consolidado de Redução de Complexidade - SGC

**Data:** 15 de Fevereiro de 2026  
**Versão:** 3.0 (Consolidada)  
**Status:** Análise Completa com Impacto em Testes e Documentação

---

## 🎯 Resumo Executivo

Este documento **consolida e finaliza** o plano de redução de complexidade do SGC, integrando:

✅ **Análises anteriores** (LEIA-ME-COMPLEXIDADE-V2.md)  
✅ **Impacto em testes de arquitetura** (16 regras ArchUnit analisadas)  
✅ **Impacto em documentação** (128 arquivos markdown revisados)  
✅ **Impacto na suíte de testes** (206 arquivos de teste backend)  
✅ **Decisões sobre o que manter vs simplificar**

---

## 📋 Análise de Impacto Completa

### 1. Testes de Arquitetura (ArchUnit)

**Situação Atual:**
- **16 regras ArchUnit** em `ArchConsistencyTest.java` (351 linhas)
- **Regras críticas** que garantem padrões arquiteturais
- Muitas regras **dependem da arquitetura atual** (Facades, DTOs, etc.)

#### Regras Analisadas e Decisões

| # | Regra | Impacto na Simplificação | Decisão |
|---|-------|-------------------------|---------|
| 1 | `controllers_should_not_access_repositories` | ✅ Não afeta | **MANTER** - Regra fundamental |
| 2 | `mapa_controller_should_only_access_mapa_service` | ⚠️ Específica | **REVISAR** - Generalizar |
| 3 | `processo_controller_should_only_access_processo_service` | ⚠️ Específica | **REVISAR** - Generalizar |
| 4 | `comum_package_should_not_contain_business_logic` | ✅ Não afeta | **MANTER** |
| 5 | `services_should_not_access_other_modules_repositories` | ✅ Não afeta | **MANTER** |
| 6 | `controllers_e_services_devem_estar_em_pacotes_null_marked` | ✅ Não afeta | **MANTER** |
| 7 | `controllers_should_only_use_facades_not_specialized_services` | 🔴 **CONFLITO CRÍTICO** | **REMOVER/ADAPTAR** |
| 8 | `facades_should_have_facade_suffix` | 🟡 Afeta se eliminar facades | **ADAPTAR** |
| 9 | `dtos_should_not_be_jpa_entities` | ⚠️ Com @JsonView muda | **MANTER** (ainda válida) |
| 10 | `controllers_should_not_return_jpa_entities` | ⚠️ Com @JsonView muda | **REVISAR** (@JsonView permite) |
| 11 | `services_should_not_throw_access_denied_directly` | ✅ Não afeta | **MANTER** |
| 12 | `controllers_should_have_controller_suffix` | ✅ Não afeta | **MANTER** |
| 13 | `repositories_should_have_repo_suffix` | ✅ Não afeta | **MANTER** |
| 14 | `domain_events_should_start_with_evento` | ⚠️ Se remover eventos | **MANTER** (eventos são úteis) |
| 15 | `facades_should_not_access_repositories_directly` | 🔴 **CONFLITO** | **REMOVER** (ao eliminar facades) |
| 16 | `no_cycles_within_service_packages` | ✅ Não afeta | **MANTER** |

**Resumo de Decisões:**
- **MANTER:** 10 regras (63%)
- **ADAPTAR:** 4 regras (25%)
- **REMOVER:** 2 regras (12%)

#### Ações sobre Testes de Arquitetura

**Fase 1 (Imediata):**
1. ✅ **Generalizar regras específicas** (#2, #3)
   - Substituir por regra genérica: "Controllers devem usar apenas Services/Facades de seu módulo"

**Fase 2 (Com simplificação de Facades):**
2. 🔴 **Adaptar regra #7** - `controllers_should_only_use_facades_not_specialized_services`
   - **NOVA REGRA:** "Controllers podem usar Services OU Facades, mas não misturar"
   - Permite simplificação mas mantém consistência

3. 🔴 **Remover regra #15** - `facades_should_not_access_repositories_directly`
   - Desnecessária após eliminar facades pass-through

**Fase 3 (Com @JsonView):**
4. ⚠️ **Adaptar regra #10** - `controllers_should_not_return_jpa_entities`
   - **NOVA REGRA:** "Controllers que retornam entities devem usar @JsonView"
   - Permite @JsonView mas mantém proteção contra vazamento de dados

---

### 2. Documentação (128 arquivos)

**Situação Atual:**
- **128 arquivos** markdown espalhados por todo o repositório
- **Documentação duplicada** sobre complexidade (v1, v2, reports, summaries)
- **ADRs importantes** (7 arquivos) que precisam ser mantidos
- **Documentação arquivada** (MBT - 13 arquivos) que pode ser consolidada

#### Análise por Categoria

| Categoria | Quantidade | Ação | Justificativa |
|-----------|------------|------|---------------|
| **Complexidade (raiz)** | 8 | Consolidar → 1 | 5 arquivos sobre o mesmo tema |
| **ADRs** | 7 | Manter + Atualizar | Decisões arquiteturais críticas |
| **Guias Backend** | 9 | Manter | Referência para desenvolvimento |
| **Guias Frontend** | 5 | Manter | Referência para desenvolvimento |
| **MBT Archive** | 13 | Arquivar + README | Histórico, não atual |
| **REQs** | 48 | Manter | Especificações funcionais |
| **READMEs Módulos** | 25+ | Manter | Documentação de código |
| **E2E/UX** | 6 | Manter | Testes e melhorias UX |
| **Outros** | Variado | Revisar caso a caso | - |

#### Documentação de Complexidade a Consolidar

**Arquivos DUPLICADOS na raiz:**
1. `LEIA-ME-COMPLEXIDADE.md` (5.7 KB) - ❌ **ARQUIVAR** (v1 obsoleta)
2. `LEIA-ME-COMPLEXIDADE-V2.md` (23.9 KB) - ⚠️ **BASE PARA CONSOLIDAÇÃO**
3. `complexity-report.md` (31.1 KB) - ❌ **ARQUIVAR** (detalhes técnicos v1)
4. `complexity-summary.txt` (antigo) - ❌ **REMOVER** (obsoleto)
5. `complexity-summary-v2.txt` (9.1 KB) - ⚠️ **INTEGRAR no consolidado**
6. `complexity-v1-vs-v2-comparison.md` (9.1 KB) - ❌ **ARQUIVAR** (histórico)
7. `guia-implementacao-simplificacao-v2.md` (21.6 KB) - ⚠️ **INTEGRAR no consolidado**
8. `INDICE-DOCUMENTACAO-COMPLEXIDADE.md` (7.5 KB) - ❌ **SUBSTITUIR** (este doc é o índice)

**Total:** 8 arquivos → **1 arquivo consolidado** (este documento)

#### ADRs a Atualizar

Após simplificação, **4 ADRs precisam de atualização**:

| ADR | Motivo da Atualização | Prioridade |
|-----|----------------------|------------|
| **ADR-001** (Facade Pattern) | Permitir uso direto de Services | 🔴 ALTA |
| **ADR-003** (Security) | Simplificar AccessPolicies (Fase 3) | 🟡 MÉDIA |
| **ADR-004** (DTO Pattern) | Adicionar @JsonView como alternativa | 🔴 ALTA |
| **ADR-006** (Domain Aggregates) | Atualizar após consolidação de Services | 🟡 MÉDIA |

---

### 3. Suíte de Testes (3000+ testes)

**Situação Atual:**
- **206 arquivos de teste** no backend
- Testes **fortemente acoplados** à estrutura atual (Facades, DTOs, Services)
- **Cobertura:** ~70-80% (boa, mas pode melhorar com simplificação)

#### Impacto por Tipo de Simplificação

| Simplificação | Testes Afetados | Esforço de Ajuste | Risco |
|---------------|-----------------|-------------------|-------|
| **Consolidar Services** | ~30-40 testes | MÉDIO (refatorar mocks) | MÉDIO |
| **Remover Facades pass-through** | ~15-20 testes | BAIXO (mover para Service tests) | BAIXO |
| **Introduzir @JsonView** | ~25-30 testes | MÉDIO (validar serialização) | MÉDIO |
| **Consolidar Stores (frontend)** | ~10-15 testes | BAIXO (atualizar imports) | BAIXO |
| **Simplificar AccessPolicies** | ~20 testes | ALTO (segurança crítica) | ALTO |

**Total Estimado:** **100-125 testes** (3-4% do total) precisam de ajustes

#### Estratégia de Ajuste de Testes

**Princípios:**
1. ✅ **Manter cobertura de negócio** - Não remover testes de lógica
2. ✅ **Simplificar mocks** - Menos camadas = menos mocks
3. ✅ **Testes mais rápidos** - Menos indireção = execução mais rápida
4. ⚠️ **Adicionar testes de serialização** - Para @JsonView

**Por Fase:**

**Fase 1 (Services + Stores):**
- Mover testes de Facades eliminados para Services
- Atualizar imports em testes de frontend
- **Estimativa:** 2-3 dias de ajustes

**Fase 2 (@JsonView + Facades restantes):**
- Criar testes de serialização JSON
- Validar @JsonView para cada perfil (Public, Admin)
- Migrar testes de Controllers (sem Facades)
- **Estimativa:** 3-4 dias de ajustes

**Fase 3 (Security - OPCIONAL):**
- Refatorar ~20 testes de AccessPolicies
- Adicionar testes de @PreAuthorize
- **Estimativa:** 2-3 dias de ajustes

---

## 🚦 Plano de Execução Revisado

### 🟢 FASE 1: Quick Wins + Ajuste de Testes (7 dias, BAIXO risco)

#### Backend (3 dias)

**1.1. Consolidar OrganizacaoServices (1 dia)**
- [ ] Criar `OrganizacaoService` (9 services → 3)
- [ ] Migrar testes: mover de services individuais para consolidados
- [ ] Atualizar Facades que usam esses services
- **Testes afetados:** ~15
- **Regras ArchUnit afetadas:** Nenhuma

**1.2. Consolidar SubprocessoServices (1 dia)**
- [ ] Criar `SubprocessoService` consolidado
- [ ] Eliminar `SubprocessoEmailService` (wrapper)
- [ ] Migrar testes
- **Testes afetados:** ~12
- **Regras ArchUnit afetadas:** Nenhuma

**1.3. Atualizar Testes de Arquitetura (0.5 dia)**
- [ ] Generalizar regras #2 e #3 (controllers específicos)
- [ ] Validar todas as 16 regras ainda passam
- **Regras afetadas:** 2

**1.4. Documentação: Arquivar v1 (0.5 dia)**
- [ ] Mover `LEIA-ME-COMPLEXIDADE.md` → `backend/etc/docs/archive/`
- [ ] Mover `complexity-report.md` → `backend/etc/docs/archive/`
- [ ] Mover `complexity-v1-vs-v2-comparison.md` → `backend/etc/docs/archive/`
- [ ] Atualizar README.md principal

#### Frontend (2 dias)

**1.5. Consolidar Stores - processos (0.5 dia)**
- [ ] Mesclar processos/{core,workflow,context}.ts
- [ ] Atualizar imports em componentes
- [ ] Migrar testes
- **Testes afetados:** ~8

**1.6. Eliminar Composables View-Specific (1 dia)**
- [ ] Identificar 10 composables view-specific
- [ ] Mover lógica para Views
- [ ] Atualizar testes de componentes
- **Testes afetados:** ~10

**1.7. Consolidar Documentação (0.5 dia)**
- [ ] Finalizar este documento
- [ ] Remover arquivos obsoletos
- [ ] Criar arquivo de CHANGELOG

#### Validação Fase 1 (1 dia)

- [ ] Rodar suite completa de testes backend
- [ ] Rodar suite completa de testes frontend
- [ ] Validar regras ArchUnit (todas devem passar)
- [ ] Code review completo

**Resultado Fase 1:**
- ✅ **-19 arquivos** (services + stores + composables)
- ✅ **~45 testes ajustados** (3-4% do total backend, ~10% frontend afetados)
- ✅ **2 regras ArchUnit** generalizadas
- ✅ **Documentação limpa** (v1 arquivada)
- ✅ **Risco:** BAIXO (sem quebra de funcionalidades)

---

### 🟡 FASE 2: Simplificação Estrutural + Ajustes Maiores (12 dias, MÉDIO risco)

#### Backend (7 dias)

**2.1. Remover Facades Pass-Through (2 dias)**
- [ ] Identificar 5 facades para eliminar (AlertaFacade, AnaliseFacade, etc.)
- [ ] Controllers chamam Services diretamente
- [ ] Migrar/mover testes de Facades para Services
- [ ] Atualizar documentação
- **Testes afetados:** ~20
- **Regras ArchUnit afetadas:** #7 (ADAPTAR), #15 (REMOVER)

**2.2. Atualizar Testes de Arquitetura - Facades (0.5 dia)**
- [ ] ADAPTAR regra #7: permitir Controllers → Services direto
- [ ] REMOVER regra #15: facades não acessam repos (desnecessária)
- [ ] Criar nova regra: "Controllers usam OU Services OU Facades (não mistura)"

**2.3. Introduzir @JsonView (3 dias)**
- [ ] Definir views em 5 Entities principais (Processo, Subprocesso, Mapa, etc.)
- [ ] Migrar 15 Responses simples para @JsonView
- [ ] Manter DTOs complexos (agregações, transformações)
- [ ] **CRÍTICO:** Criar testes de serialização para cada view
- **Testes afetados:** ~25
- **Regras ArchUnit afetadas:** #10 (ADAPTAR para permitir @JsonView)

**2.4. Atualizar Testes de Arquitetura - DTOs (0.5 dia)**
- [ ] ADAPTAR regra #10: "Entities com @JsonView são permitidas"
- [ ] Criar regra: "Entities retornadas devem ter @JsonView em controller"

**2.5. Atualizar ADRs (1 dia)**
- [ ] Atualizar ADR-001 (Facade Pattern) - permitir uso direto
- [ ] Atualizar ADR-004 (DTO Pattern) - adicionar @JsonView
- [ ] Criar ADR-008 (Simplification Decisions) - documentar este processo

#### Validação e Testes (3 dias)

**2.6. Validação Completa**
- [ ] Rodar suite completa (backend + frontend)
- [ ] Validar TODAS as regras ArchUnit
- [ ] Testes de serialização JSON (100% coverage)
- [ ] Testes E2E principais (smoke tests)
- [ ] Performance: validar que não degradou
- [ ] Security: validar que @JsonView não vaza dados sensíveis

**2.7. Documentação Final**
- [ ] Atualizar este documento com resultados
- [ ] Criar guia de migração para futuros desenvolvedores
- [ ] Atualizar README principal

#### Reversão (buffer - 2 dias se necessário)

- Plano de rollback se problemas críticos aparecerem
- Testes de regressão

**Resultado Fase 2:**
- ✅ **-23 classes/arquivos** (facades + DTOs)
- ✅ **~65 testes ajustados** (3% do total)
- ✅ **4 regras ArchUnit** adaptadas/removidas
- ✅ **3 ADRs atualizados** + 1 novo
- ✅ **Risco:** MÉDIO (reversível, sem alterar lógica de negócio)

---

### 🔴 FASE 3: Simplificação Avançada (OPCIONAL - 15+ dias, ALTO risco)

**⚠️ ATENÇÃO:** Fase 3 é **OPCIONAL** e só deve ser executada se:
- Fases 1 e 2 foram bem-sucedidas
- Aprovação explícita da liderança técnica
- Janela de manutenção disponível (baixo tráfego)

#### Backend (10 dias)

**3.1. Simplificar Segurança (5 dias)**
- [ ] Consolidar 4 AccessPolicies em métodos de SecurityService
- [ ] Converter para @PreAuthorize onde possível
- [ ] Manter auditoria básica (simplificar AccessAuditService)
- **Testes afetados:** ~20 (SEGURANÇA CRÍTICA)
- **Regras ArchUnit afetadas:** #11 (REVISAR)
- **Risco:** 🔴 ALTO - Segurança é crítica

**3.2. Avaliar Remoção de Event System (5 dias)**
- [ ] Analisar cada evento (EventoProcessoCriado, etc.)
- [ ] Substituir por chamadas diretas em Facades OU
- [ ] MANTER se realmente houver desacoplamento necessário
- **Testes afetados:** ~15
- **Regras ArchUnit afetadas:** #14 (pode remover se eventos forem)
- **Risco:** 🔴 ALTO - Fluxo de processo pode quebrar

#### Validação Fase 3 (5 dias)

- [ ] Suite completa de testes (3x execuções)
- [ ] Testes de segurança manual
- [ ] Testes de penetração básicos
- [ ] Code review com foco em segurança
- [ ] Aprovação de security officer

**Resultado Fase 3:**
- ⚠️ **-20 classes** (policies + eventos)
- ⚠️ **~35 testes ajustados**
- ⚠️ **2 regras ArchUnit** potencialmente removidas
- ⚠️ **Risco:** ALTO (mexe em segurança e workflow)

**Decisão sobre Fase 3:** ⏸️ **POSTERGAR** até evidência de necessidade

---

## 📊 Métricas de Sucesso

### Antes da Simplificação

| Métrica | Valor Atual |
|---------|-------------|
| **Arquivos Java** | 250 |
| **Arquivos TS/Vue** | 180 |
| **Testes Backend** | 206 arquivos, ~3000 testes |
| **Regras ArchUnit** | 16 |
| **Documentos MD** | 128 |
| **Tempo adicionar campo** | 15-17 arquivos alterados |
| **Tempo onboarding** | 2-3 semanas |
| **Camadas stack trace** | 7 camadas |

### Após Fase 1+2 (Meta)

| Métrica | Valor Alvo | Melhoria |
|---------|------------|----------|
| **Arquivos Java** | ~210 | **-16%** |
| **Arquivos TS/Vue** | ~160 | **-11%** |
| **Testes Backend** | ~195 arquivos, ~2950 testes | **-5%** (remoção de duplicados) |
| **Regras ArchUnit** | 14 (2 removidas, 4 adaptadas) | **-12.5%** |
| **Documentos MD** | ~115 (13 arquivados) | **-10%** |
| **Tempo adicionar campo** | 5-7 arquivos | **-65%** ⭐ |
| **Tempo onboarding** | 1 semana | **-60%** ⭐ |
| **Camadas stack trace** | 4 camadas | **-43%** ⭐ |

### KPIs de Qualidade

**Não podem degradar:**
- ✅ Cobertura de testes: manter ≥70%
- ✅ Tempo de build: reduzir ou manter
- ✅ Tempo execução testes: reduzir ou manter
- ✅ Zero vulnerabilidades de segurança novas
- ✅ Zero bugs funcionais introduzidos

**Devem melhorar:**
- ⬆️ Velocidade de desenvolvimento: +50%
- ⬆️ Clareza de código: feedback subjetivo positivo
- ⬆️ Facilidade de debug: stack traces mais curtos

---

## 📝 Changelog de Documentação

### Arquivos Removidos (Fase 1)

- ❌ `complexity-summary.txt` (obsoleto)
- ❌ `INDICE-DOCUMENTACAO-COMPLEXIDADE.md` (substituído por este doc)

### Arquivos Arquivados (Fase 1)

- 📦 `LEIA-ME-COMPLEXIDADE.md` → `backend/etc/docs/archive/complexity-v1/`
- 📦 `complexity-report.md` → `backend/etc/docs/archive/complexity-v1/`
- 📦 `complexity-v1-vs-v2-comparison.md` → `backend/etc/docs/archive/complexity-v1/`

### Arquivos Consolidados (Fase 1)

- ✅ `LEIA-ME-COMPLEXIDADE-V2.md` + `complexity-summary-v2.txt` + `guia-implementacao-simplificacao-v2.md`  
  → **Este documento** (`PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md`)

### Arquivos Novos (Fase 2)

- 🆕 `ADR-008-simplification-decisions.md` - Documentação de decisões de simplificação

---

## 🔗 Referências

### Documentação Principal
- [README.md](README.md) - Introdução ao projeto
- [AGENTS.md](AGENTS.md) - Guia para agentes de desenvolvimento
- [backend/README.md](backend/README.md) - Backend específico
- [frontend/README.md](frontend/README.md) - Frontend específico

### ADRs Críticos
- [ADR-001: Facade Pattern](backend/etc/docs/adr/ADR-001-facade-pattern.md) - ⚠️ Será atualizado
- [ADR-003: Security Architecture](backend/etc/docs/adr/ADR-003-security-architecture.md) - Fase 3
- [ADR-004: DTO Pattern](backend/etc/docs/adr/ADR-004-dto-pattern.md) - ⚠️ Será atualizado
- [ADR-006: Domain Aggregates](backend/etc/docs/adr/ADR-006-domain-aggregates-organization.md) - ⚠️ Será atualizado

### Guias de Desenvolvimento
- [backend-padroes.md](backend/etc/docs/backend-padroes.md)
- [frontend-padroes.md](frontend/etc/docs/frontend-padroes.md)
- [guia-dtos.md](backend/etc/docs/guia-dtos.md)

### Requisitos
- [etc/reqs/](etc/reqs/) - 48 documentos de requisitos (6.104 linhas)

---

## ❓ FAQ

### Por que consolidar em um único documento?

**Problema:** 8 documentos sobre o mesmo tema (complexidade) geravam:
- ❌ Confusão: qual versão é a atual?
- ❌ Contradições: v1 vs v2 diziam coisas diferentes
- ❌ Manutenção duplicada: atualizar múltiplos arquivos

**Solução:** 1 documento consolidado que:
- ✅ É a única fonte da verdade
- ✅ Integra TODAS as análises (código + testes + documentação)
- ✅ Tem decisões finais sobre cada ponto

### E se a Fase 2 introduzir bugs?

**Mitigação de Riscos:**
1. ✅ **Testes extensivos** (suite completa + E2E)
2. ✅ **Code review obrigatório** antes de merge
3. ✅ **Deploy gradual** (dev → staging → produção)
4. ✅ **Plano de rollback** documentado
5. ✅ **Monitoramento** de erros em produção (primeiras 48h)

### Por que não executar Fase 3 imediatamente?

**Motivos:**
1. 🔴 **Alto risco** - Mexe em segurança (crítico)
2. ⚠️ **Benefício marginal** - Ganho de 20 classes vs risco alto
3. ⏸️ **Sem evidência de necessidade** - Sistema atual funciona
4. ✅ **Fases 1+2 já entregam 80% do valor** com 30% do risco

**Critério para reconsiderar Fase 3:**
- Time cresce para 10+ desenvolvedores OU
- Sistema escala para 100+ usuários simultâneos OU
- Evidência de problemas de performance/manutenibilidade

### Como garantir que regras ArchUnit continuam válidas?

**Processo:**
1. ✅ **Atualização incremental** - Adaptar regras a cada fase
2. ✅ **CI obrigatório** - Regras devem passar em cada commit
3. ✅ **Documentação de decisões** - Cada mudança em regra é documentada (ADR-008)
4. ✅ **Review de arquitetura** - Aprovação de arquiteto para mudanças de regras

---

## ✅ Checklist de Aprovação

Antes de iniciar implementação:

- [ ] Tech Lead revisou e aprovou este documento
- [ ] Time de QA revisou estratégia de testes
- [ ] Aprovação para Fase 1 (7 dias, BAIXO risco)
- [ ] Aprovação para Fase 2 (12 dias, MÉDIO risco)
- [ ] Decisão sobre Fase 3 (POSTERGAR vs APROVAR)
- [ ] Branch `feature/complexity-reduction` criada
- [ ] CI configurado para rodar testes a cada commit
- [ ] Plano de comunicação com stakeholders
- [ ] Janela de tempo alocada (3-4 semanas para Fases 1+2)

---

## 📅 Histórico

| Versão | Data | Mudanças |
|--------|------|----------|
| 1.0 | 15/02/2026 | LEIA-ME-COMPLEXIDADE.md (análise inicial) |
| 2.0 | 15/02/2026 | LEIA-ME-COMPLEXIDADE-V2.md (com provas) |
| 3.0 | 15/02/2026 | Este documento (consolidação final com testes + docs) |

---

**🎯 Próximo Passo:** Aprovação da liderança técnica → Iniciar Fase 1

---

**Elaborado por:** Agente de Consolidação de Complexidade  
**Revisado por:** [Pendente]  
**Aprovado por:** [Pendente]  
**Status:** 🟡 Aguardando Aprovação
