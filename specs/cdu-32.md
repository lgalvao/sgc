# CDU-32 - Reabrir cadastro

## Atores

- ADMIN

## Pré-condições

- Login realizado com perfil ADMIN
- Processo do tipo Mapeamento
- Ao menos um subprocesso na situação 'Mapa homologado'

## Fluxo principal

1. O usuário acessa o `Painel` e aciona um processo de mapeamento em andamento.

2. O sistema mostra a tela `Detalhes do processo`

3. O usuário aciona uma unidade com a situação 'Mapa homologado'.

4. O usuário aciona `Reabrir cadastro`.

5. O sistema abre um modal "Reabertura de cadastro", com campo `Justificativa`, obrigatória.

6. usuário informa a justificativa aciona `Reabrir`.

7. O sistema altera a situação do subprocesso para 'Cadastro em andamento'.

8. O sistema registra uma movimentação para o subprocesso, com os campos:
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: ADMIN
    - `Unidade destino`: :SIGLA_UNIDADE_SUBPROCESSO:
    - `Descrição`: "Cadastro reaberto"

9. O sistema envia notificações por e-mail para a unidade do subprocesso e para a sua unidade superior na hierarquia,
   seguindo estes modelos:

   9.1. Para a unidade do subprocesso:

    ```text
    Assunto: SGC: Reabertura de cadastro de atividades - :SIGLA_UNIDADE_SUBPROCESSO:

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,

    O cadastro de atividades da sua unidade foi reaberto para ajustes.

    Justificativa: :JUSTIFICATIVA:

    Acesse o sistema SGC para realizar as alterações necessárias: :URL_SISTEMA:.
    ```

   9.2. Para a unidade imediatamente superior:

    ```text
    Assunto: SGC: Reabertura de cadastro de atividades - :SIGLA_UNIDADE_SUBPROCESSO:

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUPERIOR:,

    Informamos que o cadastro de atividades da unidade :SIGLA_UNIDADE_SUBPROCESSO: foi reaberto para ajustes.

    Justificativa: :JUSTIFICATIVA:

    Após a conclusão dos ajustes, o cadastro será submetido novamente para sua análise.
    ```

10. O sistema cria internamente alertas:
    10.1. Para a unidade solicitante:
    - `Descrição`: "Cadastro reaberto"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: :SIGLA_UNIDADE:

10.2. Para a unidade superior:
- `Descrição`: "Cadastro reaberto"
- `Processo`: :DESCRICAO_PROCESSO:
- `Data/hora`: Data/hora atual
- `Unidade de origem`: ADMIN
- `Unidade de destino`: :SIGLA_UNIDADE_SUPERIOR:

11. O sistema mostra *toast* "Cadastro reaberto".
