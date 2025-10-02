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

- /processo/:idProcesso/:siglaUnidade/diagnostico-equipe
    - Componente: DiagnosticoEquipe.vue
    - Tela para o diagnóstico de competências da equipe.

- /processo/:idProcesso/:siglaUnidade/ocupacoes-criticas
    - Componente: OcupacoesCriticas.vue
    - Tela para identificar ocupações críticas.

- /unidade/:siglaUnidade
    - Componente: Unidade.vue
    - Detalhes da unidade fora do processo (responsável, mapa vigente e unidades subordinadas, se houver); para ADMIN, dá acesso ao cad. de atribuição temporária

- /unidade/:siglaUnidade/mapa
    - Componente: CadMapa.vue
    - Edição/criação do mapa de competências no contexto da unidade.

- /unidade/:siglaUnidade/atribuicao
    - Componente: CadAtribuicao.vue
    - Cadastra e edita atribuição temporárias, permitindo acesso excepcional com perfil CHEFE

- /relatorios
    - Componente: Relatorios.vue
    - Painel de relatórios.

- /historico
    - Componente: Historico.vue
    - Tabela de processos inativados, com navegação para mapas e detalhes do processo.

- /configuracoes
    - Componente: Configuracoes.vue
    - Edição de configurações do sistema.
