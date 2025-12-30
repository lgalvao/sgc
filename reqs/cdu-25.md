# CDU-25 - Alterar data limite de subprocesso

**Ator:** ADMIN

**Pré-condições:**

- Usuário logado com perfil ADMIN
- Unidade participante com subprocesso iniciado e ainda não finalizado.

**Fluxo principal:**

- No Painel, ADMIN acessa um processo ativo e na tela Detalhes do processo, clica em uma unidade com subprocesso ativo.
- Sistema mostra tela Detalhes do subprocesso.
- ADMIN clica no botão Alterar data limite.
- Sistema abre modal "Alterar data limite" com campo de data preenchido com a data limite atual da etapa em andamento, e apresenta botões Cancelar e Confirmar.
- ADMIN altera a data limite conforme necessário e clica em Confirmar.
- Sistema atualiza a data limite do subprocesso.
- Sistema envia notificação por e-mail para a unidade do subprocesso, neste formato:

Assunto: SGC: Data limite alterada

Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

A data limite da etapa atual no processo [DESCRICAO_PROCESSO] foi alterada para [DATA_LIMITE].

- Sistema cria internamente um alerta com as seguintes informações:
- Descrição: "Data limite da etapa [NÚMERO_ETAPA] alterada para [DATA_LIMITE]"
- Processo: [DESCRICAO_PROCESSO]
- Data/hora: Data/hora atual
- Unidade de origem: 'SEDOC'
- Unidade de destino: [SIGLA_UNIDADE_SUBPROCESSO]

- Sistema mostra mensagem de confirmação: "Data limite alterada com sucesso" e fecha o modal.
