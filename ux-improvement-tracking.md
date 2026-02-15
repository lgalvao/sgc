# Rastreamento de Melhorias de UX - SGC

**Data de início:** 2026-02-14  
**Última atualização:** 2026-02-15 (UX-009 concluído)  
**Documento base:** `ux-improvement-plan.md`

---

## 📊 Visão Geral do Progresso

### Status Consolidado

| Categoria | Total | Concluído | Em Andamento | Pendente | % Completo |
|-----------|-------|-----------|--------------|----------|------------|
| **Prioridade Alta** | 3 | 3 | 0 | 0 | 100% |
| **Prioridade Média** | 3 | 3 | 0 | 0 | 100% |
| **Prioridade Estrutural** | 4 | 3 | 0 | 1 | 75% |
| **TOTAL** | 10 | 9 | 0 | 1 | **90%** |

### Legenda de Status
- ⬜ **Pendente:** Não iniciado
- 🔵 **Em Andamento:** Implementação em progresso
- ✅ **Concluído:** Implementado e testado
- ⚠️ **Bloqueado:** Aguardando dependência ou decisão
- 🔴 **Cancelado:** Não será implementado

---

## 🎯 Melhorias Priorizadas

### Prioridade Alta (Impacto Imediato)

#### ✅ UX-001: Padronizar Rodapé e Semântica de Botões em Modais
**Status:** Concluído  
**Prioridade:** Alta  
**Complexidade:** Média  
**Esforço estimado:** 3-4 horas  

**Checklist de Implementação:**
- [x] Criar `frontend/src/components/comum/ModalPadrao.vue`
- [x] Definir taxonomia de variantes (primary, secondary, danger)
- [x] Migrar `ModalConfirmacao.vue` (ajustar se necessário)
- [x] Migrar `DisponibilizarMapaModal.vue` (renomeação para `ModalMapaDisponibilizar.vue` planejada no UX-010)
- [x] Migrar `ModalAndamentoGeral.vue` (renomeação para `ModalRelatorioAndamento.vue` planejada no UX-010)
- [x] Migrar demais modais padronizados (`Modal[Contexto][Acao]`)
- [x] Criar teste E2E `e2e/ux/botoes-modais.spec.ts`

---

#### ✅ UX-002: Unificar Padrão de Validação Inline
**Status:** Concluído  
**Prioridade:** Alta  
**Complexidade:** Alta  
**Esforço estimado:** 5-6 horas  

**Checklist de Implementação:**
- [x] Criar `frontend/src/components/comum/CampoTexto.vue`
- [x] Criar `frontend/src/composables/useValidacao.ts`
- [x] Aplicar em `ProcessoFormFields.vue`
- [x] Aplicar em `CadAtividadeForm.vue`
- [x] Aplicar em demais formulários
- [x] Implementar foco automático no primeiro erro

---

#### ✅ UX-003: Melhorar Legibilidade de Tabelas
**Status:** Concluído  
**Prioridade:** Alta  
**Complexidade:** Média  
**Esforço estimado:** 4-5 horas  

**Checklist de Implementação:**
- [x] Criar `frontend/src/components/comum/BadgeSituacao.vue`
- [x] Definir cores para situações (CRIADO, EM_ANDAMENTO, FINALIZADO)
- [x] Aplicar em `TabelaProcessos.vue`
- [x] Aplicar em `TabelaMovimentacoes.vue`
- [x] Implementar estado vazio com CTA

---

### Prioridade Média

#### ✅ UX-004: Adicionar Cabeçalho Contextual por Etapa/Perfil
**Status:** Concluído  
**Prioridade:** Média  
**Complexidade:** Média  
**Esforço estimado:** 3-4 horas  

**Checklist de Implementação:**
- [x] Criar/Refatorar `frontend/src/components/layout/PageHeader.vue`
- [x] Criar `frontend/src/composables/useProximaAcao.ts`
- [x] Aplicar em `Processo.vue` e `Subprocesso.vue`
- [x] Implementar breadcrumb dinâmico (via `BarraNavegacao.vue` + `useBreadcrumbs.ts`)

---

#### ✅ UX-005: Padronizar Layout Base das Páginas
**Status:** Concluído  
**Prioridade:** Média  
**Complexidade:** Alta  
**Esforço estimado:** 6-8 horas  

**Checklist de Implementação:**
- [x] Criar `frontend/src/components/layout/LayoutPadrao.vue`
- [x] Migrar views principais (`PainelView.vue`, `Processo.vue`, `Subprocesso.vue`) para usar layout base
- [x] Expandir migração inicial para `CadAtividades.vue`, `HistoricoView.vue`, `Relatorios.vue` e `Unidades.vue`
- [x] Expandir migração para `CadProcesso.vue`, `CadMapa.vue`, `VisMapa.vue`, `VisAtividades.vue`, `Unidade.vue`, `CadAtribuicao.vue`, `MonitoramentoDiagnostico.vue`, `OcupacoesCriticasDiagnostico.vue`, `AutoavaliacaoDiagnostico.vue` e `ConclusaoDiagnostico.vue`
- [x] Testar consistência visual entre módulos (typecheck, lint e testes unitários)

---

#### ✅ UX-006: Fortalecer Estado Vazio com CTA Orientado
**Status:** Concluído  
**Prioridade:** Média  
**Complexidade:** Baixa  
**Esforço estimado:** 2-3 horas  

**Checklist de Implementação:**
- [x] Validar uso atual de `EmptyState.vue`
- [x] Padronizar mensagens e ações

---

### Prioridade Estrutural (Fundação)

#### ✅ UX-007: Criar Design Tokens (CSS Nativo)
**Status:** Concluído  
**Prioridade:** Estrutural  
**Complexidade:** Alta  
**Esforço estimado:** 8-10 horas  

**Checklist de Implementação:**
- [x] Criar `frontend/src/assets/css/tokens.css`
- [x] Definir cores semânticas (mapeadas do Bootstrap)
- [x] Definir escala de espaçamento e tipografia
- [x] Importar globalmente em `main.ts` ou `style.css`

---

#### ✅ UX-008: Definir Regras de Responsividade
**Status:** Concluído  
**Prioridade:** Estrutural  
**Complexidade:** Alta  
**Esforço estimado:** 6-8 horas  

**Checklist de Implementação:**
- [x] Criar utilitários de breakpoint
- [x] Adaptar tabelas para mobile (visualização em cards)
- [x] Adaptar modais para mobile

---

#### ✅ UX-009: Manter Suíte de Captura como Auditoria Visual
**Status:** Concluído  
**Prioridade:** Estrutural  
**Complexidade:** Baixa  
**Esforço estimado:** 2 horas  

**Checklist de Implementação:**
- [x] Manter `e2e/captura-telas.spec.ts` atualizado
- [x] Adicionar novas capturas conforme necessário

---

#### ⬜ UX-010: Padronização de Nomenclatura (Refatoração)
**Status:** Pendente  
**Prioridade:** Estrutural  
**Complexidade:** Média  
**Esforço estimado:** 4-6 horas  
**Impacto:** Manutenibilidade e consistência cognitiva

**Checklist de Implementação:**
- [ ] Renomear Views (`CadProcesso` -> `ProcessoCadastroView`, `Processo` -> `ProcessoDetalheView`, etc.)
- [ ] Renomear Modais (`DisponibilizarMapaModal` -> `ModalMapaDisponibilizar`)
- [ ] Renomear Componentes e Formulários (`Cad` -> `Form`)
- [ ] Atualizar rotas em `processo.routes.ts`
- [ ] Atualizar importações
- [ ] Validar build e testes

---

## 📝 Histórico de Mudanças

### 2026-02-15
- ✅ Sincronizado com Plano v1.2
- ✅ Adicionado UX-010 (Nomenclatura)
- ✅ Atualizado UX-007 para CSS Tokens
- ✅ Atualizado terminologias (Situacao, Modais, Views)
- ✅ UX-001 concluído com migração de modais críticos e adicionais
- ✅ Criados testes focados: unitários (`ModalPadrao`) e E2E (`e2e/ux/botoes-modais.spec.ts`)
- 🔵 UX-002 iniciado com validação inline em `ProcessoFormFields` e `CadAtividadeForm`
- 🔵 UX-002 evoluído com `CampoTexto.vue`, `useValidacao.ts` e testes unitários focados
- ✅ UX-002 concluído com cobertura de formulários mapeados no frontend
- 🔵 UX-003 iniciado com `BadgeSituacao.vue` e aplicação em `TabelaProcessos.vue`
- ✅ UX-003 concluído com `BadgeSituacao` em tabelas e CTA no estado vazio
- 🔵 UX-004 iniciado com cabeçalho contextual e `useProximaAcao.ts`
- ✅ UX-004 concluído com contexto por etapa/perfil em `PageHeader` e validação de breadcrumb dinâmico existente
- ✅ Executada rodada periódica `npm run test:e2e:captura` (18/18 cenários de captura aprovados)
- 🔵 UX-005 iniciado com criação de `LayoutPadrao.vue` e migração inicial de views principais
- 🔵 UX-005 expandido para views adicionais com validação completa de typecheck/lint/testes unitários
- ✅ UX-005 concluído com migração de todas as views de conteúdo para `LayoutPadrao.vue`
- ✅ UX-006 concluído com padronização de mensagens/CTA em estados vazios prioritários (`TabelaAlertas`, `HistoricoView`, `Unidades`)
- ✅ UX-007 concluído com criação e importação global de design tokens em `frontend/src/assets/css/tokens.css`
- ✅ UX-008 concluído com utilitários de responsividade, tabelas em modo `stacked` no mobile e rodapé de modais adaptado para telas pequenas
- ✅ UX-009 concluído com execução periódica da suíte de captura visual (`npm run test:e2e:captura`) com 18/18 cenários aprovados

### 2026-02-14
- ✅ Criado documento de rastreamento inicial
