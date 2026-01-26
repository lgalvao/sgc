# 📝 Resumo Executivo - Plano de Refatorações SGC

**Data:** 26 de Janeiro de 2026  
**Versão:** 1.0  
**Status:** ✅ Documentação Completa

---

## 🎯 Visão Geral

Este documento resume o plano completo de refatorações do Sistema de Gestão de Competências (SGC), baseado na análise detalhada do `optimization-report.md`.

**⭐ COMECE AQUI:** [REFACTORING-INDEX.md](./REFACTORING-INDEX.md)

---

## 📊 Situação Atual

### Problemas Identificados

1. **Otimizações Prematuras** 
   - Sistema com 20 usuários simultâneos tem cache complexo desnecessário
   - Múltiplas variações de queries sem justificativa

2. **Inconsistência Arquitetural**
   - Algumas áreas bem estruturadas, outras com God Objects
   - Violação do Single Responsibility Principle (SRP)

3. **Complexidade Desnecessária**
   - FetchType.EAGER onde não é necessário
   - Cascata de 3 requisições HTTP por ação no frontend

4. **Código Duplicado**
   - ~500 linhas de error handling duplicado em 13 stores
   - Função `flattenTree` duplicada
   - Queries similares em múltiplos repositórios

### Métricas de Baseline

| Métrica | Valor Atual | Meta |
|---------|-------------|------|
| Arquivos > 500 linhas | 2 | 0 |
| FetchType.EAGER | 2 | 0 |
| Código duplicado | ~800-1000 linhas | 0 |
| Requisições em cascata | 3 por ação | 1 |
| Queries N+1 | ~5 problemas | 0 |

---

## 🗺️ Plano de Refatoração

### 4 Sprints, 14 Ações, ~18-22 dias

```
Sprint 1 (1-2 dias)     → Sprint 2 (3-5 dias)     → Sprint 3 (5-10 dias)     → Sprint 4 (opcional)
Quick Wins               Consolidação Frontend      Refatoração Backend         Otimizações
5 ações                  3 ações                    3 ações                     3 ações
🔴 Alta Prioridade      🔴 Alta Prioridade        🟡 Média Prioridade         🟢 Baixa (se necessário)
```

### Sprint 1 - Quick Wins (1-2 dias) 🔴

**Objetivo:** Remover complexidade desnecessária

| # | Ação | Impacto |
|---|------|---------|
| 1 | `FetchType.EAGER` → `LAZY` em UsuarioPerfil | 🔴 Alto |
| 3 | Remover override `findAll()` em AtividadeRepo | 🟠 Médio |
| 7 | Remover cache de unidades | 🟡 Baixo |
| 11 | Subquery → JOIN em AtividadeRepo | 🟢 Baixo |
| 12 | Extrair `flattenTree` para utilitário | 🟢 Baixo |

**Resultado:** Código limpo, ~35-40 linhas removidas, performance +10-20%

📄 **Documento:** [backend-sprint-1.md](./backend-sprint-1.md)

---

### Sprint 2 - Consolidação Frontend (3-5 dias) 🔴

**Objetivo:** Frontend consistente, menos requisições HTTP

| # | Ação | Impacto |
|---|------|---------|
| 2 | Criar composable `useErrorHandler` | 🔴 Alto |
| 4 | Consolidar queries duplicadas | 🟠 Médio |
| 5 | Backend retornar dados completos | 🔴 Alto |

**Resultado:** ~550 linhas eliminadas, -25-40% requisições, -40-60% latência

📄 **Documento:** [frontend-sprint-2.md](./frontend-sprint-2.md)

---

### Sprint 3 - Refatoração Backend (5-10 dias) 🟡

**Objetivo:** Arquitetura clara, SRP respeitado

| # | Ação | Impacto |
|---|------|---------|
| 6 | Decompor `UnidadeFacade` (384 linhas) | 🟠 Médio |
| 8 | Dividir `SubprocessoWorkflowService` (775 linhas) | 🟠 Médio |
| 10 | Consolidar Atividade + Competencia Services | 🟠 Médio |

**Resultado:** 0 arquivos > 500 linhas, SRP respeitado, melhor testabilidade

📄 **Documento:** [backend-sprint-3.md](./backend-sprint-3.md)

---

### Sprint 4 - Otimizações Opcionais (conforme necessário) 🟢

**Objetivo:** Refinamentos APENAS se necessário

| # | Ação | Quando Implementar |
|---|------|--------------------|
| 9 | Cache HTTP parcial | SE latência > 500ms |
| 13 | @EntityGraph | SE surgir N+1 medido |
| 14 | Decompor stores grandes | SE manutenção dificultar |

**Resultado:** Implementar apenas com necessidade demonstrada (YAGNI)

📄 **Documento:** [otimizacoes-sprint-4.md](./otimizacoes-sprint-4.md)

---

## 📈 Ganhos Esperados

### Código

- ✅ **Redução:** 800-1000 linhas eliminadas
- ✅ **Qualidade:** 0 arquivos > 500 linhas
- ✅ **Consistência:** SRP respeitado
- ✅ **Manutenibilidade:** Significativamente melhorada

### Performance

- ✅ **Requisições HTTP:** -25-40%
- ✅ **Tempo de resposta:** +20-35%
- ✅ **Latência em ações:** -40-60%
- ✅ **Uso de memória:** -10-15%

### Arquitetura

- ✅ **God Objects:** Eliminados
- ✅ **Dependências circulares:** Eliminadas
- ✅ **Código duplicado:** Eliminado
- ✅ **Testabilidade:** Melhorada

---

## 🚀 Como Começar

### Para Agentes IA

1. **Ler documentação na ordem:**
   ```
   REFACTORING-INDEX.md
     ↓
   optimization-report.md (contexto completo)
     ↓
   backend-sprint-1.md (começar aqui)
     ↓
   Seguir passos detalhados
   ```

2. **Executar uma ação:**
   - Abrir documento da sprint
   - Localizar ação específica
   - Seguir "Passos para Execução por IA"
   - Executar comandos bash fornecidos
   - Validar com checklist

3. **Atualizar progresso:**
   - Atualizar `refactoring-tracker.md`
   - Fazer commit
   - Prosseguir para próxima ação

### Para Humanos

1. **Revisar documentação:**
   - [REFACTORING-INDEX.md](./REFACTORING-INDEX.md) - Índice completo
   - [optimization-report.md](./optimization-report.md) - Análise detalhada
   - [refactoring-tracker.md](./refactoring-tracker.md) - Tracking de progresso

2. **Priorizar sprints:**
   - Sprint 1 e 2: **OBRIGATÓRIAS** (alta prioridade)
   - Sprint 3: **RECOMENDADA** (média prioridade)
   - Sprint 4: **OPCIONAL** (apenas se necessário)

3. **Acompanhar progresso:**
   - Usar `refactoring-tracker.md`
   - Validar métricas após cada sprint
   - Ajustar plano se necessário

---

## ✅ Checklist Rápido

### Sprint 1 (Quick Wins)
- [ ] Alterar EAGER → LAZY
- [ ] Remover override findAll()
- [ ] Remover cache
- [ ] Converter subquery → JOIN
- [ ] Extrair flattenTree

### Sprint 2 (Frontend)
- [ ] Criar useErrorHandler
- [ ] Consolidar queries
- [ ] Eliminar cascata de reloads

### Sprint 3 (Backend)
- [ ] Decompor UnidadeFacade
- [ ] Dividir SubprocessoWorkflowService
- [ ] Consolidar Services de Mapa

### Sprint 4 (Opcional)
- [ ] Cache HTTP? (apenas se necessário)
- [ ] @EntityGraph? (apenas se N+1)
- [ ] Decompor stores? (apenas se dificultar)

---

## 🎯 Princípios

- **YAGNI** - You Aren't Gonna Need It
- **KISS** - Keep It Simple, Stupid
- **DRY** - Don't Repeat Yourself
- **SRP** - Single Responsibility Principle
- **Measure, Don't Assume** - Sempre medir

---

## 📚 Documentação Completa

| Documento | Descrição | Tamanho |
|-----------|-----------|---------|
| [REFACTORING-INDEX.md](./REFACTORING-INDEX.md) | Índice mestre | 8KB |
| [refactoring-tracker.md](./refactoring-tracker.md) | Tracking de progresso | 6KB |
| [backend-sprint-1.md](./backend-sprint-1.md) | Sprint 1 detalhada | 20KB |
| [frontend-sprint-2.md](./frontend-sprint-2.md) | Sprint 2 detalhada | 23KB |
| [backend-sprint-3.md](./backend-sprint-3.md) | Sprint 3 detalhada | 24KB |
| [otimizacoes-sprint-4.md](./otimizacoes-sprint-4.md) | Sprint 4 detalhada | 19KB |
| [optimization-report.md](./optimization-report.md) | Análise completa | 41KB |

**Total:** ~141KB de documentação estruturada

---

## 📞 Suporte

- **Arquitetura:** `backend/etc/docs/ARCHITECTURE.md`
- **ADRs:** `backend/etc/docs/adr/`
- **Padrões:** `AGENTS.md`, `GEMINI.md`
- **E2E:** `/regras/e2e_regras.md`

---

**Preparado por:** Agente de IA - Documentação de Refatorações  
**Data:** 26 de Janeiro de 2026  
**Versão:** 1.0  
**Status:** ✅ Pronto para Execução
