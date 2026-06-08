# CDU-46 - Indicar impossibilidade de avaliação

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento para a unidade do usuário
- Existência de servidor da unidade cuja avaliação individual ainda não tenha chegado à situação `Avaliação de consenso aprovada`.

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade.

4. O sistema apresenta uma grade com a situação dos servidores lotados na unidade do usuário. Para cada servidor, são mostrados:
   - `Nome` : nome completo do servidor
   - `Situação`: situação atual da avaliação individual
   - `Ações`
   
5. Ao lado do servidor impossibilitado, o usuário escolhe a ação `Indicar impossibilidade`.

6. O sistema abre um modal:
   - título `Indicar impossibilidade de avaliação`;
   - texto `Confirma a impossibilidade de avaliação para [NOME_SERVIDOR]?`;
   - campo `Justificativa` (obrigatório)
   - botões `Cancelar` e `Indicar impossibilidade`.

   6.1. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação e permanece na mesma tela.
   
7. O usuário informa a justificativa e aciona `Indicar impossibilidade`.
   
8. O sistema altera a situação individual do servidor para `Avaliação impossibilitada`. 

9. Sistema mostra a mensagem `Impossibilidade registrada`.