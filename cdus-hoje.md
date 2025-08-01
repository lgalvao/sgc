Sistema de Gestão de Competências

# **Especificação de Casos de Uso**

## **Convenções** {#convenções}

O atual documento segue convenções de formatação e estilo visando à padronização e clareza.

* Nomes de componentes visuais: negrito e itálico.  
* Mensagens a serem mostradas pelo sistema: texto entre aspas.  
* Nome de telas: negrito.  
* Itens variáveis dentro de mensagens: entre colchetes  
* Nome do ator (perfil) que desempenha a ação no sistema, em maiúsculas (Exceções: Usuário e Sistema).

## **Atores e perfis** {#atores-e-perfis}

O sistema de Gestão de Competências opera com os seguintes perfis de usuários, cujas atribuições e acessos são automaticamente reconhecidos com base na condição de responsabilidade ou lotação em uma unidade, de acordo com o SGRH, ou por atribuição de responsabilidade temporária realizada no próprio sistema. Caso um usuário acumule mais de um perfil ou seja responsável por mais de uma unidade, será necessário selecionar o perfil e a unidade de trabalho após o login.

* **ADMIN**: Administrador SEDOC. É responsável por criar, configurar e monitorar processos, além de criar/ajustar os mapas de competências das unidades. A unidade SEDOC é tratada como unidade raiz da estrutura organizacional para efeito dos processos de mapeamento, revisão e diagnóstico de competências.  
* **GESTOR**: Responsável por uma unidade intermediária (exemplo: Coordenador). Pode visualizar e validar as informações cadastradas pelas unidades sob sua gestão, submetendo para análise da unidade superior, ou devolver à unidade subordinada para realização de retificações.  
* **CHEFE**: Responsável por uma unidade operacional ou interoperacional. Pode cadastrar as informações de sua unidade em cada processo e submeter essas informações para validação pela unidade superior.  
* **SERVIDOR**: Servidor lotado em uma unidade operacional ou interoperacional. Este papel só atua nos processos de diagnóstico de competências.

## **CDU-001 \- Realizar login e exibir estrutura das telas** {#cdu-001---realizar-login-e-exibir-estrutura-das-telas}

**Ator principal:** Qualquer pessoa autorizada a acessar o sistema (com qualquer dos quatro perfis: ADMIN, GESTOR, CHEFE ou SERVIDOR).

**Ator secundário:** Sistema Acesso do TRE-PE

**Pré-condições:**

* Usuário deve possuir credenciais válidas (título e senha de rede no TRE-PE)  
* Usuário deve estar cadastrado no SGRH com lotação ativa em alguma unidade

**Fluxo principal:**

1. Usuário acessa o sistema  
2. Sistema exibe a tela de **Login**  
3. Usuário informa suas credenciais: número do título de eleitor e senha  
4. Sistema verifica título/senha através da API do Sistema Acesso  
5. Sistema consulta perfis e unidades do usuário  
   1. Um usuário pode estar em várias unidades (ex. substituição, atribuição temporária) e também ter mais de um perfil.  
6. Sistema determina os perfis disponíveis para usuário seguindo estas regras:  
   1. ADMIN: Servidor cadastrado como administrador do sistema.  
   2. GESTOR: Servidor responsável por uma unidade intermediária ou interoperacional..  
   3. CHEFE: Servidor responsável por unidades operacionais, interoperacionais ou pela SEDOC.  
   4. SERVIDOR: Servidor que não é o responsável por sua unidade de lotação.  
7. Sistema determina quais pares 'perfil-unidade' se aplicam ao usuário logado.  
   1. Se usuário possuir apenas um perfil e uma unidade:  
      * Sistema guarda perfil e unidade definidos  
   2. Se usuário possuir múltiplos perfis ou unidades:  
      * Sistema expande a tela de login para permitir a seleção de perfil e unidade  
      * Usuário seleciona o perfil/unidade com o qual vai atuar  
      * Sistema guarda perfil e unidade definidos  
8. Sistema exibe a estrutura de telas da aplicação, composta pelas seções: ***Barra de navegação***, ***Conteúdo*** e ***Rodapé***.  
   1. A ***Barra de Navegação***, que é sempre mostrada no topo das telas (exceto para tela de login) tem as seguintes regras de exibição:  
      * Elementos alinhado à esquerda:  
        * ***Ícone/logotipo do sistema*** (link para abrir a tela **Painel**)  
        * Link ***Painel***, para tela **Painel**  
        * Link ***Minha unidade***, para tela **Detalhe da unidade**, apresentando os dados da unidade do usuário logado  
        * Link ***Relatórios***, para tela **Relatórios**  
        * Link ***Histórico***, para tela **Histórico de processos**  
      * Elementos alinhado à direita:  
        * Se perfil ADMIN: ***Ícone de engrenagem*** para acesso à tela de configurações do sistema  
        * ***\[Perfil\] \- \[Sigla da unidade\]*** \- Texto fixo, sem interatividade (ex. CHEFE \- SESEL)  
        * ***Ícone de logout*** \- faz logout e mostra tela **Login**  
   2. O ***Conteúdo*** compõe a parte central da tela, onde serão exibidas todas as telas.  
   3. O ***Rodapé*** é a seção localizada na parte inferior das telas (exceto na tela de login) em que aparecerá a informação da versão do sistema assim como a informação ‘Desenvolvido por SESEL/COSIS/TRE-PE’.  
9. Sistema mostra inicialmente a tela **Painel** na seção de conteúdo das telas.

**Fluxo alternativo:**

5a. Caso o sistema não consiga autenticar o usuário com as credenciais fornecidas, deverá mostrar a mensagem 'Título ou senha inválidos.'

## **CDU-002 \- Visualizar Painel** {#cdu-002---visualizar-painel}

**Ator principal**: Usuário (todos os perfis)

**Pré-condição:** Usuário ter feito login (qualquer perfil)

**Fluxo principal:**

1. Sistema exibe a tela **Painel**, com as seções ***Processos Ativos*** e ***Alertas***.  
2. Na seção ***Processos Ativos***, Sistema mostra uma tabela de processos ativos (título 'Processos'). Devem ser mostrados apenas os processos que incluam entre as unidades participantes a unidade do usuário e/ou suas unidades subordinadas.  
   1. Campos da tabela  
      * ***Descrição***: Descrição dada ao processo no momento do seu cadastro  
      * ***Tipo***: Tipo do processo ('Mapeamento', 'Revisão' ou 'Diagnóstico')  
      * ***Unidades Participantes***: Lista textual das unidades, contendo apenas as unidades de nível mais alto abaixo da unidade raiz que possuam todas as suas unidades subordinadas participando do processo. Por exemplo, para uma secretaria com duas coordenadorias A e B, se apenas as seções da coordenadoria B participarem do processo, deverá aparecer apenas o nome da coordenadoria B.  
      * ***Situação***: Situação do processo (‘Criado’, ‘Em andamento’ ou ‘Finalizado’).  
   2. Regras de exibição e funcionamento  
      * Processos na situação 'Criado' deverão ser listados apenas se o usuário estiver logado com o perfil ADMIN.  
      * Cabeçalhos das colunas deverão ser clicáveis, possibilitando ordenação em ordem crescente e decrescente.  
      * Itens da tabela serão clicáveis com estas regras:  
        * Para perfil ADMIN, clicar em um processo na situação 'Criado' mostra tela **Cadastro de processo** com os dados do processo (ver [CDU-003 \- Manter processo]).  
        * Para qualquer perfil, clicar em processos nas situações 'Em Andamento' e 'Finalizado' mostrará as telas **Detalhes do processo** ou **Detalhes do processo na unidade**, dependendo do perfil do usuário (ver [CDU-005 \- Detalhar processo](#cdu-005---detalhar-processo)).  
3. Na seção ***Alertas***, Sistema mostra uma tabela de alertas referentes à unidade do usuário (título 'Alertas')  
   1. Campos da tabela  
      * ***Data/Hora***: Informação da data e da hora de geração do alerta  
        * ***Processo***: Descrição do processo a que se refere o alerta  
        * ***Unidade***: Unidade geradora do alerta  
        * ***Descrição***: Descrição do alerta  
   2. Regras de exibição e funcionamento  
      * Dados devem estar inicialmente em ordem decrescente por data/hora de envio do alerta, podendo-se alternar a ordenação clicando no cabeçalho.  
      * Cabeçalhos da coluna ***Processo*** poderá ser clicado para alterar a ordenação dos dados da tabela.  
        * Ordenação deve ser feita tendo como primeiro critério a descrição do processo (asc/desc) e em seguida a data/hora (desc).  
      * Alertas ainda não visualizados pelo usuário logado serão exibidos em negrito.  
      * Na primeira visualização do alerta pelo usuário logado, este deverá ser marcado como visualizado para o usuário, de maneira a ser exibido sem destaque a partir da próxima visualização.

## **CDU-003 \- Manter processo** {#cdu-003---manter-processo}

**Ator principal**: ADMIN

**Pré-condição:** Login realizado com perfil ADMIN

**Fluxo principal:**

1. Sistema mostra **Painel**.

**Criação:**

2. Se usuário quiser **criar** um processo, escolhe o botão ***Criar processo***.  
3. Sistema muda para a tela **Cadastro de processo** e apresenta um formulário contendo:  
   1. Campo **Descrição**  
   2. Campo **Tipo do processo**, com opções: Mapeamento, Revisão e Diagnóstico  
   3. Quadro **Unidades participantes**, contendo uma árvore de unidades com checkboxes para cada uma.  
   4. A lista de unidades só deve incluir unidades que não estejam participando de um processo ativo do tipo selecionado.  
   5. O comportamento de seleção das unidades participantes deve seguir estas regras:  
      1. Ao clicar em uma unidade intermediária na árvore, todas as unidades abaixo dela devem ser automaticamente selecionadas;  
      2. Se todas as unidades de uma subárvore estiverem selecionadas, o nó raiz desta subárvore deve ser automaticamente selecionado;  
      3. Se um nó de uma subárvore for desselecionado, o nó raiz da subárvore deve ficar num estado intermediário, indicando que há nós selecionados da subárvore, mas não todos;  
      4. Se todas as unidades de uma subárvore forem desselecionadas, o nó raiz desta subárvore deve ser automaticamente desselecionado.  
      5. Se a raiz de uma subárvore for uma unidade interoperacional, ela poderá ser selecionada ainda que as unidades subordinadas não o sejam.  
      6. Campo **Data limite** para término da etapa inicial do processo  
      7. Botões 'Cancelar', 'Salvar' e 'Iniciar processo'  
4. Usuário fornece os dados solicitados, escolhe o tipo do processo desejado e seleciona as unidades participantes.   
5. Usuário clica em 'Salvar'.  
6. Sistema faz estas validações (com mensagens de falha de validação entre aspas):   
   1. Descrição deve estar preenchida — "Preencha a descrição".  
   2. Ao menos uma unidade deve ser selecionada — "Pelo menos uma unidade participante deve ser incluída."  
7. Sistema cria o processo internamente, colocando-o na situação 'Criado', e mostra mensagem "Processo criado.".  
8. Sistema redireciona para **Painel**, onde já deve ser mostrada uma linha para o processo recém-criado.

**Edição:**

9. Se usuário quiser **editar** o processo, clica na linha do processo na listagem de processos do **Painel** (apenas processos na situação ‘Criado’ podem ser editados).  
10. Sistema abre a tela **Cadastro de processo** preenchida com os dados atuais do processo.  
11. Usuário modifica os dados desejados (descrição, unidades participantes e data limite)   
12. Usuário escolhe o botão 'Salvar'.  
13. Sistema valida os dados depois de editados, de acordo com as mesmas regras aplicadas no momento do cadastro.  
14. Sistema atualiza o processo e mostra mensagem "Processo alterado.".

**Remoção:**

15. Se usuário quiser **remover** o processo, clica na linha do processo na listagem de processos do **Painel** (apenas processos na situação ‘Criado’ podem ser removidos).  
16. Sistema abre a tela **Cadastro de processo** preenchida com os dados atuais do processo.  
17. Usuário escolhe o botão 'Remover'.  
18. Sistema mostra diálogo de confirmação "Remover o processo '\[Descrição do processo\]'? Esta ação não pode ser desfeita.", botões 'Remover' / 'Cancelar'.  
19. Se escolher 'Cancelar' no diálogo, Sistema fecha o diálogo e permanece na tela de cadastro, sem efetuar alterações.  
20. Ao escolher 'Remover' no diálogo, Sistema remove o processo permanentemente, mostra a mensagem "Processo \[Descrição do Processo\] removido" e redireciona para **Painel.**

**Fluxo alternativo:**

3.3a. Se, ao final do preenchimento dos campos da tela de Cadastro, usuário escolher o botão 'Iniciar processo', Sistema realiza as validações dos dados informados, cria o processo e segue para o fluxo do [CDU-004 \- Iniciar processo de mapeamento](#cdu-004---iniciar-processo-de-mapeamento) ou [CDU-017 \- Iniciar processo de revisão](#cdu-017---iniciar-processo-de-revisão) dependendo do tipo do processo.

## **CDU-004 \- Iniciar processo de mapeamento** {#cdu-004---iniciar-processo-de-mapeamento}

**Ator principal**: ADMIN

**Atores secundário**: Serviço de envio de e-mails (dispara as notificações)

**Pré-condição:** Login realizado com perfil ADMIN

**Fluxo principal:**

1. No **Painel**, ADMIN clica em um processo de mapeamento que esteja na situação 'Criado''.  
2. Sistema muda para a tela **Cadastro de processo** com os campos preenchidos com as informações do processo selecionado.  
3. ADMIN escolhe 'Iniciar processo'.  
4. Sistema mostra diálogo de confirmação: "Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes serão notificadas por e-mail.", botões 'Confirmar' / 'Cancelar'.  
5. ADMIN confirma.  
6. Sistema muda a situação do processo de mapeamento para 'Em andamento';  
7. Sistema cria internamente processos de unidade para todas as unidades participantes que sejam operacionais ou interoperacionais, com os seguintes campos e valores iniciais:  
   1. **Processo de origem**: O processo de mapeamento que originou este processo de unidade.  
   2. **Tipo**: 'Mapeamento'  
   3. **Unidade atual**: A unidade que acabou de ser notificada.  
   4. **Unidade anterior**: SEDOC  
   5. **Data limite**: Data copiada da data limite do processo de origem.  
   6. **Observações**: Campo de texto formatado para descrição de mudanças no mapa. Inicialmente vazio.  
   7. **Sugestões**: Campo de texto formatado, para inclusão de sugestões das unidades. Inicialmente vazio.  
8. Sistema envia notificações por e-mail para todas as unidades participantes.   
   1. As notificações devem seguir para os endereços de e-mail das unidades (ex. *sesel@tre-pe.jus.br*) e dos servidores responsáveis por elas.  
   2. Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo:

      ***Assunto:** SGC: Início de processo de mapeamento de competências*

      *Prezado(a) responsável pela \[SIGLA\_UNIDADE\],*

      *Comunicamos o início do processo de mapeamento de competências para sua unidade.* 

      *Já é possível realizar o cadastro de atividades e conhecimentos no Sistema SGC (\[URL\_SISTEMA\]).*

      *O prazo para conclusão desta etapa do processo é \[DATA\_LIMITE\].*

      *Atenciosamente,*  
      *Equipe da SEDOC*

   3. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das unidades operacionais e interoperacionais subordinadas a elas, segundo o modelo:

      ***Assunto:** SGC: Início de processo de mapeamento de competências em unidades subordinadas*

      *Prezado(a) responsável pela \[SIGLA\_UNIDADE\],*

      *Comunicamos o início do processo de mapeamento de competências das unidades \[LISTA\_DE\_SIGLAS\_DAS\_UNIDADES\_SUBORDINADAS\]. Estas unidades Já podem iniciar o cadastro de atividades e conhecimentos. À medida que estes cadastros forem sendo disponibilizados, estarão disponíveis para sua visualização e validação..*

      *O prazo para conclusão desta etapa do processo é \[DATA\_LIMITE\].*

      *Acompanhe o processo no Sistema de Gestão de Competências: \[URL\_SISTEMA\].*

      *Atenciosamente,*  
      *Equipe da SEDOC*

9. Sistema cria internamente alertas para todas as unidades participantes.  
   1. Para cada unidade operacional será criado um alerta com a seguinte informação:  
      1. **Descrição: “**Início do processo”  
      2. **Processo**: \[DESCRICAO DO PROCESSO\]  
      3. **Data/hora**: Data/hora atual  
      4. **Data limite:** Data comunicada nas notificações.  
      5. **Unidade de origem:** SEDOC   
      6. **Unidade de destino:** \[SIGLA\_UNIDADE\].  
   2. Para cada unidade intermediária será criado um alerta com a seguinte informação:  
      1. **Descrição: “**Início do processo em unidade(s) subordinada(s)”  
      2. **Processo**: \[DESCRICAO DO PROCESSO\]  
      3. **Data/hora**: Data/hora atual  
      4. **Data limite:** Data comunicada nas notificações.  
      5. **Unidade de origem:** SEDOC   
      6. **Unidade de destino:** \[SIGLA\_UNIDADE\].  
   3. Para cada unidade interoperacional serão criados dois alertas: um de unidade operacional e outro de unidade intermediária, como especificados acima.

## **CDU-005 \- Detalhar processo** {#cdu-005---detalhar-processo}

**Ator principal**: Usuário (todos os perfis)

**Pré-condições**: 

* Usuário ter feito login (qualquer perfil).  
* Ao menos um processo nas situações ‘Em Andamento’ ou ‘Finalizado’.

**Fluxo principal:**

1. No **Painel**, usuário clica em um processo nas situações ‘Em Andamento’ ou ‘Finalizado’ na lista de processos.

*Se perfil CHEFE ou SERVIDOR:*

2. Sistema mostra a tela **Detalhe do Processo na Unidade** com os dados da unidade do perfil. A tela será composta por duas seções: ***Dados da Unidade*** e ***Elementos do Processo***.  
3. Na seção ***Dados da unidade*** serão apresentadas as informações:  
   1. *(Sem label)*: Sigla e nome da unidade, em negrito e com fonte maior que o restante do texto  
   2. **Situação:** Situação do processo na unidade (ver [Situações](#heading=h.9pxnwflr1tsn)).  
   3. **Responsável:**  
      1. \[Nome\]  
      2. \[Tipo da reponsabilidade\]**:** com valores possíveis: "Titular", "Substituto(a) (até \[DATA\_TERM\_SUBST)", "Atrib. temporária (até \[DATA\_TERM\_ATRIB\])"  
      3. **Ramal:** \[Ramal do servidor no SGRH\]  
      4. **E-mail:** \[Endereço de e-mail do servidor no SGRH\]  
4. A informação da seção ***Elementos do Processo*** será variável em função do tipo do processo.  
5. Se o processo for de mapeamento ou revisão, a seção apresentará *cards* para acesso ao cadastro de ***Atividades e Conhecimentos*** (descrição “Cadastro de atividades e conhecimentos da unidade”) e ao ***Mapa de Competências*** (descrição “Mapa de competências da unidade”).  
6. Se o processo for de diagnóstico, a seção apresentará estes cards:  
   1. ***Diagnóstico da Equipe*** (descrição “Diagnóstico das competências pelos servidores da unidade”)   
   2. ***Ocupações Críticas*** (descrição “Identificação das ocupações críticas da unidade”).  
7. Em todos os casos haverá um destaque para identificar se os dados referenciados por cada *card* já se encontram disponibilizados no processo.  
   1. Se estiverem disponibilizados, os *cards* serão clicáveis para fornecer acesso às telas específicas.

*Se perfil ADMIN ou GESTOR:*

8. Sistema mostra a tela **Detalhes do processo** composta pelas seções ***Dados do Processo*** e ***Unidades Subordinadas Participantes***.  
9. Seção ***Dados do processo:***  
   1. Informações da Descrição do Tipo e da Situação dos processos.   
   2. Se perfil ADMIN, botão ‘Finalizar processo’.  
10. Seção ***Unidades subordinadas participantes:***  
    1. Subárvore das unidades hierarquicamente inferiores.  
       1. Para cada unidade operacional e interoperacional da subárvore são exibidas, em linha, as informações da situação do processo na unidade e da data limite para a conclusão da etapa atual do processo naquela unidade.  
       2. O usuário poderá clicar na unidade para visualizar a dela **Detalhe do Processo na Unidade** com os dados da unidade selecionada.

## **CDU-006 \- Manter atividades e conhecimentos** {#cdu-006---manter-atividades-e-conhecimentos}

**Ator**: CHEFE

**Pré-condições**: 

* Usuário logado com perfil CHEFE  
* Processo de mapeamento iniciado que tenha a unidade como participante

**Fluxo principal:**

1. No **Painel**, usuário clica em um processo.  
2. Sistema mostra a tela **Detalhes de processo**.  
3. CHEFE clica no botão 'Atividades e conhecimentos'.  
4. Sistema apresenta a tela **Cadastro de Atividades/Conhecimentos** para a unidade.  
5. CHEFE fornece a descrição de uma atividade, e clica no botão de adição.  
6. Sistema adiciona atividade e mostra campo para adição de conhecimento abaixo da atividade.  
7. CHEFE fornece a descrição do conhecimento e clica no botão de adição correspondente.  
8. Sistema adiciona o conhecimento, associando-o à atividade.   
   1. Deve ser indicada claramente a associação entre o conhecimento e a atividade, indentando os conhecimentos da atividade abaixo da descrição desta.  
9. CHEFE repete o fluxo de adição de atividades/conhecimentos.   
   1. Pode-se incluir primeiro várias atividades e depois os conhecimentos correspondentes; ou trabalhar em uma atividade por vez até concluir todos os seus conhecimentos. O sistema deve permitir os dois modos de trabalho.  
10. Para cada **atividade** já cadastrada, ao passar o mouse, o sistema exibe botões de edição e remoção.  
    1. Se o usuário clicar em ‘Editar’, sistema habilita a edição do nome da atividade e exibe ao lado um botão ‘Salvar’ e outro ‘Cancelar’.   
       1. Se usuário clicar em ‘Salvar’, o sistema salva a alteração e volta a exibir os botões ‘Editar’ e ‘Remover’ ao lado do nome da atividade.  
       2. Se o usuário clicar em ‘Cancelar’, o sistema não salva a alteração e volta a exibir o nome da atividade que estava antes da modificação com os botões ‘Editar’ e ‘Remover’ ao lado.  
    2. Se o usuário clicar em ‘Remover’, o sistema solicita que o usuário confirme a operação. Se o usuário confirmar, a atividade e todos os conhecimentos associados a ela são removidos.  
11. De forma análoga, para cada **conhecimento** já cadastrado, o sistema exibe, ao passar o mouse sobre o conhecimento**,** uma opção ‘Editar’ e outra ‘Remover’.  
    1. Se o usuário clicar em ‘Editar’, o sistema habilita a edição do nome do conhecimento e exibe ao lado um botão ‘Salvar’ e outro ‘Cancelar’.  
       1. Se usuário clicar em ‘Salvar’, o sistema salva a alteração e volta a exibir os botões ‘Editar’ e ‘Remover’ ao lado do nome do conhecimento.  
       2. Se usuário clicar em ‘Cancelar’, o sistema não salva a alteração e volta a exibir o nome do conhecimento que estava antes da modificação com os botões ‘Editar’ e ‘Remover’ ao lado.  
    2. Se usuário clicar em ‘Remover’, o sistema solicita que o usuário confirme a operação. Se o usuário confirmar, o conhecimento é removido.  
12. Opcionalmente, o CHEFE, durante a criação de uma atividade, clica no botão 'Importar' mostrado no topo da tela, ao lado do botão ‘Voltar’:  
    1. Sistema exibe um modal onde o usuário deverá informar a unidade da qual as atividades serão importadas e o ano de cadastro dessas atividades. Os anos disponíveis serão apenas os que têm mapa de competência finalizado para aquela unidade (ou seja, não há a opção de importar atividades de um processo em andamento).  
    2. CHEFE escolhe unidade e o ano.  
    3. Sistema apresenta a lista de atividades da unidade selecionada, para o ano selecionado, permitindo seleção de uma ou mais atividades.  
    4. CHEFE seleciona as atividades e clica em 'Importar'.  
    5. Sistema traz as atividades e seus conhecimentos associados para o cadastro de atividades atual.  
       1. Serão importadas apenas as descrições das atividades e dos conhecimentos da outra unidade, não havendo vinculação entre os conhecimentos ou atividades entre diferentes unidades.  
13. CHEFE, ao terminar a inclusão de atividades/conhecimentos (adicionando ao menos um conhecimento a cada atividade), confirma a disponibilização do cadastro, clicando no botão 'Disponibilizar'.  
14. Sistema mostra uma tela de confirmação e, ao confirmar, bloqueia a edição de Atividades/Conhecimentos e altera situação para 'Cadastro disponibilizado'.  
15. Sistema notifica unidade superior hierárquica quanto à disponibilização, com e-mail no modelo abaixo:

    ***Assunto:** SGC: Cadastro de atividades/conhecimentos disponibilizado: \[SIGLA\_UNIDADE\]*

    *À equipe da \[SIGLA\_UNIDADE\_INTERMEDIÁRIA\],*

    *A unidade \[SIGLA\_UNIDADE\] disponibilizou o cadastro de atividades e conhecimentos.*

    *A validação desse cadastro já pode ser realizada no Sistema SGC (URL\_SISTEMA).* 

    *Atenciosamente,*  
    *Equipe da SEDOC*

16. Sistema mostra mensagem de sucesso de disponibilização e redireciona para **Painel**.

## **CDU-007 \- Disponibilizar cadastro para validação** {#cdu-007---disponibilizar-cadastro-para-validação}

**Ator**: CHEFE

**Pré-condições**: 

* Usuário logado com perfil CHEFE.  
* Processo de unidade localizado na unidade do usuário.

**Fluxo principal:**

1. No **Painel**, CHEFE clica no processo ativo.  
2. Sistema mostra tela **Detalhes de processo**. Esta tela inclui diretamente o botão 'Atividades e conhecimentos'.  
3. CHEFE clica em 'Atividade/Conhecimentos'.  
4. Sistema mostra tela **Cadastro de atividades e conhecimentos**  
5. CHEFE clica no botão 'Disponibilizar'.  
6. Sistema verifica se todas as atividades têm ao menos um conhecimento associado.   
7. Caso negativo, indica quais atividades estão precisando de adição de conhecimentos e impede a finalização.  
8. Caso positivo, sistema mostra diálogo de confirmação:  
   1. Título: ''Disponibilização de cadastro'   
   2. Texto: "Confirma finalização e disponibilização do cadastro? Essa ação bloqueia a edição e habilita a validação por unidades superiores''  
   3. Botões: 'Disponibilizar', 'Cancelar'.  
9. CHEFE escolhe 'Disponibilizar.'  
10. Sistema altera a situação do processo da unidade para 'Cadastro disponibilizado'  
11. Sistema altera a localização do processo de unidade, para a unidade superior na hierarquia.  
12. Sistema mostra confirmação: "Cadastro de atividades disponibilizado" e redireciona para **Painel**.

## **CDU-008 \- Validar cadastro** {#cdu-008---validar-cadastro}

**Atores**: GESTOR e SEDOC

**Pré-condição**: Processo da unidade localizado na sua unidade e situação atual 'Cadastro disponibilizado'.

1. No **Painel**, usuário clica em um processo disponível para validação.  
2. Usuário analisa as informações do cadastro realizado pela unidade e opta por validar ou devolver.   
3. Se quer **devolver**:  
   1. Usuário escolhe 'Devolver'.  
   2. Sistema lê a 'unidade anterior' do processo de unidade, para determinar para onde ocorrerá a devolução, ajustando a 'unidade atual' do processo para esse valor e a 'unidade anterior' como sendo a sua unidade.   
   3. Caso 'unidade anterior' seja a **unidade do processo**, sistema altera a situação do processo da unidade para 'Cadastro em andamento'. Caso contrário não altera a situação.  
   4. Sistema envia notificação por e-mail para a unidade:

      ***Assunto:** SGC: Cadastro de atividades/conhecimentos devolvido para revisão*

      *À equipe da \[SIGLA\_UNIDADE\],*

      *Seu processo foi devolvido pela unidade superior, para que sejam efetuadas revisões no cadastro de atividades e conhecimentos, no Sistema SGC (URL\_SISTEMA).* 

      *Atenciosamente,*  
      *Equipe da SEDOC*

4. Se quer **validar**:  
   1. Usuário escolhe 'Validar'.  
   2. Se unidade atual não for a SEDOC, sistema lê a unidade superior armazenada, para determinar para onde o processo será encaminhado. Sistema altera a 'unidade anterior' do processo como sendo a sua unidade; e  altera 'unidade atual' para a unidade superior.  
   3. Se a unidade atual for a SEDOC, sistema altera 'unidade anterior' para a SEDOC e a situação do processo da unidade para 'Cadastro homologado'.  
   4. Sistema envia notificação:

      *Assunto: SGC: Cadastro de atividades/conhecimentos validado*

      *À equipe da \[SIGLA\_UNIDADE SUPERIOR\],*

      *As seguintes unidades foram validadas: \[LISTA DE UNIDADES\]* 

      *A validação por sua unidade já pode ser realizada no Sistema SGC.* 

      *Atenciosamente,*  
      *Equipe da SEDOC*

## **CDU-010 \- Visualizar mapa de competências** {#cdu-010---visualizar-mapa-de-competências}

**Ator**: Todos os perfis.

**Pré-condições**: 

**Fluxo principal:**

*(Detalhar a tela de exibição de mapa)*

## **CDU-011 \- Manter mapa de competências** {#cdu-011---manter-mapa-de-competências}

**Ator**: SEDOC 

**Pré-condições**: 

* Ao menos uma unidade com situação 'Finalizada'.   
* Cadastro de atividades/conhecimentos validado por todas as unidades na hierarquia da unidade 'folha' até a SEDOC.

**Fluxo principal:**

1. No **Painel** SEDOC escolhe um processo ativo  
2. Sistema mostra tela **Detalhes de processo**.  
3. SEDOC clica em uma unidade com situação 'Finalizada'.  
4. Sistema mostra a tela **Detalhes de processo de unidade**, ver [Detalhes de processo de unidade].  
5. SEDOC clica no botão 'Criar mapa''.  
6. Sistema cria internamente um novo Mapa de Competências para a unidade.  
7. Sistema mostra a tela **Edição de mapa**, com**:**  
   1. Atividades/conhecimentos cadastrados pela unidade organizados hierarquicamente (conhecimentos abaixo de cada atividade).  
   2. Checkbox ao lado de cada atividade (mas não dos conhecimentos)  
8. Bloco de criação de competência, com campo de texto formatado para descrição e botão 'Adicionar competência' (ícone '+').  
9. Botão 'Gerar mapa', fora do bloco de criação de competência.

*\[Início de fluxo de criação  de competências\]* 

10. SEDOC seleciona uma ou mais atividades e clica em 'Adicionar competência'.  
11. Sistema cria uma competência, seguindo estes passos:  
    1. Sistema associa a nova competência com as atividades selecionadas  
    2. Sistema adiciona a descrição das atividades selecionadas abaixo do campo de descrição da competência.   
       1. Ao lado da descrição da atividade deve existir um controle permitindo a expansão para mostrar os conhecimentos associados à atividade; mas a atividade deve sempre iniciar 'recolhida' (ou seja, sem mostrar a lista de conhecimentos).  
    3. Sistema mostra botão 'Salvar' ao lado do bloco de definição da competência.  
12. SEDOC clica em 'Salvar'.   
13. Sistema indica visualmente uma atividade como já associada a uma competência (mas não bloqueia seu uso em outras competências).

*\[Término de fluxo de criação de competências\]* 

14. SEDOC repete o [fluxo de criação de competências] até que o mapa esteja completo.  
15. SEDOC clica em 'Gerar mapa'.  
16. Sistema verifica se todas as atividades foram associadas a pelo menos uma competência.   
    1. Caso negativo, informa quais atividades estão ainda sem associação a competências.  
    2. Caso positivo, pergunta se no mapa gerado devem ser mostradas as atividades associadas.  
17. Sistema mostra a tela **Finalização de mapa**, incluindo ou não a descrição das atividades, dependendo da escolha anterior. Nesta tela são mostrados os botões:  
    1. Editar mapa (volta para tela **Edição de mapa**)  
    2. Disponibilizar: disponibiliza o mapa para validação  
    3. PDF: Gera um PDF com o mapa.  
    4. Planilha: Gera uma planilha .xls com o mapa.  
18. SEDOC clica em 'Disponibilizar'  
19. Sistema solicita uma data limite para conclusão da validação do mapa pela unidade e sua hierarquia.  
20. SEDOC fornece a data limite.  
21. Sistema registra essa data limite no processo da unidade.  
22. Sistema envia notificação por email para a unidade e suas unidades superiores, informando o início da etapa de validação do mapa e o prazo para a sua conclusão. Modelo:

    *Assunto: SGC: Mapa de Competências disponibilizado: \[SIGLA\_UNIDADE\]*

    *À equipe da \[SIGLA\_UNIDADE\],*

    *Comunicamos que o Mapa de Competências de sua unidade já está disponível para validação, com data limite \[DATA\_LIMITE\].*

    *A visualização e a validação do mapa podem ser realizadas no Sistema SGC.*

    *Atenciosamente,*  
    *Equipe da SEDOC*

23. A SEDOC também poderá disponibilizar os mapas de competências para várias unidades em lote, informando para todas a mesma data limite para conclusão da validação.   
    1. Se o processo de disponibilização tiver sido realizado em lote, as unidades intermediárias na árvore hierárquica receberão uma única notificação consolidada com os dados de todas as unidades subordinadas. Modelo de notificação consolidada (exemplo):

    *Assunto: SGC: Mapas de Competências Disponibilizados*

    *À equipe da STIC,*

    *Comunicamos que os mapas de competências da seguintes unidades já estão disponíveis para validação:*

    ***\- \[GAB-STIC\]***  
       ***\- \[SESEL\]***  
       ***\- \[SENIC\]***  
       ***\- \[SESUP\]***

    *A validação dos mapas pode ser realizada no Sistema SGC.* 

    *Informamos que a data limite para finalizar essa validação é  \[22/10/2026\].*

    *Atenciosamente,*  
       *Equipe da SEDOC*

    2. Uma vez disponibilizado, o mapa de competências passará a ficar disponível para consulta pelo CHEFE e GESTOR.   
       1. O CHEFE tem acesso a apenas aos mapas de sua unidade.  
       2. O GESTOR tem acesso a todos os mapas de suas unidades subordinadas.  
24. Sistema mostra **Painel**.

## **CDU-012 \- Disponibilizar mapa de competências** {#cdu-012---disponibilizar-mapa-de-competências}

**Ator**: SEDOC

**Pré-condições**: 

* Usuário logado com perfil SEDOC.  
* Processo da unidade localizado na SEDOC e situação atual 'Cadastro disponibilizado'.

**Fluxo principal:**

1. No **Painel** SEDOC escolhe um processo ativo  
2. Sistema mostra tela **Detalhes de processo**.  
3. SEDOC clica em uma unidade com situação 'Finalizada'.  
4. Sistema mostra a tela **Detalhes de processo de unidade**, ver [Detalhes de processo de unidade].  
5. SEDOC clica no botão 'Disponibilizar mapa''.  
6. Sistema mostra diálogo de confirmação:  
   1. Título: ''Disponibilização de mapa'  
   2. Texto: "Confirma finalização e disponibilização do mapa? Essa ação bloqueia a edição e habilita a validação pelas unidades.''  
   3. Botões: 'Cancelar', 'Confirmar''  
7. SEDOC clica em 'Confirmar'.  
8. Sistema mostra mensagem de confirmação "Mapa da \[SIGLA\_UNIDADE\] disponibilizado"  
9. Referente ao processo da unidade, o sistema:  
   1. Muda a situação para 'Mapa disponibilizado'.  
   2. Muda a localização para a unidade do processo.   
   3. Desabilita a edição do mapa.  
10. Sistema volta para a tela **Detalhes de processo**.

## **CDU-013 \- Validar mapa de competências (CHEFE)** {#cdu-013---validar-mapa-de-competências-(chefe)}

**Atores:** CHEFE

**Pré-condição**: Usuário logado com perfil CHEFE

**Fluxo principal:**

1. No **Painel,** CHEFE escolhe um processo ativo.  
2. Sistema mostra tela **Detalhes de processo**.  
3. Usuário clica em uma unidade com situação 'Mapa disponibilizado'.  
4. Sistema mostra tela **Detalhes de processo de unidade**.  
5. No seção 'Mapa de competências', usuário clica em 'Visualizar'.  
6. Sistema mostra a tela **Mapa de competências**, com:  
   1. *(Sem label)*: Sigla e nome da unidade, em destaque  
   2. **Disponibilizado em:** Data/hora de disponibilização do mapa pela SEDOC  
   3. **Mapa:** Tabela hierárquica com três níveis, inicialmente 'recolhida' para mostrar apenas as competências. Com estrutura: Descrição da competência \>\> Descrições das atividades da competência \>\> Descrição dos conhecimentos da atividade.  
   4. Botão 'Voltar', que volta à tela anterior.  
   5. Botões 'Validar sem sugestões' e 'Validar com sugestões'.  
   6. Campo em texto formatado para comentários.  
7. Se usuário clicar em 'Validar com sugestões':  
   1. Sistema muda a localização do processo para a unidade superior.  
   2. Se unidade superior for SEDOC, sistema muda situação do processo de unidade para 'Mapa criado', permitindo com isso que a SEDOC faça os ajustes necessários no mapa.  
   3. Se unidade superior NÃO for SEDOC, sistema muda situação do processo de unidade para 'Mapa com sugestões' e a localização para a unidade superior.  
   4. Este caso de uso recomeça quando a SEDOC termina os ajustes e disponibiliza o mapa novamente.  
8. Se usuário clicar em 'Validar sem sugestões':  
   1. Sistema muda situação do processo de unidade para 'Validado' e a localização para a unidade superior.

## **CDU-014 \- Validar mapa de competências (GESTOR)** {#cdu-014---validar-mapa-de-competências-(gestor)}

**Ator:** GESTOR

**Pré-condições**: 

* Usuário logado com perfil GESTOR  
* Existe ao menos um processo de mapeamento com o mapa disponibilizado.

**Fluxo principal:**

1. No **Painel**, GESTOR escolhe um processo.  
2. Sistema mostra tela **Detalhes de processo**.  
3. GESTOR clica em uma unidade com situação 'Mapa disponibilizado'.  
4. Sistema mostra tela **Detalhes de processo de unidade**.  
5. Usuário clica em 'Visualizar mapa'.  
6. Sistema mostra a tela **Mapa de Competências**:  
   1. *(Sem label)*: Sigla e nome da unidade.  
   2. **Disponibilizado em:** Data/hora de disponibilização do mapa pela SEDOC  
   3. **Mapa:** Tabela hierárquica com três níveis, inicialmente 'recolhida' para mostrar apenas as competências. Detalhes:  
      1. Estrutura: Descrição da competência \>\> Descrições das atividades da competência \>\> Descrição dos conhecimentos da atividade.  
   4. Botão 'Voltar', que volta à tela anterior.  
   5. Botões 'Validar' e 'Devolver'.  
7. Se usuário clicar em 'Validar:  
   1. Sistema muda a localização do processo da unidade para a unidade superior.  
   2. Se unidade superior NÃO for SEDOC, sistema muda a situação do processo de unidade para 'Mapa validado' e localização para a unidade superior.  
   3. Se unidade superior for SEDOC, sistema muda situação do processo de unidade para 'Mapa homologado'. **Isso conclui a validação do mapa.**  
8. Se usuário clicar em 'Devolver':  
   1. Sistema muda a localização do processo da unidade para a unidade inferior.  
   2. Se unidade inferior for a unidade do processo, sistema muda situação do processo de unidade para 'Mapa disponibilizado'.  
   3. Se unidade inferior NÃO for a unidade do processo sistema muda a situação para 'Mapa criado', permitindo com isso que a SEDOC faça os ajustes necessários no mapa.   
9. O caso de uso se repete (com diferentes usuários no perfil GESTOR), até que todas as unidades intermediárias acima da unidade do processo validem o mapa.

## **CDU-015 \- Consultar histórico de processos** {#cdu-015---consultar-histórico-de-processos}

**Ator**: SEDOC/GESTOR/CHEFE

**Pré-condição**: Usuário logado com qualquer perfil, exceto SERVIDOR

**Fluxo principal:**

1. Em qualquer tela do sistema, na barra de navegação, usuário clica 'Histórico'.  
2. Sistema apresenta uma tabela com todos os processos com situação 'Finalizado', com:  
   1. **Processo**: Descrição do processo.  
   2. **Tipo**: Tipo do processo.  
   3. **Finalizado em**: Data de finalização do processo  
   4. **Unidades participantes**: Lista de unidades participantes, agregando pelas unidades que tiverem todas as subunidades participando.  
   5. **Botão 'Mapa'** (em cada linha) para mostrar mapa da unidade.  
3. Usuário clica em um processo para detalhamento.  
4. Sistema apresenta a página **Detalhes do processo** (somente leitura).:

## **CDU-016 \- Manter atribuição temporária** {#cdu-016---manter-atribuição-temporária}

**Ator Principal**: SEDOC 

**Pré-condições**: Processo com situação 'iniciado' e usuário autenticado com perfil SEDOC. 

**Fluxo principal:**

1. No **Painel**, SEDOC clica no item 'Unidades' na barra de navegação.  
2. Sistema mostra tela **Unidades**.  
3. SEDOC clica em umas das unidades  
4. Sistema mostra tela **Detalhes de unidade**.  
5. No bloco 'Atribuição temporária', SEDOC clica no botão 'Criar'.  
6. Sistema apresenta:  
   1. Lista de servidores da unidade selecionada.   
   2. Campo 'Início' (data)  
   3. Campo 'Término' (data)  
   4. Campo 'Justificativa' (texto simples)  
   5. Botões 'Criar atribuição' e 'Cancelar'  
7. SEDOC seleciona servidor e define a data de término da atribuição, além de incluir justificativa.   
   1. Para fins deste sistema, a atribuição temporária terá prioridade sobre os dados de titularidade importados do SGRH.  
8. Sistema registra internamente a atribuição temporária e mostra uma confirmação "Atribuição criada".  
   1. O usuário que recebe a atribuição temporária passa a ter os mesmos direitos do perfil CHEFE.  
9. Sistema agenda remoção automática da atribuição na data definida.

## **CDU-017 \- Iniciar processo de revisão** {#cdu-017---iniciar-processo-de-revisão}

**Ator**: SEDOC

**Pré-condições**: 

* Usuário logado com perfil SEDOC.   
* Ao menos um processo de revisão na situação 'Não iniciado'.

**Fluxo principal:**

1. No **Painel**, SEDOC clica, em um processo com situação 'Não iniciado'.  
2. Sistema muda para tela **Detalhes de processo**  
3. SEDOC escolhe 'Iniciar processo'.  
4. Sistema mostra uma tela de confirmação, com este texto (substituindo o valor indicado entre colchetes):

   Ao confirmar, o processo será marcado como Iniciado e as seguintes unidades serão notificadas por e-mail.

5. SEDOC confirma.  
6. Sistema copia a data limite do processo para a data limite de todas as unidades participantes.  
7. Sistema cria, para cada unidade participante, uma nova versão do mapa de competências, uma cópia da versão até então vigente (do ano anterior).   
   1. A nova versão do mapa será rotulada, por padrão, com o ano de realização do processo de revisão.  
8. Sistema envia notificações para as unidades participantes.  
   1. As notificações são enviadas para os e-mails das unidades (ex. sesel@tre-pe.jus.br)  
9. Sistema envia notificações consolidadas para as unidades superiores (GESTOR), informando o início do processo de revisão das unidades subordinadas a elas.  
10. Sistema muda a situação do processo para 'Em andamento'' e volta para **Painel**, que já deve refletir o novo andamento do processo.  
    1. Após o processo ser iniciado, não será possível alterar dados do processo (descrição, unidades), nem removê-lo.

## **CDU-018 \- Revisar atividades e conhecimentos** {#cdu-018---revisar-atividades-e-conhecimentos}

**Ator**: CHEFE

**Pré-condições**: 

* Processo de revisão iniciado para a unidade  
* Usuário logado com perfil CHEFE. 

**Fluxo principal:**

1. No **Painel**, CHEFE clica no botão ‘Atividades e conhecimentos’.  
2. Sistema apresenta tela **Atividades/Conhecimentos** com o   
3. CHEFE realiza a revisão do cadastro das atividades da sua unidade e dos conhecimentos necessários para a sua execução.  
4. Alternativamente o CHEFE poderá **importar atividades e seus conhecimentos de outras unidades da hierarquia**.  
5. **Caso não seja detectada necessidade de ajuste no cadastro de atividades e conhecimentos**, o CHEFE poderá **concluir a etapa informando a validação do mapa de competência da forma vigente**.  
6. **Caso ajustes tenham sido realizados no cadastro de atividades**, será possível visualizar no mapa as **competências que podem ser afetadas pelas alterações realizadas**.  
7. CHEFE informa a finalização da etapa de revisão do cadastro de atividades e conhecimentos na unidade. Neste momento, fica bloqueada a manutenção do cadastro.  
8. Sistema envia uma notificação para a unidade imediatamente superior comunicando a conclusão da etapa de revisão do cadastro de atividades e conhecimentos na unidade.

## **CDU-019 \- Revisar mapa de competências** {#cdu-019---revisar-mapa-de-competências}

**Ator Principal**: SEDOC

**Pré-condições**: SEDOC concluiu a análise da revisão do cadastro de atividades e conhecimentos. 

**Fluxo principal:**

1. Ao final da etapa de revisão do cadastro de atividades e conhecimentos de cada unidade participante do processo de revisão, **se tiver sido realizado ajuste nos cadastros de atividades**, a SEDOC fará a **adequação dos mapas de competências atuais das respectivas unidades** para que fiquem de acordo com a nova versão do cadastro de atividades e conhecimentos.  
2. Quando o ajuste do mapa tiver sido finalizado, a SEDOC irá comandar a **disponibilização deste para validação pela unidade**, sendo informada neste momento a data limite para conclusão desta última etapa do processo.  
3. A SEDOC também poderá **disponibilizar os mapas de competências ajustados para várias unidades em lote**, informando para todas a mesma data limite para conclusão da validação.  
4. A disponibilização do mapa de competências de uma unidade irá resultar no envio de notificação para a unidade e suas unidades superiores, informando o início da etapa de validação do mapa ajustado e o prazo para a sua conclusão.  
5. Se o processo de disponibilização tiver sido realizado em lote, as unidades intermediárias na árvore hierárquica receberão uma única notificação consolidada com os dados de todas as unidades subordinadas.

## **CDU-020 \- Validar mapa de competências revisado** {#cdu-020---validar-mapa-de-competências-revisado}

**Atores**: CHEFE, GESTOR 

**Pré-condições**: SEDOC disponibilizou a nova versão do mapa de competências ajustado. 

**Fluxo principal:**

1. CHEFE, a partir da notificação recebida, acessa o sistema e terá a visão da nova versão do mapa de competências disponibilizado pela SEDOC para a sua unidade.  
2. Poderão ser indicadas **sugestões de melhoria para o mapa ou informar a validação** na forma como foi disponibilizado pela SEDOC.  
3. Com o registro da informação de validação ou o de sugestões de melhoria, o sistema envia uma notificação para a unidade imediatamente superior na hierarquia comunicando a conclusão da etapa de validação pela unidade.

**Fluxo de Validação pelo GESTOR na Revisão**:

1. GESTOR, a partir da notificação recebida (por email), ao acessar o sistema terá a visão do andamento da etapa de validação do mapa de competências ajustado de todas as unidades subordinadas.  
2. Será exibida uma árvore de unidades hierarquicamente inferiores com o estado de conclusão da etapa.  
3. Será possível detalhar a unidade para visualizar a nova versão do mapa de competências disponibilizado pela SEDOC e a situação de validação deste.  
4. Quando a unidade subordinada tiver indicado a validação ou sugestões no mapa disponibilizado, será possível **ratificar essa informação, ou devolver para a unidade inferior para que sejam feitas retificações**.  
   * Se houver devolução, a unidade inferior deverá atuar novamente na validação do mapa.  
   * Se houver ratificação, a unidade superior será demandada a repetir o mesmo processo.  
5. O fluxo segue até a indicação de validação do mapa ajustado chegar na SEDOC (unidade raiz).  
6. SEDOC realiza a **análise das eventuais sugestões propostas** para a nova versão do mapa de competências, promove os ajustes que entender pertinentes e realiza uma **nova disponibilização do mapa para validação pelas unidades, reiniciando essa etapa** (Validação do mapa de competências ajustado).  
   * Nessa nova disponibilização a SEDOC informará as modificações que foram aceitas e implementadas e aquelas que não foram acatadas, sendo essa informação apresentada para as unidades na tela do sistema e nas notificações.  
7. O fluxo se repete até que os mapas sejam validados sem sugestões de melhoria.  
8. Quando todas as unidades participantes do processo de revisão do mapa de competências tiverem concluído a validação, a SEDOC poderá indicar a conclusão do processo de revisão do mapa de competências.  
9. Com a conclusão do processo, o mapa de competências ajustado passará a ser considerado a **versão atual do mapa de competências das unidades**.

## **CDU-021 \- Reabrir cadastro** {#cdu-021---reabrir-cadastro}

Se durante as etapas de criação e de validação do mapa de competências, alguma unidade indicar a necessidade tardia de ajuste no cadastro de atividades e conhecimentos, a SEDOC poderá, mediante solicitação da unidade em questão, **retorná-la para a etapa de cadastro** a fim de que a mesma possa realizar as alterações necessárias.

* A reabertura do cadastro de atividades e conhecimentos da unidade será **notificada para todas unidades hierarquicamente superiores**.  
* Após os ajustes realizados, o cadastro de atividades e conhecimentos da unidade precisará passar por **nova análise por todas as unidades superiores** na hierarquia.

## **CDU-022 \- Vincular unidades** {#cdu-022---vincular-unidades}

Em caso de alteração de nomenclatura de unidades (criação de novas e mudança de situações das antigas no SGRH), o sistema deverá permitir a **vinculação das unidades novas às antigas correspondentes para reaproveitamento das informações**.

## **CDU-023 \- Reabrir revisão** {#cdu-023---reabrir-revisão}

Se durante as etapas de ajuste e de validação do mapa de competências ajustado, alguma unidade indicar a necessidade tardia de revisão do cadastro de atividades e conhecimentos, a SEDOC poderá, mediante solicitação dessa unidade, **retorná-la para a etapa de revisão do cadastro** a fim de que a mesma possa realizar as alterações necessárias.

* A reabertura da revisão do cadastro de atividades e conhecimentos da unidade será **notificada para todas unidades hierarquicamente superiores**.  
* Após os ajustes realizados, a revisão do cadastro de atividades e conhecimentos da unidade precisará passar por **nova análise por todas as unidades superiores** na hierarquia.

O processo de revisão poderá ser concluído quando todas as unidades participantes concluírem a validação do mapa de competência.

As etapas de ajuste do mapa, e de validação do mapa ajustado, deverão ser realizadas apenas para as unidades que efetuarem alterações no cadastro de atividades e conhecimentos. As unidades que não identificarem necessidades de alteração poderão indicar a validação do mapa de competências já na primeira etapa do processo.

## **CDU-024 \- Enviar lembrete de prazo** {#cdu-024---enviar-lembrete-de-prazo}

Se houver processos próximos ao prazo final, o Sistema apresenta indicadores de alertas de prazo e permite enviar um lembrete por e-mail para uma unidade.

## **CDU-025 \- Alterar data limite para unidade** {#cdu-025---alterar-data-limite-para-unidade}

Uma unidade pode solicitar diretamente à SEDOC, fora do contexto do sistema, que sua data limite seja alterada. Na tela de Detalhes de unidade, a SEDOC navega para a tela da unidade e altera a data. A unidade recebe a notificação da mudança de data.

## **CDU-026 \- Configurar sistema** {#cdu-026---configurar-sistema}

SEDOC clica no botão 'engrenagem' na barra de navegação  
Sistema mostra tela de configurações  
SEDOC altera as configurações  
Sistema guarda as configurações internamente.

## **Situações de Processos e de Processos de Unidades** {#situações-de-processos-e-de-processos-de-unidades}

O sistema gerencia diferentes situações para os três tipos de processos, tanto em nível geral (do processo) quanto no nível da unidade.

### **Situações de processos** {#situações-de-processos}

* **Criado**: Processo cadastrado, mas não iniciado.  
* **Em andamento**: Processo foi iniciado e todas as unidades participantes foram notificadas.   
* **Finalizado**: Mapa de competências homologado para todas as unidades.

### **Situações dos processos de unidades** {#situações-dos-processos-de-unidades}

**Processo de Mapeamento**

* **Não iniciado**: Unidade notificada pela SEDOC, mas sem cadastro salvo.  
* **Cadastro em andamento**: Cadastro salvo mas não finalizado.  
* **Cadastro disponibilizado**: Cadastro finalizado, aguardando validação.  
* **Cadastro homologado**: Cadastro validado pela SEDOC.  
* **Mapa criado:** SEDOC criou mapa para a unidade mas ainda não disponibilizou.  
* **Mapa disponibilizado:** SEDOC disponibilizou mapa da unidade para validação.  
* **Mapa com sugestões**: CHEFE indicou sugestões para o mapa da unidade.  
* **Mapa validado**: Hierarquia aprovou mapa disponibilizado pela SEDOC.  
* **Mapa homologado:** SEDOC homologou mapa para a unidade após validações sem sugestões.

**Processo de Revisão**

* **Não iniciado**: Unidade foi notificada, mas não alterou cadastro.  
* **Revisão em andamento**: Unidade fez alteração no cadastro.  
* **Revisão finalizada com ajustes**: Revisão do cadastro concluída com alterações, aguardando validação.  
* **Revisão finalizada sem ajustes**: Revisão do cadastro concluída sem alterações, aguardando validação.  
* **Revisão homologada**: Revisão do cadastro validada pela SEDOC.  
* **Mapa ajustado:** SEDOC criou mapa ajustado para a unidade mas ainda não disponibilizou.  
* **Mapa disponibilizado:** SEDOC disponibilizou mapa ajustado da unidade para validação.  
* **Mapa com sugestões**: CHEFE indicou sugestões para o mapa da unidade.  
* **Mapa validado**: Hierarquia aprovou o mapa disponibilizado pela SEDOC.  
* **Mapa homologado:** SEDOC homologou mapa para a unidade após validações sem sugestões.

**Processo de Diagnóstico** (em definição\!)

* **Não iniciado**: Unidade foi notificada, mas não alterou cadastro.  
* **Diagnóstico em andamento**: Unidade fez alteração no cadastro.  
* **Diagnóstico finalizado**: Unidade finalizou todos os passos do diagnóstico.

## 

## **Glossário** {#glossário}

**Acesso TRE-PE**  
Sistema de Acesso do TRE-PE, que oferece uma API REST de autenticação de usuários, usando título de eleitor e senha. O Acesso também inclui sistemas e perfis de usuários, mas esta funcionalidade não será usada no sistema, sendo os perfis determinados a partir do SGRH juntamente com atribuições temporárias cadastradas.

**Atribuição temporária de responsabilidade**  
Designação provisória da responsabilidade de uma unidade organizacional, realizada pela SEDOC, com data de início e término definidas, sobrepondo temporariamente a informação de responsabilidade obtida do SGRH. Também referida como **atribuição temporária**.

**Atividade**  
Ação desempenhada por uma unidade operacional ou interoperacional no exercício de suas funções.

**Árvore de unidades**  
Estrutura hierárquica das unidades organizacionais; no contexto dos processos de mapeamento, revisão e diagnóstico de competências, tem a SEDOC como **unidade raiz**.

**Conhecimento**  
Conhecimento técnico necessário para desempenhar uma atividade específica. Uma atividade geralmente requer mais de um conhecimento.

**Competência**  
Elemento sintetizante das atribuições de uma unidade. É criado pela SEDOC a partir das atividades e conhecimentos cadastrados pelas unidades.

**Devolução**  
Ato de retornar para retificação, após análise, as informações fornecidas por uma unidade subordinada. A devolução retorna a análise para a unidade que havia validado anteriormente.

**Mapa de competências**   
Conjunto consolidado de competências criado pela SEDOC, para uma unidade. Também referido como **mapa de competências técnicas**.

**Processos ativos/inativos**  
Os processos são considerados ativos no sistema quando ainda não estão finalizados ou foram finalizados há no máximo o número de dias definidos no parâmetro DIAS\_INATIVACAO\_PROCESSO (padrão 10 dias). A partir dessa quantidade de dias da finalização do processo, ele será considerado inativo, sendo disponível apenas para consulta a partir da tela **Histórico de processos**;

**Processo de mapeamento**  
Ciclo completo de coleta, validação e consolidação de atividades e conhecimentos das unidades operacionais e interoperacionais, e posterior geração e validação do mapa de competências. Também referido como **processo de mapeamento de competências técnicas**.

**Processo de revisão**  
Ciclo de revisão e validação do cadastro de atividades e conhecimentos das unidades operacionais e interoperacionais, e posterior adequação e validação do mapa de competências. Também referido como **processo de revisão do mapa de competências técnicas**.

**Processo de diagnóstico** Avaliação realizada pelos servidores e pelos responsáveis pelas unidades para identificar a importância e o domínio das competências da unidade por parte dos seus servidores, assim como as competências com poucos servidores capacitados. Também referido como  **de competências técnicas e identificação das ocupações críticas**.

**Responsável**  
Servidor titular ou substituto da titularidade de uma unidade organizacional, de acordo com as informações vigentes no SGRH no momento da consulta.

**SEDOC**  
Seção de desenvolvimento organizacional e capacitação. Principal unidade usuária do sistema no contexto dos processos de mapeamento, revisão e diagnóstico.

**SGRH**  
Sistema de Gestão de Recursos Humanos. É o sistema cujo banco de dados fornecerá as informações das unidades de lotação e titularidade dos servidores do Tribunal. 

**Unidade**  
Elemento da estrutura hierárquica do tribunal (árvore de unidades) onde os servidores estão lotados. Para efeito do sistema, podem ser classificadas em intermediárias, operacionais ou interoperacionais. Também referido como **unidade organizacional**.

**Unidade intermediária**  
Unidade abaixo da SEDOC (raiz) que possua uma ou mais unidades subordinadas a ela.

**Unidade interoperacional**  
Unidade que possui unidades subordinadas, mas também mais de um servidor lotado.

**Unidade operacional**  
Unidade com mais de um servidor lotado.

**Unidade raiz**  
Unidade que não possui unidade superior na árvore de unidades do sistema. Para efeito dos processos de mapeamento, revisão e diagnóstico, esse papel é exercido pela SEDOC.

**Validação**  
Ato de ratificar, após análise, as informações fornecidas por uma unidade subordinada. A validação encaminha a análise para a unidade superior.