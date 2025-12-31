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

7. O sistema registra uma movimentação para o subprocesso com os campos:
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: SEDOC
    - `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Descrição`: 'Reabertura de cadastro'
    - `Observação`: [JUSTIFICATIVA]

8. O sistema envia notificações por e-mail para a unidade solicitante e unidades superiores.

   8.1. Para a unidade solicitante (operacional/interoperacional):

    ```text
    Assunto: SGC: Reabertura de cadastro de atividades - [SIGLA_UNIDADE]

    Prezado(a) responsável pela [SIGLA_UNIDADE],

    O cadastro de atividades da sua unidade foi reaberto pela SEDOC para ajustes.
    Justificativa: [JUSTIFICATIVA]

    Acesse o sistema para realizar as alterações necessárias: [URL_SISTEMA].
    ```

   8.2. Para as unidades superiores:

    ```text
    Assunto: SGC: Reabertura de cadastro de atividades - [SIGLA_UNIDADE_SUBORDINADA]

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    Informamos que o cadastro de atividades da unidade [SIGLA_UNIDADE_SUBORDINADA] foi reaberto pela SEDOC para ajustes.
    Justificativa: [JUSTIFICATIVA]

    Após a conclusão dos ajustes, o cadastro será submetido novamente para sua análise.
    ```

9. O sistema cria internamente alertas:

   9.1. Para a unidade solicitante:
    - `Descrição`: "Cadastro de atividades reaberto pela SEDOC"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: SEDOC
    - `Unidade de destino`: [SIGLA_UNIDADE]

   9.2. Para as unidades superiores:
    - `Descrição`: "Cadastro da unidade [SIGLA_UNIDADE_SUBORDINADA] reaberto pela SEDOC"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: SEDOC
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

10. O sistema exibe mensagem de sucesso "Cadastro reaberto com sucesso".
