- /login
    - Componente: Login.vue
    - Tela de login (título e senha), permite selecionar o servidor/perfil de uso.

- /painel
    - Componente: Painel.vue
    - Tela inicial (no estilo de 'dashboard') com lista de processos e alertas.

- /processo/:idProcesso
    - Componente: Processo.vue
    - Mostra detalhes do processo e a tabela/árvore de unidades participantes.

- /processo/:idProcesso/:siglaUnidadeUnidade
    - Componente: Subprocesso.vue
    - Mostra o contexto do processo para a unidade (responsável, ações disponíveis etc.).

- /processo/:idProcesso/:siglaUnidadeUnidade/mapa
    - Componente: CadMapa.vue
    - Edição/criação do mapa de competências no contexto do processo

- /processo/:idProcesso/:siglaUnidadeUnidade/cadastro
    - Componente: CadAtividades.vue
    - Cadastro de atividades e conhecimentos para a unidade dentro do processo

- /processo/:idProcesso/:siglaUnidadeUnidade/vis-cadastro
    - Componente: CadAtividades.vue
    - Cadastro de atividades e conhecimentos para a unidade dentro do processo

- /unidade/:siglaUnidadeUnidade
    - Componente: Unidade.vue
    - Detalhes da unidade fora do processo (responsável, mapa vigente e subordinadas, se houver); para ADMIN, dá acesso
      ao cad. de atribuição temporária

- /unidade/:siglaUnidadeUnidade/atribuicao
    - Componente: CadAtribuicao.vue
    - Cadastra e edita atribuição temporárias, permitindo acesso excepcional com perfil CHEFE

- /unidade/:siglaUnidadeUnidade/mapa
    - Componente: CadMapa.vue
    - Edição/criação do mapa de competências da unidade (fora do contexto de um processo específico).

- /relatorios
    - Componente: Relatorios.vue
    - Painel de relatórios.

- /historico
    - Componente: HistoricoProcessos.vue
    - Tabela de processos inativados, com navegação para mapas e detalhes do processo.

- /configuracoes
    - Componente: Configuracoes.vue
    - Edição de configurações do sistema.