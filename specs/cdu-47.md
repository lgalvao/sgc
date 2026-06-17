# CDU-47 - Indicar impossibilidade de avaliação

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário
- Subprocesso localizado na unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md).`.

2. Ao lado do nome do servidor, o usuário escolhe a ação `Indicar impossibilidade`.

3. O sistema abre uma tela de confirmação:
    - título: "Indicação de impossibilidade";
    - texto: "Confirma a impossibilidade de avaliação para [NOME_SERVIDOR]?";
    - campo `Justificativa`, obrigatório;
    - botões `Cancelar` e `Indicar impossibilidade`.

4. O usuário informa a justificativa e aciona `Indicar impossibilidade`.

5. O sistema altera a situação do servidor para 'Avaliação impossibilitada'.

   5.1. A partir desse momento o sistema passa a habilitar a ação `Desfazer impossibilidade` para o servidor.