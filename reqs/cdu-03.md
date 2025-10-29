# CDU-03 - Manter processo

- Ator: ADMIN
- Pré-condição: Login realizado com perfil ADMIN, com tela Painel aberta.

## Fluxo principal

### Criação de processo:

1. Se o usuário quiser criar um processo, escolhe o botão Criar processo.
2. O sistema muda para a tela Cadastro de processo e apresenta um formulário contendo:

   2.1. Campo Descrição
   2.2. Campo Tipo do processo, com opções: 'Mapeamento', 'Revisão' e 'Diagnóstico'
   2.3. Quadro de Unidades participantes, contendo uma árvore de unidades com checkboxes para cada uma.
   2.3.1. A lista de unidades só deve incluir unidades que não estejam participando de um processo ativo do tipo
   selecionado.

   2.3.2. O comportamento de seleção das unidades participantes deve seguir estas regras:

   2.3.2.1. Ao clicar em uma unidade intermediária na árvore, todas as unidades abaixo dela devem ser automaticamente
   selecionadas;

   2.3.2.2. Se todas as unidades de uma subárvore estiverem selecionadas, o nó raiz desta subárvore deve ser
   automaticamente selecionado;

   2.3.2.3. Se um nó de uma subárvore tiver a seleção removida, o nó raiz da subárvore deve ficar num estado
   intermediário, indicando que há nós selecionados da subárvore, mas não todos;

   2.3.2.4. Se todas as unidades de uma subárvore tiverem a seleção removida, o nó raiz desta subárvore deve ter
   automaticamente a seleção removida.

   2.3.2.5. Se a raiz de uma subárvore for uma unidade interoperacional, ela poderá ser selecionada ainda que as
   unidades subordinadas não o sejam.

   2.4. Campo Data limite etapa 1 para informação do prazo que as unidades terão para concluir a etapa inicial do
   processo.
   2.5. Botões Cancelar, Salvar e Iniciar processo

3. O usuário fornece os dados solicitados, escolhe o tipo do processo desejado e seleciona as unidades participantes.

4. Usuário clica em Salvar.

5. O sistema faz estas validações (com mensagens de falha de validação entre aspas):

   5.1. Descrição deve estar preenchida — "Preencha a descrição".

   5.2. Ao menos uma unidade deve ser selecionada — "Pelo menos uma unidade participante deve ser incluída."

   5.3. Em caso de processos dos tipos 'Revisão' ou 'Diagnóstico', só poderão ser selecionadas unidades com mapas de
   competência vigentes — "Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não
   passaram por processo de mapeamento."
   .
6. O sistema cria o processo internamente, colocando-o na situação 'Criado', e mostra a mensagem "Processo criado.".

7. O sistema redireciona para o Painel, onde já será mostrada uma linha para o processo recém-criado.

### Edição de processo:

1. Se usuário quiser editar o processo, clica na linha do processo na listagem de processos do Painel (apenas processos
   na situação 'Criado' podem ser editados).

   8.1. Além dos botões Cancelar, Salvar e Iniciar processo, também será exibido o botão Remover.

2. O sistema abre a tela Cadastro de processo preenchida com os dados atuais do processo.

3. O usuário modifica os dados desejados (descrição, unidades participantes e data limite apenas)

4. O usuário escolhe o botão Salvar.

5. O sistema valida os dados depois de editados, de acordo com as mesmas regras aplicadas no momento do cadastro.

6. O sistema atualiza o processo e mostra a mensagem "Processo alterado.".

### Remoção:

1. Se usuário quiser remover o processo, clica na linha do processo na listagem de processos do Painel (apenas processos
   na situação 'Criado' podem ser removidos).

   14.1. Além dos botões Cancelar, Salvar e Iniciar processo, também será exibido o botão Remover.

2. O sistema abre a tela Cadastro de processo preenchida com os dados atuais do processo.

3. O usuário escolhe o botão Remover.

4. O sistema mostra o diálogo de confirmação "Remover o processo '[Descrição do processo]'? Esta ação não poderá ser
   desfeita.", botões Remover e Cancelar.

   17.1. Se escolher Cancelar no diálogo, o sistema fecha o diálogo e permanece na tela Cadastro de processo, sem
   efetuar alterações.

   17.2. Ao escolher Remover no diálogo, o sistema remove o processo permanentemente, mostra a mensagem "
   Processo [Descrição do Processo] removido" e redireciona para o Painel.

## Fluxo alternativo
Nos passos 4 ou 11, caso o usuário escolha o botão Iniciar processo em vez do botão Salvar, o sistema realiza as
validações dos dados informados, cria o processo (se ainda não tiver sido criado) e segue para o fluxo de do caso de uso
Iniciar processo de mapeamento, ou Iniciar processo de revisão ou Iniciar processo de diagnóstico (fora do atual
documento), dependendo do tipo do processo.