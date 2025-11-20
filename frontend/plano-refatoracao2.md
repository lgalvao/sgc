# Plano de Refatoração e Integração Frontend - Fase 2

Este documento detalha o estado atual do frontend do SGC e define o plano de ação para finalizar a integração com o backend, remover lógica duplicada/legada e completar a migração para `BootstrapVueNext`.

## 1. Análise do Estado Atual

O frontend encontra-se em um estágio híbrido. A estrutura base e a maioria dos serviços já se comunicam com o backend, mas restam artefatos da fase de prototipagem e implementações manuais de componentes de UI que deveriam usar a biblioteca adotada (`BootstrapVueNext`).

### 1.1. Pontos Críticos Identificados

1.  **Lógica de Prototipagem (Dead Code):**
    *   A store `processos.ts` mantém um array `movements` e uma action `addMovement` que geram IDs localmente (`generateUniqueId`). Isso é código morto, pois a visualização (`TabelaMovimentacoes`) consome dados vindos de `SubprocessoDetalhe`, que são populados corretamente pelo backend.
    *   O utilitário `frontend/src/utils/index.ts` contém `generateUniqueId`, que é perigoso se usado para dados persistentes e desnecessário se o backend gera os IDs.

2.  **Performance e Integração:**
    *   **Carregamento de Usuários:** A `usuariosStore` possui uma action `fetchUsuarios` que chama `GET /usuarios`. No `PainelView.vue`, isso é chamado no `onMounted`. Se o sistema tiver milhares de usuários, isso travará o frontend. O painel deve depender apenas dos dados retornados pelos endpoints de painel (`listarProcessos`, `listarAlertas`), que já devem trazer as informações necessárias (ou IDs para busca pontual).
    *   **Login/Segurança:** O fluxo de login (`LoginView.vue`) realiza três chamadas (`autenticar` -> `autorizar` -> `entrar`). Embora funcional para o protótipo, a chamada `autorizar` apenas com `tituloEleitoral` expõe dados de perfil sem validar a senha novamente (assumindo estado stateless). *Ação:* O frontend deve seguir estritamente o protocolo atual, mas deve-se validar se o token JWT retornado no passo `entrar` está sendo usado corretamente em todas as chamadas subsequentes (via `axios-setup.ts`).

3.  **Interface de Usuário (BootstrapVueNext):**
    *   A adoção está parcial. Componentes como `CadMapa.vue` utilizam modais (`<div class="modal">`) e tooltips manuais (`new Tooltip(...)`), ignorando os componentes `BModal` e `BTooltip`/diretivas do framework.
    *   Isso gera código verboso, difícil de manter e propenso a erros de ciclo de vida (ex: listeners não removidos).

## 2. Plano de Ação

O plano deve ser executado sequencialmente.

### Passo 1: Limpeza de Código Morto e Lógica de Protótipo

**Objetivo:** Remover a geração local de dados e confiar na "Verdade Única" do backend.

1.  **Refatorar `frontend/src/stores/processos.ts`:**
    *   Remover o state `movements`.
    *   Remover a action `addMovement`.
    *   Verificar e remover qualquer uso de `generateUniqueId` nesta store.
2.  **Limpar `frontend/src/utils/index.ts`:**
    *   Remover a função `generateUniqueId`.
    *   Verificar se algum teste unitário depende disso e ajustar (mockar IDs fixos nos testes).

### Passo 2: Otimização do Carregamento de Dados

**Objetivo:** Impedir o carregamento massivo de dados desnecessários.

1.  **Refatorar `frontend/src/views/PainelView.vue`:**
    *   Remover a chamada `await usuariosStore.fetchUsuarios()` do `onMounted`.
    *   Remover a chamada `await unidadesStore.fetchUnidades()` se não for estritamente necessária para filtros locais. Se for para filtros, deve-se usar um endpoint de "lookup" ou "combo" que retorne apenas id/nome, não a entidade completa.
2.  **Verificar `TabelaProcessos` e `TabelaAlertas`:**
    *   Garantir que esses componentes exibam os nomes de usuários/unidades que já vêm nos DTOs (`ProcessoResumo`, `Alerta`) do backend, sem tentar fazer "join" no frontend com a store de usuários completa.

### Passo 3: Migração para BootstrapVueNext (Foco em `CadMapa.vue`)

**Objetivo:** Eliminar manipulação manual de DOM e usar componentes Vue reativos.

1.  **Refatorar Modais em `CadMapa.vue`:**
    *   Substituir as `div` com classes `.modal` por `<BModal>`.
    *   Usar `v-model` para controlar a visibilidade (`mostrarModalCriarNovaCompetencia`, etc.).
    *   Remover classes como `modal-dialog`, `modal-content` que são gerenciadas automaticamente pelo componente.
2.  **Refatorar Tooltips em `CadMapa.vue`:**
    *   Remover a importação manual de `bootstrap` (`import('bootstrap').then(...)`).
    *   Remover os hooks `onMounted` que inicializam `new Tooltip()`.
    *   Substituir os atributos `data-bs-toggle="tooltip"` pela diretiva `v-b-tooltip` (se disponível) ou usar o componente `<BTooltip>` envolvendo o elemento alvo.
3.  **Padronizar Botões e Inputs:**
    *   Substituir `<button class="btn ...">` por `<BButton variant="...">`.
    *   Garantir que todos os inputs usem `<BFormInput>`, `<BFormTextarea>`, `<BFormCheckbox>`.

### Passo 4: Revisão de Tipagem e Serviços

1.  **Correção em `painelService.ts`:**
    *   Verificar o parâmetro `usuarioTitulo`. Se a API espera um número (`Long`), o frontend deve converter ou tipar como `number`. Ajustar a chamada na `PainelView` para passar o tipo correto.

### Passo 5: Verificação Final

1.  Executar `npm run typecheck` para garantir que as refatorações não quebraram contratos de tipos.
2.  Executar testes unitários afetados (`CadMapa.spec.ts` provavelmente falhará e precisará ser atualizado para testar os novos componentes BModal/BButton em vez de classes CSS puras).

## 3. Status da Execução

### Concluído em 2024-01-28
- [x] **Passo 1:** Removido código morto (`movements`, `addMovement`, `generateUniqueId`) em `processos.ts`, `utils/index.ts` e views (`DiagnosticoEquipe.vue`, `OcupacoesCriticas.vue`).
- [x] **Passo 2:** Otimizado `PainelView.vue` removendo fetchs desnecessários de usuários e unidades.
- [x] **Passo 3:** Refatorado `CadMapa.vue` para usar componentes `BootstrapVueNext` (`BModal`, `BButton`, `v-b-tooltip`) e removida dependência direta do JS do Bootstrap.
- [x] **Passo 4:** Corrigida tipagem em `painelService.ts` (`usuarioTitulo` para `number`) e ajustado `alertasStore`.
- [x] **Passo 5:** Verificação final realizada com `npm run typecheck`, `npm run lint` e `npm run test:unit`. Todos os testes passaram.

### Próximos Passos Sugeridos (Fora deste plano)
- Implementar backend para `DiagnosticoEquipe` e `OcupacoesCriticas` (atualmente com TODOs).
- Continuar migração de outros componentes para `BootstrapVueNext`.
