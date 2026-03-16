# CDU-32 - Reabrir cadastro

**Ator:** ADMIN

## Pré-condições

- Login realizado com perfil ADMIN
- Processo do tipo Mapeamento ou Revisão
- Ao menos um subprocesso que tenha passado da situação 'Mapa homologado'  

## Fluxo principal

1. O usuário acessa o `Painel` e acessa o subprocesso de uma unidade que esteja com a situação 'Mapa homologado ou posterior' (ver situações de subprocessos no arquivo **_intro.md**).

2. O usuário clica no botão `Reabrir cadastro`.

3. O sistema abre um modal "Reabertura de cadastro" solicitando uma justificativa (obrigatória) para a reabertura.

4. O usuário informa a justificativa e confirma.

5. O sistema altera a situação do subprocesso para `Cadastro em andamento'.

6. sistema registra uma movimentação para o subprocesso com os campos:

- `Data/hora`: Data/hora atual
- `Unidade origem`: ADMIN
- `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
- `Descrição`: 'Reabertura de cadastro'

1. O sistema envia notificações por e-mail para a unidade do subprocesso e para as suas unidades superiores na hierarquia.

   8.1. Para a unidade do subprocesso:

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

3. O sistema exibe mensagem de sucesso "Cadastro reaberto".