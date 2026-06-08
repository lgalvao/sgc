# CDU-47 - Preencher situação de capacitação

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade, com detalhes do subprocesso, os cards e a grade de servidores.

3. O usuário clica no card `Situação de capacitação`.

4. O sistema apresenta uma matriz `Competência x Servidor`, contendo:
   - uma linha para cada competência vigente da unidade;
   - uma coluna para cada servidor participante da unidade;
   - uma célula editável (dropdown) `Situação de capacitação` para cada combinação competência/servidor.
  4.1 Cada célula `Situação de capacitação` admite os seguintes valores:
   - `NA` (Não se aplica);
   - `AC` (A capacitar);
   - `EC` (Em capacitação);
   - `C`  (Capacitado);
   - `I`  (Instrutor).
   4.2. Regras de apresentação da matriz:
   - A primeira coluna da matriz deve identificar a competência.
   - As colunas de servidores devem usar nomes abreviados, por limitação de espaço horizontal.
   - O nome completo do servidor deve continuar acessível na interface, por exemplo via 'tooltip' no cabeçalho.
   - Quando necessário, para evitar ambiguidade, o título eleitoral pode aparecer como informação secundária no cabeçalho do servidor.
   - Exemplo de matriz, depois de preenchida com situações de capacitação:

     | Competência           | BOB MARLEY        | DAVID BOWIE       | ELVIS PRESLEY     |
     |:----------------------|:------------------|:------------------|:------------------|
     | Desc. Competência 1   | NA \- Não se aplica | NA \- Não se aplica | I \- Instrutor    |
     | Desc. Competência 2   | I \- Instrutor    | C \- Capacitado   | C \- Capacitado   |
     | Desc. Competência 3   | EC \- Em capacitação | EC \- Em capacitação | EC \- Em capacitação |
   
6. O usuário informa os valores para cada célula da matriz.

7. O sistema salva automaticamente cada alteração realizada. 
    *NOTA*: O usuário não precisa confirmar o cadastro de situações de capacitação de uma vez só, podendo retornar a esta tela em outro momento para finalizar.