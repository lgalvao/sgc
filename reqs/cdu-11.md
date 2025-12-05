# CDU-11 - Visualizar cadastro de atividades e conhecimentos

Ator: Usuário (todos os perfis)

Pré-condições:

- Usuário logado com qualquer perfil
- Processo de mapeamento ou de revisão iniciado ou finalizado, que tenha a unidade como participante
- Subprocesso da unidade com cadastro de atividades e conhecimentos (processo de mapeamento) ou revisão do cadastro (
  processo de revisão) já disponibilizados.

Fluxo principal:

1. No Painel, o usuário clica no processo de mapeamento ou revisão na situação `Em andamento` ou `Finalizado`.

2. Se usuário estiver logado com perfil ADMIN ou GESTOR:
   
   2.1. O sistema mostra a tela `Detalhes do processo`

   2.2. Usuário clica em uma unidade subordinada, que seja operacional ou interoperacional

   2.3. O sistema mostra a tela `Detalhes do subprocesso`, com os dados do subprocesso da unidade selecionada

3. Se perfil logado for CHEFE ou SERVIDOR:

   3.1. O sistema exibe a tela `Detalhes do subprocesso` com os dados do subprocesso da unidade do usuário

4. Na tela de Detalhes do subprocesso, usuário clica no card `Atividades e conhecimentos`.

5. O sistema apresenta a tela `Atividades e conhecimentos`, preenchida com os dados da unidade.

6. Nesta tela, são apresentados a sigla e o nome da unidade, e cada atividade é apresentada como uma tabela, com
   cabeçalho a descrição da atividade, e as linhas preenchidas com os conhecimentos cadastrados para aquela atividade.