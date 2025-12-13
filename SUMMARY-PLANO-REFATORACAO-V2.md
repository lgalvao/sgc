# Resumo: Plano de Refatoração Vue.js v2.0

## 📄 Documento Principal

**Arquivo:** [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md)

**Tamanho:** 751 linhas | 27KB | 82 seções

**Última atualização:** 2025-12-13

---

## 🎯 Objetivo

Continuar removendo lógicas de "protótipo" do frontend Vue.js, otimizar integração com backend, e consolidar melhorias arquiteturais já implementadas.

---

## ✅ Progresso Atual

### Implementado Recentemente

- ✅ **Tratamento de Erros Padronizado** — Sistema completo de normalização de erros (`utils/apiError.ts`)
- ✅ **Novos Componentes** — `AtividadeItem.vue`, `UnidadeTreeNode.vue` (seguindo boas práticas)
- ✅ **Módulo de Diagnóstico** — 4 novas views para fluxo completo de diagnóstico
- ✅ **Eliminação de Alerts Nativos** — Sem `window.alert()` ou `window.confirm()` no código
- ✅ **Store de Feedback** — `feedback.ts` para notificações toast centralizadas

### Padrões Consolidados

- Arquitetura: Views → Stores (Pinia) → Services (Axios) → API
- Componentes "dumb" com props/emits
- Tratamento de erro com `lastError: NormalizedError | null` nas stores
- `BAlert` inline para erros de validação; toast global para erros inesperados
- Server-side filtering e paginação
- Testes: 85+ specs unitários (Vitest) + 15+ specs E2E (Playwright)

---

## 🔧 Componentes para Refatoração

### 🔴 Alta Prioridade (5-7h)

1. **ImportarAtividadesModal.vue**
   - ❌ Problema: Filtragem client-side de processos (paginação hardcoded 1000)
   - ✅ Solução: Usar endpoint `/processos/finalizados` e `processosStore.processosFinalizados`
   - ⏱️ Estimativa: 1-1.5h

2. **ArvoreUnidades.vue**
   - ❌ Problema: Hardcoding `codigo === 1` e `sigla === 'SEDOC'`
   - ✅ Solução: Critério genérico baseado em `nivel` ou prop `ocultarRaiz`
   - ⏱️ Estimativa: 2-2.5h

3. **ImpactoMapaModal.vue**
   - ❌ Problema: Depende de `processosStore.processoDetalhe`
   - ✅ Solução: Receber `codSubprocesso` via prop obrigatória
   - ⏱️ Estimativa: 1.5-2h

### 🟡 Média Prioridade (3-4h)

4. **SubprocessoCards.vue**
   - ❌ Problema: Usa `useRoute()` internamente (não reusável)
   - ✅ Solução: Props obrigatórias, remover `useRoute()`
   - ⏱️ Estimativa: 1-1.5h

5. **ModalAcaoBloco.vue / AcoesEmBlocoModal.vue**
   - ❌ Problema: Duplicação, possível uso de `alert()`
   - ✅ Solução: Consolidar, substituir por `BAlert`/emits
   - ⏱️ Estimativa: 45min-1h

6. **TabelaProcessos.vue**
   - ❌ Problema: Ambiguidade sobre ordenação (client vs server)
   - ✅ Solução: Documentar que é server-side, validar que não há `Array.sort()`
   - ⏱️ Estimativa: 1-1.5h

### 🟢 Baixa Prioridade (5-6h)

7. **HistoricoAnaliseModal.vue**
   - ❌ Problema: Race conditions no watch
   - ✅ Solução: Adicionar verificação de `loading`, limpar dados ao fechar
   - ⏱️ Estimativa: 45min-1h

8. **Stores Restantes**
   - ❌ Problema: Nem todas as stores usam padrão `lastError`
   - ✅ Solução: Padronizar `lastError: NormalizedError | null` em todas
   - ⏱️ Estimativa: 3-4h

9. **Views de Diagnóstico**
   - ❌ Problema: Módulo novo, precisa auditoria
   - ✅ Solução: Validar que seguem boas práticas (sem hardcoding, server-side filtering)
   - ⏱️ Estimativa: 2-3h

---

## 📋 Estratégia de Rollout

### Fase 1: Alta Prioridade (Sprint 1-2)
- Componentes críticos com impacto funcional
- **Estimativa:** 5-7 horas

### Fase 2: Média Prioridade (Sprint 3-4)
- Melhorias arquiteturais
- **Estimativa:** 3-4 horas

### Fase 3: Baixa Prioridade (Sprint 5)
- Melhorias de qualidade
- **Estimativa:** 5-6 horas

### Fase 4: Documentação (Sprint 6)
- Atualizar READMEs, AGENTS.md, guias
- **Estimativa:** 2-3 horas

**Total:** 15-20 horas

---

## ✓ Checklist de Qualidade (Por PR)

- [ ] Testes unitários do componente passam
- [ ] Testes E2E relacionados passam
- [ ] `npm run lint` sem erros
- [ ] `npm run typecheck` sem erros
- [ ] Sem `window.alert()` ou `window.confirm()`
- [ ] Tratamento de erro usa `NormalizedError` e `lastError`
- [ ] Sem IDs/siglas hardcoded em lógica de negócio
- [ ] Modais recebem dados via props (não leem estado global diretamente)
- [ ] Componentes "dumb" não usam `useRoute()`
- [ ] Documentação inline atualizada
- [ ] Payload de exemplo da API documentado (se novo endpoint)

---

## 🛠️ Comandos Úteis

```bash
# Frontend (dentro de frontend/)
npm run test:unit              # Testes unitários
npm run lint                   # ESLint
npm run typecheck              # TypeScript
npm run quality:all            # Todas verificações

# E2E (raiz)
npm run test:e2e               # Testes E2E

# Buscar no código
grep -r "ImportarAtividadesModal" frontend/src/ --include="*.vue"
grep -r "window.alert\|window.confirm" frontend/src/ --include="*.vue" --include="*.ts"
grep -r "codigo === 1\|codigo === '1'" frontend/src/ --include="*.vue" --include="*.ts"
grep -r "feedbackStore.show" frontend/src/stores/ --include="*.ts"
```

---

## 📚 Referências

- **Documento completo:** [`plano-refatoracao-vue-atualizado.md`](plano-refatoracao-vue-atualizado.md)
- **Changelog:** [`CHANGELOG-PLANO-REFATORACAO.md`](CHANGELOG-PLANO-REFATORACAO.md)
- **Documento original (supersedido):** [`plano-refatoracao-vue.md`](plano-refatoracao-vue.md)
- **Plano de erros:** [`plano-refatoracao-erros.md`](plano-refatoracao-erros.md)
- **Guia para agentes:** [`AGENTS.md`](AGENTS.md)

---

## 🚀 Próximos Passos

1. **Revisar e aprovar** este plano
2. **Criar issues** no GitHub para cada componente prioritário
3. **Iniciar Fase 1** com `ImportarAtividadesModal.vue`
4. **Auditar** views de Diagnóstico
5. **Padronizar** `lastError` em todas as stores

---

**Versão:** 2.0  
**Data:** 2025-12-13  
**Status:** ✅ Pronto para execução
