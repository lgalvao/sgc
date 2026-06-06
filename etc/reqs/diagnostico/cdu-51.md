# CDU-51 - Validar diagnósticos em bloco

Ator: GESTOR

Maturidade: Média

Base principal: fluxo negocial acordado no PDF, que prevê validação de várias unidades em bloco com notificação consolidada para a unidade superior.

## Pré-condições

- Login realizado com perfil GESTOR
- Existência de processo de diagnóstico em andamento
- Existência de mais de uma unidade subordinada com diagnóstico concluído e elegível para aceite pelo usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`, com a árvore das unidades acessíveis ao usuário e a situação atual do diagnóstico de cada unidade.

3. Havendo mais de uma unidade elegível para aceite, o sistema habilita a ação `Validar diagnósticos em bloco`.

4. O usuário clica em `Validar diagnósticos em bloco`.

5. O sistema mostra uma confirmação contendo:
   - lista das unidades elegíveis, com sigla, nome e situação;
   - opção de selecionar quais unidades serão aceitas;
   - botões `Cancelar` e `Registrar aceite`.

6. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação.

7. O usuário seleciona as unidades desejadas e clica em `Registrar aceite`.

8. Para cada unidade selecionada, o sistema:
   - registra análise com resultado `Aceite`;
   - registra movimentação de aceite;
   - encaminha o subprocesso para análise da unidade hierarquicamente superior.

9. O sistema envia uma única notificação consolidada para a unidade superior, informando as unidades cujos diagnósticos foram aceitos e submetidos para análise.

10. O sistema mostra a mensagem `Diagnósticos aceitos em bloco`.

## Observação

- A operação em bloco cobre somente aceite/validação. Devolução permanece uma análise individual da unidade.
- As movimentações devem continuar individualizadas por subprocesso, mesmo quando a comunicação para a unidade superior for consolidada.
- A existência de alerta interno consolidado ou registro histórico agregado adicional deve ser tratada como detalhe de implementação ou requisito complementar, não como regra já confirmada.
