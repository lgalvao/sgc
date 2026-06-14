# CDU-48 - Preencher situações de capacitação

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário
- Subprocesso localizado na unidade do usuário
- Existência de servidores da unidade com avaliação individual na situação 'Avaliação de consenso aprovada'

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md)`.

3. O usuário aciona o card `Situação de capacitação`.

4. O sistema apresenta a lista das competências vigentes da unidade para um servidor selecionado, contendo:
    - um controle `Servidor analisado`, em formato *drop-down*, com os servidores participantes da unidade;
    - uma linha para cada competência vigente da unidade;
    - uma coluna editável (dropdown) `Situação de capacitação` para cada competência do servidor selecionado;
    - cabeçalho com nome completo e título do servidor selecionado;
    - cada campo `Situação de capacitação` admite os seguintes valores:
        - `NA` (Não se aplica);
        - `AC` (A capacitar);
        - `EC` (Em capacitação);
        - `C` (Capacitado);
        - `I` (Instrutor).

5. O usuário informa os valores de situação de capacitação para cada célula.

6. O sistema salva automaticamente cada alteração realizada.

7. O usuário preenche os valores totalmente ou parcialmente, podendo retornar a esta tela em outro momento para
   finalizar (ou seja, o sistema permite sair da tela sem completar o preenchimento de todos os campos).
