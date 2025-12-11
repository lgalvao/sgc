# CDU-18 - Visualizar mapa de competências

Ator: Usuário (todos os perfis)

Pré-condições:

- Usuário logado com qualquer perfil.
- Processo de mapeamento ou de revisão iniciado ou finalizado, que tenha a unidade como participante
- Subprocesso da unidade com mapa de competência já disponibilizado.

Fluxo principal:

1. No Painel, o usuário clica no processo de mapeamento ou revisão na situação 'Em andamento' ou 'Finalizado'.

2. Se perfil logado for ADMIN ou GESTOR:

   2.1. O sistema exibe a tela Detalhes do processo.

   2.2. Usuário clica em uma unidade subordinada que seja operacional ou interoperacional.

   2.3. O sistema exibe a tela Detalhes do subprocesso com os dados do subprocesso da unidade selecionada.

3. Se perfil logado for CHEFE ou SERVIDOR:

   3.1. O sistema exibe a tela Detalhes do subprocesso com os dados do subprocesso da unidade do usuário.

4. Na tela de Detalhes do subprocesso, usuário clica no card Mapa de Competências.

5. O sistema mostra a tela Visualização de mapa, com as seguintes informações:

    - Título "Mapa de competências técnicas"
    - Identificação da unidade (sigla e nome).
    - Conjunto de competências, com cada competência mostrada em um bloco individual, contendo:
        - Descrição da competência como título.
        - Conjunto das atividades associadas àquela competência.
        - Para cada atividade, conjunto de conhecimentos da atividade.