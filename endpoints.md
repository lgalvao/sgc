- /login
  - Componente: Login.vue
  - Tela de login (título e senha), permite selecionar o servidor/perfil de uso.

- /painel
  - Componente: Painel.vue 
  - Tela inicial (no estilo de 'dashboard') com lista de processos e alertas.

- /processo/:idProcesso
  - Componente: Processo.vue
  - Mostra detalhes do processo e a tabela/árvore de unidades participantes.

- /processo/:idProcesso/:siglaUnidade
  - Componente: ProcessoUnidade.vue
  - Mostra o contexto do processo para a unidade (responsável, ações disponíveis etc.).

- /processo/:idProcesso/:siglaUnidade/mapa
  - Componente: CadMapa.vue 
  - Edição/criação do mapa de competências no contexto do processo
    
- /processo/:idProcesso/:siglaUnidade/cadastro
  - Componente: CadAtividades.vue 
  - Cadastro de atividades e conhecimentos para a unidade dentro do processo

- /unidade/:siglaUnidade
  - Componente: Unidade.vue
  - Detalhes da unidade fora do processo (responsável, mapa vigente e subordinadas, se houver); para ADMIN, dá acesso ao cad. de atribuição temporária

- /unidade/:siglaUnidade/atribuicao
  - Componente: CadAtribuicao.vue (cadastro de atribuição temporária)
  - Cadastra e edita atribuição temporária de servidores a unidades.

- /unidade/:siglaUnidade/mapa
  - Componente: (ASD) 
  - Visualização do mapa vigente da unidade (quando aplicável).

- /relatorios
  - Componente: Relatorios.vue
  - Painel de relatórios. 

- /historico
  - Componente: HistoricoProcessos.vue
  - Tabela de processos inativados.

- /configuracoes
  - Componente: Configuracoes.vue
  - Edição de configurações do sistema.