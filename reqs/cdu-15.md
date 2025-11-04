# CDU-15 - Manter mapa de competências

Ator: ADMIN

Pré-condições:
- Processo de mapeamento com ao menos uma unidade com subprocesso nas situações 'Cadastro homologado' ou 'Mapa criado'.

Fluxo principal:

1. No Painel ADMIN escolhe um processo na tela Detalhes do processo clica em uma unidade operacional ou interoperacional
   com subprocesso nas situações 'Cadastro homologado' ou 'Mapa criado'.
2. O sistema mostra a tela Detalhes do subprocesso.
3. ADMIN clica no card Mapa de Competências.
4. O sistema mostra a tela Edição de mapa preenchida com os dados do mapa da unidade, com os seguintes elementos
   visuais:

   4.1. Um bloco para cada competência criada, cujo título é a descrição da competência
   4.2. Ao lado da descrição da competência, botões de ação (ícones), para editar e para excluir a competência.
   4.3. Dentro de cada bloco de uma competência, mostrar as descrições das atividades associadas à competência em
   pequenos blocos internos.
   4.4. À direita da descrição de cada atividade mostrar um badge com o número de conhecimentos da atividade. Ao passar
   o mouse sobre esse badge, o sistema exibirá em um tooltip a lista de conhecimentos da atividade.
   4.5. Botões Criar competência e Disponibilizar alinhados no canto superior direito da lista de blocos de competências

[Início de fluxo de criação de competências]

1. ADMIN clica no botão Criar competência.
2. O sistema abre a tela modal Edição de competência, com:

   6.1. Um campo para a descrição da competência.
   6.2. Uma lista das atividades cadastradas pela unidade, cada uma podendo ser selecionada para inclusão na
   competência.
   6.3. Botões Cancelar e Salvar.

3. ADMIN informa a descrição da competência que será criada e seleciona uma ou mais atividades para associar a ela e
   clica em Salvar para confirmar as mudanças.
4. O sistema armazena internamente a competência e o vínculo desta com as atividades selecionadas
5. O sistema insere a competência criada no mapa de competências.
6. Se a situação do subprocesso da unidade ainda for 'Cadastro homologado', o sistema altera a situação para 'Mapa
   criado'.

[Término de fluxo de criação de competências]

8. ADMIN repete o fluxo de criação de competências até que o mapa esteja completo.
8. Se desejar editar uma competência criada:

   12.1. ADMIN clica no botão de ação de editar no cabeçalho do bloco correspondente à competência.
   12.2. O sistema exibe a tela Edição de competência preenchida com a descrição da competência e apresenta selecionada(
   s) a(s) atividade(s) atualmente associada(s) à competência.
   12.3. ADMIN altera a descrição ou a associação com as atividades e clica no botão Salvar.
   12.4. O sistema armazena a nova descrição da competência e os vínculos com as atividades.
   12.5. O sistema retorna para a tela Edição do mapa, exibindo o bloco da competência correspondente atualizado.

9. Se desejar excluir uma competência criada:

   13.1. ADMIN clica no botão de ação de excluir no cabeçalho do bloco correspondente à competência.
   13.2. O sistema mostra diálogo de confirmação: Título ''Exclusão de competência", mensagem "Confirma a exclusão da
   competência [DESCRICAO_COMPETENCIA]?" / Botões Confirmar e Cancelar.
   13.3. ADMIN confirma a exclusão.
   13.4. O sistema remove a competência e todos os seus vínculos com as atividades da unidade.
   13.5. O sistema retorna para a tela Edição do mapa, mostrando o bloco da competência correspondente atualizado.

10. Se o usuário clicar em Disponibilizar, o sistema segue para o caso de uso Disponibilizar mapa de competências.