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

### `common/`
Componentes utilitários de uso geral (ex: `ErrorAlert.vue`, `InlineEditor.vue`).

### `configuracoes/`
Componentes para as telas de configurações do sistema.

### `layout/`
Componentes de estrutura da página (ex: `PageHeader.vue`).

### `mapa/`
Componentes relacionados ao mapa de competências (ex: `CompetenciasListSection.vue`).

### `processo/`
Componentes para exibição e gestão de processos (ex: `ProcessoInfo.vue`).

### `relatorios/`
Modais e cards específicos para a central de relatórios.

### `ui/`
Componentes de interface atômicos (ex: `LoadingButton.vue`).

### `unidade/`
Componentes para visualização de dados de unidades.

## Componentes Globais (Raiz)

### Navegação
- **MainNavbar.vue**: Menu superior principal.
- **BarraNavegacao.vue**: Breadcrumbs e navegação de nível secundário.

### Modais e Diálogos
- **ModalConfirmacao.vue**: Diálogo genérico para ações destrutivas ou importantes.
- **HistoricoAnaliseModal.vue**: Exibe o log de auditoria de um subprocesso.
- **ImportarAtividadesModal.vue**: Facilita a cópia de atividades entre processos.

### Tabelas e Árvores
- **TabelaProcessos.vue**: Listagem principal com filtros e paginação.
- **ArvoreUnidades.vue**: Hierarquia organizacional com seleção.
- **TreeTableView.vue**: Componente base para visualização de dados hierárquicos.

### Feedback
- **EmptyState.vue**: Exibido quando não há dados para mostrar.
