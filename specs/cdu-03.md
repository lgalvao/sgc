# CDU-03 - Manter processo

## Atores

- ADMIN

## Pré-condições

- Login realizado com perfil ADMIN
- Tela painel aberta

## Fluxo principal

### Criação de um processo

1. Se o usuário quiser criar um processo, escolhe o botão `Criar processo`.

2. O sistema muda para a tela `Cadastro de processo` e apresenta um formulário contendo:
    - Campo `Descrição`
    - Campo `Tipo`, com opções: 'Mapeamento', 'Revisão' e 'Diagnóstico'
    - Quadro `Unidades participantes`, contendo uma árvore de unidades com checkboxes para cada uma.
        - A lista de unidades **deve deixar desativadas** (não selecionáveis) as unidades que já estejam participando de
          um processo ativo do tipo selecionado em `Tipo`.
        - O comportamento de seleção das unidades participantes deve seguir estas regras:
            - Ao clicar em uma unidade intermediária na árvore, todas as unidades abaixo dela devem ser automaticamente
              selecionadas;
            - Se todas as unidades de uma subárvore estiverem selecionadas, o nó raiz desta subárvore deve ser
              automaticamente selecionado;
            - Se um nó de uma subárvore tiver a seleção removida, o nó raiz da subárvore deve ficar num estado
              intermediário, indicando que há nós selecionados da subárvore, mas não todos;
            - Se todas as unidades de uma subárvore tiverem a seleção removida, o nó raiz desta subárvore deve ter
              automaticamente a seleção removida.
            - Se a raiz de uma subárvore for uma unidade interoperacional, seu checkbox deve refletir apenas o estado da
              subárvore, sem tratamento visual especial.
    - Campo `Data limite etapa 1`, para informação do prazo que as unidades terão para concluir a etapa inicial do
      processo.
    - Botões `Cancelar`, `Salvar` e `Iniciar processo`

3. O usuário fornece os dados solicitados e seleciona as unidades participantes, e clica em `Salvar`.

4. O sistema faz as seguintes validações (com mensagens de falha de validação indicadas entre aspas):

   4.1. Descrição deve estar preenchida. Validação: "Preencha a descrição".

   4.2. Ao menos uma unidade deve ser selecionada. Validação: "Pelo menos uma unidade participante deve ser incluída."

   4.3. Em caso de processos dos tipos 'Revisão' ou 'Diagnóstico', só poderão ser selecionadas unidades com mapas de
   competência vigentes. A mensagem de aviso a ser mostrada, caso contrário será: "Não é possível incluir em processos
   de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento."

5. O sistema cria o processo internamente, colocando-o na situação 'Criado', e mostrando a mensagem "Processo criado.".

6. O sistema redireciona para o Painel, onde já será mostrada uma linha para o processo recém-criado.

### Edição de processo

7. Se usuário quiser editar o processo, clica na linha do processo na listagem de processos do `Painel` (apenas
   processos na situação 'Criado' podem ser editados). Além dos botões `Cancelar`, `Salvar` e `Iniciar processo`, também
   será exibido o botão `Remover`.

8. O sistema abre a tela `Cadastro de processo` preenchida com os dados atuais do processo.

9. O usuário modifica os dados desejados. Apenas a descrição, as unidades participantes e a data limite podem ser
   alteradas. O tipo do processo não pode ser alterado.

10. O usuário escolhe o botão `Salvar`.

11. O sistema valida os dados depois de editados, de acordo com as mesmas regras aplicadas no momento do primeiro
   cadastro.

12. O sistema atualiza o processo e mostra a mensagem "Processo alterado.".

### Remoção de processo

13. Se usuário quiser remover o processo, clica na linha do processo na listagem de processos do `Painel` (apenas
   processos na situação 'Criado' podem ser removidos). Além dos botões `Cancelar`, `Salvar` e `Iniciar processo`,
   também será exibido o botão `Remover`.

14. O sistema abre a tela `Cadastro de processo`, preenchida com os dados atuais do processo.

15. O usuário escolhe o botão `Remover`.

16. O sistema mostra o diálogo de confirmação "Remover o processo '[Descrição do processo]'? Esta ação não poderá ser
   desfeita.", com botões `Remover` e `Cancelar`.

   4.1. Se escolher `Cancelar` no diálogo: sistema fecha o diálogo e permanece na tela `Cadastro de processo`, sem
   efetuar alterações.

   4.2. Ao escolher `Remover` no diálogo: sistema remove o processo permanentemente e redireciona para o Painel,
   mostrando a mensagem "Processo :DESCRICAO_PROCESSO: removido".

## Fluxo alternativo

No **Passo 3**, caso o usuário escolha o botão `Iniciar processo` em vez do botão `Salvar`, o sistema realiza as
validações dos dados informados, cria internamente o processo (se ainda não tiver sido criado) e segue para o fluxo do
caso de uso `Iniciar processo de mapeamento`, `Iniciar processo de revisão` ou `Iniciar processo de diagnóstico`,
dependendo do tipo do processo.
