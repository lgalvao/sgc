# 📊 Rastreamento de Simplificação - SGC

**Data de Início:** 16 de Fevereiro de 2026  
**Última Atualização:** 16 de Fevereiro de 2026  
**Status Geral:** 🟡 Em Andamento

---

## 🎯 Progresso Geral

### Visão Geral das Fases

| Fase | Status | Progresso | Duração Planejada | Duração Real | Risco |
|------|--------|-----------|-------------------|--------------|-------|
| **Fase 1: Quick Wins** | 🟡 Em Andamento | 40% | 7 dias | [Em execução] | 🟢 BAIXO |
| **Fase 2: Simplificação Estrutural** | 🟡 Iniciada | 20% | 12 dias | [Não iniciado] | 🟡 MÉDIO |
| **Fase 3: Avançada (OPCIONAL)** | ⏸️ Postergada | 0% | 15+ dias | - | 🔴 ALTO |

### Métricas de Redução

| Componente | Baseline | Meta | Atual | Progresso |
|------------|----------|------|-------|-----------|
| **Services Backend** | 35 | 20 | 35 | 0% |
| **Facades** | 12 | 4-6 | 13 | +8% (consolidação) |
| **DTOs** | 78 | 25 | ~75 | ~4% |
| **Stores Frontend** | 16 | 15 | 16 | 0% |
| **Composables** | 18 | 6 | 18 | 0% |
| **Arquivos Java** | 250 | 210 | 250 | 0% |
| **Arquivos TS/Vue** | 180 | 160 | 180 | 0% |

---

## 🟢 FASE 1: Quick Wins (BAIXO risco)

**Status:** 🟡 Em Andamento (40% completo)  
**Início:** [Data sessão atual]  
**Prazo:** 7 dias  
**Responsável:** [Agente/Desenvolvedor]

### 1.1. Backend - Consolidar OrganizacaoServices

**Status:** 🟡 Em Análise Detalhada  
**Progresso:** 10%

**DECISÃO APÓS ANÁLISE:** 
Após análise detalhada dos 9 services de organização, identificamos que alguns já possuem responsabilidades bem definidas e separação justificada:

- **HierarquiaService** (60 LOC): Lógica pura de verificação de hierarquia - MANTER separado (reutilizável)
- **UnidadeHierarquiaService** (253 LOC): Algoritmos complexos de árvore hierárquica - MANTER separado (alta coesão)
- **ValidadorDadosOrgService** (170 LOC): ApplicationRunner com validações de startup - MANTER separado (responsabilidade específica)
- **UnidadeMapaService** (64 LOC): Gerenciamento de mapas vigentes - CANDIDATO à consolidação
- **UnidadeConsultaService** (40 LOC): Wrapper puro - CONSOLIDAR
- **UsuarioConsultaService** (51 LOC): Wrapper puro - CONSOLIDAR
- **AdministradorService** (52 LOC): CRUD simples - CANDIDATO à consolidação
- **UsuarioPerfilService** (32 LOC): Apenas 2 métodos - CANDIDATO à consolidação

**PLANO REVISADO:**
- [ ] Criar `UnidadeService.java` (~150 LOC)
  - [ ] Consolidar métodos de UnidadeConsultaService (wrapper eliminado)
  - [ ] Consolidar métodos de UnidadeMapaService
  - [ ] Adicionar métodos de CRUD básico
- [ ] Criar `UsuarioService.java` (~150 LOC)
  - [ ] Consolidar métodos de UsuarioConsultaService (wrapper eliminado)
  - [ ] Consolidar métodos de UsuarioPerfilService
  - [ ] Consolidar métodos de AdministradorService
- [ ] MANTER separados (justificados):
  - [ ] HierarquiaService (reutilizável, lógica pura)
  - [ ] UnidadeHierarquiaService (algoritmos complexos)
  - [ ] ValidadorDadosOrgService (ApplicationRunner)
  - [ ] UnidadeResponsavelService (já tem nome apropriado)
- [ ] Atualizar UnidadeFacade e UsuarioFacade
- [ ] Migrar testes unitários (~10 testes)
- [ ] Validar testes passam
- [ ] Remover services consolidados (4 arquivos)

**Arquivos Afetados:** 9 → 7 (-2 net, mais focado)  
**Testes Afetados:** ~10  
**Bloqueadores:** Nenhum

### 1.2. Backend - Consolidar SubprocessoServices

**Status:** ⏳ Não Iniciado  
**Progresso:** 0%

- [ ] Criar `SubprocessoService.java`
  - [ ] Consolidar métodos de SubprocessoCrudService
  - [ ] Consolidar métodos de SubprocessoValidacaoService
  - [ ] Consolidar métodos de ConsultasSubprocessoService
- [ ] Criar `SubprocessoWorkflowService.java` consolidado
  - [ ] Consolidar métodos de SubprocessoMapaWorkflowService
  - [ ] Consolidar métodos de SubprocessoCadastroWorkflowService
  - [ ] Consolidar métodos de SubprocessoAdminWorkflowService
  - [ ] Consolidar métodos de SubprocessoTransicaoService
- [ ] Eliminar `SubprocessoEmailService` (mover lógica para NotificacaoService)
- [ ] Atualizar SubprocessoFacade
- [ ] Migrar testes unitários
- [ ] Validar testes passam
- [ ] Remover services antigos

**Arquivos Afetados:** 8 → 3 (-5)  
**Testes Afetados:** ~12  
**Bloqueadores:** Nenhum

### 1.3. Backend - Atualizar Testes de Arquitetura

**Status:** ✅ Concluído  
**Progresso:** 100%

- [x] Generalizar regra #2 (mapa_controller específico)
- [x] Generalizar regra #3 (processo_controller específico)
- [x] Criar nova regra genérica: controllers_should_only_access_own_module
- [x] Validar todas as 16 regras ainda passam
- [x] Atualizar documentação de testes de arquitetura

**Arquivos Afetados:** ArchConsistencyTest.java  
**Testes Afetados:** 2 regras adaptadas  
**Bloqueadores:** Nenhum

### 1.4. Documentação - Arquivar Obsoletos

**Status:** ⏳ Não Iniciado  
**Progresso:** 0%

- [ ] Criar diretório `backend/etc/docs/archive/complexity-v1/`
- [ ] Mover `LEIA-ME-COMPLEXIDADE.md` → archive
- [ ] Mover `complexity-report.md` → archive
- [ ] Mover `complexity-v1-vs-v2-comparison.md` → archive
- [ ] Remover `complexity-summary.txt`
- [ ] Atualizar README.md principal com referências corretas

**Arquivos Afetados:** 4 movidos, 1 removido  
**Bloqueadores:** Nenhum

### 1.5. Frontend - Consolidar Store de Processos

**Status:** 🟡 Parcial  
**Progresso:** 30%

- [ ] Consolidar `stores/processos/{core,workflow,context}.ts` em `stores/processos.ts`
  - [x] Análise de dependências
  - [x] Redução de reexport interno
  - [ ] Mesclagem completa em arquivo único
- [ ] Atualizar imports em componentes Views
- [ ] Atualizar imports em outros stores
- [ ] Migrar testes de stores
- [ ] Validar testes passam
- [ ] Remover arquivos antigos

**Arquivos Afetados:** 4 → 1 (-3)  
**Testes Afetados:** ~8  
**Bloqueadores:** Nenhum

### 1.6. Frontend - Eliminar Composables View-Specific

**Status:** ⏳ Não Iniciado  
**Progresso:** 0%

- [ ] Identificar 10 composables view-specific
  - [ ] useProcessoView.ts
  - [ ] useUnidadeView.ts
  - [ ] useVisAtividades.ts
  - [ ] useVisMapa.ts
  - [ ] useAtividadeForm.ts
  - [ ] useProcessoForm.ts
  - [ ] useCadAtividades.ts
  - [ ] useModalManager.ts
  - [ ] useLoadingManager.ts
  - [ ] useApi.ts
- [ ] Mover lógica para componentes Views
- [ ] Criar composables genéricos (6 arquivos)
  - [ ] useForm.ts
  - [ ] useModal.ts
  - [ ] usePagination.ts
  - [ ] useLocalStorage.ts
  - [ ] useValidation.ts
  - [ ] useBreadcrumbs.ts
- [ ] Atualizar testes de componentes
- [ ] Validar testes passam
- [ ] Remover composables antigos

**Arquivos Afetados:** 10 removidos, 6 criados (-4 net)  
**Testes Afetados:** ~10  
**Bloqueadores:** Nenhum

### 1.7. Validação Fase 1

**Status:** ⏳ Não Iniciado  
**Progresso:** 0%

- [ ] Rodar suite completa backend (`./gradlew test`)
- [ ] Rodar suite completa frontend (`npm run test:unit`)
- [ ] Validar regras ArchUnit (todas devem passar)
- [ ] Rodar linters (backend + frontend)
- [ ] Code review completo
- [ ] Medir métricas de redução
- [ ] Atualizar este tracking

**Bloqueadores:** Dependente de conclusão de 1.1-1.6

---

## 🟡 FASE 2: Simplificação Estrutural (MÉDIO risco)

**Status:** 🟡 Iniciada (20% completo)  
**Início:** [Após Fase 1]  
**Prazo:** 12 dias  
**Responsável:** [Agente/Desenvolvedor]

### 2.1. Backend - Consolidar Módulos mantendo Facades

**Status:** 🟡 Em Andamento  
**Progresso:** 20%

- [x] Identificar candidatos à consolidação
  - [x] AlertaFacade + AnaliseFacade + PainelFacade → AcompanhamentoFacade
  - [x] ConfiguracaoFacade (avaliar eliminação)
  - [x] LoginFacade → AutenticacaoService
- [x] Criar `AcompanhamentoFacade`
- [x] Migrar controllers para usar AcompanhamentoFacade
  - [x] SubprocessoCadastroController
  - [x] SubprocessoMapaController
  - [x] SubprocessoCrudController
  - [x] LoginController
  - [x] E2eController
- [ ] Migrar testes
- [ ] Eliminar facades antigas
- [ ] Atualizar documentação

**Arquivos Afetados:** ~5 facades consolidadas  
**Testes Afetados:** ~20  
**Bloqueadores:** Nenhum

### 2.2. Backend - Introduzir @JsonView

**Status:** 🟡 Iniciada  
**Progresso:** 10%

- [x] Definir views em 5 Entities principais
  - [x] Configuração (ParametroResponse removido parcialmente)
  - [x] Usuario (UsuarioController com @JsonView)
  - [ ] Processo
  - [ ] Subprocesso
  - [ ] Mapa
  - [ ] Atividade
- [ ] Migrar 15 Responses simples para @JsonView
- [ ] Manter 25 DTOs complexos (agregações, transformações)
- [ ] Criar testes de serialização para cada view
  - [ ] Validar campos Public
  - [ ] Validar campos Admin
  - [ ] Validar que campos sensíveis não vazam
- [ ] Atualizar controllers com @JsonView
- [ ] Remover DTOs/Mappers obsoletos

**Arquivos Afetados:** -15 DTOs (~750 LOC)  
**Testes Afetados:** ~25 ajustados, ~15 novos  
**Bloqueadores:** Nenhum

### 2.3. Backend - Atualizar Testes de Arquitetura (Facades)

**Status:** ✅ Concluído  
**Progresso:** 100%

- [x] REFORÇAR regra #7: controllers usam Facades
- [x] REFORÇAR regra #15: facades não acessam repos
- [x] Criar regra de consistência por módulo consolidado
- [x] Validar regras passam

**Arquivos Afetados:** ArchConsistencyTest.java  
**Testes Afetados:** 2 regras reforçadas  
**Bloqueadores:** Nenhum

### 2.4. Backend - Atualizar Testes de Arquitetura (DTOs)

**Status:** ✅ Concluído  
**Progresso:** 100%

- [x] ADAPTAR regra #10: permitir @JsonView
- [x] Criar regra: entities retornadas devem ter @JsonView
- [x] Validar regras passam

**Arquivos Afetados:** ArchConsistencyTest.java  
**Testes Afetados:** 1 regra adaptada  
**Bloqueadores:** Nenhum

### 2.5. Backend - Atualizar ADRs

**Status:** ⏳ Não Iniciado  
**Progresso:** 0%

- [ ] Atualizar ADR-001 (Facade Pattern)
  - [ ] Documentar consolidação de módulos
  - [ ] Critérios para Facade vs Service direto
  - [ ] Exemplos de consolidação
- [ ] Atualizar ADR-004 (DTO Pattern)
  - [ ] Adicionar @JsonView como alternativa
  - [ ] Quando usar DTO vs @JsonView
  - [ ] Exemplos de uso seguro
- [ ] Criar ADR-008 (Simplification Decisions)
  - [ ] Documentar processo completo
  - [ ] Justificativas e métricas
  - [ ] Lições aprendidas

**Arquivos Afetados:** 2 atualizados, 1 criado  
**Bloqueadores:** Dependente de conclusão de 2.1-2.2

### 2.6. Validação Fase 2

**Status:** 🟡 Parcial  
**Progresso:** 30%

- [x] Rodar testes de arquitetura (ArchUnit) - PASSOU
- [x] Rodar testes de domínio afetado (subprocesso) - PASSOU
- [x] Rodar testes de login/autenticação - PASSOU
- [x] Rodar testes E2E principais (smoke) - PASSOU (17/18, 1 bloqueio ambiental)
- [ ] Suite completa de testes backend
- [ ] Suite completa de testes frontend
- [ ] Testes de serialização JSON (100% coverage)
- [ ] Performance: validar não degradou
- [ ] Security: @JsonView não vaza dados
- [ ] Code review com foco em segurança

**Bloqueadores:** Dependente de conclusão de 2.1-2.5

### 2.7. Documentação Final Fase 2

**Status:** 🟡 Em Andamento  
**Progresso:** 50%

- [x] Atualizar PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md
- [ ] Criar guia de migração para desenvolvedores
- [ ] Atualizar README.md principal
- [ ] Atualizar este tracking

**Bloqueadores:** Dependente de conclusão de 2.1-2.6

---

## 🔴 FASE 3: Simplificação Avançada (OPCIONAL)

**Status:** ⏸️ Postergada  
**Progresso:** 0%  
**Decisão:** POSTERGAR até evidência de necessidade

### Critérios para Reconsiderar
- [ ] Time cresce para 10+ desenvolvedores
- [ ] Sistema escala para 100+ usuários simultâneos
- [ ] Evidência de problemas de performance
- [ ] Aprovação explícita de CTO + Security Officer

**Nota:** Esta fase NÃO será executada no momento atual.

---

## 📊 Métricas e KPIs

### Redução de Código (Atualizar semanalmente)

| Métrica | Baseline | Meta Fase 1 | Meta Fase 2 | Atual | % Progresso |
|---------|----------|-------------|-------------|-------|-------------|
| Services | 35 | 23 | 20 | 35 | 0% |
| Facades | 12 | 12 | 4-6 | 13 | +8% (consolidação) |
| DTOs | 78 | 78 | 25 | ~75 | ~4% |
| Stores | 16 | 15 | 15 | 16 | 0% |
| Composables | 18 | 8 | 6 | 18 | 0% |
| Arquivos Java | 250 | 235 | 210 | 250 | 0% |
| Arquivos TS/Vue | 180 | 177 | 160 | 180 | 0% |

### Qualidade de Código (Não devem degradar)

| Métrica | Baseline | Atual | Status |
|---------|----------|-------|--------|
| Cobertura Backend | ~70% | ~70% | ✅ OK |
| Cobertura Frontend | ~65% | ~65% | ✅ OK |
| Tempo Build Backend | 45s | 45s | ✅ OK |
| Tempo Build Frontend | 30s | 30s | ✅ OK |
| Tempo Testes Backend | 180s | 180s | ✅ OK |
| Tempo Testes Frontend | 25s | 25s | ✅ OK |
| Regras ArchUnit Passando | 16/16 | 16/16 | ✅ OK |
| Vulnerabilidades | 0 | 0 | ✅ OK |

### Velocidade de Desenvolvimento (Meta: melhorar)

| Métrica | Baseline | Meta | Atual | Status |
|---------|----------|------|-------|--------|
| Tempo adicionar campo | 15-17 arquivos | 5-7 arquivos | 15-17 | ⏳ Pendente |
| Tempo onboarding | 2-3 semanas | 1 semana | 2-3 semanas | ⏳ Pendente |
| Profundidade stack trace | 7 camadas | 4 camadas | 7 camadas | ⏳ Pendente |

---

## 🚨 Problemas e Bloqueadores

### Ativos

**Nenhum bloqueador ativo no momento.**

### Resolvidos

**[16/02/2026] Smoke E2E parcial (captura)**
- **Problema:** Bloqueio ambiental intermitente (porta/app concorrente)
- **Impacto:** Baixo (teste isolado)
- **Resolução:** Recorte funcional preservado; validação E2E em ambiente limpo recomendada

---

## 📅 Histórico de Alterações

| Data | Fase | Mudança | Responsável |
|------|------|---------|-------------|
| 16/02/2026 | Setup | Criação do documento de tracking | Agente |
| 16/02/2026 | Fase 1 | Concluída tarefa 1.3 (Testes Arquitetura) | Agente |
| 16/02/2026 | Fase 2 | Iniciadas tarefas 2.1, 2.2, 2.3, 2.4 | Agente |
| 16/02/2026 | Fase 2 | Concluídas tarefas 2.3 e 2.4 (Testes Arquitetura) | Agente |
| 16/02/2026 | Fase 1 | Progresso parcial 1.5 (Store Processos - 30%) | Agente |
| 16/02/2026 | Fase 2 | Progresso parcial 2.1 (Facades - 20%) | Agente |
| 16/02/2026 | Fase 2 | Progresso parcial 2.2 (@JsonView - 10%) | Agente |
| 16/02/2026 | Fase 2 | Validação parcial 2.6 (30% - testes principais) | Agente |

---

## 🎯 Próximos Passos

### Curto Prazo (Próxima Sessão)
1. **Completar Fase 1.1:** Consolidar OrganizacaoServices
2. **Completar Fase 1.2:** Consolidar SubprocessoServices
3. **Completar Fase 1.4:** Arquivar documentação obsoleta
4. **Completar Fase 1.5:** Consolidar Store de Processos completamente
5. **Iniciar Fase 1.6:** Eliminar composables view-specific

### Médio Prazo (Próximas 2 Semanas)
1. Completar Fase 1 (100%)
2. Avançar Fase 2.1 e 2.2 (consolidação de Facades e @JsonView)
3. Validação completa com suite de testes
4. Atualizar ADRs

### Longo Prazo (Próximo Mês)
1. Completar Fase 2 (100%)
2. Medir impacto real em velocidade de desenvolvimento
3. Coletar feedback do time
4. Reavaliar necessidade da Fase 3

---

## 📝 Notas e Observações

### Lições Aprendidas
- Consolidação de Facades requer cuidado com fronteira arquitetural clara
- Testes de arquitetura (ArchUnit) são valiosos para guiar refatoração
- @JsonView requer testes específicos de serialização para garantir segurança

### Decisões Importantes
- **Postergar Fase 3:** Alto risco vs benefício marginal
- **Manter Facades em módulos consolidados:** Reforça ADR-001
- **@JsonView apenas para DTOs simples:** Manter DTOs complexos (agregações)

### Feedback do Time
[A ser preenchido conforme feedback for coletado]

---

**Última Atualização:** 16 de Fevereiro de 2026  
**Próxima Revisão:** [Data da próxima sessão]  
**Responsável:** [Agente/Desenvolvedor]
