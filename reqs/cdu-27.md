# CDU-27 - Alterar data limite de subprocesso

**Ator:** ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN
- Unidade participante com subprocesso iniciado e ainda não finalizado.

## Fluxo principal

1. No Painel, ADMIN acessa um processo ativo e na tela Detalhes do processo, clica em uma unidade com subprocesso ativo.
2. Sistema mostra tela Detalhes do subprocesso.
3. ADMIN clica no botão `Alterar data limite`.
4. Sistema abre modal "Alterar data limite" com campo de data preenchido com a data limite atual da etapa em andamento, e apresenta botões `Cancelar` e `Confirmar`.
5. ADMIN altera a data limite conforme necessário e clica em `Confirmar`.
6. Sistema atualiza a data limite do subprocesso.
7. Sistema envia notificação por e-mail para a unidade do subprocesso, neste formato:

    ```text
    Assunto: SGC: Data limite alterada

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    A data limite da etapa atual no processo [DESCRICAO_PROCESSO] foi alterada para [NOVA_DATA_LIMITE].
    ```

8. Sistema cria internamente um alerta com as seguintes informações:

   - `Descrição`: "Data limite da etapa [NÚMERO_ETAPA] alterada para [NOVA_DATA_LIMITE]"
   - `Processo`: [DESCRICAO_PROCESSO]
   - `Data/hora`: Data/hora atual
   - `Unidade de origem`: 'SEDOC'
   - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

9. Sistema mostra mensagem de confirmação: "Data limite alterada com sucesso" e fecha o modal.
