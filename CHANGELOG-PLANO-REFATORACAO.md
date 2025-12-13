# Changelog: Plano de Refatoração Vue.js

## Versão 2.0 (2025-12-13)

### Mudanças Principais

**Novo Documento:** `plano-refatoracao-vue-atualizado.md` — Versão expandida e atualizada do plano de refatoração.

**Documento Original:** `plano-refatoracao-vue.md` — Marcado como supersedido, mantido para referência histórica.

### Conteúdo Adicionado

1. **Contexto Expandido:**
   - Stack tecnológico completo (Vue 3.5, Pinia, BootstrapVueNext, Vitest, Playwright)
   - Arquitetura de camadas detalhada
   - Estrutura de diretórios com contagem de arquivos (25 componentes, 18 views, 12 stores, 12 services)
   - Informações sobre testes (85+ specs unitários, 15+ specs E2E)

2. **Mudanças Implementadas Documentadas:**
   - ✅ Tratamento de Erros Padronizado (referência ao `plano-refatoracao-erros.md`)
   - ✅ Sistema de normalização de erros (`utils/apiError.ts`)
   - ✅ Novos componentes: `AtividadeItem.vue`, `UnidadeTreeNode.vue`
   - ✅ Módulo de Diagnóstico (4 novas views)
   - ✅ Store `feedback.ts` para notificações toast
   - ✅ Eliminação de `window.alert()` e `window.confirm()`

3. **Análise Detalhada de Componentes:**
   - 7 componentes prioritários para refatoração com análise expandida:
     1. `ImportarAtividadesModal.vue` (Alta prioridade)
     2. `ImpactoMapaModal.vue` (Média prioridade)
     3. `SubprocessoCards.vue` (Média prioridade)
     4. `AcoesEmBlocoModal.vue` & `ModalAcaoBloco.vue` (Média prioridade)
     5. `ArvoreUnidades.vue` (Alta prioridade)
     6. `HistoricoAnaliseModal.vue` (Baixa prioridade)
     7. `TabelaProcessos.vue` (Média prioridade)

4. **Novos Itens Identificados:**
   - Componentes novos já seguindo boas práticas (sem refatoração necessária)
   - Views de Diagnóstico para auditoria
   - Padronização de `lastError` em stores restantes

5. **Estratégia de Rollout:**
   - Fase 1: Alta Prioridade (5-7 horas)
   - Fase 2: Média Prioridade (3-4 horas)
   - Fase 3: Baixa Prioridade (5-6 horas)
   - Fase 4: Documentação (2-3 horas)
   - **Total estimado:** 15-20 horas

6. **Ferramentas e Comandos:**
   - Scripts de desenvolvimento, testes, e qualidade
   - Comandos de busca no código para auditoria
   - Referências a documentação interna e externa

### Estatísticas

- **Linhas:** 751 (vs. 360 no documento original)
- **Seções:** 82 (vs. ~40 no documento original)
- **Blocos de código:** 26 (vs. ~15 no documento original)
- **Componentes analisados:** 7 principais + 2 novos + 4 views de diagnóstico
- **Estimativas de tempo:** Detalhadas por componente e fase

### Melhorias de Documentação

- Contexto completo do estado atual do projeto
- Seção "Mudanças Implementadas Recentemente" para rastreabilidade
- Seção "Padrões Consolidados" para evitar retrabalho
- Checklist geral de qualidade para PRs
- Ferramentas e comandos úteis prontos para copiar/colar
- Referências cruzadas a outros documentos do projeto

### Próximos Passos Recomendados

1. Iniciar Fase 1 (Alta Prioridade) com `ImportarAtividadesModal.vue`
2. Criar issues/tasks no GitHub para cada componente prioritário
3. Executar auditoria de views de Diagnóstico
4. Completar padronização de `lastError` em todas as stores
5. Atualizar documentação de componentes conforme refatorações avançam

---

## Versão 1.0 (Data da criação original)

Documento inicial criado para guiar refatoração de componentes Vue.js do protótipo para arquitetura de produção.

**Componentes identificados para refatoração:**
- ImportarAtividadesModal
- ImpactoMapaModal
- SubprocessoCards
- AcoesEmBlocoModal & ModalAcaoBloco
- ArvoreUnidades
- HistoricoAnaliseModal
- TabelaProcessos

**Foco:** Remover hardcoding, filtros client-side, dependências de estado global.
