# CDU-33 - Reabrir revisão de cadastro

## Atores

- ADMIN

## Pré-condições

- Login realizado com perfil ADMIN
- Processo do tipo Revisão
- Ao menos um subprocesso na situação 'Mapa homologado'

## Fluxo principal

1. No `Painel`, o usuário aciona um processo de revisão em andamento e escolhe a unidade com subprocesso na situação
   'Mapa homologado'.

2. Na tela `Detalhes do subprocesso`, o usuário aciona `Reabrir revisão de cadastro`.

3. O sistema mostra um modal com título "Reabertura de revisão de cadastro", campo `Justificativa` obrigatório, e botões
   `Cancelar` e `Reabrir cadastro`.

4. O usuário informa a justificativa e aciona `Reabrir cadastro`.

5. O sistema altera a situação do subprocesso para 'Revisão do cadastro em andamento'.

6. O sistema registra uma movimentação para o subprocesso:
    - `Descrição`: "Revisão do cadastro reaberta"
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: ADMIN
    - `Unidade destino`: :UNIDADE_SUBPROCESSO:
    - `Justificativa`

7. O sistema envia notificação por e-mail para a unidade do subprocesso:
    ```text
    Assunto: SGC: Reabertura de revisão de cadastro

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,

    A revisão do cadastro de atividades da sua unidade foi reaberta para ajustes.

    Justificativa: :JUSTIFICATIVA:

    Acesse o sistema para realizar as alterações necessárias: :URL_SISTEMA:.
    ```
8. O sistema cria um alerta para a unidade do subprocesso:
    - `Descrição`: "Revisão do cadastro reaberta"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: :UNIDADE_SUBPROCESSO:

9. O sistema redireciona para o `Painel` e mostra *toast* "Revisão reaberta".
