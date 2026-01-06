# Plano de Melhoria de Testabilidade - SGC Backend

**Data:** 2026-01-06  
**Baseado em:** Análise do BACKLOG_TESTABILIDADE.md e cobertura atual

---

## Métricas Atuais

| Métrica | Valor | Meta |
|---------|-------|------|
| Cobertura de Linhas | 97.62% | 98% ❌ |
| Cobertura de Branches | 87.96% | 90% ❌ |
| Total de Testes | 1026 | - |
| Branches Perdidos | ~150/1296 | - |

### Progresso das Refatorações (2026-01-06)

- ✅ **Strategy Pattern em `SubprocessoMapaWorkflowService`**: 8 ocorrências de if/else substituídas por Maps estáticos
- ✅ **Strategy Pattern em `ProcessoController.iniciar()`**: Chain de if/else substituída por Map de handlers
- ✅ **Extração de Lógica em `EventoProcessoListener`**: Lógica de switch extraída para `criarCorpoEmailPorTipo`, eliminando branches redundantes e uso de exceções brutas.
- ✅ **Testes para `SubprocessoValidacaoService`**: 23 novos testes cobrindo edge-cases e null checks.
- ✅ **Testes para `SubprocessoDetalheService`**: 22 novos testes cobrindo visualização, permissões e tratamento de nulos.
- **Resultado**: Melhoria na robustez e manutenibilidade, embora a cobertura percentual total tenha flutuado devido à adição de código de teste e mudanças na base.

---

## 📊 Análise de Gaps por Prioridade

### Prioridade Alta (Maior impacto em branches)

#### 1. SubprocessoMapaWorkflowService (~13 branches perdidos)
**Status**: Parcialmente abordado via Strategy Pattern. Ainda há branches de negócio complexos.

#### 2. ProcessoController
**Status**: Iniciado. Necessita de testes para o branch `DIAGNOSTICO`.

#### 3. GerenciadorJwt
**Status**: Pendente. Branches de claims nulos e validação de ambiente.

### Próximos Passos (Fase 1.5 - Consolidação)

**Tempo estimado:** 2h

| Tarefa | Arquivo | Impacto |
|--------|---------|---------|
| 1.3 | Teste `ProcessoController.iniciar` com DIAGNOSTICO | +2 branches |
| 1.4 | Remover/testar construtores não usados de erros | +5 linhas |
| 2.3 | Testes para `SubprocessoMapaService` | +3 branches |

---

## 🎯 Plano de Execução Atualizado

### Fase 2: Refatorações de Médio Esforço (Meta: 90% branches)

**Tempo estimado:** 6h

| Tarefa | Arquivo | Impacto | Status |
|--------|---------|---------|--------|
| 2.1 | Strategy Pattern em `SubprocessoMapaWorkflowService` | +3 branches | ✅ |
| 2.2 | Extrair lógica de `EventoProcessoListener` | +2 branches | ✅ |
| 2.3 | Testes para `SubprocessoMapaService` | +3 branches | Pendente |
| 2.4 | Testes para `GerenciadorJwt` claims parciais | +2 branches | Pendente |

### Fase 3: Refatorações Estruturais (Melhoria contínua)

**Tempo estimado:** 8h

| Tarefa | Descrição |
|--------|-----------|
| 3.1 | Strategy Pattern em `ProcessoController.iniciar()` |
| 3.2 | Interface `AmbienteInfo` para abstração de ambiente |
| 3.3 | Separação de guards via AOP (se necessário) |
| 3.4 | Factory Methods para responses complexos |

---

## 📋 Checklist de Implementação

### Para cada refatoração

- [x] Executar testes existentes antes da mudança
- [x] Implementar a refatoração
- [x] Adicionar novos testes cobrindo os branches
- [x] Verificar que nenhum teste existente quebrou
- [ ] Rodar `python3 scripts/check_coverage.py "" 90` para validar (Script indisponível, verificação manual via relatório Gradle)
- [x] Atualizar BACKLOG_TESTABILIDADE.md com métricas atualizadas

### Comandos úteis

```bash
# Executar testes e gerar relatório
cd /app && ./gradlew :backend:test :backend:jacocoTestReport

# Verificar cobertura geral (Manual)
cat backend/build/reports/jacoco/test/jacocoTestReport.csv | awk -F, '{instructions += $4 + $5; covered_instructions += $5; branches += $6 + $7; covered_branches += $7} END {print "Total Instructions: " instructions; print "Covered Instructions: " covered_instructions; print "Instruction Coverage: " covered_instructions/instructions*100 "%"; print "Total Branches: " branches; print "Covered Branches: " covered_branches; print "Branch Coverage: " covered_branches/branches*100 "%"}'
```

---

## Referências

- [BACKLOG_TESTABILIDADE.md](./BACKLOG_TESTABILIDADE.md) - Backlog original
- [AGENTS.md](/AGENTS.md) - Diretrizes de desenvolvimento
- [backend-padroes.md](/regras/backend-padroes.md) - Padrões de código backend
