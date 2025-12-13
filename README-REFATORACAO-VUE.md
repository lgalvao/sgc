# 📘 Guia de Refatoração Vue.js do SGC

## Visão Geral

Este guia centraliza toda a documentação relacionada ao processo de refatoração do frontend Vue.js do Sistema de Gestão de Competências (SGC), desde o protótipo inicial até a arquitetura de produção consolidada.

---

## 📚 Documentos Disponíveis

### 🎯 Documento Principal — **LEIA ESTE PRIMEIRO**

**[`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md)** (v2.0)
- 📏 751 linhas | 27KB | 82 seções
- 📅 Última atualização: 2025-12-13
- 📊 9 componentes analisados em detalhes
- ⏱️ Estimativa total: 15-20 horas
- ✨ Inclui: contexto completo, análise de componentes, estratégia de rollout, checklists

**Quando usar:** Ao planejar ou executar qualquer refatoração de componentes Vue.js

---

### 📋 Resumo Executivo — **REFERÊNCIA RÁPIDA**

**[`SUMMARY-PLANO-REFATORACAO-V2.md`](SUMMARY-PLANO-REFATORACAO-V2.md)**
- 📏 176 linhas | 5.8KB
- 🎯 Quick reference com prioridades e estimativas
- 🛠️ Comandos úteis prontos para usar
- ✓ Checklist de qualidade por PR

**Quando usar:** Para consulta rápida de prioridades, estimativas e comandos

---

### 📜 Histórico de Versões

**[`CHANGELOG-PLANO-REFATORACAO.md`](CHANGELOG-PLANO-REFATORACAO.md)**
- 📏 94 linhas | 3.7KB
- 📊 Comparativo v1.0 vs v2.0
- 📈 Estatísticas e melhorias documentadas

**Quando usar:** Para entender a evolução do plano e mudanças entre versões

---

### 🗂️ Documento Original (Supersedido)

**[`plano-refatoracao-vue.md`](plano-refatoracao-vue.md)** (v1.0)
- ⚠️ Marcado como supersedido
- 📅 Mantido para referência histórica
- 🔗 Contém link para v2.0

**Quando usar:** Apenas para referência histórica ou comparação

---

### 🛡️ Tratamento de Erros (Relacionado)

**[`plano-refatoracao-erros.md`](plano-refatoracao-erros.md)**
- 📏 882 linhas | 28KB
- ✅ Implementação concluída
- 🎯 Sistema de normalização de erros (`utils/apiError.ts`)

**Quando usar:** Para entender o sistema de tratamento de erros (já implementado)

---

## 🗺️ Navegação Rápida

### Por Objetivo

| Objetivo | Documento |
|----------|-----------|
| Entender contexto completo do projeto | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §1-2 |
| Ver lista de componentes para refatorar | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §1-7 |
| Consultar prioridades e estimativas | [`SUMMARY-PLANO-REFATORACAO-V2.md`](SUMMARY-PLANO-REFATORACAO-V2.md) §3-4 |
| Ver mudanças já implementadas | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §3 |
| Entender estratégia de rollout | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §11 |
| Checklist de qualidade | [`SUMMARY-PLANO-REFATORACAO-V2.md`](SUMMARY-PLANO-REFATORACAO-V2.md) §6 |
| Comandos úteis | [`SUMMARY-PLANO-REFATORACAO-V2.md`](SUMMARY-PLANO-REFATORACAO-V2.md) §7 |
| Histórico de mudanças | [`CHANGELOG-PLANO-REFATORACAO.md`](CHANGELOG-PLANO-REFATORACAO.md) |

### Por Componente

| Componente | Prioridade | Estimativa | Seção |
|------------|------------|------------|-------|
| ImportarAtividadesModal.vue | 🔴 Alta | 1-1.5h | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §1 |
| ArvoreUnidades.vue | 🔴 Alta | 2-2.5h | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §5 |
| ImpactoMapaModal.vue | 🔴 Alta | 1.5-2h | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §2 |
| SubprocessoCards.vue | 🟡 Média | 1-1.5h | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §3 |
| ModalAcaoBloco.vue | 🟡 Média | 45min-1h | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §4 |
| TabelaProcessos.vue | 🟡 Média | 1-1.5h | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §7 |
| HistoricoAnaliseModal.vue | 🟢 Baixa | 45min-1h | [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md) §6 |

---

## 🚀 Início Rápido

### Para Começar uma Refatoração

1. **Leia o resumo executivo:**
   ```bash
   cat SUMMARY-PLANO-REFATORACAO-V2.md
   ```

2. **Escolha um componente** da lista de prioridades (veja tabela acima)

3. **Consulte a seção específica** do documento principal:
   ```bash
   # Exemplo: ImportarAtividadesModal.vue
   grep -A 50 "### 1. \`ImportarAtividadesModal.vue\`" plano-refatoracao-vue-atualizado.md
   ```

4. **Valide o ambiente:**
   ```bash
   cd frontend
   npm run test:unit
   npm run lint
   npm run typecheck
   ```

5. **Execute a refatoração** seguindo a seção específica do plano

6. **Use o checklist de qualidade** antes de abrir PR:
   - Ver [`SUMMARY-PLANO-REFATORACAO-V2.md`](SUMMARY-PLANO-REFATORACAO-V2.md) §6

---

## 📊 Status Atual do Projeto

### ✅ Implementado

- ✅ Tratamento de erros padronizado (`utils/apiError.ts`)
- ✅ Novos componentes: `AtividadeItem.vue`, `UnidadeTreeNode.vue`
- ✅ Módulo de Diagnóstico (4 views)
- ✅ Store `feedback.ts` para toasts
- ✅ Eliminação de `window.alert()` e `window.confirm()`
- ✅ Arquitetura: Views → Stores → Services → API

### 🔧 Em Progresso

- 🔧 Filtragem server-side em modais
- 🔧 Remoção de hardcoding de IDs/siglas
- 🔧 Desacoplamento de modais do estado global
- 🔧 Padronização de `lastError` em todas as stores

### 📈 Métricas

- **Componentes:** 25 (7 prioritários para refatoração)
- **Views:** 18 (incluindo 4 de diagnóstico)
- **Stores:** 12 (parcialmente padronizadas)
- **Services:** 12 (arquitetura consolidada)
- **Testes Unitários:** 85+ specs (Vitest)
- **Testes E2E:** 15+ specs (Playwright)
- **CDUs Implementados:** 21

---

## 🛠️ Ferramentas e Recursos

### Comandos Essenciais

```bash
# Desenvolvimento (frontend/)
npm run dev                    # Dev server
npm run test:unit              # Testes unitários
npm run test:unit -- [arquivo] # Teste específico
npm run lint                   # ESLint
npm run typecheck              # TypeScript
npm run quality:all            # Todas verificações

# E2E (raiz)
npm run test:e2e               # Testes E2E

# Backend (raiz)
./gradlew :backend:test        # Testes backend
./gradlew qualityCheckAll      # Qualidade completa
```

### Buscas Úteis

```bash
# Encontrar uso de componente
grep -r "ImportarAtividadesModal" frontend/src/ --include="*.vue"

# Encontrar alerts nativos
grep -r "window.alert\|window.confirm" frontend/src/ --include="*.{vue,ts}"

# Encontrar hardcoding de IDs
grep -r "codigo === 1\|codigo === '1'" frontend/src/ --include="*.{vue,ts}"

# Encontrar uso de feedbackStore em stores
grep -r "feedbackStore.show" frontend/src/stores/ --include="*.ts"
```

---

## 📚 Documentação Adicional

### Documentação Interna

- [`README.md`](README.md) — Visão geral do projeto
- [`AGENTS.md`](AGENTS.md) — Guia para agentes de desenvolvimento
- [`frontend/README.md`](frontend/README.md) — Arquitetura do frontend
- [`backend/README.md`](backend/README.md) — Arquitetura do backend
- [`frontend/src/components/README.md`](frontend/src/components/README.md) — Componentes
- [`frontend/src/stores/README.md`](frontend/src/stores/README.md) — Stores Pinia
- [`frontend/src/utils/README.md`](frontend/src/utils/README.md) — Utilitários

### Casos de Uso

- [`reqs/`](reqs/) — 21 CDUs documentados
- [`e2e/`](e2e/) — Testes E2E cobrindo CDUs

### Recursos Externos

- [Vue 3 Documentation](https://vuejs.org/)
- [Pinia Documentation](https://pinia.vuejs.org/)
- [BootstrapVueNext](https://bootstrap-vue-next.github.io/bootstrap-vue-next/)
- [Vitest Documentation](https://vitest.dev/)
- [Playwright Documentation](https://playwright.dev/)

---

## 🤝 Contribuindo

### Processo de Refatoração

1. **Escolha um componente** prioritário
2. **Crie uma branch** `refactor/nome-componente`
3. **Implemente** seguindo o plano
4. **Teste** (unit + E2E quando aplicável)
5. **Valide qualidade** (lint + typecheck)
6. **Abra PR** com referência ao plano
7. **Code review** e merge

### Convenções

- **Commits:** `refactor(componente): descrição sucinta`
- **PRs:** Incluir link para seção específica do plano
- **Testes:** Sempre atualizar testes relacionados
- **Documentação:** Atualizar READMEs se necessário

---

## 📞 Suporte

**Dúvidas sobre o plano?**
- Consulte [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md)
- Veja exemplos de código nas seções específicas
- Revise [`CHANGELOG-PLANO-REFATORACAO.md`](CHANGELOG-PLANO-REFATORACAO.md) para contexto

**Problemas técnicos?**
- Revise [`AGENTS.md`](AGENTS.md) para convenções
- Consulte [`frontend/src/utils/README.md`](frontend/src/utils/README.md) para utilitários
- Veja [`plano-refatoracao-erros.md`](plano-refatoracao-erros.md) para tratamento de erros

---

## 📝 Notas Finais

**Versão do Plano:** 2.0  
**Data:** 2025-12-13  
**Status:** ✅ Pronto para execução  
**Estimativa Total:** 15-20 horas  

**Próximo passo recomendado:** Iniciar Fase 1 (Alta Prioridade) com `ImportarAtividadesModal.vue`

---

**Última atualização:** 2025-12-13
