Sistema de Gestão de Competências  
Especificação de Casos de Uso

## PERFIS/ATORES

**SEDOC**: Administrador do sistema, responsável por criar, configurar, monitorar processos de mapeamento e criar o Mapa de Competências.

**GESTOR**: Responsável por uma unidade com subordinadas hierárquicas. Pode visualizar e validar mapeamentos das unidades sob sua gestão, mas não editará essas atividades e conhecimentos.

**CHEFE**: Responsável por uma unidade operacional. Pode cadastrar, editar e finalizar atividades e conhecimentos de sua unidade, além de solicitar a validação por unidades superiores.

## CASOS DE USO

### **UC001 \- Gerenciar Processo de Mapeamento**

**Ator Principal**: SEDOC  
**Pré-condições**: Usuário autenticado com perfil SEDOC  
**Fluxo Principal**:

1. Usuário abre a página inicial do sistema.  
2. O Sistema mostra painel listando os processos de mapeamento em andamento e estatísticas gerais de andamento de cada processo.  
3. SEDOC acessa funcionalidade de Iniciar Processo de Mapeamento  
4. O Sistema faz a sincronização com o SGRH da estrutura hierárquica e dos responsáveis. A sincronização é feita importando diretamente do banco de dados do SGRH, todas as unidades e suas unidades superiores e subordinadas, além dos nomes de todos os servidores em atividade em cada unidade, e os titulares de cada unidade.  
5. O Sistema apresenta a árvore de unidades organizacionais atualizadas, incluindo o nome dos responsáveis por cada unidade.  
6. SEDOC seleciona unidades participantes e define data limite para conclusão  
7. SEDOC confirma início do Processo de Mapeamento  
8. O Sistema envia notificações para as unidades participantes, incluindo link/botão que abre a página apropriada no sistema.  
9. O Sistema envia notificações consolidadas para as unidades superiores informando o início do processo de mapeamento das unidades subordinadas a elas.  
10. O Sistema disponibiliza dashboard de monitoramento.

    

**Fluxos Excepcionais**:

* E001.1: Se já existe processo ativo, sistema exibe aviso e impede criação  
* E001.2: Se falha na sincronização com SGRH, sistema exibe erro e impede início  
* E001.3: Se nenhuma unidade selecionada, sistema exibe erro  
* E001.5: Se falha no envio de notificações, sistema registra log e permite reenvio

### **UC002 \- Realizar Atribuição Temporária**

**Ator Principal**: SEDOC  
**Pré-condições**: Processo iniciado e Usuário autenticado com perfil SEDOC  
**Fluxo Principal**:

1. SEDOC acessa funcionalidade de atribuições temporárias  
2. Sistema apresenta lista de unidades organizacionais permitindo pesquisa  
3. SEDOC escolhe uma unidade  
4. Sistema mostra lista de servidores na unidade selecionada, destacando o responsável atual (se houver)  
5. SEDOC seleciona servidor que receberá a atribuição temporária e define data de término da atribuição, além de justificativa  
6. Sistema registra atribuição temporária  
7. Sistema envia notificação para o servidor atribuído  
8. Sistema agenda remoção automática da atribuição na data definida

E002.1: Antes de permitir a atribuição deve fazer as validações essenciais: servidor deve pertencer à unidade, não pode ter outra atribuição temporária, e já não é titular. Se possível deve tornar impossíveis essas escolhas.

### **UC003 \- Monitorar Andamento do Processo**

**Ator Principal**: SEDOC  
**Pré-condições**: Processo de mapeamento iniciado  
**Fluxo Principal**:

1. SEDOC acessa página principal do sistema: painel de monitoramento  
2. Sistema mostra lista de processos de mapeamento em andamento.  
3. Sistema mostra situação consolidada de todas as unidades participantes dentro de cada processo em andamento.  
4. SEDOC pode filtrar por situação, prazo ou unidade, inclusive recolher/expandir a árvore e ver estatísticas agregadas (por exemplo, número de unidades em andamento, número de unidades não iniciadas.  
5. Se houver processos próximos ao prazo final de, o Sistema apresenta indicadores de alertas de prazo e permite enviar um lembrete por e-mail.

**Fluxos Excepcionais**:

* E003.1: Se unidade está com prazo vencido, sistema destaca com alerta visual

### **UC004 \- Gerenciar Unidades Subordinadas**

**Ator Principal**: GESTOR  
**Pré-condições**: Processo iniciado, usuário com perfil GESTOR  
**Fluxo Principal**:

1. GESTOR acessa sistema via notificação recebida  
2. O Sistema verifica se usuário está atualmente acumulando cargos (ex. Coordenação e Chefia) e se for este o caso, solicita que GESTOR seleciona unidade de trabalho.   
   1. A informação de acumulação é deduzida a partir dos dados importados do SGRH: a existência de uma substituição ativa para uma unidade e uma titularidade ativa para outra unidade configura uma acumulação.  
3. O Sistema mostra árvore de unidades subordinadas com as situações correspondentes  
4. GESTOR seleciona unidade para detalhamento  
5. O Sistema apresenta atividades e conhecimentos cadastrados  
6. Para unidades Finalizadas, GESTOR escolhe validar ou devolver  
7. Se for escolhido Validação: Sistema marca unidade como VALIDADA pela unidade e notifica próximo nível, acima.  
8. Se for escolhida Devolução: Sistema retorna unidade para "Em andamento" e notifica o nível abaixo, correspondente.

**Fluxos Excepcionais**:

* E004.1: Se unidade não estiver Finalizada, sistema só permite visualização da situação: não são mostrados os conhecimentos e as atividades.  
* E004.2: Se última instância de validação (SEDOC), sistema marca unidade como HOMOLOGADA.

### **UC005 \- Cadastrar Atividades e Conhecimentos**

**Ator Principal**: CHEFE  
**Pré-condições**: Processo iniciado, usuário com perfil CHEFE  
**Fluxo Principal**:

1. CHEFE acessa sistema via notificação recebida  
2. Sistema apresenta interface de cadastro de atividades e conhecimentos para a unidade  
3. CHEFE cadastra atividades da unidade  
4. Para cada atividade, CHEFE cria e associa conhecimentos necessários  
5. CHEFE pode criar novos conhecimentos ou reutilizar existentes (porém os conhecimentos, mesmo com mesma descrição não serão vinculados).  
6. CHEFE revisa informações cadastradas  
7. CHEFE confirma finalização do cadastro  
8. Sistema bloqueia edição e altera situação para "Finalizada"  
9. Sistema notifica unidade superior hierárquica quanto à finalização da unidade.

**Fluxos Excepcionais**:
* E005.1: Se tiver sido devolvido para revisão, sistema desbloqueia edição  

### **UC006 \- Gerar Mapa de Competências**

**Ator Principal**: SEDOC  
**Pré-condições**: Unidade com situação "Finalizada"  
**Fluxo Principal**:

1. A SEDOC acessa funcionalidade de geração do mapa de competências  
2. A SEDOC escolhe uma unidade  
3. O Sistema mostra todas as atividades e conhecimentos da unidade  
4. A SEDOC analisa informações e cria competências baseadas nos dados coletados, relacionando as atividades e os respectivos conhecimentos com as competências.  
   1. As competências serão, essencialmente, textos descritivos que são redigidos pela SEDOC com base nas atividades e seus respectivos conhecimentos (fornecidos pelas unidades organizacionais).  
5. O Sistema gera o Mapa de Competências  
   1. Apesar do nome, este mapa não inclui a relação entre atividades/conhecimentos e as competências criadas; trata-se de uma lista com as descrições das competências agrupadas por unidade organizacional.  
   2. O Sistema permite exportar o mapa como PDF ou planilha, para posterior disponibilização.

### **UC007 \- Consultar Histórico de Processos de mapeamento**

**Ator Principal**: SEDOC  
**Fluxo Principal**:

1. SEDOC acessa funcionalidade de histórico de processos  
2. Sistema apresenta filtros de pesquisa: período, situação, unidades participantes, criador  
3. SEDOC define critérios de pesquisa (opcional)  
4. Sistema exibe lista paginada de processos com informações resumidas:  
   * Nome do processo  
   * Criador do processo  
   * Situação atual  
   * Data de criação e finalização  
   * Número de unidades participantes  
   * Percentual de conclusão  
5. Usuário seleciona processo para detalhamento  
6. Sistema apresenta informações detalhadas do processo:  
   * Timeline com marcos principais  
   * Lista de unidades participantes e suas situações  
   * Estatísticas consolidadas  
   * Mapas de competências gerados (se aplicável)  
   * Histórico de atribuições temporárias do período

## SITUAÇÕES

### **Situações do Processo de Mapeamento (GERAL)**

* **Criado**: Processo configurado mas não iniciado  
* **Em andamento**: Ao menos uma unidade iniciou o cadastro  
* **Apto**: Todas as validações, de todas as unidades envolvidas no processo Finalizadas, apto para geração do mapa  
* **Finalizada**: Mapa de Competências gerado para todas as unidades

### **Situações do Processo de Mapeamento (por unidade)**

* **Não iniciado**: Notificações enviadas, mas cadastro não iniciado  
* **Em andamento**: Cadastro iniciado  
* **Apto**: Todas as validações concluídas, apto para geração do mapa  
* **Finalizada**: Mapa de Competências gerado

### **Situações das Unidades no Processo de Mapeamento:**

* **Não iniciado**: Unidade notificada mas sem cadastro iniciado  
* **Em andamento**: Unidade com cadastro em progresso  
* **Finalizada**: Cadastro concluído pelo CHEFE, aguardando validação  
* **Validada**: Aprovada pela hierarquia superior  
* **Homologada**: Cadastro recebido e homologado pela SEDOC.

## REGRAS DE NEGÓCIO

**RN001**: Apenas usuários SEDOC podem criar e iniciar processos de mapeamento.

**RN002**: Pode haver mais de um processo de mapeamento ativo simultaneamente.

**RN003**: Atividades e conhecimentos só podem ser editados por usuários com perfil CHEFE na respectiva unidade, ou por usuários que receberam atribuição temporária pela SEDOC.

**RN004**: A validação de uma unidade só pode ser feita pelo GESTOR da unidade superior, ou, quando já validada pela última instância, pela SEDOC (neste último caso, será chamada Homologação).

**RN005**: Uma vez marcada como finalizada pelo CHEFE, o cadastro na unidade fica bloqueado, só sendo desbloqueado se houver uma eventual devolução.

**RN006**: Apenas SEDOC pode criar atribuições temporárias.

**RN007**: Notificações consolidadas são enviadas para unidades superiores quando há unidades subordinadas participantes.

**RN008**: Usuários com múltiplos perfis devem selecionar o perfil com o qual vão operar (CHEFE ou GESTOR), antes de acessar funcionalidades.

**RN009**: O Sistema deve sincronizar dados do SGRH periodicamente (de forma automática); e obrigatoriamente no início de processos e no momento de atribuição temporária. Neste último caso, a sincronização pode ser específica, para aquela unidade apenas.

**RN010**: Atribuições temporárias são removidas automaticamente na data de término definida.

**RN011**: O Mapa de Competências só pode ser gerado após finalização do cadastro de atividades/conhecimentos e a validação de todas as unidades superiores.

**RN012**: Uma unidade só pode ser homologada péla SEDOC se todas as validações das unidades subordinadas forem finalizadas antes.

**RN013**: Cada atividade deve ter pelo menos um conhecimento associado

**RN014**: Conhecimentos com a mesma descrição permanecem independentes, mesmo quando reutilizados.

