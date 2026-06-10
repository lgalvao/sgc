# CDU-46 - Indicar impossibilidade de avaliação

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Subprocesso da unidade com localização atual na unidade do usuário
- Existência de servidor da unidade cuja avaliação individual ainda não tenha chegado à situação 'Avaliação de consenso
  aprovada'

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso`, conforme o caso de uso
   `CDU-42 - Visualizar detalhes de subprocesso de diagnóstico`.

2. Ao lado do nome do servidor desejado, o usuário escolhe a ação `Indicar impossibilidade`.

3. O sistema abre um modal::
    - título: "Indicar impossibilidade de avaliação";
    - texto "Confirma a impossibilidade de avaliação para [NOME_SERVIDOR]?";
    - campo `Justificativa` (obrigatório);
    - botões `Cancelar` e `Indicar impossibilidade`.

4. O usuário informa a justificativa e aciona `Indicar impossibilidade`.

5. O sistema altera a situação individual do servidor para `Avaliação impossibilitada` e mostra a mensagem `Impossibilidade registrada`.