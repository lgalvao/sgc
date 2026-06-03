# CDU-46 - Indicar impossibilidade de avaliação

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento para a unidade do usuário
- Existência de servidor da unidade cuja avaliação individual ainda não tenha chegado à situação `Avaliação de consenso aprovada`.

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade.

3. O usuário clica no card `Monitoramento`.

4. O sistema apresenta a tela `Monitoramento de diagnóstico`, com a situação dos servidores lotados na unidade do usuário. Para cada servidor, são mostrados:
   - `Nome` : nome completo do servidor
   - `Situação`: situação atual da avaliação individual
   
5. Na coluna de ações, o usuário escolhe a opção `Indicar impossibilidade

6. O sistema abre um modal com:
   - título `Indicar impossibilidade de avaliação`;
   - texto `Confirma a impossibilidade de avaliação para [NOME_SERVIDOR]?`;
   - campo obrigatório `Justificativa`;
   - botões `Cancelar` e `Indicar impossibilidade`.

3. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação e permanece na mesma tela.

4. O usuário informa a justificativa e clica em `Indicar impossibilidade`.
   
5. O sistema altera a situação da avaliação individual do servidor para `Avaliação impossibilitada`. e passa  a desconsiderar, para fins de conclusão da unidade, quaisquer dados parciais anteriormente registrados para aquela avaliação individual.

7. O sistema mostra a mensagem `Impossibilidade registrada`.