# Componentes do Frontend

Este diretório contém os componentes Vue.js reutilizáveis, organizados por funcionalidade (feature-based).

## Princípios dos Componentes

- **Reutilização:** Componentes devem ser agnósticos ao contexto quando possível.
- **Props/Emits:** Comunicação estrita via propriedades e eventos.
- **BootstrapVueNext:** Baseado no Bootstrap 5 através da biblioteca `bootstrap-vue-next`.
- **Acessibilidade:** Devem seguir as diretrizes de acessibilidade (WAI-ARIA).

## Organização por Pastas

### `atividades/`

Componentes específicos para gestão de atividades (ex: `CadAtividadeForm.vue`).

### `comum/`

Componentes utilitários e compartilhados (ex: `ErrorAlert.vue`, `InlineEditor.vue`, `LoadingButton.vue`,
`EmptyState.vue`, `ModalConfirmacao.vue`).

### `configuracoes/`

Componentes para as telas de configurações do sistema.

### `layout/`

Componentes de estrutura e navegação da página (ex: `LayoutPadrao.vue`, `PageHeader.vue`, `MainNavbar.vue`,
`BarraNavegacao.vue`).

### `mapa/`

Componentes relacionados ao mapa de competências (ex: `CompetenciasListSection.vue`).

### `processo/`

Componentes para exibição e gestão de processos (ex: `ProcessoInfo.vue`).

### `relatorios/`

Modais e cards específicos para a central de relatórios.

### `unidade/`

Componentes para visualização de dados de unidades.
