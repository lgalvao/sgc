# Componentes do Frontend

Este diretório contém os componentes Vue.js reutilizáveis utilizados na aplicação frontend.

## Princípios dos Componentes

- **Reutilizáveis e Agnósticos:** Componentes devem ser projetados para serem reutilizáveis em diferentes partes da
  aplicação.
- **Controlados por Props e Eventos:** A comunicação deve ser feita via `props` e `emits`.
- **Uso de BootstrapVueNext:** A maioria dos componentes utiliza componentes base da biblioteca `bootstrap-vue-next` (
  ex: `BModal`, `BButton`).

## Componentes de Navegação

### MainNavbar
**Responsabilidade:** Menu principal do sistema (topo da aplicação)
- Links para páginas principais (Home, Alertas, Movimentações)
- Links contextuais baseados em perfil do usuário
- Responsivo com toggle para mobile
- Posição: Fixa no topo

### BarraNavegacao
**Responsabilidade:** Breadcrumbs contextuais e navegação hierárquica
- Mostra caminho atual na hierarquia (Processo → Subprocesso → Seção)
- Botão de voltar
- Breadcrumbs dinâmicos baseados na rota atual
- Posição: Abaixo do MainNavbar, dentro do conteúdo

---

## Lista de Componentes

### Modais

- **AceitarMapaModal.vue**: Diálogo para aceitar (Gestor) ou homologar (Admin) um mapa.
- **ConfirmacaoDisponibilizacaoModal.vue**: Confirmação para envio de cadastro para validação.
- **CriarCompetenciaModal.vue**: Criação ou edição de competências e associação com atividades.
- **DisponibilizarMapaModal.vue**: Definição de prazo e disponibilização de mapa.
- **HistoricoAnaliseModal.vue**: Exibe histórico de auditoria/análises de um subprocesso.
- **ImpactoMapaModal.vue**: Comparativo de mudanças (adições/remoções) em revisões de mapa.
- **ImportarAtividadesModal.vue**: Importação de atividades de outros processos.
- **ModalAcaoBloco.vue**: Ações em massa para unidades (aceite/homologação).
- **ModalConfirmacao.vue**: Modal genérico de confirmação.
- **SubprocessoModal.vue**: Alteração de data limite de subprocesso.

### Tabelas e Listas

- **TabelaAlertas.vue**: Listagem de alertas do usuário.
- **TabelaMovimentacoes.vue**: Histórico de movimentações recentes.
- **TabelaProcessos.vue**: Listagem principal de processos.
- **TreeTableView.vue**: Tabela hierárquica genérica.
- **TreeRowItem.vue**: Linha recursiva para `TreeTableView`.

### Árvore de Unidades

- **ArvoreUnidades.vue**: Componente principal de árvore com seleção tri-state.
- **UnidadeTreeNode.vue**: Nó recursivo da árvore.

### Subprocesso e Mapa

- **AtividadeItem.vue**: Exibição e edição de itens de atividade.
- **CompetenciaCard.vue**: Card visual para competências no mapa.
- **SubprocessoCards.vue**: Navegação por cards para seções do subprocesso.
- **SubprocessoHeader.vue**: Cabeçalho com detalhes do subprocesso e status.

### Utilitários

- **EmptyState.vue**: Feedback visual para listas ou estados vazios.
- **ProcessoAcoes.vue**: Botões de ação para detalhes do processo.