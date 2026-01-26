# 📊 Tracking de Refatorações - SGC

**Última Atualização:** 26 de Janeiro de 2026  
**Status Geral:** 🟡 Em Planejamento

---

## 📈 Progresso Geral

| Fase | Status | Progresso | Ações Concluídas | Total |
|------|--------|-----------|------------------|-------|
| **Sprint 1** - Quick Wins | 🔵 Planejada | 0% | 0/5 | [Ver detalhes](#sprint-1) |
| **Sprint 2** - Frontend | 🔵 Planejada | 0% | 0/3 | [Ver detalhes](#sprint-2) |
| **Sprint 3** - Backend | 🔵 Planejada | 0% | 0/3 | [Ver detalhes](#sprint-3) |
| **Sprint 4** - Opcionais | 🔵 Planejada | 0% | 0/3 | [Ver detalhes](#sprint-4) |

**Total Geral:** 0/14 ações (0%)

---

## 🎯 Sprints e Ações

### Sprint 1 - Quick Wins (1-2 dias)
**Objetivo:** Remover complexidade desnecessária, ganhos rápidos  
**Status:** 🔵 Planejada

| # | Ação | Prioridade | Status | Esforço | Impacto |
|---|------|------------|--------|---------|---------|
| 1 | Alterar `FetchType.EAGER` → `LAZY` em UsuarioPerfil | 🔴 Alta | ⚪ Pendente | 🟢 Baixo | 🔴 Alto |
| 3 | Remover override de `findAll()` em AtividadeRepo | 🔴 Alta | ⚪ Pendente | 🟢 Baixo | 🟠 Médio |
| 7 | Remover cache de unidades (CacheConfig) | 🟡 Média | ⚪ Pendente | 🟢 Baixo | 🟡 Baixo |
| 11 | Converter subquery → JOIN em AtividadeRepo | 🟢 Baixa | ⚪ Pendente | 🟢 Baixo | 🟢 Baixo |
| 12 | Extrair `flattenTree` para utilitário compartilhado | 🟢 Baixa | ⚪ Pendente | 🟢 Baixo | 🟢 Baixo |

**Documentação:** [backend-sprint-1.md](./backend-sprint-1.md)

---

### Sprint 2 - Consolidação Frontend (3-5 dias)
**Objetivo:** Frontend mais consistente, menos requisições HTTP  
**Status:** 🔵 Planejada

| # | Ação | Prioridade | Status | Esforço | Impacto |
|---|------|------------|--------|---------|---------|
| 2 | Criar composable `useErrorHandler` para stores | 🔴 Alta | ⚪ Pendente | 🟡 Médio | 🔴 Alto |
| 4 | Consolidar queries duplicadas (AtividadeRepo, CompetenciaRepo) | 🔴 Alta | ⚪ Pendente | 🟡 Médio | 🟠 Médio |
| 5 | Backend retornar dados completos (eliminar cascata de reloads) | 🔴 Alta | ⚪ Pendente | 🔴 Alto | 🔴 Alto |

**Documentação:** [frontend-sprint-2.md](./frontend-sprint-2.md)

---

### Sprint 3 - Refatoração Backend (5-10 dias)
**Objetivo:** Arquitetura mais clara, SRP respeitado  
**Status:** 🔵 Planejada

| # | Ação | Prioridade | Status | Esforço | Impacto |
|---|------|------------|--------|---------|---------|
| 6 | Decompor `UnidadeFacade` em 3 services | 🟡 Média | ⚪ Pendente | 🔴 Alto | 🟠 Médio |
| 8 | Dividir `SubprocessoWorkflowService` (775 linhas) | 🟡 Média | ⚪ Pendente | 🔴 Alto | 🟠 Médio |
| 10 | Consolidar AtividadeService + CompetenciaService em MapaManutencaoService | 🟡 Média | ⚪ Pendente | 🟡 Médio | 🟠 Médio |

**Documentação:** [backend-sprint-3.md](./backend-sprint-3.md)

---

### Sprint 4 - Otimizações Opcionais (conforme necessário)
**Objetivo:** Refinamentos, não críticos  
**Status:** 🔵 Planejada

| # | Ação | Prioridade | Status | Esforço | Impacto |
|---|------|------------|--------|---------|---------|
| 9 | Implementar cache HTTP parcial (frontend) | 🟡 Média | ⚪ Pendente | 🟡 Médio | 🟡 Baixo |
| 13 | Adicionar @EntityGraph onde apropriado | 🟢 Baixa | ⚪ Pendente | 🟡 Médio | 🟢 Baixo |
| 14 | Decompor `processos.ts` store (345 linhas) | 🟢 Baixa | ⚪ Pendente | 🔴 Alto | 🟢 Baixo |

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

| Ícone | Status | Descrição |
|-------|--------|-----------|
| ⚪ | Pendente | Ainda não iniciado |
| 🔵 | Planejada | Sprint planejada, aguardando início |
| 🟡 | Em Progresso | Sprint/ação em andamento |
| 🟢 | Concluída | Sprint/ação finalizada e validada |
| 🔴 | Bloqueada | Sprint/ação com impedimento |
| ⚠️ | Atenção | Sprint/ação necessita revisão |

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
