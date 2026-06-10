# CDU-51 - Validar diagnósticos em bloco

Ator: GESTOR

## Pré-condições

- Login realizado com perfil GESTOR
- Existência de processo de diagnóstico em andamento
- Existência de mais de uma unidade subordinada com diagnóstico concluído e elegível para aceite pelo usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`, com a árvore das unidades acessíveis ao usuário e a situação atual do
   diagnóstico de cada unidade.

3. O usuário aciona em `Validar em bloco`.

4. O sistema mostra uma confirmação contendo:
    - lista das unidades elegíveis, com sigla, nome e situação, permitindo selecionar quais unidades serão aceitas;
    - botões `Cancelar` e `Registrar aceite`.

5. O usuário seleciona as unidades desejadas e clica em `Registrar aceite`.

6. Para cada unidade selecionada, o sistema:
    - registra análise com resultado `Aceite`;
    - registra movimentação de aceite;
    - encaminha o subprocesso para análise da unidade hierarquicamente superior.

7. O sistema envia uma única notificação consolidada para a unidade superior, informando as unidades cujos diagnósticos
   foram aceitos e submetidos para análise.

8. O sistema mostra a mensagem `Diagnósticos aceitos em bloco`.