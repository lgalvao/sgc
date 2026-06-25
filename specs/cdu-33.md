# CDU-33 - Reabrir revisão de cadastro

**Ator:** ADMIN

## Pré-condições

- Login realizado com perfil ADMIN
- Processo do tipo Revisão
- Ao menos um subprocesso na situação 'Mapa homologado'

## Fluxo principal

1. No `Painel`, o usuário aciona um processo de revisão em andamento e escolhe a unidade que tenha situação 'Mapa
   homologado'.

3. Na tela `Detalhe do subrocesso`, o usuário aciona `Reabrir revisão de cadastro`.

4. O sistema mostra um modal "Reabertura de revisão de cadastro", com campo `Justificativa` obrigatório, e botões
   `Cancelar` e `Reabrir cadastro`.

5. O usuário informa a justificativa e aciona `Reabrir cadastro`.

6. O sistema altera a situação do subprocesso para 'Revisão de cadastro em andamento'.

7. O sistema registra uma movimentação para o subprocesso com os campos:
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: ADMIN
    - `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Descrição`: 'Reabertura de revisão de cadastro'

8. O sistema envia notificações por e-mail para a unidade solicitante e a unidade superior.

   8.1. Para a unidade do subprocesso:

    ```text
    Assunto: SGC: Reabertura de revisão de cadastro

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    A revisão do cadastro de atividades da sua unidade foi reaberta para ajustes.

    Justificativa: [JUSTIFICATIVA]

    Acesse o sistema para realizar as alterações necessárias: [URL_SISTEMA].
    ```

   8.2. Para a unidade imediatamente superior:

    ```text
    Assunto: SGC: Reabertura de revisão de cadastro - [SIGLA_UNIDADE_SUBPROCESSO]]

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    Informamos que a revisão do cadastro de atividades da unidade [SIGLA_UNIDADE_SUBPROCESSO] foi reaberta para ajustes.

    Justificativa: [JUSTIFICATIVA]

    Após a conclusão dos ajustes, o cadastro será submetido novamente para sua análise.
    ```

9. O sistema cria internamente alertas:

   9.1. Para a unidade solicitante:
    - `Descrição`: "Revisão de cadastro da unidade [SIGLA_UNIDADE] reaberta"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: [SIGLA_UNIDADE]

   9.2. Para a unidade superior:
    - `Descrição`: "Revisão de cadastro da unidade [SIGLA_UNIDADE_SUBPROCESSO] reaberta"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

10. O sistema mostra *toast* "Revisão reaberta".
