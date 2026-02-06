# Data Alignment Tracking

**Iniciado:** 2026-02-05  
**Última Atualização:** 2026-02-06  
**Plano Completo:** Ver `data-alignment-plan.md`  
**Relatório:** Ver `data-alignment-report.md`

---

## Status Geral

| Sprint | Descrição | Status | Progresso |
|--------|-----------|--------|-----------|
| 1 | Frontend DTO Fix | ✅ Concluído | 3/3 |
| 2 | JPA ANALISE.motivo | ✅ Concluído | 2/2 |
| 3 | SQL Nullability | ✅ Concluído | 5/5 |
| 4 | View Vinculação | ✅ Concluído | 4/4 |
| 5 | Snapshot Decision | ✅ Concluído | 4/4 |

**Legenda:** ⏳ Pendente | 🔄 Em Progresso | ✅ Concluído | ❌ Bloqueado | ⏸️ Pausado

---

## Sprint 1: Frontend DTO Fix

| Task | Descrição | Status | Data | Notas |
|------|-----------|--------|------|-------|
| 1.1 | Atualizar `dtos.ts` | ✅ | 2026-02-06 | Renomear codigo→codUnidade + add codUnidadeSuperior |
| 1.2 | Atualizar `processos.ts` | ✅ | 2026-02-06 | Ajustar mapper |
| 1.3 | Executar testes | ✅ | 2026-02-06 | 1370/1373 testes passaram (2 falhas pré-existentes) |

---

## Sprint 2: SQL ANALISE.motivo

| Task | Descrição | Status | Data | Notas |
|------|-----------|--------|------|-------|
| 2.1 | Alterar JPA motivo | ✅ | 2026-02-06 | length 500→200 (alinhado c/ SQL) |
| 2.2 | Atualizar validação Request | ✅ | 2026-02-06 | @Size max 500→200 |

---

## Sprint 3: SQL Nullability

| Task | Descrição | Status | Data | Notas |
|------|-----------|--------|------|-------|
| 3.1 | PROCESSO.data_limite | ✅ | 2026-02-06 | NULL→NOT NULL |
| 3.2 | SUBPROCESSO (2 campos) | ✅ | 2026-02-06 | unidade_codigo, situacao |
| 3.3 | MOVIMENTACAO (4 campos) | ✅ | 2026-02-06 | data_hora, unidades, usuario |
| 3.4 | ATRIBUICAO_TEMPORARIA (5 campos) | ✅ | 2026-02-06 | todos os campos |
| 3.5 | Atualizar schema.sql | ✅ | 2026-02-06 | H2 alinhado com Oracle |

---

## Sprint 4: View Vinculação

| Task | Descrição | Status | Data | Notas |
|------|-----------|--------|------|-------|
| 4.1 | Avaliar uso atual | ✅ | 2026-02-06 | Usado apenas em VinculacaoUnidadeRepo |
| 4.2 | Refatorar JPA | ✅ | 2026-02-06 | ID simplificado para Long, nullable em unidadeAnterior |
| 4.3 | Adicionar isUnidadeRaiz() | ✅ | 2026-02-06 | Helper method para verificar raiz |
| 4.4 | Atualizar schemas de teste | ✅ | 2026-02-06 | schema.sql (main e test) |

---

## Sprint 5: Snapshot Decision

| Task | Descrição | Status | Data | Notas |
|------|-----------|--------|------|-------|
| 5.1 | Obter decisão | ✅ | 2026-02-06 | Implementada Opção A (Entidade UnidadeProcesso) |
| 5.2-4 | Implementar snapshots | ✅ | 2026-02-06 | Snapshots capturados via UnidadeProcesso.java |

**Decisão pendente:**
- [x] A) Implementar entidade UnidadeProcesso (Escolha realizada e implementada)
- [ ] B) Remover colunas do SQL
- [ ] C) Manter para implementação futura

---

## Resumo de Achados

### Críticos (3)
| ID | Achado | Status |
|----|--------|--------|
| C1 | ANALISE.motivo 200→500 | ⏳ Sprint 2 |
| C2 | VinculacaoUnidade @Id NULL | ⏳ Sprint 4 |
| C3 | UnidadeParticipanteDto naming | ⏳ Sprint 1 |

### Médios (5)
| ID | Achado | Status |
|----|--------|--------|
| M1 | PROCESSO.dataLimite nullability | ⏳ Sprint 3 |
| M2 | SUBPROCESSO.unidadeCodigo nullability | ⏳ Sprint 3 |
| M3 | MOVIMENTACAO nullabilities (4) | ⏳ Sprint 3 |
| M4 | ATRIBUICAO_TEMPORARIA nullabilities (5) | ⏳ Sprint 3 |
| M5 | UNIDADE_PROCESSO snapshots | ⏳ Sprint 5 |

### Baixos/Informativos (4)
| ID | Achado | Status |
|----|--------|--------|
| L1 | DATE vs TIMESTAMP (Processo) | ℹ️ Sem ação |
| L2 | DATE vs TIMESTAMP (Subprocesso) | ℹ️ Sem ação |
| L3 | DATE vs TIMESTAMP (AtribuicaoTemp) | ℹ️ Sem ação |
| L4 | ANALISE.acao 20 vs 100 | ℹ️ Opcional |

### Corrigidos/Removidos
| ID | Achado | Status |
|----|--------|--------|
| X1 | Analise.subprocesso nullability | ✅ Já corrigido |
| X2 | Movimentacao.observacoes | ✅ Erro no relatório |

---

## Histórico de Atualizações

| Data | Ação | Por |
|------|------|-----|
| 2026-02-05 | Criação do tracking | AI |
| - | - | - |

---

## Notas

- Sprint 5 requer decisão humana antes de prosseguir
- Oracle DATE inclui hora - achados L1-L3 são apenas informativos
- Após cada sprint, atualizar este arquivo
