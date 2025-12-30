# CDU-24B - Homologar mapas de competências em bloco

**Ator:** GESTOR ou ADMIN

**Pré-condições:**

- Usuário logado com perfil GESTOR ou ADMIN.
- Processo de mapeamento ou de revisão iniciado que tenha a unidade como participante.
- Subprocesso nas situações ‘Mapa validado’ ou ‘Mapa com sugestões’ e com localização atual na unidade do usuário.

**Fluxo principal:**

- No Painel, o usuário acessa um processo de mapeamento ou revisão em andamento.
- O sistema mostra tela Detalhes do processo.
- O sistema identifica que existem unidades subordinadas com subprocessos elegíveis para aceitação/homologação em bloco do mapa de competências (de acordo com as pré-condições do caso de uso).
- Na seção de unidades participantes, abaixo da árvore de unidades, sistema mostra:
- Se o usuário tiver perfil GESTOR, botão Aceitar mapa de competências em bloco.
- Se o usuário tiver perfil ADMIN, botão Homologar mapa de competências em bloco.
- O usuário clica no botão Aceitar mapa de competências em bloco ou Homologar mapa de competências em bloco, conforme o caso.
- O sistema abre modal de confirmação, com os elementos a seguir:
- Título "Aceite de mapa em bloco" ou "Homologação de mapa em bloco";
- Texto “Selecione abaixo as unidades cujos mapas deverão ser aceitos:” ou “Selecione abaixo as unidades cujos mapas deverão ser homologados:” (conforme o caso);
- Lista das unidades operacionais ou interoperacionais subordinadas cujos mapas poderão ser aceitos/homologados, sendo apresentados, para cada unidade, um checkbox (selecionado por padrão), a sigla e o nome; e
- Botão Cancelar e botão Registrar aceite ou Homologar (conforme o caso).
- Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação, permanecendo na tela Detalhes do processo.
- O usuário clica em Registrar aceite ou Homologar.
- O sistema atua, para cada unidade selecionada, da seguinte forma:
- Se usuário for GESTOR:
- O sistema registra uma análise de validação para o subprocesso:
- Data/hora: [Data/hora atual]
- Unidade: [SIGLA_UNIDADE_ATUAL]
- Resultado: "Aceite"
- Observação: “De acordo com a validação do mapa realizada pela unidade”
- O sistema registra uma movimentação para o subprocesso:
- Data/hora: [Data/hora atual]
- Unidade origem: [SIGLA_UNIDADE_ATUAL]
- Unidade destino: [SIGLA_UNIDADE_SUPERIOR]
- Descrição: "Cadastro de atividades e conhecimentos aceito"
- O sistema cria internamente um alerta:
- Descrição: "Cadastro de atividades da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise"
- Processo: [DESCRIÇÃO_PROCESSO]
- Data/hora: [Data/hora atual]
- Unidade de origem: [SIGLA_UNIDADE_ATUAL]
- Unidade de destino: [SIGLA_UNIDADE_SUPERIOR]
- Se usuário for ADMIN:
- O sistema registra uma movimentação para o subprocesso:
- Data/hora: [Data/hora atual]
- Unidade origem: “SEDOC”
- Unidade destino: “SEDOC”
- Descrição: "Cadastro de atividades e conhecimentos homologado"
- O sistema altera a situação do subprocesso da unidade para ‘Cadastro homologado’.
- Caso o usuário seja GESTOR, o sistema envia esta notificação por e-mail para a unidade superior:

Assunto: SGC: Cadastros de atividades e conhecimentos submetidos para análise

Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

Os cadastros de atividades e conhecimentos das unidades [LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS] no processo [DESCRIÇÃO_PROCESSO] foram submetidos para análise por essa unidade.

As análises já podem ser realizadas no Sistema de Gestão de Competências ([URL_SISTEMA]).

- O sistema mostra mensagem de confirmação: "Cadastros aceitos em bloco” ou “Cadastros homologados em bloco"  (conforme o caso) e redireciona para o Painel.
