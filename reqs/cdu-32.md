# CDU-32 - Reabrir cadastro

**Ator:** ADMIN

## Descrição
Se durante as etapas de criação e de validação do mapa de competências, alguma unidade indicar a necessidade tardia de ajuste no cadastro de atividades e conhecimentos, a SEDOC poderá, mediante solicitação da unidade em questão, retorná-la para a etapa de cadastro a fim de que a mesma possa realizar as alterações necessárias.

## Regras de Negócio

- A reabertura do cadastro de atividades e conhecimentos da unidade será notificada para todas unidades hierarquicamente superiores.
- Após os ajustes realizados, o cadastro de atividades e conhecimentos da unidade precisará passar por nova análise por todas as unidades superiores na hierarquia.
- A ação altera a situação do subprocesso para "MAPEAMENTO_CADASTRO_EM_ANDAMENTO".
- Esta ação só pode ser realizada se o subprocesso estiver em uma etapa posterior ao cadastro inicial, mas ainda dentro do processo de Mapeamento.

## Fluxo principal

1. O usuário (Administrador) acessa o Painel de Controle ou a lista de subprocessos.
2. O usuário localiza o subprocesso da unidade solicitante.
3. O usuário seleciona a opção "Reabrir Cadastro".
4. O sistema solicita uma justificativa para a reabertura.
5. O usuário informa a justificativa e confirma.
6. O sistema altera a situação do subprocesso para `MAPEAMENTO_CADASTRO_EM_ANDAMENTO`.
7. O sistema registra a movimentação/log da ação.
8. O sistema envia notificações para a unidade solicitante e para as unidades superiores informando sobre a reabertura e a necessidade de nova análise futura.
9. O sistema exibe mensagem de sucesso.
