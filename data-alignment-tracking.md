# Data Alignment Tracking

**Iniciado:** 2026-02-05  
**Última Atualização:** 2026-02-05  
**Plano Completo:** Ver `data-alignment-plan.md`  
**Relatório:** Ver `data-alignment-report.md`

---

## Status Geral

| Sprint | Descrição | Status | Progresso |
|--------|-----------|--------|-----------|
| 1 | Frontend DTO Fix | ⏳ Pendente | 0/3 |
| 2 | SQL ANALISE.motivo | ⏳ Pendente | 0/2 |
| 3 | SQL Nullability | ⏳ Pendente | 0/5 |
| 4 | View Vinculação | ⏳ Pendente | 0/4 |
| 5 | Snapshot Decision | ⏳ Aguardando Decisão | 0/? |

**Legenda:** ⏳ Pendente | 🔄 Em Progresso | ✅ Concluído | ❌ Bloqueado | ⏸️ Pausado

---

## Sprint 1: Frontend DTO Fix

| Task | Descrição | Status | Data | Notas |
|------|-----------|--------|------|-------|
| 1.1 | Atualizar `dtos.ts` | ⏳ | - | Renomear codigo→codUnidade |
| 1.2 | Atualizar `processos.ts` | ⏳ | - | Ajustar mapper |
| 1.3 | Executar testes | ⏳ | - | npm run test && typecheck |

---

## Sprint 2: SQL ANALISE.motivo

| Task | Descrição | Status | Data | Notas |
|------|-----------|--------|------|-------|
| 2.1 | Alterar DDL motivo | ⏳ | - | VARCHAR2(200)→VARCHAR2(500) |
| 2.2 | Verificar consistência | ⏳ | - | JPA e SQL = 500 |

---

## Sprint 3: SQL Nullability

| Task | Descrição | Status | Data | Notas |
|------|-----------|--------|------|-------|
| 3.1 | PROCESSO.data_limite | ⏳ | - | NULL→NOT NULL |
| 3.2 | SUBPROCESSO (2 campos) | ⏳ | - | unidade_codigo, situacao |
| 3.3 | MOVIMENTACAO (4 campos) | ⏳ | - | data_hora, unidades, usuario |
| 3.4 | ATRIBUICAO_TEMPORARIA (5 campos) | ⏳ | - | todos os campos |
| 3.5 | Verificar consistência | ⏳ | - | grep NOT NULL |

---

## Sprint 4: View Vinculação

| Task | Descrição | Status | Data | Notas |
|------|-----------|--------|------|-------|
| 4.1 | Avaliar uso atual | ⏳ | - | grep VinculacaoUnidade |
| 4.2 | Modificar view NVL | ⏳ | - | NULL→0 para raiz |
| 4.3 | Atualizar JPA | ⏳ | - | isUnidadeRaiz() |
| 4.4 | Executar testes | ⏳ | - | gradlew test |

---

## Sprint 5: Snapshot Decision

| Task | Descrição | Status | Data | Notas |
|------|-----------|--------|------|-------|
| 5.1 | Obter decisão | ⏳ | - | A/B/C? |
| 5.2-4 | Implementar decisão | ⏳ | - | Depende de 5.1 |

**Decisão pendente:**
- [ ] A) Implementar entidade UnidadeProcesso
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
