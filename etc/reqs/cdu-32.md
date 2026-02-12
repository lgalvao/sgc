# CDU-32 - Reabrir cadastro

**Ator:** ADMIN

## Fluxo principal

1. O usuário acessa o Painel.

2. O usuário localiza o subprocesso de uma unidade.

3. O usuário clica no botão `Reabrir cadastro`.

4. O sistema solicita uma justificativa para a reabertura.

5. O usuário informa a justificativa e confirma.

6. O sistema altera a situação do subprocesso para `MAPEAMENTO_CADASTRO_EM_ANDAMENTO`.

8 sistema registra uma movimentação para o subprocesso com os campos:

- `Data/hora`: Data/hora atual
- `Unidade origem`: ADMIN
- `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
- `Descrição`: 'Reabertura de cadastro'
- `Observação`: [JUSTIFICATIVA]

1. O sistema envia notificações por e-mail para a unidade solicitante e unidades superiores.

   8.1. Para a unidade solicitante (operacional/interoperacional):

    ```text
    Assunto: SGC: Reabertura de cadastro de atividades - [SIGLA_UNIDADE]

    Prezado(a) responsável pela [SIGLA_UNIDADE],

    O cadastro de atividades da sua unidade foi reaberto para ajustes.

    Justificativa: [JUSTIFICATIVA]

    Acesse o sistema para realizar as alterações necessárias: [URL_SISTEMA].
    ```

   8.2. Para as unidades superiores:

    ```text
    Assunto: SGC: Reabertura de cadastro de atividades - [SIGLA_UNIDADE_SUBORDINADA]

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    Informamos que o cadastro de atividades da unidade [SIGLA_UNIDADE_SUBORDINADA] foi reaberto para ajustes.

    Justificativa: [JUSTIFICATIVA]

    Após a conclusão dos ajustes, o cadastro será submetido novamente para sua análise.
    ```

2. O sistema cria internamente alertas:

   9.1. Para a unidade solicitante:
    - `Descrição`: "Cadastro de atividades reaberto"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: [SIGLA_UNIDADE]

   9.2. Para as unidades superiores:
    - `Descrição`: "Cadastro da unidade [SIGLA_UNIDADE_SUBORDINADA] reaberto"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

3. O sistema exibe mensagem de sucesso "Cadastro reaberto com sucesso".