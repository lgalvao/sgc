# CDU-34 - Enviar lembrete de prazo

**Ator:** ADMIN

## Fluxo principal

1. O usuário acessa o `Painel`

2. O usuário entra em um processo em andamento e escolhe uma unidade participante.

3. O sistema mostra a tela `Detalhes do subprocesso` com as informações do subrocesso e unidade. 
 
4. O usuário clica em `Enviar lembrete`.

5. O sistema mostra um modal de confirmação, com título "Enviar lembrete", e texto "Confirma envio de lembrete para a unidade [SIGLA_UNIDADE]?", e botões `Cancelar` e `Enviar`. 

6. O usuário confirma.

7. O sistema envia e-mail para o responsável pela unidade.

    ```text
    Assunto: SGC: Lembrete de prazo - [DESCRICAO_PROCESSO]

    Prezado(a) responsável pela [SIGLA_UNIDADE],

    Este é um lembrete de que o prazo para a conclusão da etapa atual do processo [DESCRICAO_PROCESSO] encerra em [DATA_LIMITE].

    Por favor, acesse o sistema para concluir suas pendências: [URL_SISTEMA].
    ```

8. O sistema cria internamente um alerta para a unidade:

    - `Descrição`: "Lembrete: Prazo do processo [DESCRICAO_PROCESSO] encerra em [DATA_LIMITE]"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: [SIGLA_UNIDADE]

9. O sistema exibe mensagem de sucesso "Lembrete enviado".
