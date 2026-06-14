# CDU-47 - Indicar impossibilidade de avaliação

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Subprocesso localizado na unidade do usuário
- Existência de servidores da unidade suja situação ainda não tenha chegado a 'Avaliação de consenso aprovada'

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md).`.

2. Ao lado do nome do servidor desejado, o usuário escolhe a ação `Indicar impossibilidade`.

3. O sistema abre um modal:
    - título: "Indicar impossibilidade de avaliação";
    - texto: "Confirma a impossibilidade de avaliação para [NOME_SERVIDOR]?";
    - campo `Justificativa`, obrigatório;
    - botões `Cancelar` e `Indicar impossibilidade`.

4. O usuário informa a justificativa e aciona `Indicar impossibilidade`.

5. O sistema altera a situação individual do servidor para 'Avaliação impossibilitada' e mostra a mensagem
   "Impossibilidade registrada".

6. A partir desse momento, na tela `Detalhes do subprocesso`, o sistema passa a habilitar a ação `Permitir avaliação`
   para o servidor impossibilitado, permitindo reverter a impossibilidade quando necessário.
