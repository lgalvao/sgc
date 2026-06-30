# CDU-27 - Alterar data limite de subprocesso

## Atores

- ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN
- Unidade participante com subprocesso em andamento.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo em andamento, e na tela `Detalhes do processo` clica em uma unidade que
   tenha um subprocesso em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada.

3. O usuário aciona `Alterar data limite`.

4. O sistema abre um modal com título "Alterar data limite", campo de data preenchido com a data limite da
   etapa atual, e botões `Cancelar` e `Alterar`.

5. O usuário fornece a nova data limite e aciona `Alterar`. 
   - A data limite deve ser estritamente no futuro.

6. O sistema atualiza a data limite do subprocesso e envia notificação por e-mail para a unidade do subprocesso:

    ```text
    Assunto: SGC: Data limite alterada

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,

    A data limite da etapa atual no processo :DESCRICAO_PROCESSO: foi alterada para :NOVA_DATA_LIMITE:.
    ```

7. O sistema cria um alerta com as seguintes informações:
    - `Descrição`: "Data limite da etapa :NUMERO_ETAPA: alterada para :NOVA_DATA_LIMITE:"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: :DATA_HORA:
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: :SIGLA_UNIDADE_SUBPROCESSO:

8. O sistema fecha o modal e mostra um *toast*: "Data limite alterada".