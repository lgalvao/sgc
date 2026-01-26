# 📖 Guia de Refatorações - SGC

> Documentação completa do plano de refatorações do Sistema de Gestão de Competências

---

## 🎯 Início Rápido

**Primeira vez aqui?** Comece por:

1. **📝 [REFACTORING-SUMMARY.md](./REFACTORING-SUMMARY.md)** - Resumo executivo (1 página)
2. **📖 [REFACTORING-INDEX.md](./REFACTORING-INDEX.md)** - Índice completo com guias
3. **📊 [refactoring-tracker.md](./refactoring-tracker.md)** - Acompanhe o progresso

---

## 📚 Documentação Disponível

### 🎯 Documentos Principais

| Documento | Descrição | Para Quem | Tamanho |
|-----------|-----------|-----------|---------|
| **[REFACTORING-SUMMARY.md](./REFACTORING-SUMMARY.md)** | Resumo executivo de uma página | Todos | 7KB |
| **[REFACTORING-INDEX.md](./REFACTORING-INDEX.md)** | Índice mestre com guias | Agentes IA / Devs | 8KB |
| **[refactoring-tracker.md](./refactoring-tracker.md)** | Tracking de progresso | PMs / Tech Leads | 6KB |
| **[optimization-report.md](./optimization-report.md)** | Análise detalhada original | Arquitetos / Tech Leads | 41KB |

### 🚀 Documentos de Sprints

| Sprint | Arquivo | Duração | Ações | Prioridade |
|--------|---------|---------|-------|------------|
| **1** | [backend-sprint-1.md](./backend-sprint-1.md) | 1-2 dias | 5 | 🔴 Alta |
| **2** | [frontend-sprint-2.md](./frontend-sprint-2.md) | 3-5 dias | 3 | 🔴 Alta |
| **3** | [backend-sprint-3.md](./backend-sprint-3.md) | 5-10 dias | 3 | 🟡 Média |
| **4** | [otimizacoes-sprint-4.md](./otimizacoes-sprint-4.md) | Conforme necessário | 3 | 🟢 Baixa |

---

## 🗺️ Visão Geral das Sprints

### Sprint 1 - Quick Wins (Backend) 🔴
**1-2 dias | 5 ações | Alta Prioridade**

Remover complexidade desnecessária:
- ✅ EAGER → LAZY em UsuarioPerfil
- ✅ Remover override findAll()
- ✅ Remover cache de unidades
- ✅ Subquery → JOIN
- ✅ Extrair flattenTree

**Ganho:** ~35-40 linhas removidas, +10-20% performance

---

### Sprint 2 - Consolidação Frontend 🔴
**3-5 dias | 3 ações | Alta Prioridade**

Frontend consistente e eficiente:
- ✅ Criar composable useErrorHandler
- ✅ Consolidar queries duplicadas
- ✅ Eliminar cascata de reloads

**Ganho:** ~550 linhas eliminadas, -25-40% requisições

---

### Sprint 3 - Refatoração Backend 🟡
**5-10 dias | 3 ações | Média Prioridade**

Arquitetura clara, SRP respeitado:
- ✅ Decompor UnidadeFacade
- ✅ Dividir SubprocessoWorkflowService
- ✅ Consolidar Services de Mapa

**Ganho:** 0 arquivos > 500 linhas, melhor testabilidade

---

### Sprint 4 - Otimizações Opcionais 🟢
**Conforme necessário | 3 ações | Baixa Prioridade**

APENAS se houver necessidade demonstrada:
- ⚠️ Cache HTTP (SE latência > 500ms)
- ⚠️ @EntityGraph (SE surgir N+1)
- ⚠️ Decompor stores (SE manutenção dificultar)

**Ganho:** Implementar apenas com YAGNI

---

## 📊 Métricas

### Baseline → Metas

| Métrica | Atual | Meta | Melhoria |
|---------|-------|------|----------|
| Arquivos > 500L | 2 | 0 | -2 |
| Código duplicado | ~800L | 0 | -800L |
| Requisições/ação | 3 | 1 | -66% |
| FetchType.EAGER | 2 | 0 | -2 |
| Queries N+1 | 5 | 0 | -5 |

**Performance:**
- Tempo de resposta: +20-35%
- Latência em ações: -40-60%
- Uso de memória: -10-15%

---

## 🚀 Como Executar

### Para Agentes IA

```bash
# 1. Ler documentação
cat REFACTORING-SUMMARY.md
cat REFACTORING-INDEX.md

# 2. Começar Sprint 1
cat backend-sprint-1.md
# Seguir "Passos para Execução por IA"

# 3. Atualizar tracker
# Editar refactoring-tracker.md após cada ação

# 4. Validar
./gradlew :backend:test
npm run test:e2e
```

### Para Desenvolvedores

1. **Revisar:** Ler REFACTORING-SUMMARY.md
2. **Planejar:** Priorizar sprints conforme necessidade
3. **Executar:** Seguir documentos de sprint
4. **Validar:** Executar testes e medir métricas
5. **Documentar:** Atualizar tracker e ADRs

---

## ✅ Checklist Rápido

### Sprint 1 (1-2 dias)
- [ ] Alterar EAGER → LAZY
- [ ] Remover override findAll()
- [ ] Remover cache
- [ ] Converter subquery → JOIN
- [ ] Extrair flattenTree

### Sprint 2 (3-5 dias)
- [ ] Criar useErrorHandler
- [ ] Consolidar queries
- [ ] Eliminar cascata de reloads

### Sprint 3 (5-10 dias)
- [ ] Decompor UnidadeFacade
- [ ] Dividir SubprocessoWorkflowService
- [ ] Consolidar Services de Mapa

### Sprint 4 (opcional)
- [ ] Cache HTTP? (se necessário)
- [ ] @EntityGraph? (se N+1)
- [ ] Decompor stores? (se dificultar)

---

## 🎯 Princípios

- **YAGNI** - You Aren't Gonna Need It
- **KISS** - Keep It Simple, Stupid
- **DRY** - Don't Repeat Yourself
- **SRP** - Single Responsibility Principle
- **Measure, Don't Assume**

---

## 📞 Suporte

- **Arquitetura:** `backend/etc/docs/ARCHITECTURE.md`
- **ADRs:** `backend/etc/docs/adr/`
- **Padrões:** `AGENTS.md`, `GEMINI.md`
- **E2E:** `/regras/e2e_regras.md`

---

## 📈 Estatísticas

- **Documentos:** 7 arquivos (124KB)
- **Ações:** 14 refatorações
- **Sprints:** 4 (18-22 dias)
- **Linhas:** ~3.455 linhas de documentação
- **Código exemplo:** ~2.000 linhas
- **Comandos:** ~200 comandos bash

---

**Preparado por:** Agente de IA - Documentação de Refatorações  
**Data:** 26 de Janeiro de 2026  
**Versão:** 1.0  
**Status:** ✅ Pronto para Execução

---

> 💡 **Dica:** Comece sempre pela Sprint 1 (Quick Wins) para estabelecer uma base sólida!
