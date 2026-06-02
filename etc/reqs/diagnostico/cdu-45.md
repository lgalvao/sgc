# CDU-45 - Preencher situação de capacitação

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso` da unidade.

3. O usuário clica no card `Situação de capacitação`.

4. O sistema apresenta uma tabela contendo, para cada servidor participante da unidade e para cada
   competência vigente da unidade, um campo editável `Situação de capacitação`, que admite os seguintes valores:
   - `NA` (Não se aplica);
   - `AC` (A capacitar);
   - `EC` (Em capacitação);
   - `C` (Capacitado);
   - `I` (Instrutor).
   Inicialmente, todos os valores de situação estarão vazios.   

- Exemplo de tabela, depois de preenchida com situações:

     | Nome          | Competência | Situação de capacitação |
     |:--------------| :---- | :---- |
     | BOB MARLEY    | Desc. Competência 1 | NA \- Não se aplica |
     |               | Desc. Competência 2 | I \- Instrutor |
     |               | Desc. Competência 3 | Em capacitação |
     | DAVID BOWIE   | Desc. Competência 1 | NA \- Não se aplica |
     |               | Desc. Competência 2 | C \- Capacitado |
     |               | Desc. Competência 3 | Em capacitação |
     | ELVIS PRESLEY | Desc. Competência 1 | I \- Instrutor |
     |               | Desc. Competência 2 | C \- Capacitado  |
     |               | Desc. Competência 3 | Em capacitação |
   
5. O usuário informa os valores para cada para competência/servidor.

6. O sistema salva automaticamente cada alteração realizada.

7. O usuário não precisa confirmar o cadastro de situações de capacitação na primeira 'rodada', podendo retornar a esta tela em outro momento para finalizar. 