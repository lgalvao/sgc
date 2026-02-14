# Rastreamento de Melhorias de UX - SGC

**Data de início:** 2026-02-14  
**Última atualização:** 2026-02-14  
**Documento base:** `ux-improvement-plan.md`

---

## 📊 Visão Geral do Progresso

### Status Consolidado

| Categoria | Total | Concluído | Em Andamento | Pendente | % Completo |
|-----------|-------|-----------|--------------|----------|------------|
| **Prioridade Alta** | 3 | 0 | 0 | 3 | 0% |
| **Prioridade Média** | 3 | 0 | 0 | 3 | 0% |
| **Prioridade Estrutural** | 3 | 0 | 0 | 3 | 0% |
| **TOTAL** | 9 | 0 | 0 | 9 | **0%** |

### Legenda de Status
- ⬜ **Pendente:** Não iniciado
- 🔵 **Em Andamento:** Implementação em progresso
- ✅ **Concluído:** Implementado e testado
- ⚠️ **Bloqueado:** Aguardando dependência ou decisão
- 🔴 **Cancelado:** Não será implementado

---

## 🎯 Melhorias Priorizadas

### Prioridade Alta (Impacto Imediato)

#### ⬜ UX-001: Padronizar Rodapé e Semântica de Botões em Modais
**Status:** Pendente  
**Prioridade:** Alta  
**Complexidade:** Média  
**Esforço estimado:** 3-4 horas  
**Assignee:** -  
**Data início:** -  
**Data conclusão:** -

**Evidências:**
- `03-processo--02-modal-iniciar-processo.png`
- `03-processo--04-modal-finalizar-processo.png`
- `05-mapa--07-modal-disponibilizar-mapa.png`
- `14-relatorios--02-modal-relatorio-andamento.png`

**Checklist de Implementação:**
- [ ] Criar `frontend/src/components/comum/ModalPadrao.vue`
- [ ] Definir taxonomia de variantes (primary, secondary, danger)
- [ ] Migrar `ModalIniciarProcesso.vue`
- [ ] Migrar `ModalFinalizarProcesso.vue`
- [ ] Migrar `ModalDisponibilizarMapa.vue`
- [ ] Migrar `ModalRelatorioAndamento.vue`
- [ ] Migrar demais modais (~8 arquivos)
- [ ] Criar teste E2E `e2e/ux/botoes-modais.spec.ts`
- [ ] Atualizar `captura-telas.spec.ts`
- [ ] Validar com capturas de tela

**Arquivos Afetados:**
- Criar: `frontend/src/components/comum/ModalPadrao.vue`
- Modificar: ~12 componentes de modal

**Notas:**
-

**Bloqueios:**
-

---

#### ⬜ UX-002: Unificar Padrão de Validação Inline
**Status:** Pendente  
**Prioridade:** Alta  
**Complexidade:** Alta  
**Esforço estimado:** 5-6 horas  
**Assignee:** -  
**Data início:** -  
**Data conclusão:** -

**Evidências:**
- `03-processo--10-botoes-desativados-form-vazio.png`
- `04-subprocesso--23-validacao-inline-primeira-atividade.png`
- `04-subprocesso--25-detalhe-card-com-erro.png`
- `04-subprocesso--26-erro-desaparece-apos-correcao.png`

**Checklist de Implementação:**
- [ ] Criar `frontend/src/components/comum/CampoTexto.vue`
- [ ] Criar `frontend/src/components/comum/ResumoErros.vue`
- [ ] Criar `frontend/src/composables/useValidacao.ts`
- [ ] Aplicar em `FormularioProcesso.vue`
- [ ] Aplicar em `FormularioAtividade.vue`
- [ ] Aplicar em `FormularioMapa.vue`
- [ ] Aplicar em demais formulários (~12 arquivos)
- [ ] Implementar foco automático no primeiro erro
- [ ] Criar testes unitários `CampoTexto.spec.ts`
- [ ] Criar teste E2E `e2e/ux/validacao-formularios.spec.ts`
- [ ] Validar com capturas de tela

**Arquivos Afetados:**
- Criar: 
  - `frontend/src/components/comum/CampoTexto.vue`
  - `frontend/src/components/comum/ResumoErros.vue`
  - `frontend/src/composables/useValidacao.ts`
- Modificar: ~15-20 formulários

**Notas:**
-

**Bloqueios:**
-

---

#### ⬜ UX-003: Melhorar Legibilidade de Tabelas
**Status:** Pendente  
**Prioridade:** Alta  
**Complexidade:** Média  
**Esforço estimado:** 4-5 horas  
**Assignee:** -  
**Data início:** -  
**Data conclusão:** -

**Evidências:**
- `02-painel--06a-tabela-processos.png`
- `07-estados--03-tabela-com-multiplos-estados.png`
- `12-historico--02-tabela-processos-finalizados.png`

**Checklist de Implementação:**
- [ ] Criar `frontend/src/components/comum/TabelaPadrao.vue`
- [ ] Criar `frontend/src/components/comum/BadgeStatus.vue`
- [ ] Definir paleta de cores para status
- [ ] Aplicar em `TabelaProcessos.vue`
- [ ] Aplicar em `TabelaHistorico.vue`
- [ ] Aplicar em demais tabelas (~6 arquivos)
- [ ] Implementar estado vazio com CTA
- [ ] Testar responsividade (desktop, tablet, mobile)
- [ ] Criar testes de snapshot para badges
- [ ] Validar com capturas de tela

**Arquivos Afetados:**
- Criar:
  - `frontend/src/components/comum/TabelaPadrao.vue`
  - `frontend/src/components/comum/BadgeStatus.vue`
- Modificar: ~6-8 componentes de tabela

**Notas:**
-

**Bloqueios:**
-

---

### Prioridade Média

#### ⬜ UX-004: Adicionar Cabeçalho Contextual por Etapa/Perfil
**Status:** Pendente  
**Prioridade:** Média  
**Complexidade:** Média  
**Esforço estimado:** 3-4 horas  
**Assignee:** -  
**Data início:** -  
**Data conclusão:** -

**Evidências:**
- `02-painel--10-painel-gestor.png`
- `02-painel--11-painel-chefe.png`
- `04-subprocesso--01-dashboard-subprocesso.png`
- `09-operacoes-bloco--01-detalhes-processo-gestor.png`

**Checklist de Implementação:**
- [ ] Criar `frontend/src/components/comum/CabecalhoContextual.vue`
- [ ] Criar `frontend/src/composables/useProximaAcao.ts`
- [ ] Implementar lógica por perfil (ADMIN, GESTOR, CHEFE)
- [ ] Aplicar em views principais (~10 arquivos)
- [ ] Implementar breadcrumb dinâmico
- [ ] Testar integração com router
- [ ] Validar mensagens por situação/etapa
- [ ] Validar com capturas de tela

**Arquivos Afetados:**
- Criar:
  - `frontend/src/components/comum/CabecalhoContextual.vue`
  - `frontend/src/composables/useProximaAcao.ts`
- Modificar: ~10 views

**Notas:**
-

**Bloqueios:**
-

---

#### ⬜ UX-005: Padronizar Layout Base das Páginas
**Status:** Pendente  
**Prioridade:** Média  
**Complexidade:** Alta  
**Esforço estimado:** 6-8 horas  
**Assignee:** -  
**Data início:** -  
**Data conclusão:** -

**Evidências:**
- `06-navegacao--01-menu-principal.png`
- `06-navegacao--05a-barra-lateral.png`
- `06-navegacao--03-unidades.png`
- `06-navegacao--04-relatorios.png`

**Checklist de Implementação:**
- [ ] Criar `frontend/src/components/layout/LayoutPadrao.vue`
- [ ] Definir grid de espaçamentos
- [ ] Padronizar posição de títulos e ações
- [ ] Migrar views para usar layout base
- [ ] Testar consistência visual entre módulos
- [ ] Validar com capturas de tela

**Arquivos Afetados:**
- Criar: `frontend/src/components/layout/LayoutPadrao.vue`
- Modificar: Todas as views principais

**Notas:**
-

**Bloqueios:**
-

---

#### ⬜ UX-006: Fortalecer Estado Vazio com CTA Orientado
**Status:** Pendente  
**Prioridade:** Média  
**Complexidade:** Baixa  
**Esforço estimado:** 2-3 horas  
**Assignee:** -  
**Data início:** -  
**Data conclusão:** -

**Evidências:**
- `02-painel--06a-tabela-processos.png` (quando vazio)

**Checklist de Implementação:**
- [ ] Criar `frontend/src/components/comum/EstadoVazio.vue`
- [ ] Definir mensagens por contexto
- [ ] Aplicar em todas as listas/tabelas
- [ ] Incluir CTAs orientados
- [ ] Validar com capturas de tela

**Arquivos Afetados:**
- Criar: `frontend/src/components/comum/EstadoVazio.vue`
- Modificar: Componentes de lista/tabela

**Notas:**
-

**Bloqueios:**
-

---

### Prioridade Estrutural (Fundação)

#### ⬜ UX-007: Criar Design Tokens
**Status:** Pendente  
**Prioridade:** Estrutural  
**Complexidade:** Alta  
**Esforço estimado:** 8-10 horas  
**Assignee:** -  
**Data início:** -  
**Data conclusão:** -

**Impacto:** Base para todas as outras melhorias

**Checklist de Implementação:**
- [ ] Criar `frontend/src/assets/styles/_tokens.scss`
- [ ] Definir cores semânticas (primária, sucesso, aviso, erro, info)
- [ ] Definir tons de cinza (100-900)
- [ ] Definir escala tipográfica (xs, sm, base, lg, xl, 2xl, 3xl, 4xl)
- [ ] Definir pesos de fonte (normal, médio, semibold, bold)
- [ ] Definir escala de espaçamento (xs, sm, md, lg, xl, 2xl, 3xl)
- [ ] Definir raios de borda (sm, md, lg, xl, pill)
- [ ] Definir sombras (sm, md, lg, xl)
- [ ] Definir breakpoints (xs, sm, md, lg, xl, xxl)
- [ ] Definir z-index (dropdown, sticky, fixed, modal, etc)
- [ ] Definir transições (rápida, base, lenta)
- [ ] Documentar uso dos tokens
- [ ] Migrar componentes existentes para usar tokens

**Arquivos Afetados:**
- Criar: `frontend/src/assets/styles/_tokens.scss`
- Modificar: Todos os componentes que usam estilos diretos

**Notas:**
- Esta é a base fundamental para todas as outras melhorias
- Deve ser implementada antes ou em paralelo com outras melhorias

**Bloqueios:**
-

---

#### ⬜ UX-008: Definir Regras de Responsividade
**Status:** Pendente  
**Prioridade:** Estrutural  
**Complexidade:** Alta  
**Esforço estimado:** 6-8 horas  
**Assignee:** -  
**Data início:** -  
**Data conclusão:** -

**Impacto:** Crítico para mobile

**Evidências:**
- `08-responsividade--01-desktop-1920x1080.png`
- `08-responsividade--02-tablet-768x1024.png`
- `08-responsividade--03-tablet-landscape-1024x768.png`
- `08-responsividade--04-mobile-375x667.png`

**Checklist de Implementação:**
- [ ] Criar `frontend/src/utils/breakpoints.ts` com utilitários
- [ ] Definir comportamento de tabelas no mobile (cards)
- [ ] Definir comportamento de modais no mobile (fullscreen)
- [ ] Definir comportamento de ações no mobile (menu)
- [ ] Testar em xs (0-575px)
- [ ] Testar em sm (576-767px)
- [ ] Testar em md (768-991px)
- [ ] Testar em lg (992-1199px)
- [ ] Testar em xl (1200-1399px)
- [ ] Testar em xxl (≥1400px)
- [ ] Atualizar `captura-telas.spec.ts` com mais resoluções
- [ ] Validar com capturas de tela

**Arquivos Afetados:**
- Criar: `frontend/src/utils/breakpoints.ts`
- Modificar: Todos os componentes que precisam de comportamento responsivo

**Notas:**
- Depende de UX-007 (Design Tokens) para breakpoints

**Bloqueios:**
-

---

#### ⬜ UX-009: Manter Suíte de Captura como Auditoria Visual
**Status:** Pendente  
**Prioridade:** Estrutural  
**Complexidade:** Baixa  
**Esforço estimado:** 2 horas (manutenção contínua)  
**Assignee:** -  
**Data início:** -  
**Data conclusão:** -

**Checklist de Implementação:**
- [ ] Manter `e2e/captura-telas.spec.ts` atualizado
- [ ] Adicionar capturas para novos componentes
- [ ] Criar script de comparação visual automatizada
- [ ] Documentar processo de auditoria
- [ ] Integrar com CI/CD para detectar regressões visuais

**Arquivos Afetados:**
- Modificar: `e2e/captura-telas.spec.ts`
- Criar: Scripts de comparação visual

**Notas:**
- Processo contínuo, não é uma implementação única

**Bloqueios:**
-

---

## 📝 Histórico de Mudanças

### 2026-02-14
- ✅ Criado documento de rastreamento
- ✅ Definido estrutura de acompanhamento
- ✅ Listadas todas as 9 melhorias do plano

---

## 📋 Próximas Ações

### Ações Imediatas (Esta Semana)
1. Decidir ordem de implementação das melhorias de prioridade alta
2. Alocar responsável para UX-001
3. Revisar e validar especificações técnicas

### Ações de Curto Prazo (Próximas 2 Semanas)
1. Implementar UX-001, UX-002 e UX-003 (prioridade alta)
2. Validar com stakeholders
3. Coletar feedback

### Ações de Médio Prazo (Próximo Mês)
1. Implementar UX-004, UX-005 e UX-006 (prioridade média)
2. Iniciar UX-007 (Design Tokens)

### Ações de Longo Prazo (Próximos 3 Meses)
1. Concluir UX-007 e UX-008 (fundação)
2. Estabelecer UX-009 como processo contínuo
3. Segunda rodada de auditoria visual

---

## 📊 Métricas de Qualidade

### Cobertura de Testes
| Tipo | Atual | Meta | Status |
|------|-------|------|--------|
| **Testes Unitários** | - | >85% | ⬜ Pendente |
| **Testes E2E** | - | Cenários críticos | ⬜ Pendente |
| **Capturas de Tela** | 81 | +20 | ⬜ Pendente |

### Acessibilidade (WCAG 2.1 AA)
| Critério | Status |
|----------|--------|
| **Contraste 4.5:1** | ⬜ Não validado |
| **Navegação por teclado** | ⬜ Não validado |
| **Labels em formulários** | ⬜ Não validado |
| **ARIA attributes** | ⬜ Não validado |

### Responsividade
| Breakpoint | Status |
|------------|--------|
| **xs (Mobile)** | ⬜ Não testado |
| **sm (Mobile landscape)** | ⬜ Não testado |
| **md (Tablet)** | ⬜ Não testado |
| **lg (Desktop)** | ⬜ Não testado |
| **xl (Large desktop)** | ⬜ Não testado |
| **xxl (Extra large)** | ⬜ Não testado |

---

## 🔍 Observações e Aprendizados

### Decisões de Design
_Documentar decisões importantes tomadas durante a implementação_

### Problemas Encontrados
_Documentar problemas técnicos ou de UX descobertos durante a implementação_

### Feedback de Usuários
_Documentar feedback recebido de usuários ou stakeholders_

### Melhorias Futuras
_Documentar ideias de melhorias que surgirem durante a implementação_

---

## 📚 Referências

- **Plano de Melhorias:** `ux-improvement-plan.md`
- **Relatório Base:** `ux-improvement-report.md`
- **Capturas de Tela:** `screenshots/`
- **Suite E2E:** `e2e/captura-telas.spec.ts`

---

**Última atualização:** 2026-02-14  
**Próxima revisão:** A cada conclusão de melhoria  
**Responsável pelo tracking:** Equipe de Desenvolvimento
