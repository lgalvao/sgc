# Plano e Tracking Consolidado — Backlog Remanescente

Este documento foca exclusivamente nas diretrizes operacionais e no backlog pendente após a estabilização de QA (100% testes verdes).

---

## 1. Diretrizes de Qualidade e Arquitetura

Para todas as próximas tarefas, manter os seguintes guardrails:
- **Idioma:** Todo o código e documentação em Português Brasileiro.
- **Identificadores:** Usar `codigo` em vez de `id`.
- **Logic Location:** Views devem ser "burras", delegando orquestração para composables e dados para stores.
- **Error Handling:** Erros de backend devem falhar rápido e serem tratados pelo `useErrorHandler` / `useNotification`.
- **Testes:** Toda refatoração deve manter a suite unitária verde e ser validada pelo Dashboard de QA.

---

## 2. Backlog Priorizado (O que falta)

### P1 — Desacoplamento de Views e Services (Foco Atual)
| Item | Descrição | Objetivo |
|---|---|---|
| **P1.1** | Extração de Bootstrap: `CadastroView.vue` | Mover `carregarDados` e orquestração inicial para um composable ou store. |
| **P1.2** | Extração de Bootstrap: `MapaView.vue` | Isolar lógica de carga de contexto e tratamento de erros de inicialização. |
| **P1.3** | Quebra do `ProcessoService` | Isolar responsabilidades de Workflow, Consulta e Validação. |
| **P1.4** | Simplificação do `CadastroFluxoService` | Eliminar duplicações remanescentes entre fluxos de Mapeamento e Revisão. |

### P2 — Dívida Técnica e Sustentabilidade
| Item | Descrição | Objetivo |
|---|---|---|
| **P2.1** | Redução de `any` em Testes | Limpar tipos genéricos em helpers compartilhados de Vitest e Playwright. |
| **P2.2** | Nulabilidade em DTOs | Ajustar DTOs estáveis para usar tipos não-nulos onde o contrato é garantido. |
| **P2.3** | Cobertura Frontend | Atingir o threshold de >95% (atualmente em 90.04%) para esverdear o dashboard. |

---

## 3. Próxima Ação Imediata

**Extração do Bootstrap de `CadastroView.vue`**
- Criar `useCadastroOrquestracao.ts` (nome sugerido).
- Mover lógica de carregamento inicial, tratamento de erro de integridade e sincronização de snapshots.
- Reduzir `CadastroView.vue` de ~860 linhas para <500 linhas.

---

## 4. Histórico Recente (Status: Concluído)
- [x] Estabilização de 100% dos testes unitários (Backend e Frontend).
- [x] Resolução de conflitos Prop vs Ref (`codigoSubprocesso`).
- [x] Padronização de feedback em `useFluxoSubprocesso`.
- [x] Auditoria de recargas redundantes e invalidação de cache.
