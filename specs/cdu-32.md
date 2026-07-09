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

3. O usuário aciona uma unidade com subprocesso na situação 'Mapa homologado'.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada.

5. O usuário aciona `Reabrir cadastro`.

6. O sistema abre um modal "Reabertura de cadastro" com campo `Justificativa` obrigatória.

7. O usuário informa a justificativa e aciona `Reabrir`.

8. O sistema altera a situação do subprocesso para 'Cadastro em andamento'.

9. O sistema registra uma movimentação para o subprocesso:
    - `Descrição`: "Cadastro reaberto"
    - `Data/hora`: :DATA_HORA:
    - `Unidade origem`: ADMIN
    - `Unidade destino`: :UNIDADE_SUBPROCESSO:

10. O sistema envia notificação por e-mail para a unidade do subprocesso, seguindo este modelo:

    ```text
    Assunto: SGC: Reabertura de cadastro de atividades

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,

    O cadastro de atividades da sua unidade foi reaberto para ajustes.

    Justificativa: :JUSTIFICATIVA:

    Acesse o sistema SGC para realizar as alterações necessárias: :URL_SISTEMA:.
    ```

11. O sistema cria um alerta:
    - `Descrição`: "Cadastro reaberto"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: :UNIDADE_SUBPROCESSO:

12. O sistema redireciona para `Painel` e mostra o *toast* "Cadastro reaberto".
