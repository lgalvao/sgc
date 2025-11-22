# Componentes do Frontend

Este diretório contém os componentes Vue.js reutilizáveis utilizados na aplicação frontend.

## Princípios dos Componentes

-   **Reutilizáveis e Agnósticos:** Componentes devem ser projetados para serem reutilizáveis em diferentes partes da aplicação.
-   **Controlados por Props e Eventos:** A comunicação deve ser feita via `props` e `emits`.
-   **Uso de BootstrapVueNext:** A maioria dos componentes utiliza componentes base da biblioteca `bootstrap-vue-next` (ex: `BModal`, `BButton`).

---

## Lista de Componentes

### AceitarMapaModal.vue
Diálogo modal utilizado para aceitar (Gestor) ou homologar (Admin) um mapa de competências.
- **Props:** `mostrarModal`, `perfil`.
- **Eventos:** `fecharModal`, `confirmarAceitacao`.

### AcoesEmBlocoModal.vue
Modal para realizar ações em massa (aceitar ou homologar) em registros de unidades. Exibe uma lista de seleção.
- **Props:** `mostrarModal`, `titulo`, `acao`, `unidades`.
- **Eventos:** `fechar`, `confirmar`.

### ArvoreUnidades.vue
Componente de árvore hierárquica para seleção de unidades com checkboxes tri-state.
- **Props:** `unidades`, `modelValue` (v-model), `desabilitadas`.
- **Eventos:** `update:modelValue`.

### BarraNavegacao.vue
Fornece navegação contextual, incluindo botão "Voltar" e breadcrumbs dinâmicos baseados na rota atual.

### CriarCompetenciaModal.vue
Modal para criar ou editar uma competência e associá-la a atividades.
- **Props:** `mostrarModal`, `competenciaParaEditar` (opcional).
- **Eventos:** `fechar`, `salvar`.

### DisponibilizarMapaModal.vue
Modal para definir o prazo de validação e disponibilizar um mapa.
- **Eventos:** `fechar`, `disponibilizar`.

### EditarConhecimentoModal.vue
Modal simples para editar a descrição de um conhecimento.

### HistoricoAnaliseModal.vue
Exibe o histórico de análises (auditoria) de um subprocesso em formato de tabela.
- **Props:** `mostrarModal`, `codSubprocesso`.

### ImpactoMapaModal.vue
Analisa e exibe os impactos (adições/remoções) de uma revisão de mapa comparando com a versão anterior.

### ImportarAtividadesModal.vue
Permite importar atividades de outros processos/unidades finalizados.

### MainNavbar.vue
Barra de navegação superior da aplicação. Gerencia a exibição do usuário logado, seleção de perfil/unidade e logout.

### ModalAcaoBloco.vue
Modal alternativo ou legado para realizar ações em massa (aceitar/homologar) em uma lista de unidades com seleção via checkbox.

### ModalFinalizacao.vue
Modal de confirmação para finalizar um processo.

### ProcessoAcoes.vue
Barra de botões de ação para a tela de detalhes do processo (Aceitar em bloco, Homologar em bloco, Finalizar).

### ProcessoDetalhes.vue
Exibe o cabeçalho com informações resumidas do processo (Descrição, Tipo, Situação).

### SistemaNotificacoesModal.vue
Modal que lista todas as notificações do sistema, permitindo visualização de detalhes e limpeza.

### SistemaNotificacoesToast.vue
Container fixo para exibir notificações flutuantes (Toasts) no canto da tela. Reage à store de notificações.

### SubprocessoCards.vue
Exibe os cards de navegação para as diferentes seções de um subprocesso (Mapa, Atividades, Diagnóstico), adaptando-se ao tipo do processo.

### SubprocessoHeader.vue
Cabeçalho detalhado para as telas de subprocesso, mostrando unidade, responsáveis e status.

### SubprocessoModal.vue
Modal para alterar a data limite de um subprocesso.

### TabelaAlertas.vue
Tabela para listar alertas do usuário no painel principal.

### TabelaMovimentacoes.vue
Tabela para listar as últimas movimentações (histórico) no painel.

### TabelaProcessos.vue
Tabela principal para listagem de processos com suporte a ordenação e seleção.

### TreeRowItem.vue
Componente interno usado pelo `TreeTableView` para renderizar uma linha recursiva da árvore.

### TreeTableView.vue
Tabela hierárquica genérica com suporte a expansão/colapso de linhas.
- **Props:** `data` (estrutura de árvore), `columns`.

### UnidadeTreeItem.vue
Item recursivo para a árvore de seleção de unidades (`ArvoreUnidades`).
