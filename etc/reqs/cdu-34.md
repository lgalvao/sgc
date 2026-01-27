# CDU-34 - Enviar lembrete de prazo

**Ator:** Sistema/ADMIN

## Fluxo principal

1. O usuário (Administrador) acessa a tela de Acompanhamento de Processos.

2. O sistema exibe os processos e subprocessos, indicando com cores ou ícones aqueles que estão próximos do prazo ou
   atrasados.

3. O usuário seleciona uma ou mais unidades/subprocessos que possuem pendências.

4. O usuário aciona a opção "Enviar lembrete".

5. O sistema exibe um modelo da mensagem que será enviada para confirmação.

6. O usuário confirma o envio.

7. O sistema envia e-mail para os responsáveis pela unidade (Titular e Substitutos).

    ```text
    Assunto: SGC: Lembrete de prazo - [DESCRICAO_PROCESSO]

    Prezado(a) responsável pela [SIGLA_UNIDADE],

    Este é um lembrete de que o prazo para a conclusão da etapa atual do processo [DESCRICAO_PROCESSO] encerra em [DATA_LIMITE].

    Por favor, acesse o sistema para concluir suas pendências: [URL_SISTEMA].
    ```

8. O sistema registra o envio no histórico do processo (Movimentação interna).

    - `Data/hora`: Data/hora atual
    - `Unidade origem`: SEDOC
    - `Unidade destino`: [SIGLA_UNIDADE]
    - `Descrição`: 'Lembrete de prazo enviado'

9. O sistema cria internamente um alerta para a unidade:

    - `Descrição`: "Lembrete: Prazo do processo [DESCRICAO_PROCESSO] encerra em [DATA_LIMITE]"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: SEDOC
    - `Unidade de destino`: [SIGLA_UNIDADE]

10. O sistema exibe mensagem de sucesso "Lembrete enviado".
