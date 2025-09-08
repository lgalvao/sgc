- /login
    - Componente: Login.vue
    - Tela de login (título e senha), permite selecionar o servidor/perfil de uso.

- /painel
    - Componente: Painel.vue
    - Tela inicial (no estilo de 'dashboard') com lista de processos e alertas.

- /processo/cadastro
    - Componente: CadProcesso.vue
    - Tela para cadastro de novos processos.

- /processo/:idProcesso
    - Componente: Processo.vue
    - Mostra detalhes do processo e a tabela/árvore de unidades participantes.

- /processo/:idProcesso/:siglaUnidade
    - Componente: Subprocesso.vue
    - Mostra o contexto do processo para a unidade (responsável, ações disponíveis etc.).

- /processo/:idProcesso/:siglaUnidade/mapa
    - Componente: CadMapa.vue
    - Edição/criação do mapa de competências no contexto do processo

- /processo/:idProcesso/:siglaUnidade/vis-mapa
    - Componente: VisMapa.vue
    - Visualização do mapa de competências no contexto do processo

- /processo/:idProcesso/:siglaUnidade/cadastro
    - Componente: CadAtividades.vue
    - Cadastro de atividades e conhecimentos para a unidade dentro do processo

- /processo/:idProcesso/:siglaUnidade/vis-cadastro
    - Componente: VisAtividades.vue
    - Visualização de atividades e conhecimentos para a unidade dentro do processo

- /unidade/:siglaUnidade
    - Componente: Unidade.vue
    - Detalhes da unidade fora do processo (responsável, mapa vigente e subordinadas, se houver); para ADMIN, dá acesso
      ao cad. de atribuição temporária

- /unidade/:siglaUnidade/atribuicao
    - Componente: CadAtribuicao.vue
    - Cadastra e edita atribuição temporárias, permitindo acesso excepcional com perfil CHEFE

// TODO esse endpoint esta estranho!
- /unidade/:siglaUnidade/mapa
    - Componente: CadMapa.vue
    - Edição/criação do mapa de competências da unidade (fora do contexto de um processo específico).

- /relatorios
    - Componente: Relatorios.vue
    - Painel de relatórios.

- /historico
    - Componente: Historico.vue
    - Tabela de processos inativados, com navegação para mapas e detalhes do processo.

- /configuracoes
    - Componente: Configuracoes.vue
    - Edição de configurações do sistema.
