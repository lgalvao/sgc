# 📊 Tracking de Refatorações - SGC

**Última Atualização:** 27 de Janeiro de 2026  
**Status Geral:** 🟡 Em Andamento

---

## 📈 Progresso Geral

| Fase                      | Status       | Progresso | Ações Concluídas | Total                     |
|---------------------------|--------------|-----------|------------------|---------------------------|
| **Sprint 1** - Quick Wins | 🟢 Concluída | 100%      | 5/5              | [Ver detalhes](#sprint-1) |
| **Sprint 2** - Frontend   | 🟢 Concluída | 100%      | 3/3              | [Ver detalhes](#sprint-2) |
| **Sprint 3** - Backend    | 🟢 Concluída | 100%      | 3/3              | [Ver detalhes](#sprint-3) |
| **Sprint 4** - Opcionais  | 🟡 Em Andamento| 33%       | 1/3              | [Ver detalhes](#sprint-4) |

**Total Geral:** 12/14 ações (85%)

---

## 🎯 Sprints e Ações

### Sprint 1 - Quick Wins (1-2 dias)

**Objetivo:** Remover complexidade desnecessária, ganhos rápidos  
**Status:** 🟢 Concluída

| #  | Ação                                                | Prioridade | Status       | Esforço  | Impacto  |
|----|-----------------------------------------------------|------------|--------------|----------|----------|
| 1  | Alterar `FetchType.EAGER` → `LAZY` em UsuarioPerfil | 🔴 Alta    | 🟢 Concluída | 🟢 Baixo | 🔴 Alto  |
| 3  | Remover override de `findAll()` em AtividadeRepo    | 🔴 Alta    | 🟢 Concluída | 🟢 Baixo | 🟠 Médio |
| 7  | Remover cache de unidades (CacheConfig)             | 🟡 Média   | 🟢 Concluída | 🟢 Baixo | 🟡 Baixo |
| 11 | Converter subquery → JOIN em AtividadeRepo          | 🟢 Baixa   | 🟢 Concluída | 🟢 Baixo | 🟢 Baixo |
| 12 | Extrair `flattenTree` para utilitário compartilhado | 🟢 Baixa   | 🟢 Concluída | 🟢 Baixo | 🟢 Baixo |

**Documentação:** [backend-sprint-1.md](./backend-sprint-1.md)

**Resultados Alcançados:**

- ✅ 130 testes backend passando
- ✅ ~40 linhas de código removidas
- ✅ FetchType.EAGER eliminado (2 → 0)
- ✅ Cache desnecessário removido
- ✅ Queries otimizadas (JOIN em vez de subquery)
- ✅ flattenTree centralizado em utilitário compartilhado

---

### Sprint 2 - Consolidação Frontend (3-5 dias)

**Objetivo:** Frontend mais consistente, menos requisições HTTP  
**Status:** 🟢 Concluída (100%)

| # | Ação                                                           | Prioridade | Status       | Esforço  | Impacto  |
|---|----------------------------------------------------------------|------------|--------------|----------|----------|
| 2 | Criar composable `useErrorHandler` para stores                 | 🔴 Alta    | 🟢 Concluída | 🟡 Médio | 🔴 Alto  |
| 4 | Consolidar queries duplicadas (AtividadeRepo, CompetenciaRepo) | 🔴 Alta    | 🟢 Concluída | 🟡 Médio | 🟠 Médio |
| 5 | Backend retornar dados completos (eliminar cascata de reloads) | 🔴 Alta    | 🟢 Concluída | 🔴 Alto  | 🔴 Alto  |

**Documentação:** [frontend-sprint-2.md](./frontend-sprint-2.md)

**Resultados Alcançados:**

- ✅ useErrorHandler criado e testado (8/8 testes passando)
- ✅ 11 stores refatorados (100%)
- ✅ ~200 linhas de código duplicado eliminadas (frontend)
- ✅ 50+ métodos assíncronos refatorados
- ✅ 1157/1157 testes frontend passando (100%)
- ✅ AtividadeRepo e CompetenciaRepo usando @EntityGraph
- ✅ ~20 linhas de código reduzidas (backend)
- ✅ 27/27 testes backend relacionados passando
- ✅ Código mais idiomático usando padrões do Spring Data
- ✅ 0 vulnerabilidades de segurança (verificado com CodeQL)
- ✅ Ação #5 concluída - backend retorna `atividadesAtualizadas` e frontend usa dados diretamente

---

### Sprint 3 - Refatoração Backend (5-10 dias)

**Objetivo:** Arquitetura mais clara, SRP respeitado  
**Status:** 🔵 Planejada

| #  | Ação                                                                      | Prioridade | Status       | Esforço  | Impacto  |
|----|---------------------------------------------------------------------------|------------|--------------|----------|----------|
| 6  | Decompor `UnidadeFacade` em 3 services                                    | 🟡 Média   | 🟢 Concluída | 🔴 Alto  | 🟠 Médio |
| 8  | Dividir `SubprocessoWorkflowService` (775 linhas)                         | 🟡 Média   | 🟢 Concluída | 🔴 Alto  | 🟠 Médio |
| 10 | Consolidar AtividadeService + CompetenciaService em MapaManutencaoService | 🟡 Média   | 🟢 Concluída | 🟡 Médio | 🟠 Médio |

**Documentação:** [backend-sprint-3.md](./backend-sprint-3.md)

**Resultados Alcançados:**

- ✅ `UnidadeFacade` decomposto em services especializados (Hierarquia, Mapa, Responsavel)
- ✅ `SubprocessoWorkflowService` (God Object) dividido em `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `SubprocessoAdminWorkflowService`
- ✅ `SubprocessoWorkflowFacade` criado para manter contrato
- ✅ `AtividadeService`, `CompetenciaService`, `ConhecimentoService` consolidados em `MapaManutencaoService` (eliminando dependências circulares)
- ✅ 1369/1369 testes backend passando (100%)
- ✅ Código morto/obsoleto removido

---

### Sprint 4 - Otimizações Opcionais (conforme necessário)

**Objetivo:** Refinamentos, não críticos  
**Status:** 🔵 Planejada

| #  | Ação                                       | Prioridade | Status     | Esforço  | Impacto  |
|----|--------------------------------------------|------------|------------|----------|----------|
| 9  | Implementar cache HTTP parcial (frontend)  | 🟡 Média   | ⚪ Pendente | 🟡 Médio | 🟡 Baixo |
| 13 | Adicionar @EntityGraph onde apropriado     | 🟢 Baixa   | ⚪ Pendente | 🟡 Médio | 🟢 Baixo |
| 14 | Decompor `processos.ts` store (345 linhas) | 🟢 Baixa   | 🟢 Concluída | 🔴 Alto  | 🟢 Baixo |

**Documentação:** [otimizacoes-sprint-4.md](./otimizacoes-sprint-4.md)

---

## 📊 Métricas e KPIs

### Baseline (Antes das Refatorações)

**Backend:**

- Classes > 500 linhas: 2 arquivos
- Código duplicado: ~800-1000 linhas
- Queries N+1: ~5 problemas identificados

**Frontend:**

- Stores > 300 linhas: 1 arquivo (processos.ts - 345 linhas)
- Código duplicado: ~104 blocos de error handling
- Requisições em cascata: ~3 requisições por ação

### Metas (Após Refatorações)

**Backend:**

- ✅ Classes > 500 linhas: 0 arquivos
- ✅ Redução de código: 800-1000 linhas
- ✅ Queries otimizadas: 100% dos problemas N+1 resolvidos

**Frontend:**

- ✅ Redução de requisições HTTP: 25-40%
- ✅ Código duplicado: ~500 linhas economizadas
- ✅ Cascata de reloads: Eliminada

**Performance:**

- ✅ Tempo de resposta: Melhoria de 20-35%
- ✅ Uso de memória: Redução de 10-15%

---

## 🔄 Status Legenda

| Ícone | Status       | Descrição                           |
|-------|--------------|-------------------------------------|
| ⚪     | Pendente     | Ainda não iniciado                  |
| 🔵    | Planejada    | Sprint planejada, aguardando início |
| 🟡    | Em Progresso | Sprint/ação em andamento            |
| 🟢    | Concluída    | Sprint/ação finalizada e validada   |
| 🔴    | Bloqueada    | Sprint/ação com impedimento         |
| ⚠️    | Atenção      | Sprint/ação necessita revisão       |

---

## 📝 Notas e Decisões

### Decisões Arquiteturais

1. **Cache de Unidades** - Decisão: REMOVER
    - Justificativa: Complexidade > Benefício para 20 usuários simultâneos
    - Data: Planejado na Sprint 1

2. **Cascata de Reloads** - Decisão: ELIMINAR
    - Justificativa: Backend deve retornar dados completos
    - Data: Planejado na Sprint 2

3. **God Objects** - Decisão: DECOMPOR
    - Justificativa: Respeitar SRP, melhorar testabilidade
    - Data: Planejado na Sprint 3

### Riscos Identificados

1. **Ação #5 (Cascata de Reloads)** - 🔴 Alto risco
    - Impacto: 6 controllers, 6 stores
    - Mitigação: Testes E2E extensivos antes e depois

2. **Ação #6 e #8 (Decomposição)** - 🟡 Médio risco
    - Impacto: Mudança arquitetural significativa
    - Mitigação: Refatoração incremental, testes unitários

---

## ✅ Checklist de Validação (Por Sprint)

Após cada sprint, validar:

- [ ] ✅ Testes unitários passam (100%)
- [ ] ✅ Testes E2E passam (100%)
- [ ] ✅ Nenhuma regressão de funcionalidade
- [ ] ✅ Código mais simples que antes
- [ ] ✅ Performance igual ou melhor (medida com Playwright)
- [ ] ✅ Documentação atualizada (ADRs, READMEs)
- [ ] ✅ Code review aprovado

---

## 📚 Referências

- [optimization-report.md](./optimization-report.md) - Relatório de análise completo
- [backend-sprint-1.md](./backend-sprint-1.md) - Detalhes da Sprint 1
- [frontend-sprint-2.md](./frontend-sprint-2.md) - Detalhes da Sprint 2
- [backend-sprint-3.md](./backend-sprint-3.md) - Detalhes da Sprint 3
- [otimizacoes-sprint-4.md](./otimizacoes-sprint-4.md) - Detalhes da Sprint 4

---

**Mantido por:** Equipe de Desenvolvimento SGC  
**Versão:** 1.0
