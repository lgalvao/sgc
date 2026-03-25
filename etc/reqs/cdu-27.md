# CDU-27 - Alterar data limite de subprocesso

**Ator:** ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN
- Unidade participante com subprocesso em andamento.

## Fluxo principal

1. No painel, o usuário acessa um processo ativo e na tela `Detalhes do processo`, clica em uma unidade que tenha subprocesso em andamento.
2. O sistema mostra a tela `Detalhes do subprocesso`.
3. O usuário clica no botão `Alterar data limite`.
4. O sistema abre um modal com título "Alterar data limite", campo de data preenchido com a data limite atual da etapa em andamento, e apresenta botões `Cancelar` e `Alterar`.
5. O usuário fornece a nova data limite e clica em `Alterar`.
   5.1. A data limite deve ser estritamente no futuro (amanhã em diante)
6. O sistema atualiza a data limite do subprocesso e envia notificação por e-mail para a unidade do subprocesso, neste modelo:

    ```text
    Assunto: SGC: Data limite alterada

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    A data limite da etapa atual no processo [DESCRICAO_PROCESSO] foi alterada para [NOVA_DATA_LIMITE].
    ```

8. O sistema cria internamente um alerta com as seguintes informações:

    - `Descrição`: "Data limite da etapa [NÚMERO_ETAPA] alterada para [NOVA_DATA_LIMITE]"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: 'ADMIN'
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

9. O sistema fecha o modal e mostra uma mensagem de confirmação "Data limite alterada".
