# CDU-47 - Preencher situações de capacitação

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

4. O sistema apresenta uma matriz `Competência x Servidor`, contendo:
    - uma linha para cada competência vigente da unidade;
    - uma coluna para cada servidor participante da unidade;
    - uma célula editável (dropdown) `Situação de capacitação` para cada combinação competência/servidor;
    - cada célula `Situação de capacitação` admite os seguintes valores:
        - `NA` (Não se aplica);
        - `AC` (A capacitar);
        - `EC` (Em capacitação);
        - `C` (Capacitado);
        - `I` (Instrutor).

   **Regras de apresentação da matriz**:
    - a primeira coluna da matriz identifica a competência;
    - as colunas de servidores devem usar nomes abreviados, por limitação de espaço horizontal;
    - o nome completo do servidor deve continuar acessível na interface, por exemplo via `tooltip` no cabeçalho.

   Exemplo de matriz, depois de preenchida com situações de capacitação:

   | Competência         | BOB MARLEY           | DAVID BOWIE         | ELVIS PRESLEY       |
      |:--------------------|:---------------------|:--------------------|:--------------------|
   | Desc. Competência 1 | NA - Não se aplica   | NA - Não se aplica  | I - Instrutor       |
   | Desc. Competência 2 | I - Instrutor        | C - Capacitado      | C - Capacitado      |
   | Desc. Competência 3 | EC - Em capacitação  | EC - Em capacitação | EC - Em capacitação |

5. O usuário informa os valores de situação de capacitação para cada célula.

6. O sistema salva automaticamente cada alteração realizada.

7. O usuário preenche os valores totalmente ou parcialmente, podendo retornar a esta tela em outro momento para
   finalizar (ou seja, o sistema permite sair da tela sem completar o preenchimento de todos os campos).