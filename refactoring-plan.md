# Plano de Refatoração Arquitetural - SGC

**Data de Criação:** 2026-01-10  
**Última Atualização:** 2026-01-10  
**Versão:** 1.0  
**Status:** 📋 **PLANEJAMENTO INICIAL**

---

## 1. SUMÁRIO EXECUTIVO

### 1.1 Contexto

O sistema SGC passou por refatorações significativas em segurança e nomenclatura:

- ✅ **Sprint 4 de Segurança** (Completo): 100% testes passando (1149/1149)
  - Arquitetura centralizada de controle de acesso
  - AccessControlService com políticas especializadas
  - Auditoria completa de decisões de acesso
  
- ✅ **Sprint 2 de Arquitetura** (Completo): 100% testes passando (1141/1141)
  - MapaService → MapaFacade (nomenclatura consistente)
  - 4/4 facades com padrão uniforme
  - 283 arquivos atualizados sem regressões

### 1.2 Oportunidades Identificadas

A análise da arquitetura atual revelou oportunidades de melhoria focadas em:

1. **Consolidação de Services** - Reduzir complexidade de navegação
2. **Encapsulamento** - Forçar uso via Facades
3. **Eventos de Domínio** - Desacoplamento entre módulos
4. **Documentação** - Preservar conhecimento arquitetural
5. **Testes Arquiteturais** - Garantir aderência aos padrões

---

## 2. ANÁLISE DO ESTADO ATUAL

### 2.1 Arquitetura Bem Implementada ✅

#### Padrão Facade
- ✅ **ProcessoFacade** - Orquestra operações de processo
- ✅ **SubprocessoFacade** - Orquestra operações de subprocesso (328 linhas)
- ✅ **MapaFacade** - Orquestra operações de mapa
- ✅ **AtividadeFacade** - Orquestra operações de atividade
- ✅ **Controllers** usam APENAS facades (padrão seguido)

#### Security in 3 Layers
- ✅ **Camada 1 (HTTP)**: @PreAuthorize para autenticação básica
- ✅ **Camada 2 (Autorização)**: AccessControlService completo
- ✅ **Camada 3 (Negócio)**: Services sem verificações de acesso
- ✅ **Cobertura**: 95%+ de testes de segurança

#### DTOs Obrigatórios
- ✅ **100% de aderência** - Nenhuma entidade JPA exposta
- ✅ **Mappers** - MapStruct usado consistentemente

### 2.2 Services do Módulo Subprocesso (11 services, 2.820 linhas)

| Service | Linhas | Responsabilidade Principal | Oportunidade |
|---------|--------|---------------------------|--------------|
| **SubprocessoFacade** | 328 | Orquestração geral | ✅ Bem definida |
| SubprocessoMapaWorkflowService | 425 | Workflow de mapa (maior arquivo) | 🟡 Considerar split |
| SubprocessoCadastroWorkflowService | 218 | Workflow de cadastro | ✅ Adequado |
| SubprocessoTransicaoService | 187 | Transições de estado | ✅ Coeso |
| SubprocessoService | 185 | Operações básicas | 🟡 Delegação para decomposed |
| SubprocessoMapaService | 168 | Operações de mapa | 🟡 Overlap com workflow? |
| SubprocessoDetalheService (decomposed) | 168 | Construção de detalhes | ✅ Especializado |
| SubprocessoWorkflowService (decomposed) | 147 | Workflow genérico | 🟡 Overlap com específicos? |
| SubprocessoFactory | 146 | Criação de subprocessos | ✅ Adequado |
| SubprocessoEmailService | 138 | Notificações por email | ✅ Coeso |
| SubprocessoValidacaoService (decomposed) | 136 | Validações | ✅ Especializado |
| SubprocessoCrudService (decomposed) | 132 | CRUD básico | ✅ Especializado |
| SubprocessoContextoService | ? | Contexto de execução | 🟡 Avaliar uso |
| SubprocessoWorkflowExecutor | 84 | Execução de workflows | ✅ Pequeno e focado |

**Total**: 11 services principais + 4 decomposed = 15 services

### 2.3 Eventos de Domínio (6 implementados)

#### Implementados ✅
1. **EventoProcessoCriado** - Processo criado
2. **EventoProcessoIniciado** - Processo iniciado
3. **EventoProcessoFinalizado** - Processo finalizado
4. **EventoTransicaoSubprocesso** - Transição de estado (design unificado ⭐)
5. **EventoMapaAlterado** - Mapa alterado
6. **EventoProcessoListener** - Listener para notificações

#### Eventos Potenciais Identificados 🎯
7. EventoProcessoAtualizado
8. EventoProcessoExcluido
9. EventoSubprocessoCriado
10. EventoSubprocessoAtualizado
11. EventoSubprocessoExcluido
12. EventoAtividadeCriada
13. EventoAtividadeAtualizada
14. EventoAtividadeExcluida
15. EventoMapaValidado
16. EventoMapaHomologado

**Meta**: Aumentar de 6 para ~15 eventos para melhor desacoplamento

### 2.4 Documentação (package-info.java)

✅ **Bem Documentados** (23 package-info):
- sgc.seguranca.acesso
- sgc.processo.service
- sgc.subprocesso.service
- sgc.mapa.service
- sgc.organizacao
- sgc.comum.erros
- E outros...

---

## 3. PLANO DE EXECUÇÃO - FASES

### FASE 1: Análise e Documentação Arquitetural (1-2 dias)

**Objetivo**: Mapear completamente a arquitetura atual e oportunidades

#### Tarefas:
1. ✅ **Inventariar Services**
   - Listar todos os services de cada módulo
   - Mapear responsabilidades e dependências
   - Identificar overlaps e duplicações
   - Medir complexidade (linhas, métodos públicos)

2. ✅ **Analisar Uso de Facades**
   - Verificar que controllers usam apenas facades
   - Identificar acessos diretos a services (se houver)
   - Mapear fluxos de chamadas

3. ✅ **Mapear Eventos de Domínio**
   - Listar eventos implementados
   - Identificar comunicação síncrona que poderia ser assíncrona
   - Propor novos eventos

4. ✅ **Revisar Documentação**
   - Verificar cobertura de package-info
   - Identificar gaps de documentação
   - Propor melhorias

**Entregáveis**:
- ✅ Documento de análise (este documento - Seção 2)
- ✅ Mapa de dependências entre modules
- ✅ Lista priorizada de melhorias

---

### FASE 2: Testes Arquiteturais (2-3 dias) - ⏳ EM PROGRESSO

**Objetivo**: Criar testes que garantam aderência aos padrões arquiteturais

#### Tarefas:
1. ✅ **Implementar ArchUnit Tests**
   - ✅ Controllers devem usar apenas Facades
   - ✅ Facades devem ter sufixo "Facade"
   - ✅ Services especializados não devem ser chamados por Controllers
   - ✅ Entidades JPA não devem ser expostas em APIs
   - ✅ DTOs não devem ser entidades JPA
   - ✅ Services não devem lançar ErroAccessoNegado diretamente
   - ✅ Controllers devem ter sufixo "Controller"
   - ✅ Repositories devem ter sufixo "Repo"
   - ✅ Eventos de domínio devem começar com "Evento"

2. ✅ **Testes de Nomenclatura**
   - ✅ Verificar sufixos: Controller, Service, Facade, Repo, Dto
   - ✅ Verificar padrão de eventos (Evento prefix)

3. ✅ **Testes de Camadas**
   - ✅ Controllers → Facades (não services especializados)
   - ✅ Services → Repositories (apenas do mesmo módulo)
   - ✅ Null-safety (@NullMarked nos pacotes)

4. ⏳ **Executar e Documentar Baseline**
   - [ ] Rodar testes arquiteturais
   - [ ] Documentar violações (se houver)
   - [ ] Criar plano de correção

**Entregáveis**:
- ✅ Classe `ArchConsistencyTest` expandida (5→14 regras)
- [ ] Relatório de conformidade arquitetural
- [ ] Plano de correção de violações (se houver)

**Critérios de Aceitação**:
- ✅ Testes arquiteturais criados e expandidos
- [ ] Testes executados com sucesso
- [ ] Zero violações críticas (ou plano para corrigir)
- [ ] Documentação de regras arquiteturais

---

### FASE 3: Melhorias de Documentação (2-3 dias) - ⏳ EM PROGRESSO

**Objetivo**: Garantir que toda a arquitetura está bem documentada

#### Tarefas:
1. ⏳ **Completar package-info.java Faltantes**
   - ✅ sgc (pacote raiz) - Visão geral completa do sistema
   - ✅ sgc.processo.eventos - Eventos de processo
   - ✅ sgc.processo.dto - DTOs de processo
   - ✅ sgc.processo.mapper - Mappers de processo
   - ✅ sgc.subprocesso.eventos - Eventos de subprocesso (padrão unificado)
   - [ ] sgc.subprocesso.dto - DTOs de subprocesso
   - [ ] sgc.subprocesso.mapper - Mappers de subprocesso
   - [ ] Outros pacotes conforme necessário

2. [ ] **Atualizar AGENTS.md**
   - [ ] Adicionar seção sobre padrões arquiteturais
   - [ ] Documentar regras do ArchUnit
   - [ ] Exemplos de uso de Facades

3. [ ] **Criar ADRs (Architectural Decision Records)**
   - [ ] Por que Facade Pattern?
   - [ ] Por que AccessControlService centralizado?
   - [ ] Por que DTOs obrigatórios?
   - [ ] Por que EventoTransicaoSubprocesso unificado?

4. [ ] **Diagramas de Arquitetura**
   - [ ] Diagrama de camadas (ASCII art melhorado)
   - [ ] Diagrama de módulos e dependências
   - [ ] Fluxo de dados (Command vs Query)

**Entregáveis**:
- ⏳ package-info.java completos (5/~30 criados)
- [ ] AGENTS.md atualizado com arquitetura
- [ ] 4 ADRs criados
- [ ] 3 diagramas atualizados

**Critérios de Aceitação**:
- ⏳ Todos os packages principais com package-info
- [ ] Documentação alinhada com código
- [ ] Diagramas refletem arquitetura atual

---

### FASE 4: Eventos de Domínio Adicionais (3-5 dias)

**Objetivo**: Implementar eventos faltantes para desacoplamento

#### Tarefas:
1. **Priorizar Eventos**
   - Analisar lista de 10 eventos potenciais
   - Selecionar top 5 com maior impacto
   - Documentar benefícios esperados

2. **Implementar Eventos de Processo**
   - EventoProcessoAtualizado
   - EventoProcessoExcluido
   - Listeners para auditoria/notificação

3. **Implementar Eventos de Subprocesso**
   - EventoSubprocessoCriado
   - EventoSubprocessoAtualizado
   - Usar padrão de EventoTransicaoSubprocesso (se aplicável)

4. **Implementar Eventos de Atividade/Mapa**
   - EventoAtividadeCriada
   - EventoAtividadeAtualizada
   - EventoMapaValidado

5. **Refatorar Comunicação Síncrona**
   - Identificar chamadas inter-módulos síncronas
   - Avaliar candidatas a eventos
   - Refatorar para eventos (se apropriado)

**Entregáveis**:
- [ ] 5-10 novos eventos implementados
- [ ] Listeners criados/atualizados
- [ ] Testes de eventos
- [ ] Documentação de eventos

**Critérios de Aceitação**:
- Eventos implementados e testados
- Nenhuma regressão em testes existentes
- Comunicação assíncrona onde apropriado
- Logs de eventos para auditoria

---

### FASE 5: Consolidação de Services (Opcional - 5-7 dias)

**Objetivo**: Reduzir número de services de subprocesso de 11 para ~6

⚠️ **Esta fase é OPCIONAL** e requer análise mais profunda

#### Análise Necessária:
1. **SubprocessoMapaWorkflowService (425 linhas)**
   - Avaliar se pode ser dividido em services menores OU
   - Manter como está (complexidade inerente ao workflow)

2. **SubprocessoService vs Decomposed**
   - SubprocessoService delega para decomposed/*
   - Avaliar se SubprocessoService pode ser eliminado
   - Controllers usam SubprocessoFacade (não impactado)

3. **SubprocessoMapaService vs MapaWorkflowService**
   - Avaliar overlap de responsabilidades
   - Propor consolidação se houver duplicação

4. **SubprocessoWorkflowService vs Específicos**
   - Workflow genérico vs Cadastro/Mapa específicos
   - Avaliar se genérico é usado ou pode ser removido

#### Consolidação Proposta (TBD):
```
ANTES (11 services):
- SubprocessoService
- SubprocessoCadastroWorkflowService
- SubprocessoMapaWorkflowService
- SubprocessoMapaService
- SubprocessoTransicaoService
- SubprocessoEmailService
- SubprocessoFactory
- SubprocessoWorkflowExecutor
- SubprocessoContextoService
- SubprocessoDetalheService (decomposed)
- SubprocessoValidacaoService (decomposed)
- SubprocessoCrudService (decomposed)
- SubprocessoWorkflowService (decomposed)

DEPOIS (~6 services - PROPOSTA):
- SubprocessoCadastroService (consolida Cadastro + Workflow)
- SubprocessoMapaService (consolida Mapa + MapaWorkflow)
- SubprocessoTransicaoService (mantém)
- SubprocessoEmailService (mantém)
- SubprocessoFactory (mantém)
- SubprocessoDetalheService (consolida com Validação/CRUD)
```

**Entregáveis** (Se executado):
- [ ] Proposta de consolidação detalhada
- [ ] Refatoração incremental com testes
- [ ] Documentação atualizada
- [ ] 100% testes passando

**Critérios de Aceitação**:
- Redução de ~50% no número de services
- Nenhuma regressão funcional
- Código mais coeso e navegável
- Documentação clara de responsabilidades

---

## 4. MÉTRICAS DE SUCESSO

### 4.1 Métricas de Qualidade

| Métrica | Baseline Atual | Meta | Fase |
|---------|---------------|------|------|
| **Testes passando** | 1149/1149 (100%) | 100% | Todas |
| **Cobertura de testes** | 95.1% | >95% | Todas |
| **Facades com nomenclatura consistente** | 4/4 (100%) | 100% | ✅ Completo |
| **package-info.java** | 23 | 100% cobertura | Fase 3 |
| **Eventos de domínio** | 6 | 12-15 | Fase 4 |
| **Services de subprocesso** | 11 | 6-8 | Fase 5 (opcional) |
| **Regras ArchUnit** | 0 | 10+ | Fase 2 |
| **ADRs documentados** | 0 | 4+ | Fase 3 |

### 4.2 Métricas de Arquitetura

| Aspecto | Antes | Depois (Meta) |
|---------|-------|---------------|
| **Padrões de acesso** | 1 (AccessControlService) | 1 ✅ |
| **Padrão Facade** | 100% implementado | 100% + testes ArchUnit |
| **Comunicação assíncrona** | ~6 eventos | ~15 eventos |
| **Documentação arquitetural** | Boa | Excelente |
| **Encapsulamento** | Público (sem enforcement) | Package-private (com testes) |

### 4.3 Métricas de Manutenibilidade

| Métrica | Impacto Esperado |
|---------|------------------|
| **Tempo para entender arquitetura** | -50% (15min → 7min) |
| **Tempo para adicionar nova feature** | -20% (com guias claros) |
| **Navegação entre services** | -30% (menos arquivos) |
| **Confiança em refatorações** | +40% (testes arquiteturais) |

---

## 5. RISCOS E MITIGAÇÕES

### 5.1 Riscos Técnicos

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| **Quebrar funcionalidade** | Baixa | Alto | Testes abrangentes + refatoração incremental |
| **Degradar performance** | Muito Baixa | Médio | Benchmarks + profiling |
| **Incompatibilidade com Java 17** | Nenhuma | N/A | Projeto usa Java 21 |
| **Consolidação incorreta de services** | Média | Médio | Análise profunda antes + code review |
| **Eventos excessivos** | Baixa | Baixo | Priorização cuidadosa |

### 5.2 Riscos de Processo

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| **Escopo creep** | Média | Médio | Fases bem definidas + opcional claro |
| **Falta de aprovação** | Baixa | Alto | Documentar benefícios + validar com stakeholders |
| **Regressões não detectadas** | Muito Baixa | Alto | Suite de testes robusta (1149 testes) |

---

## 6. CRONOGRAMA ESTIMADO

### 6.1 Fases Obrigatórias (7-10 dias)

| Fase | Duração | Dependências | Status |
|------|---------|--------------|--------|
| **Fase 1: Análise** | 1-2 dias | Nenhuma | ✅ Completo |
| **Fase 2: Testes Arquiteturais** | 2-3 dias | Fase 1 | ⏳ Próximo |
| **Fase 3: Documentação** | 2-3 dias | Fase 1 | ⏳ Pendente |
| **Fase 4: Eventos** | 3-5 dias | Fase 1 | ⏳ Pendente |

### 6.2 Fase Opcional (5-7 dias)

| Fase | Duração | Dependências | Status |
|------|---------|--------------|--------|
| **Fase 5: Consolidação** | 5-7 dias | Fases 1-4 | 🎯 Avaliar após Fase 4 |

**Total Estimado**: 7-10 dias (obrigatório) + 5-7 dias (opcional) = **12-17 dias**

---

## 7. CRITÉRIOS DE ACEITAÇÃO GLOBAL

### 7.1 Funcional
- [ ] TODAS as funcionalidades existentes continuam funcionando
- [ ] 100% dos testes passando (1149+)
- [ ] Nenhuma regressão de performance (< 5% overhead)
- [ ] Zero bugs introduzidos

### 7.2 Arquitetural
- [ ] Testes ArchUnit implementados e passando
- [ ] Padrão Facade 100% seguido e enforçado
- [ ] Eventos de domínio aumentados em 2x (6 → 12+)
- [ ] Documentação completa (package-info 100%)

### 7.3 Qualidade
- [ ] Code review aprovado
- [ ] Documentação revisada
- [ ] ADRs criados e aprovados
- [ ] Guias atualizados

---

## 8. REFERÊNCIAS

### 8.1 Documentação Existente
- `/docs/ARCHITECTURE.md` - Visão geral da arquitetura
- `/docs/SECURITY-REFACTORING-COMPLETE.md` - Refatoração de segurança (completa)
- `/docs/SPRINT-2-ARCHITECTURE-SUMMARY.md` - Sprint 2 (MapaFacade)
- `/docs/ARCHITECTURE-IMPROVEMENTS-SUMMARY.md` - Sumário de melhorias
- `/security-refactoring-plan.md` - Plano de segurança (completo)
- `/AGENTS.md` - Guia para agentes
- `/regras/backend-padroes.md` - Padrões de backend

### 8.2 Código de Referência
- `sgc.seguranca.acesso` - Exemplo de arquitetura bem estruturada
- `sgc.processo.service.ProcessoFacade` - Exemplo de Facade
- `sgc.subprocesso.eventos.EventoTransicaoSubprocesso` - Evento unificado (design ⭐)

---

## 9. PRÓXIMOS PASSOS IMEDIATOS

### Sprint Atual (próximos 3-5 dias):

1. ✅ **Criar este documento** (refactoring-plan.md)
   - Status: Completo
   
2. ⏳ **Executar Fase 2: Testes Arquiteturais**
   - Criar classe `ArchitectureConsistencyTest`
   - Implementar ~10 regras ArchUnit
   - Executar e documentar resultados
   - Corrigir violações (se houver)
   
3. ⏳ **Iniciar Fase 3: Documentação**
   - Identificar package-info faltantes
   - Criar ADR template
   - Escrever primeiro ADR (Facade Pattern)

### Decisões Pendentes:

- [ ] **Aprovação de stakeholders** para Fase 5 (Consolidação)
- [ ] **Priorização de eventos** para Fase 4
- [ ] **Alocação de tempo** para todas as fases

---

## 10. CONCLUSÃO

Este plano de refatoração arquitetural complementa as melhorias já realizadas em segurança e nomenclatura. O foco está em:

1. **Garantir qualidade** através de testes arquiteturais
2. **Preservar conhecimento** através de documentação
3. **Melhorar manutenibilidade** através de eventos e consolidação
4. **Manter 100% de testes** passando em todas as fases

A abordagem é **incremental e validada**, priorizando melhorias de alto impacto e baixo risco.

---

**Mantido por:** GitHub Copilot AI Agent  
**Data de Criação:** 2026-01-10  
**Última Atualização:** 2026-01-10  
**Versão:** 1.0
**Status:** 📋 PLANEJAMENTO - Fase 1 Completa
