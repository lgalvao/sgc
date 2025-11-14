# Componentes do Frontend

Este documento fornece uma visão geral dos componentes Vue.js reutilizáveis desenvolvidos para o projeto SGC.

## Princípios dos Componentes

Para manter a arquitetura do projeto limpa e manutenível, os componentes neste diretório devem seguir os seguintes princípios:

-   **Reutilizáveis e Agnósticos:** Componentes devem ser projetados para serem reutilizáveis em diferentes partes da aplicação. Eles não devem ter conhecimento do contexto específico da página (view) em que estão inseridos.
-   **Controlados por Props e Eventos:** A comunicação com um componente deve ser feita primariamente através de `props` (para passar dados para dentro) e `emits` (para comunicar eventos para fora).
-   **Evitar Lógica de Negócio:** Componentes de UI não devem conter lógica de negócio complexa, chamadas de API (serviços) ou manipulação direta do estado global (stores). Essa responsabilidade pertence às `views` e `stores`.

---

Este diretório contém os componentes Vue.js reutilizáveis utilizados na aplicação frontend. Abaixo, uma descrição de cada componente:

Este diretório contém os componentes Vue.js reutilizáveis utilizados na aplicação frontend. Abaixo, uma descrição de cada componente:

---

## AceitarMapaModal.vue

Este componente é um diálogo modal utilizado para aceitar ou homologar um mapa de competências. Ele utiliza o componente `BaseModal` para sua estrutura. O título e o corpo do modal mudam dinamicamente com base na propriedade `perfil`. Se o `perfil` for 'ADMIN', ele é usado para "Homologação" e pede confirmação. Caso contrário, é para "Aceitar Mapa de Competências" e permite ao usuário adicionar observações opcionais em uma área de texto. Ele emite `fecharModal` para fechar o modal e `confirmarAceitacao` com o texto da observação quando o botão de aceitar é clicado.

---

## AcoesEmBlocoModal.vue

Este componente é um diálogo modal utilizado para realizar ações em massa (aceitar ou homologar) em registros de unidades. Ele exibe uma tabela de unidades disponíveis, permitindo ao usuário selecionar múltiplas unidades através de caixas de seleção. O título do modal e o texto do botão de ação mudam dinamicamente com base na propriedade `tipoAcao` ('aceitar' ou 'homologar'). Ele emite `fechar` para fechar o modal e `confirmar` com um array das siglas das unidades selecionadas quando o botão de confirmação é clicado. Uma validação garante que pelo menos uma unidade seja selecionada antes de confirmar.

---

## ArvoreUnidades.vue

Este componente implementa uma árvore hierárquica de seleção de unidades com suporte a checkboxes com três estados (marcado, desmarcado, indeterminado). Ele é totalmente controlado via `v-model` e permite filtragem customizada de unidades através de uma função. A seleção é hierárquica: marcar uma unidade marca todas suas filhas automaticamente, e desmarcar remove todas as filhas. Unidades do tipo `INTEROPERACIONAL` podem permanecer marcadas mesmo quando nem todas suas filhas estão selecionadas, diferentemente de unidades `INTERMEDIARIA` que ficam em estado indeterminado nessa situação. O componente aceita as propriedades: `unidades` (array hierárquico de unidades), `modelValue` (array de códigos selecionados para v-model), `desabilitadas` (array de códigos de unidades desabilitadas) e `filtrarPor` (função para filtrar quais unidades são elegíveis). Emite `update:modelValue` quando a seleção muda. A hierarquia é exibida com indentação visual usando classes Bootstrap.

---

## BarraNavegacao.vue

Este componente fornece elementos de navegação, incluindo um botão "Voltar" e breadcrumbs dinâmicos. O botão "Voltar" é exibido em todas as páginas, exceto '/login' e '/painel', e aciona a função `router.back()`. Os breadcrumbs também são ocultados nas páginas '/login' e '/painel'. Os breadcrumbs são gerados dinamicamente com base na rota atual, incluindo parâmetros como `codProcesso` e `siglaUnidade`, e o `perfilSelecionado` do usuário. Ele também considera metadados `breadcrumb` definidos na configuração da rota para exibir o rótulo da página atual.

---

## BaseModal.vue

Este é um componente base reutilizável para a criação de diálogos modais. Ele fornece uma estrutura personalizável para modais, incluindo um cabeçalho com título e um ícone opcional, um corpo para conteúdo (através de um slot `conteudo`) e um rodapé opcional para ações (através de um slot `acoes`). A aparência do modal pode ser configurada através de propriedades como `mostrar` (para controlar a visibilidade), `titulo`, `tipo` (para estilizar o cabeçalho com diferentes cores do Bootstrap), `tamanho` (para o tamanho do modal), `centralizado` (para centralizar o modal) e `icone`. Ele emite um evento `fechar` quando o botão de fechar é clicado.

---

## CriarCompetenciaModal.vue

Este componente é um diálogo modal utilizado para criar ou editar uma competência. Ele permite ao usuário inserir uma descrição para a competência e associá-la a uma lista de atividades. Cada atividade pode ter conhecimentos associados, que são exibidos em um tooltip. O título do modal muda com base na existência da propriedade `competenciaParaEditar`. Ele emite `fechar` para fechar o modal e `salvar` com os detalhes da competência nova ou atualizada (descrição e IDs das atividades selecionadas) quando o botão de salvar é clicado. O botão de salvar é desabilitado se nenhuma descrição for fornecida ou nenhuma atividade for selecionada.

---

## DisponibilizarMapaModal.vue

Este componente é um diálogo modal, construído usando `BaseModal`, que permite aos usuários definir um prazo de validação para um mapa de competências antes de disponibilizá-lo. Ele fornece um campo de entrada para selecionar uma data e pode exibir uma mensagem de notificação opcional. O botão "Disponibilizar" é habilitado apenas quando uma data é selecionada. Ele emite `fechar` para fechar o modal e `disponibilizar` com a data selecionada quando o botão "Disponibilizar" é clicado.

---

## EditarConhecimentoModal.vue

Este componente é um diálogo modal projetado para editar a descrição de um item de conhecimento. Ele exibe uma área de texto preenchida com a descrição atual do item de conhecimento passado através da propriedade `conhecimento`. O botão "Salvar" é habilitado apenas quando a descrição editada não está vazia. Ele emite `fechar` para fechar o modal e `salvar` com o ID do item de conhecimento e a nova descrição quando o botão de salvar é clicado ou `Ctrl+Enter` é pressionado na área de texto.

---

## HistoricoAnaliseModal.vue

Este componente é um diálogo modal que exibe um histórico de análises para um determinado subprocesso. Ele busca dados de análise do `useAnalisesStore` com base na propriedade `codSubrocesso`. Se nenhuma análise for encontrada, ele exibe uma mensagem indicando isso. Caso contrário, ele apresenta as análises em uma tabela listrada com colunas para "Data/Hora", "Unidade", "Resultado" e "Observação". As datas são formatadas usando `date-fns`. Ele emite um evento `fechar` quando o botão de fechar é clicado.

---

## ImpactoMapaModal.vue

Este componente é um diálogo modal que exibe o impacto das alterações em um mapa de competências. Ele mostra uma lista de atividades inseridas e seus conhecimentos associados, bem como uma lista de competências impactadas. As competências impactadas são determinadas por alterações (adições, remoções ou modificações) em atividades e conhecimentos, seja explicitamente marcadas ou implicitamente através de associações de atividades. Ele usa várias stores (`useUnidadesStore`, `useProcessosStore`, `useMapasStore`, `useRevisaoStore`) para buscar dados relevantes. Durante o carregamento, ele exibe um spinner. Ele emite um evento `fechar` quando o botão de fechar é clicado.

---

## ImportarAtividadesModal.vue

Este componente é um diálogo modal que facilita a importação de atividades de um processo de mapeamento ou revisão finalizado de outra unidade. Os usuários podem selecionar um processo de origem e uma unidade dentro desse processo. O modal então exibe uma lista de atividades da unidade de origem selecionada, permitindo ao usuário escolher quais atividades importar. O botão "Importar" é habilitado apenas quando pelo menos uma atividade é selecionada. Ele usa `useProcessosStore` e `useAtividadesStore` para gerenciar dados de processo e atividade. Ele emite `fechar` para fechar o modal e `importar` com as atividades selecionadas quando o botão de importação é clicado.

---

## Navbar.vue

Este componente implementa a barra de navegação principal da aplicação. Ele inclui links para várias seções como "Painel", "Minha unidade", "Relatórios" e "Histórico". Ele também apresenta um mecanismo dinâmico de seleção de perfil, permitindo aos usuários alternar entre diferentes perfis (por exemplo, ADMIN, GESTOR, SERVIDOR) associados à sua conta e unidade logadas. O perfil e a unidade selecionados são exibidos, e clicar neles revela um menu suspenso para alterar o perfil ativo. Um link "Configurações do sistema" é visível apenas para usuários ADMIN, e um link "Sair" está sempre presente para logout. As ações de navegação são tratadas por `navigateFromNavbar`, que define um item de armazenamento de sessão antes de rotear.

---

## NotificacaoContainer.vue

Este componente é responsável por exibir notificações do sistema em uma posição fixa na tela, geralmente no canto superior direito. Ele usa `TransitionGroup` para entrada e saída animadas de notificações. As notificações são buscadas do `useNotificacoesStore` e podem ser de vários tipos (sucesso, erro, aviso, informação, e-mail), cada uma com um estilo visual e ícone distintos. Para notificações do tipo 'e-mail', um botão é fornecido para abrir um modal exibindo o conteúdo completo do e-mail. Os usuários podem dispensar notificações individuais.

---

## SistemaNotificacoesModal.vue

Este componente é um diálogo modal que serve como uma exibição centralizada para todas as notificações do sistema. Ele recupera as notificações do `useNotificacoesStore` e as apresenta em uma lista, ordenadas por carimbo de data/hora (as mais recentes primeiro). Cada notificação mostra seu tipo (com um ícone), título, mensagem e carimbo de data/hora. Os usuários podem remover notificações individuais ou limpar todas elas. Para notificações do tipo e-mail, um botão permite visualizar o conteúdo completo do e-mail em um modal aninhado separado. Ele emite um evento `fecharModal` quando o modal é fechado.

---

## SubprocessoCards.vue

Este componente exibe um conjunto de cartões acionáveis relacionados a um subprocesso, com o conteúdo variando com base na propriedade `tipoProcesso`.

*   **Para tipos de processo `MAPEAMENTO` ou `REVISAO`:**
    *   Ele mostra um cartão para "Atividades e conhecimentos", que navega para a seção de registro de atividades.
    *   Ele mostra um cartão para "Mapa de Competências", que navega para o mapa de competências. Este cartão pode ser desabilitado se nenhuma propriedade `mapa` for fornecida, e seu distintivo de status (`situacao`) é atualizado dinamicamente com base no estado do mapa (por exemplo, "Em andamento", "Disponibilizado").

*   **Para o tipo de processo `DIAGNOSTICO`:**
    *   Ele mostra um cartão para "Diagnóstico da Equipe", que navega para a seção de diagnóstico da equipe.
    *   Ele mostra um cartão para "Ocupações Críticas", que navega para a seção de identificação de ocupações críticas.

Todos os cartões são acionáveis e emitem eventos específicos (`irParaAtividades`, `navegarParaMapa`, `irParaDiagnosticoEquipe`, `irParaOcupacoesCriticas`) quando clicados.

---

## SubprocessoHeader.vue

Este componente exibe um cartão de cabeçalho com informações detalhadas sobre um subprocesso. Ele mostra a descrição do processo, a sigla e o nome da unidade, a situação atual do subprocesso (com um distintivo colorido) e informações de contato para o titular e, opcionalmente, para a pessoa responsável (se `responsavelNome` for fornecido). Ele também exibe a `unidadeAtual` se disponível. Um botão "Alterar data limite" é renderizado condicionalmente e visível apenas para usuários ADMIN quando o subprocesso está em andamento, emitindo um evento `alterarDataLimite` quando clicado.

---

## SubprocessoModal.vue

Este componente é um diálogo modal especificamente projetado para alterar o prazo de um subprocesso. Ele exibe o prazo atual e fornece um campo de entrada para selecionar uma nova data. A nova data deve ser válida e futura. Ele também mostra informações sobre o estágio atual (`etapaAtual`) e seu status (`situacaoEtapaAtual`). O botão "Confirmar" é habilitado apenas quando uma nova data válida é selecionada. Ele emite `fecharModal` para fechar o modal e `confirmarAlteracao` com a nova data quando o botão de confirmação é clicado.

---

## TabelaProcessos.vue

Este componente exibe uma tabela classificável de processos. Ele recebe um array de `processos` como propriedade, cada um com informações de unidade formatadas e uma data de finalização formatada opcional. Os cabeçalhos da tabela ("Descrição", "Tipo", "Unidades participantes", "Situação" e, opcionalmente, "Finalizado em") são clicáveis e emitem um evento `ordenar` com a chave da coluna clicada, permitindo a funcionalidade de classificação. Um indicador de seta ao lado do cabeçalho da coluna mostra a direção de classificação atual. Clicar em uma linha da tabela emite um evento `selecionarProcesso` com o objeto de processo selecionado. A coluna "Finalizado em" é exibida apenas se a propriedade `showDataFinalizacao` for verdadeira.

---

## TreeRow.vue

Este componente representa uma única linha em uma estrutura de tabela em árvore. Ele exibe dados com base nas propriedades `item` e `columns`. A propriedade `level` é usada para aplicar preenchimento esquerdo apropriado, indicando visualmente a profundidade do item na árvore. Se um `item` tiver filhos, um ícone de alternância (chevron-down ou chevron-right) é exibido para expandir ou recolher seus subitens, emitindo um evento `toggle` com o ID do item quando clicado. Clicar na própria linha emite um evento `row-click` com os dados do `item`, a menos que o `item` seja explicitamente marcado como `clickable: false`.

---

## TreeTable.vue

Este componente renderiza uma tabela de dados hierárquica, permitindo linhas expansíveis e recolhíveis. Ele recebe `data` (um array de objetos `TreeItem`, potencialmente aninhados) e `columns` (um array que define as colunas da tabela) como propriedades. Ele pode opcionalmente exibir um `title` e ocultar os cabeçalhos da tabela (`hideHeaders`). O componente fornece botões "Expandir Tudo" e "Recolher Tudo" para gerenciar a visibilidade dos itens aninhados. Ele usa o componente `TreeRow` para renderizar linhas individuais, passando os dados do item, o nível e as definições das colunas. Ele emite um evento `row-click` quando uma linha é clicada.

---

## UnidadeTreeItem.vue

Este componente é um componente recursivo usado para exibir uma lista hierárquica de unidades com caixas de seleção. Cada `UnidadeTreeItem` representa uma única unidade e pode ter componentes `UnidadeTreeItem` aninhados para suas unidades filhas (`filhas`). Ele exibe a `sigla` (acrônimo) e o `nome` da unidade. Uma caixa de seleção permite aos usuários selecionar ou desmarcar unidades, e ela suporta um estado indeterminado para unidades pai onde alguns, mas não todos os filhos, estão selecionados. Ele recebe `unidade`, `isChecked`, `toggleUnidade` e `isIndeterminate` como propriedades para gerenciar seu estado e interações.
