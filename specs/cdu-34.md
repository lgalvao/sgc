# CDU-34 - Enviar lembrete de prazo

## Atores

- ADMIN

## Pré-condições

- Login realizado com perfil ADMIN

## Fluxo principal

1. No `Painel`, o usuário aciona um processo em andamento e escolhe uma unidade participante.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade.

3. O usuário clica em `Enviar lembrete`.

4. O sistema mostra um modal de confirmação, com título `Enviar lembrete`, e texto "Confirma envio de lembrete para a
   unidade :SIGLA_UNIDADE:?", e botões `Cancelar` e `Enviar`.

5. O usuário aciona `Enviar`.

6. O sistema envia uma notificação por e-mail para o responsável pela unidade, com este modelo:

    ```text
    Assunto: SGC: Lembrete de prazo - :DESCRICAO_PROCESSO:

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPRROCESSO:,

    Este é um lembrete de que o prazo para a conclusão da etapa atual do processo 
    :DESCRICAO_PROCESSO: encerra em :DATA_LIMITE:.

    Acesse o sistema para concluir as realizar as ações remanescentes: :URL_SISTEMA:.
    ```

8. O sistema cria internamente um alerta para a unidade:
  - `Descrição`: "Lembrete: Prazo do processo :DESCRICAO_PROCESSO: encerra em :DATA_LIMITE:"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: :SIGLA_UNIDADE_SUBPRROCESSO:

9. O sistema mostra *toast* "Lembrete enviado" e permanece na mesma tela.
