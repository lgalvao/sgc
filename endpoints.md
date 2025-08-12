# Endpoints e Navegação

Abaixo está a lista de endpoints da aplicação com a descrição do propósito de cada rota, componentes envolvidos e observações de navegação/breadcrumbs.

## Autenticação e Painel

- /login
  - Tela de login. Permite selecionar o servidor/perfil de uso.
  - Breadcrumbs: ocultos. Botão Voltar: oculto.

- /painel
  - Tela inicial (dashboard) com lista de processos e alertas.
  - Breadcrumbs: ocultos. Botão Voltar: fallback de destino quando não há histórico.

## Processo e Unidades no contexto do processo

- /processo/:idProcesso
  - Componente: Processo.vue
  - Mostra detalhes do processo e a tabela/árvore de unidades participantes.
  - Breadcrumbs: (home) > Processo

- /processo/:idProcesso/:siglaUnidade
  - Componente: ProcessoUnidade.vue
  - Mostra o contexto do processo para a unidade (responsável, cards de ação etc.).
  - Breadcrumbs: (home) > Processo > SIGLA

- /processo/:idProcesso/:siglaUnidade/mapa
  - Componente: CadMapa.vue (edição/criação do mapa de competências no contexto do processo)
  - Breadcrumbs: (home) > Processo > SIGLA > Mapa

- /processo/:idProcesso/:siglaUnidade/cadastro
  - Componente: CadAtividades.vue (cadastro de atividades e conhecimentos para a unidade dentro do processo)
  - Breadcrumbs: (home) > Processo > SIGLA > Cadastro

## Unidades (fora do contexto de processo)

- /unidade/:siglaUnidade
  - Componente: Unidade.vue
  - Mostra detalhes da unidade (responsável, mapa vigente e árvore de subordinadas).
  - Breadcrumbs: (home) > SIGLA

- /unidade/:siglaUnidade/atribuicao
  - Componente: CadAtribuicao.vue (cadastro de atribuição temporária)
  - Breadcrumbs: (home) > SIGLA > Atribuição

- /unidade/:siglaUnidade/mapa
  - Componente: Visualização do mapa vigente da unidade (quando aplicável).
  - Breadcrumbs: (home) > SIGLA > Mapa

## Outras

- /relatorios
  - Componente: Relatorios.vue
  - Breadcrumbs: (home) > Relatórios

- /historico
  - Componente: HistoricoProcessos.vue
  - Breadcrumbs: (home) > Histórico

- /configuracoes
  - Componente: Configuracoes.vue
  - Breadcrumbs: (home) > Configurações

## Notas de Navegação

- Breadcrumbs globais seguem o padrão: (home) > Processo > SIGLA > Página, quando aplicável.
- O último breadcrumb nunca é link.
- Ao navegar via navbar, a query `fromNavbar` é utilizada para ocultar breadcrumbs e botão Voltar.
- O botão Voltar global retorna no histórico quando possível; se não houver histórico ou se o retorno for inválido, redireciona para o Painel.

Resumo rápido:

/login
/painel

/processo/:idProcesso
/processo/:idProcesso/:siglaUnidade
/processo/:idProcesso/:siglaUnidade/mapa
/processo/:idProcesso/:siglaUnidade/cadastro

/unidade/:siglaUnidade
/unidade/:siglaUnidade/atribuicao
/unidade/:siglaUnidade/mapa

/relatorios
/historico
/configuracoes