# CDU-47 - Preencher situação de capacitação

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário
- Subprocesso com localização atual na unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso`, conforme o caso de uso `CDU-42 - Visualizar detalhes de subprocesso de diagnóstico`.

3O usuário clica no card `Situação de capacitação`.

4O sistema apresenta uma grade com a matriz `Competência x Servidor`, contendo:
   - uma linha para cada competência vigente da unidade;
   - uma coluna para cada servidor participante da unidade;
   - uma célula editável (dropdown) `Situação de capacitação` para cada combinação competência/servidor;
   - cada célula `Situação de capacitação` admite os seguintes valores:
     - `NA` (Não se aplica);
     - `AC` (A capacitar);
     - `EC` (Em capacitação);
     - `C` (Capacitado);
     - `I` (Instrutor).

   *Regras de apresentação da matriz*:
   - a primeira coluna da matriz identifica a competência;
   - as colunas de servidores devem usar nomes abreviados, por limitação de espaço horizontal;
   - o nome completo do servidor deve continuar acessível na interface, por exemplo via `tooltip` no cabeçalho.

   Exemplo de matriz, depois de preenchida com situações de capacitação:

   | Competência         | BOB MARLEY           | DAVID BOWIE         | ELVIS PRESLEY       |
   |:--------------------|:---------------------|:--------------------|:--------------------|
   | Desc. Competência 1 | NA - Não se aplica   | NA - Não se aplica  | I - Instrutor       |
   | Desc. Competência 2 | I - Instrutor        | C - Capacitado      | C - Capacitado      |
   | Desc. Competência 3 | EC - Em capacitação  | EC - Em capacitação | EC - Em capacitação |

4. O usuário informa os valores de situação de capacitação para cada par competência-servidor.

5. O sistema salva automaticamente cada alteração realizada.

6. O usuário não precisa confirmar o cadastro de situações de capacitação de uma vez só, podendo retornar a esta tela em
   outro momento para finalizar. Ou seja, o sistema permite sair da tela sem completar todos os campos.
