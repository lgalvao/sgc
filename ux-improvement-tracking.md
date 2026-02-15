# Rastreamento de Melhorias de UX - SGC

**Data de início:** 2026-02-14  
**Última atualização:** 2026-02-15 (UX-003 concluído)  
**Documento base:** `ux-improvement-plan.md`

---

## 📊 Visão Geral do Progresso

### Status Consolidado

| Categoria | Total | Concluído | Em Andamento | Pendente | % Completo |
|-----------|-------|-----------|--------------|----------|------------|
| **Prioridade Alta** | 3 | 3 | 0 | 0 | 100% |
| **Prioridade Média** | 3 | 0 | 0 | 3 | 0% |
| **Prioridade Estrutural** | 4 | 0 | 0 | 4 | 0% |
| **TOTAL** | 10 | 3 | 0 | 7 | **30%** |

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
- [x] Migrar `ModalMapaDisponibilizar.vue` (antigo `DisponibilizarMapaModal`)
- [x] Migrar `ModalRelatorioAndamento.vue`
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
- [x] Aplicar em `AtividadeCadastroForm.vue`
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

#### ⬜ UX-004: Adicionar Cabeçalho Contextual por Etapa/Perfil
**Status:** Pendente  
**Prioridade:** Média  
**Complexidade:** Média  
**Esforço estimado:** 3-4 horas  

**Checklist de Implementação:**
- [ ] Criar/Refatorar `frontend/src/components/layout/PageHeader.vue`
- [ ] Criar `frontend/src/composables/useProximaAcao.ts`
- [ ] Aplicar em `ProcessoDetalheView.vue` e `SubprocessoDetalheView.vue`
- [ ] Implementar breadcrumb dinâmico

---

#### ⬜ UX-005: Padronizar Layout Base das Páginas
**Status:** Pendente  
**Prioridade:** Média  
**Complexidade:** Alta  
**Esforço estimado:** 6-8 horas  

**Checklist de Implementação:**
- [ ] Criar `frontend/src/components/layout/LayoutPadrao.vue`
- [ ] Migrar views principais para usar layout base
- [ ] Testar consistência visual entre módulos

---

#### ⬜ UX-006: Fortalecer Estado Vazio com CTA Orientado
**Status:** Pendente  
**Prioridade:** Média  
**Complexidade:** Baixa  
**Esforço estimado:** 2-3 horas  

**Checklist de Implementação:**
- [ ] Validar uso atual de `EmptyState.vue`
- [ ] Padronizar mensagens e ações

---

### Prioridade Estrutural (Fundação)

#### ⬜ UX-007: Criar Design Tokens (CSS Nativo)
**Status:** Pendente  
**Prioridade:** Estrutural  
**Complexidade:** Alta  
**Esforço estimado:** 8-10 horas  

**Checklist de Implementação:**
- [ ] Criar `frontend/src/assets/css/tokens.css`
- [ ] Definir cores semânticas (mapeadas do Bootstrap)
- [ ] Definir escala de espaçamento e tipografia
- [ ] Importar globalmente em `main.ts` ou `style.css`

---

#### ⬜ UX-008: Definir Regras de Responsividade
**Status:** Pendente  
**Prioridade:** Estrutural  
**Complexidade:** Alta  
**Esforço estimado:** 6-8 horas  

**Checklist de Implementação:**
- [ ] Criar utilitários de breakpoint
- [ ] Adaptar tabelas para mobile (visualização em cards)
- [ ] Adaptar modais para mobile

---

#### ⬜ UX-009: Manter Suíte de Captura como Auditoria Visual
**Status:** Pendente  
**Prioridade:** Estrutural  
**Complexidade:** Baixa  
**Esforço estimado:** 2 horas  

**Checklist de Implementação:**
- [ ] Manter `e2e/captura-telas.spec.ts` atualizado
- [ ] Adicionar novas capturas conforme necessário

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

### 2026-02-14
- ✅ Criado documento de rastreamento inicial
