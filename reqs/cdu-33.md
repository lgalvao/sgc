# CDU-33 - Reabrir revisão de cadastro

**Ator:** ADMIN

## Descrição

Se durante as etapas de ajuste e de validação do mapa de competências ajustado, alguma unidade indicar a necessidade tardia de revisão do cadastro de atividades e conhecimentos, a SEDOC poderá, mediante solicitação dessa unidade, retorná-la para a etapa de revisão do cadastro a fim de que a mesma possa realizar as alterações necessárias.

## Regras de Negócio

- A reabertura da revisão do cadastro de atividades e conhecimentos da unidade será notificada para todas unidades hierarquicamente superiores.
- Após os ajustes realizados, a revisão do cadastro de atividades e conhecimentos da unidade precisará passar por nova análise por todas as unidades superiores na hierarquia.
- O processo de revisão poderá ser concluído quando todas as unidades participantes concluírem a validação do mapa de competência.
- As etapas de ajuste do mapa, e de validação do mapa ajustado, deverão ser realizadas apenas para as unidades que efetuarem alterações no cadastro de atividades e conhecimentos. As unidades que não identificarem necessidades de alteração poderão indicar a validação do mapa de competências já na primeira etapa do processo.
- A ação altera a situação do subprocesso para "REVISAO_CADASTRO_EM_ANDAMENTO".

## Fluxo principal

1. O usuário (Administrador) acessa o Painel de Controle ou a lista de subprocessos de Revisão.
2. O usuário localiza o subprocesso da unidade solicitante.
3. O usuário seleciona a opção "Reabrir Revisão de Cadastro".
4. O sistema solicita uma justificativa.
5. O usuário informa a justificativa e confirma.
6. O sistema altera a situação do subprocesso para `REVISAO_CADASTRO_EM_ANDAMENTO`.
7. O sistema registra a movimentação.
8. O sistema envia notificações para a unidade solicitante e unidades superiores.
9. O sistema exibe mensagem de sucesso.
