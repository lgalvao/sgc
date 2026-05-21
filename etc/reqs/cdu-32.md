# CDU-32 - Reabrir cadastro

**Ator:** ADMIN

## Pré-condições

- Login realizado com perfil ADMIN
- Processo do tipo Mapeamento
- Ao menos um subprocesso na situação 'Mapa homologado'

## Fluxo principal

1. O usuário acessa o `Painel` e acessa o subprocesso de uma unidade que esteja com a situação 'Mapa homologado' do
   processo de mapeamento.

2. O usuário clica no botão `Reabrir cadastro`.

3. O sistema abre um modal "Reabertura de cadastro" solicitando uma justificativa (obrigatória) para a reabertura.

4. O usuário informa a justificativa e escolhe `Reabrir`.

5. O sistema altera a situação do subprocesso para `MAPEAMENTO_CADASTRO_EM_ANDAMENTO` (`Cadastro em andamento`).

6. O sistema registra uma movimentação para o subprocesso com os campos:

    - `Data/hora`: Data/hora atual
    - `Unidade origem`: ADMIN
    - `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Descrição`: 'Reabertura de cadastro'

7. O sistema envia notificações por e-mail para a unidade do subprocesso e para a sua unidade superior na hierarquia.

   7.1. Para a unidade do subprocesso:

    ```text
    Assunto: SGC: Reabertura de cadastro de atividades - [SIGLA_UNIDADE]

    Prezado(a) responsável pela [SIGLA_UNIDADE],

    O cadastro de atividades da sua unidade foi reaberto para ajustes.

    Justificativa: [JUSTIFICATIVA]

    Acesse o sistema SGC para realizar as alterações necessárias: [URL_SISTEMA].
    ```

   7.2. Para a unidade superior:

    ```text
    Assunto: SGC: Reabertura de cadastro de atividades - [SIGLA_UNIDADE_SUBORDINADA]

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    Informamos que o cadastro de atividades da unidade [SIGLA_UNIDADE_SUBORDINADA] foi reaberto para ajustes.

    Justificativa: [JUSTIFICATIVA]

    Após a conclusão dos ajustes, o cadastro será submetido novamente para sua análise.
    ```

8. O sistema cria internamente alertas:

   8.1. Para a unidade solicitante:
    - `Descrição`: "Cadastro de atividades reaberto"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: [SIGLA_UNIDADE]

   8.2. Para a unidade superior:
    - `Descrição`: "Cadastro da unidade [SIGLA_UNIDADE_SUBORDINADA] reaberto"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

9. O sistema mostra mensagem de sucesso "Cadastro reaberto".
