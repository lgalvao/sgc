# CDU-33 - Reabrir revisão de cadastro

**Ator:** ADMIN

## Fluxo principal

1. O usuário (Administrador) acessa o Painel de Controle ou a lista de subprocessos de Revisão.

2. O usuário localiza o subprocesso da unidade solicitante.

3. O usuário seleciona a opção "Reabrir Revisão de Cadastro".

4. O sistema solicita uma justificativa.

5. O usuário informa a justificativa e confirma.

6. O sistema altera a situação do subprocesso para `REVISAO_CADASTRO_EM_ANDAMENTO`.

7. O sistema registra uma movimentação para o subprocesso com os campos:
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: SEDOC
    - `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Descrição`: 'Reabertura de revisão de cadastro'
    - `Observação`: [JUSTIFICATIVA]

8. O sistema envia notificações por e-mail para a unidade solicitante e unidades superiores.

   8.1. Para a unidade solicitante (operacional/interoperacional):

    ```text
    Assunto: SGC: Reabertura de revisão de cadastro - [SIGLA_UNIDADE]

    Prezado(a) responsável pela [SIGLA_UNIDADE],

    A revisão do cadastro de atividades da sua unidade foi reaberta pela SEDOC para ajustes.
    Justificativa: [JUSTIFICATIVA]

    Acesse o sistema para realizar as alterações necessárias: [URL_SISTEMA].
    ```

   8.2. Para as unidades superiores:

    ```text
    Assunto: SGC: Reabertura de revisão de cadastro - [SIGLA_UNIDADE_SUBORDINADA]

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    Informamos que a revisão do cadastro de atividades da unidade [SIGLA_UNIDADE_SUBORDINADA] foi reaberta pela SEDOC para ajustes.
    Justificativa: [JUSTIFICATIVA]

    Após a conclusão dos ajustes, o cadastro será submetido novamente para sua análise.
    ```

9. O sistema cria internamente alertas:

   9.1. Para a unidade solicitante:
    - `Descrição`: "Revisão de cadastro reaberta pela SEDOC"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: SEDOC
    - `Unidade de destino`: [SIGLA_UNIDADE]

   9.2. Para as unidades superiores:
    - `Descrição`: "Revisão de cadastro da unidade [SIGLA_UNIDADE_SUBORDINADA] reaberta pela SEDOC"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: SEDOC
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

10. O sistema exibe mensagem de sucesso "Revisão reaberta com sucesso".
