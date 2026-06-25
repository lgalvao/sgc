# CDU-06 - Detalhar processo

Atores: ADMIN e GESTOR

## Pré-condições

- Usuário ter feito login com os perfis ADMIN ou GESTOR
- Ao menos um processo na situação 'Em andamento'.

## Fluxo principal

1. No `Painel`, o usuáio aciona um processo na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do processo` com os dados do processo escolhido.

3. A tela será composta por duas seções principais:  e `Unidades participantes`.

   3.1. Seção `Dados do processo` (sem título):
    - Descrição, tipo e da situação do processo;
    - Para perfil ADMIN, botão `Finalizar processo`.

   3.2. Seção `Unidades participantes`:
    - Subárvore das unidades hierarquicamente inferiores.
        - Para cada unidade operacional e interoperacional da subárvore são exibidas, em linha, as informações da
          situação do subprocesso da unidade e da data limite para a conclusão da etapa atual do processo naquela
          unidade.
        - O usuário poderá clicar nas unidades operacionais e interoperacionais para visualizar a tela Detalhes do
          subprocesso (ver caso de uso Detalhar subprocesso) com os dados da unidade selecionada. Caso o perfil do
          usuário seja ADMIN, serão exibidos, na seção Dados da unidade da tela, elementos para possibilitar a alteração
          da data limite da etapa atual da unidade assim como da situação atual do subprocesso da unidade (ex.
          Reabertura do cadastro de atividades)

    - Caso existam unidades subordinadas com subprocesso localizado na unidade do usuário, os seguintes botões serão
      apresentados, de acordo com as condições dadas:
        - `Aceitar cadastros em bloco` ou `Homologar cadastros em bloco`, se existirem unidades subordinadas com
          subprocesso na situação 'Cadastro disponibilizado' (processo de mapeamento) ou 'Revisão do cadastro
          disponibilizada' (processo de revisão); ver caso de uso [Aceitar cadastros em bloco](cdu-22.md) .
        - `Aceitar mapas em bloco` ou `Homologar mapas em bloco`, se existirem unidades subordinadas com subprocesso
          nas situações 'Mapa validado' ou 'Mapa com sugestões'; ver caso de uso [Aceitar mapas em bloco/).