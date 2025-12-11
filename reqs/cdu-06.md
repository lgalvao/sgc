# CDU-06 - Detalhar processo

Atores: ADMIN e GESTOR

Pré-condições:

- Usuário ter feito login com os perfis ADMIN ou GESTOR
- Ao menos um processo nas situações 'Em andamento' ou 'Finalizado'.

Fluxo principal:

1. O sistema mostra a tela Detalhes do processo com os dados do processo escolhido.
2. A tela será composta pelas seções Dados do processo e Unidades participantes.

   2.1. Seção `Dados do processo` (sem título):

   2.1.1. Informações da descrição, tipo e da situação dos processos (ver arquivo _situacoes.md).

   2.1.2. Se for perfil ADMIN, exibe o botão Finalizar processo.

   2.2. Seção Unidades participantes:

   2.2.1. Subárvore das unidades hierarquicamente inferiores.

    - Para cada unidade operacional e interoperacional da subárvore são exibidas, em linha, as informações da situação
      do
      subprocesso da unidade e da data limite para a conclusão da etapa atual do processo naquela unidade.
    - O usuário poderá clicar nas unidades operacionais e interoperacionais para visualizar a tela Detalhes do
      subprocesso (ver caso de uso Detalhar subprocesso) com os dados da unidade selecionada.

   ○ Caso o perfil do usuário seja ADMIN, serão exibidos, na seção Dados da unidade da tela, elementos para possibilitar
   a alteração da data limite da etapa atual da unidade assim como da situação atual do subprocesso da unidade (ex.
   Reabertura do cadastro de atividades)

   2.2.2. Caso existam unidades subordinadas cujos subprocessos estejam localizados na unidade do usuário, os seguintes
   botões poderão ser apresentados:

    - Aceitar/Homologar cadastro em bloco, se existirem unidades subordinadas com subprocesso na situação 'Cadastro
      disponibilizado' (processo de mapeamento) ou 'Revisão do cadastro disponibilizada' (processo de revisão)  (ver
      caso de uso Aceitar/homologar cadastro em bloco).
    - Aceitar/Homologar mapa em bloco, se existirem unidades subordinadas com subprocesso nas situações 'Mapa validado'
      ou 'Mapa com sugestões' (ver caso de uso Aceitar/Homologar mapa de competências em bloco).