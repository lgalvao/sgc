**Sistema de Gestão de Competências**

# **Especificação de Casos de Uso**

## **Atores e perfis** {#atores-e-perfis}

O sistema de Gestão de Competências opera com os seguintes perfis de usuários, cujas atribuições e acessos são automaticamente reconhecidos com base na condição de responsabilidade ou lotação em uma unidade, de acordo com o SGRH, ou por atribuição de responsabilidade temporária realizada no próprio sistema. Caso um usuário acumule mais de um perfil ou seja responsável por mais de uma unidade, será necessário selecionar o perfil e a unidade de trabalho após o login.

* **ADMIN**: Administrador da SEDOC. É responsável por criar, configurar e monitorar processos, além de criar/ajustar os mapas de competências das unidades. A unidade SEDOC é tratada como unidade raiz da estrutura organizacional para efeito dos processos de mapeamento, de revisão e de diagnóstico.  
* **GESTOR**: Responsável por uma unidade intermediária (exemplo: Coordenador). Pode visualizar e validar as informações cadastradas pelas unidades sob sua gestão, submetendo para análise da unidade superior, ou devolver à unidade subordinada para realização de retificações.  
* **CHEFE**: Responsável por uma unidade operacional ou interoperacional. Pode cadastrar as informações de sua unidade em cada processo e submeter essas informações para validação pela unidade superior.  
* **SERVIDOR**: Servidor lotado em uma unidade operacional ou interoperacional. Este papel só atua nos processos de diagnóstico.

## **Glossário** {#glossário}

**Acesso TRE-PE**  
Sistema de Acesso do TRE-PE, que oferece uma API REST de autenticação de usuários, usando título de eleitor e senha. O Acesso também inclui sistemas e perfis de usuários, mas esta funcionalidade não será usada no sistema, sendo os perfis determinados a partir do SGRH juntamente com atribuições temporárias cadastradas.

**Atribuição temporária**   
Designação provisória da responsabilidade de uma unidade organizacional, realizada pela SEDOC, com data de início e término definidas, sobrepondo temporariamente a informação de responsabilidade obtida do SGRH. Também referida como **atribuição temporária de responsabilidade**.

**Atividade**  
Ação desempenhada por uma unidade operacional ou interoperacional no exercício de suas funções.

**Árvore de unidades**  
Estrutura hierárquica das unidades organizacionais; no contexto dos processos de mapeamento, de revisão e de diagnóstico, tem a SEDOC como **unidade raiz**.

**Cadastro**  
Termo simplificado para o **cadastro de atividades e conhecimentos**. Sempre que for usado o termo sem qualificações, refere-se a esse cadastro e a apenas este.

**Conhecimento**  
Conhecimento técnico necessário para desempenhar uma atividade específica. Uma atividade geralmente requer mais de um conhecimento.

**Competência**  
Elemento sintetizante das atribuições de uma unidade. É criado pela SEDOC a partir das atividades e conhecimentos cadastrados pelas unidades.

**Devolução**  
Ato de retornar para retificação, após análise, as informações fornecidas por uma unidade subordinada. A devolução retorna a análise para a unidade que havia validado anteriormente.

**Localização atual de subprocesso**  
Unidade destino da última movimentação registrada para o subprocesso.

**Mapa de competências**   
Conjunto consolidado de competências criado pela SEDOC, para uma unidade. Também referido como **mapa de competências técnicas**.

**Movimentação**  
Registro da transição do subprocesso de uma unidade origem para uma unidade destino.

**Processos ativos/inativos**  
Os processos são considerados ativos no sistema quando ainda não estão finalizados ou foram finalizados há no máximo o número de dias definidos na configuração DIAS\_INATIVACAO\_PROCESSO (padrão 10 dias). A partir dessa quantidade de dias da finalização do processo, ele será considerado inativo, sendo disponível apenas para consulta a partir da tela **Histórico de processos**;

**Processo de mapeamento**  
Ciclo completo de coleta, validação e consolidação de atividades e conhecimentos das unidades operacionais e interoperacionais, e posterior geração e validação do mapa de competências. Também referido como **processo de mapeamento de competências técnicas**.

**Processo de revisão**  
Ciclo de revisão e validação do cadastro de atividades e conhecimentos das unidades operacionais e interoperacionais, e posterior adequação e validação do mapa de competências. Também referido como **processo de revisão do mapa de competências técnicas**.

**Processo de diagnóstico**  
Avaliação realizada pelos servidores e pelos responsáveis pelas unidades para identificar a importância e o domínio das competências da unidade por parte dos seus servidores, assim como as competências com poucos servidores capacitados. Também referido como  **de competências técnicas e identificação das ocupações críticas**.

**Responsável**  
Servidor titular ou substituto da titularidade de uma unidade organizacional, de acordo com as informações vigentes no SGRH no momento da consulta.

**SEDOC**  
Seção de desenvolvimento organizacional e capacitação. Principal unidade usuária do sistema no contexto dos processos de mapeamento, de revisão e de diagnóstico. 

**SGRH**  
Sistema de Gestão de Recursos Humanos. É o sistema cujo banco de dados fornecerá as informações das unidades de lotação e titularidade dos servidores do Tribunal. 

**Subprocesso**  
Instância de um processo de mapeamento, revisão ou diagnóstico no contexto de uma unidade operacional ou interoperacional.

**Unidade**  
Elemento da estrutura hierárquica do tribunal (árvore de unidades) onde os servidores estão lotados. Para efeito do sistema, podem ser classificadas em intermediárias, operacionais ou interoperacionais. Também referido como **unidade organizacional**.

**Unidade intermediária**  
Unidade abaixo da SEDOC (raiz) que possua uma ou mais unidades subordinadas a ela.

**Unidade interoperacional**  
Unidade que possui unidades subordinadas, mas também mais de um servidor lotado.

**Unidade operacional**  
Unidade com mais de um servidor lotado.

**Unidade raiz**  
Unidade que não possui unidade superior na árvore de unidades do sistema. Com relação aos processos de mapeamento, de revisão e de diagnóstico, esse papel é exercido pela SEDOC.

**Validação**  
Ato de ratificar, após análise, as informações fornecidas por uma unidade subordinada. A validação encaminha a análise para a unidade superior.

## **Situações de processos** {#situações-de-processos}

* **Criado**: Processo cadastrado, mas não iniciado.  
* **Em andamento**: Processo foi iniciado e todas as unidades participantes foram notificadas.   
* **Finalizado**: Mapa de competências homologado para todas as unidades.

## **Situações de subprocessos** {#situações-de-subprocessos}

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
* **Revisão do cadastro em andamento**: Unidade fez alteração no cadastro.  
* **Revisão do cadastro disponibilizada**: Revisão do cadastro concluída, aguardando validação.  
* **Revisão do cadastro homologada**: Revisão do cadastro validada pela SEDOC.  
* **Mapa ajustado:** SEDOC criou mapa ajustado para a unidade mas ainda não disponibilizou.  
* **Mapa disponibilizado:** SEDOC disponibilizou mapa ajustado da unidade para validação.  
* **Mapa com sugestões**: CHEFE indicou sugestões para o mapa da unidade.  
* **Mapa validado**: Hierarquia aprovou o mapa disponibilizado pela SEDOC.  
* **Mapa homologado:** SEDOC homologou mapa para a unidade após validações sem sugestões.

**Processo de Diagnóstico** (em definição\!)

* **Não iniciado**: Unidade foi notificada, mas não alterou cadastro.  
* **Diagnóstico em andamento**: Unidade fez alteração no cadastro.  
* **Diagnóstico finalizado**: Unidade finalizou todos os passos do diagnóstico.

## **CDU-01 \- Realizar login e exibir estrutura das telas** {#cdu-01---realizar-login-e-exibir-estrutura-das-telas}

**Ator principal:** Qualquer pessoa autorizada a acessar o sistema (com qualquer dos perfis).

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
8. O sistema exibe a estrutura de telas da aplicação, composta pelas seções: ***Barra de navegação***, ***Conteúdo*** e ***Rodapé***.  
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
   2. O ***conteúdo*** compõe a parte central da tela, onde serão exibidas todas as telas.  
   3. O ***Rodapé*** é a seção localizada na parte inferior das telas (exceto na tela de login) em que aparecerá a informação da versão do sistema assim como a informação ‘Desenvolvido por SESEL/COSIS/TRE-PE’.  
9. O sistema mostra inicialmente a tela **Painel** na seção de conteúdo das telas.

**Fluxo alternativo:**

5a. Caso o sistema não consiga autenticar o usuário com as credenciais fornecidas, deverá mostrar a mensagem 'Título ou senha inválidos.'

## **CDU-02 \- Visualizar Painel** {#cdu-02---visualizar-painel}

**Ator principal**: Usuário (todos os perfis)

**Pré-condição:** Usuário ter feito login (qualquer perfil)

**Fluxo principal:**

1. Sistema exibe a tela **Painel**, com as seções ***Processos Ativos*** e ***Alertas***.  
2. Na seção ***Processos Ativos***, o Sistema mostra uma tabela de processos ativos (título 'Processos'). Devem ser mostrados apenas os processos que incluam entre as unidades participantes a unidade do usuário e/ou suas unidades subordinadas.  
   1. Campos da tabela  
      * ***Descrição***: Descrição dada ao processo no momento do seu cadastro  
      * ***Tipo***: Tipo do processo ('Mapeamento', 'Revisão' ou 'Diagnóstico')  
      * ***Unidades Participantes***: Lista textual das unidades, contendo apenas as unidades de nível mais alto abaixo da unidade raiz que possuam todas as suas unidades subordinadas participando do processo. Por exemplo, para uma secretaria com duas coordenadorias A e B, se apenas as seções da coordenadoria B participarem do processo, deverá aparecer apenas o nome da coordenadoria B.  
      * ***Situação***: Situação do processo (‘Criado’, ‘Em andamento’ ou ‘Finalizado’).  
   2. Regras de exibição e funcionamento  
      * Processos na situação 'Criado' deverão ser listados apenas se o usuário estiver logado com o perfil ADMIN.  
      * Cabeçalhos das colunas deverão ser clicáveis, possibilitando ordenação em ordem crescente e decrescente.  
      * Itens da tabela serão clicáveis com estas regras:  
        * Para perfil ADMIN, clicar em um processo na situação 'Criado' mostra tela **Cadastro de processo** com os dados do processo (ver caso de uso [Manter processo](#cdu-03---manter-processo)).  
        * Clicar em processos nas situações 'Em Andamento' e 'Finalizado' mostrará as telas **Detalhes do processo**, caso o perfil logado seja ADMIN ou GESTOR (ver caso de uso [Detalhar processo](#cdu-07a---detalhar-processo)), ou **Detalhes do subprocesso**, caso o perfil logado seja CHEFE ou SERVIDOR (ver caso de uso [Detalhar subprocesso](#cdu-07b---detalhar-subprocesso)).  
   3. Caso o usuário logado esteja no perfil ADMIN, no topo da seção de ***Processos Ativos*** deverá ser exibido o botão ***Criar processo*** a partir do qual será efetuado o cadastro de novos processos (ver caso de uso [Manter processo](#cdu-03---manter-processo)).  
3. Na seção ***Alertas***, Sistema mostra uma tabela com os alertas registrados pelo sistema que tiverem como destino a unidade do usuário (título 'Alertas')  
   1. Campos da tabela  
      * ***Data/Hora***: Informação da data e da hora de geração do alerta  
        * ***Processo***: Descrição do processo a que se refere o alerta  
        * ***Unidade***: Unidade de origem do alerta  
        * ***Descrição***: Descrição do alerta  
   2. Regras de exibição e funcionamento  
      * Dados devem estar inicialmente em ordem decrescente por data/hora de envio do alerta, podendo-se alternar a ordenação clicando no cabeçalho.   
      * Cabeçalhos da coluna ***Processo*** poderá ser clicado para alterar a ordenação dos dados da tabela.  
        * Ordenação deve ser feita tendo como primeiro critério a descrição do processo (asc/desc) e em seguida a data/hora (desc).  
      * Alertas ainda não visualizados pelo usuário logado serão exibidos em negrito.  
      * Na primeira visualização do alerta pelo usuário logado, este deverá ser marcado como visualizado para o usuário, de maneira a ser exibido sem destaque a partir da próxima visualização.

## **CDU-03 \- Manter processo** {#cdu-03---manter-processo}

**Ator principal**: ADMIN

**Pré-condição:** Login realizado com perfil ADMIN, com tela **Painel** aberta.

**Fluxo principal:**

*Criação de processo:*

1. Se o usuário quiser **criar** um processo, escolhe o botão ***Criar processo***.  
2. Sistema muda para a tela **Cadastro de processo** e apresenta um formulário contendo:  
   1. Campo **Descrição**  
   2. Campo **Tipo do processo**, com opções: ‘Mapeamento’, ‘Revisão’ e ‘Diagnóstico’  
   3. Quadro de **Unidades participantes**, contendo uma árvore de unidades com checkboxes para cada uma.  
      1. A lista de unidades só deve incluir unidades que não estejam participando de um processo ativo do tipo selecionado.  
      2. O comportamento de seleção das unidades participantes deve seguir estas regras:  
         1. Ao clicar em uma unidade intermediária na árvore, todas as unidades abaixo dela devem ser automaticamente selecionadas;  
         2. Se todas as unidades de uma subárvore estiverem selecionadas, o nó raiz desta subárvore deve ser automaticamente selecionado;  
         3. Se um nó de uma subárvore tiver a seleção removida, o nó raiz da subárvore deve ficar num estado intermediário, indicando que há nós selecionados da subárvore, mas não todos;  
         4. Se todas as unidades de uma subárvore tiverem a seleção removida, o nó raiz desta subárvore deve ter automaticamente a seleção removida.  
         5. Se a raiz de uma subárvore for uma unidade interoperacional, ela poderá ser selecionada ainda que as unidades subordinadas não o sejam.  
   4. Campo ***Data limite etapa 1*** para informação do prazo que as unidades terão para concluir a etapa inicial do processo.  
   5. Botões ***Cancelar***, ***Salvar*** e ***Iniciar processo***  
3. O usuário fornece os dados solicitados, escolhe o tipo do processo desejado e seleciona as unidades participantes.   
4. Usuário clica em ***Salvar***.  
5. Sistema faz estas validações (com mensagens de falha de validação entre aspas):   
   1. Descrição deve estar preenchida — "Preencha a descrição".  
   2. Ao menos uma unidade deve ser selecionada — "Pelo menos uma unidade participante deve ser incluída."  
   3. Em caso de processos dos tipos ‘Revisão’ ou ‘Diagnóstico’, só poderão ser selecionadas unidades com mapas de competência vigentes — "Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento.".  
6. O Sistema cria o processo internamente, colocando-o na situação 'Criado', e mostra a mensagem "Processo criado.".  
7. Sistema redireciona para **Painel**, onde já será mostrada uma linha para o processo recém-criado.

*Edição de processo:*

8. Se usuário quiser **editar** o processo, clica na linha do processo na listagem de processos do **Painel** (apenas processos na situação ‘Criado’ podem ser editados).  
   1. Além dos botões ***Cancelar***, ***Salvar*** e ***Iniciar processo***, também será exibido o botão ***Remover***.  
9. Sistema abre a tela **Cadastro de processo** preenchida com os dados atuais do processo.  
10. Usuário modifica os dados desejados (descrição, unidades participantes e data limite apenas)   
11. Usuário escolhe o botão ***Salvar***.  
12. Sistema valida os dados depois de editados, de acordo com as mesmas regras aplicadas no momento do cadastro.  
13. Sistema atualiza o processo e mostra mensagem "Processo alterado.". 

*Remoção:*

14. Se usuário quiser **remover** o processo, clica na linha do processo na listagem de processos do **Painel** (apenas processos na situação ‘Criado’ podem ser removidos).  
    1. Além dos botões ***Cancelar***, ***Salvar*** e ***Iniciar processo***, também será exibido o botão ***Remover***.  
15. Sistema abre a tela **Cadastro de processo** preenchida com os dados atuais do processo.  
16. Usuário escolhe o botão ***Remover***.  
17. Sistema mostra diálogo de confirmação "Remover o processo '\[Descrição do processo\]'? Esta ação não poderá ser desfeita.", botões ***Remover*** e ***Cancelar***.  
    1. Se escolher ***Cancelar*** no diálogo, o sistema fecha o diálogo e permanece na tela **Cadastro de processo**, sem efetuar alterações.  
    2. Ao escolher ***Remover*** no diálogo, o sistema remove o processo permanentemente, mostra a mensagem "Processo \[Descrição do Processo\] removido" e redireciona para **Painel.**

**Fluxo alternativo:**

Nos passos 4 ou 11, caso o usuário escolha o botão ***Iniciar processo***, o sistema realiza as validações dos dados informados, cria o processo (se ainda não tiver sido criado) e segue para o fluxo dos casos de uso [Iniciar processo de mapeamento](#cdu-04---iniciar-processo-de-mapeamento), [Iniciar processo de revisão](#cdu-05---iniciar-processo-de-revisão) ou [Iniciar processo de diagnóstico](#cdu-06---iniciar-processo-de-diagnóstico), dependendo do tipo do processo.

## **CDU-04 \- Iniciar processo de mapeamento** {#cdu-04---iniciar-processo-de-mapeamento}

**Ator principal**: ADMIN

**Ator secundário**: Serviço de envio de e-mails (dispara as notificações)

**Pré-condições:** 

* Login realizado com perfil ADMIN  
* Existência de processo de mapeamento na situação ‘Criado’

**Fluxo principal:**

1. No **Painel**, ADMIN clica em um processo de mapeamento que esteja na situação 'Criado'.  
2. Sistema muda para a tela **Cadastro de processo** com os campos preenchidos com as informações do processo selecionado.  
3. ADMIN clica no botão ***Iniciar processo***.  
4. Sistema mostra diálogo de confirmação: "Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes serão notificadas por e-mail.", botões ***Confirmar*** / ***Cancelar***.  
5. Caso ADMIN escolha ***Cancelar***, o sistema interrompe a operação de iniciação do processo, permanecendo na mesma tela.  
6. ADMIN confirma.  
7. Sistema armazena internamente uma cópia da árvore de unidades participantes e a vincula com o processo a fim de preservar a representação hierárquica vigente no momento da sua inicialização.  
8. Sistema muda a situação do processo de mapeamento para 'Em andamento';  
9. Sistema cria internamente um subprocesso para cada unidade participante que seja operacional ou interoperacional, com os seguintes campos e valores iniciais:  
   1. **Data limite etapa 1**: Data copiada da data limite da etapa inicial do processo.  
   2. **Situação**: Não iniciado  
   3. **Observações**: Campo de texto formatado reservado para registro de informações futuras pela SEDOC.  
   4. **Sugestões**: Campo de texto formatado reservado para registro de sugestões futuras pelas unidades.  
10. Sistema registra uma movimentação para cada subprocesso criado com os campos:  
    1. **Data/hora**: Data/hora atual  
    2. **Unidade origem**: SEDOC  
    3. **Unidade destino**: \[SIGLA\_UNIDADE\_SUBPROCESSO\]  
    4. **Descrição**: ‘Processo iniciado’  
11. O sistema envia notificações por e-mail para todas as unidades participantes.   
    1. As notificações devem seguir para os endereços de e-mail das unidades (ex. *sesel@tre-pe.jus.br*) e dos servidores responsáveis por elas.  
    2. Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo:

       ***Assunto:** SGC: Início de processo de mapeamento de competências*

       *Prezado(a) responsável pela \[SIGLA\_UNIDADE\],*

       *Comunicamos o início do processo \[DESCRICAO DO PROCESSO\] para a sua unidade.* 

       *Já é possível realizar o cadastro de atividades e conhecimentos no Sistema de Gestão de Competências (\[URL\_SISTEMA\]).*

       *O prazo para conclusão desta etapa do processo é \[DATA\_LIMITE\].*

    3. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das unidades operacionais e interoperacionais subordinadas a elas, segundo o modelo:

       ***Assunto:** SGC: Início de processo de mapeamento de competências em unidades subordinadas*

       *Prezado(a) responsável pela \[SIGLA\_UNIDADE\],*

       *Comunicamos o início do processo \[DESCRICAO DO PROCESSO\] nas unidades \[LISTA\_DE\_SIGLAS\_DAS\_UNIDADES\_SUBORDINADAS\]. Estas unidades já podem iniciar o cadastro de atividades e conhecimentos. À medida que estes cadastros forem sendo disponibilizados, será possível visualizar e realizar a sua validação.*

       *O prazo para conclusão desta etapa do processo é \[DATA\_LIMITE\].*

       *Acompanhe o processo no Sistema de Gestão de Competências: \[URL\_SISTEMA\].*

12. O sistema cria internamente alertas para todas as unidades participantes.  
    1. Para cada unidade operacional será criado um alerta com a seguinte informação:  
       1. **Descrição:** “Início do processo”  
       2. **Processo**: \[DESCRICAO DO PROCESSO\]  
       3. **Data/hora**: Data/hora atual  
       4. **Unidade de origem:** SEDOC   
       5. **Unidade de destino:** \[SIGLA\_UNIDADE\].  
    2. Para cada unidade intermediária será criado um alerta com a seguinte informação:  
       1. **Descrição:** “Início do processo em unidade(s) subordinada(s)”  
       2. **Processo**: \[DESCRICAO DO PROCESSO\]  
       3. **Data/hora**: Data/hora atual  
       4. **Unidade de origem:** SEDOC   
       5. **Unidade de destino:** \[SIGLA\_UNIDADE\].  
    3. Para cada unidade interoperacional serão criados dois alertas: um de unidade operacional e outro de unidade intermediária, como especificado acima.

## **CDU-05 \- Iniciar processo de revisão** {#cdu-05---iniciar-processo-de-revisão}

**Ator principal**: ADMIN

**Ator secundário**: Serviço de envio de e-mails (dispara as notificações)

**Pré-condições:** 

* Login realizado com perfil ADMIN  
* Existência de processo de revisão na situação ‘Criado’

**Fluxo principal:**

1. No **Painel**, ADMIN clica em um processo de revisão que esteja na situação 'Criado'.  
2. O sistema muda para a tela **Cadastro de processo** com os campos preenchidos com as informações do processo selecionado.  
3. ADMIN clica no botão ***Iniciar processo***.  
4. O sistema mostra diálogo de confirmação: "Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes serão notificadas por e-mail.", botões ***Confirmar*** e ***Cancelar***.  
5. Caso ADMIN escolha ***Cancelar***, o sistema interrompe a operação de iniciação do processo, permanecendo na mesma tela.  
6. ADMIN confirma.  
7. Sistema armazena internamente uma cópia da árvore de unidades participantes e a vincula com o processo, a fim de preservar a hierarquia de unidades vigente, no momento da iniciação do processo.  
8. Sistema muda a situação do processo de revisão para 'Em andamento';  
9. Sistema cria internamente um subprocesso para cada unidade operacional ou interoperacional participante, com os seguintes campos e valores iniciais:  
   1. **Data limite etapa 1**: Data copiada da data limite da etapa inicial do processo.  
   2. **Situação**: 'Não iniciado'  
   3. **Observações**: Campo de texto formatado reservado para registro de informações futuras pela SEDOC.  
   4. **Sugestões**: Campo de texto formatado reservado para registro de sugestões futuras pelas unidades.  
10. O sistema cria internamente uma cópia do mapa de competências vigente, juntamente com as respectivas atividades e conhecimentos, de cada unidade operacional ou interoperacional participante, vinculando-o ao subprocesso da unidade.  
11. Sistema registra uma **movimentação** para cada subprocesso criado com os campos:  
    1. **Data/hora**: Data/hora atual  
    2. **Unidade origem**: ‘SEDOC’  
    3. **Unidade destino**: \[SIGLA\_UNIDADE\_SUBPROCESSO\]  
    4. **Descrição**: ‘Processo iniciado’  
12. O sistema envia notificações por e-mail para todas as unidades participantes.   
    1. As notificações devem seguir para os endereços de e-mail das unidades (ex. *sesel@tre-pe.jus.br*) e dos servidores responsáveis por elas.  
    2. Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo:

       ***Assunto:** SGC: Início de processo de revisão do mapa de competências*

       *Prezado(a) responsável pela \[SIGLA\_UNIDADE\],*

       *Comunicamos o início do processo \[DESCRICAO DO PROCESSO\] para a sua unidade.*

       *Já é possível realizar a revisão do seu cadastro de atividades e conhecimentos no Sistema de Gestão de Competências (\[URL\_SISTEMA\]).*

       *O prazo para conclusão desta etapa do processo é \[DATA\_LIMITE\].*

    3. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das unidades operacionais e interoperacionais subordinadas a elas, segundo o modelo:

       ***Assunto:** SGC: Início de processo de mapeamento de competências em unidades subordinadas*

       *Prezado(a) responsável pela \[SIGLA\_UNIDADE\],*

       *Comunicamos o início do processo \[DESCRICAO DO PROCESSO\] nas unidades \[LISTA\_DE\_SIGLAS\_DAS\_UNIDADES\_SUBORDINADAS\]. Estas unidades já podem iniciar a revisão do cadastro de atividades e conhecimentos. À medida que estas revisões forem sendo disponibilizadas, será possível visualizar e realizar a sua validação.*

       *O prazo para conclusão desta etapa do processo é \[DATA\_LIMITE\].*

       *Acompanhe o processo no Sistema de Gestão de Competências: \[URL\_SISTEMA\].*

13. O sistema cria internamente alertas para todas as unidades participantes.  
    1. Para cada unidade operacional será criado um alerta com a seguinte informação:  
       1. **Descrição:** “Início do processo”  
       2. **Processo**: \[DESCRICAO DO PROCESSO\]  
       3. **Data/hora**: Data/hora atual  
       4. **Unidade de origem:** SEDOC   
       5. **Unidade de destino:** \[SIGLA\_UNIDADE\].  
    2. Para cada unidade intermediária será criado um alerta com a seguinte informação:  
       1. **Descrição:** “Início do processo em unidade(s) subordinada(s)”  
       2. **Processo**: \[DESCRICAO DO PROCESSO\]  
       3. **Data/hora**: Data/hora atual  
       4. **Unidade de origem:** SEDOC   
       5. **Unidade de destino:** \[SIGLA\_UNIDADE\].  
    3. Para cada unidade interoperacional serão criados dois alertas: um de unidade operacional e outro de unidade intermediária, como especificado acima.

## **CDU-06 \- Iniciar processo de diagnóstico** {#cdu-06---iniciar-processo-de-diagnóstico}

**Ator principal**: ADMIN

**Pré-condições**: 

**Fluxo principal:**

## **CDU-07A \- Detalhar processo** {#cdu-07a---detalhar-processo}

**Ator principal**: ADMIN e GESTOR

**Pré-condições**: 

* Usuário ter feito login com os perfis ADMIN ou GESTOR  
* Ao menos um processo nas situações ‘Em Andamento’ ou ‘Finalizado’. 

**Fluxo principal:**

1. Sistema mostra a tela **Detalhes do processo** com os dados do processo escolhido.  
2. A tela será composta pelas seções ***Dados do processo*** e ***Unidades participantes***.  
   1. Seção ***Dados do processo** (sem título)**:***  
      1. Informações da Descrição do Tipo e da Situação dos processos (ver [Situações de processos](https://docs.google.com/document/d/1bnyhBfW5AMvCht5aaeqJ3IIWU-itApdVb98TMTf5KWE/edit?pli=1&tab=t.0#heading=h.px64uax98gkk)).   
      2. Se perfil ADMIN, botão ‘Finalizar processo’.  
   2. Seção ***Unidades participantes:***  
      1. Subárvore das unidades hierarquicamente inferiores.  
         * Para cada unidade operacional e interoperacional da subárvore são exibidas, em linha, as informações da situação do subprocesso da unidade e da data limite para a conclusão da etapa atual do processo naquela unidade.  
         * O usuário poderá clicar nas unidades operacionais e interoperacionais para visualizar a tela **Detalhes do subprocesso** (estende caso de uso [Detalhar subprocesso](#cdu-07b---detalhar-subprocesso)) com os dados da unidade selecionada.  
           * Caso o perfil do usuário seja ADMIN, serão exibidos, na seção ***Dados da unidade*** da tela, elementos para possibilitar a alteração da data limite da etapa atual da unidade assim como da situação atual do subprocesso da unidade (ex. Reabertura do cadastro de atividades)  
      2. Caso existam unidades subordinadas cujos subprocessos estejam localizados na unidade do usuário, os seguintes botões poderão ser apresentados:  
         * ***Aceitar/Homologar cadastro em bloco***, se existirem unidades subordinadas com subprocesso na situação ‘Cadastro disponibilizado’ (processo de mapeamento) ou ‘Revisão do cadastro disponibilizada’ (processo de revisão)  (ver caso de uso [Aceitar/homologar cadastro em bloco](#cdu-14---aceitar/homologar-cadastro-em-bloco)).  
         * ***Aceitar/Homologar mapa de competências em bloco***, se existirem unidades subordinadas com subprocesso nas situações ‘Mapa validado’ ou ‘Mapa com sugestões’ (ver caso de uso [Aceitar/Homologar mapa de competências em bloco](#cdu-22---aceitar/homologar-mapa-de-competências-em-bloco)).

## **CDU-07B \- Detalhar subprocesso** {#cdu-07b---detalhar-subprocesso}

**Ator principal**: CHEFE e SERVIDOR

**Ator secundário:** ADMIN E GESTOR (acesso através do caso de uso Detalhar processo)

**Pré-condições**: 

* Usuário ter feito login (qualquer perfil).  
* Ao menos um subprocesso nas situações ‘Em Andamento’ ou ‘Finalizado’. 

**Fluxo principal:**

1. Sistema mostra a tela **Detalhes do subprocesso** com os dados do subprocesso da unidade do perfil.  
2. A tela **Detalhes do subprocesso** será composta por três seções: ***Dados da Unidade***, ***Movimentações do Processo*** e ***Elementos do Processo***.  
   1. Na seção ***Dados da unidade*** serão apresentadas as informações:  
      1. *(Sem título)*: Sigla e nome da unidade, destacado.  
      2. **Titular**: \[Nome do titular\]  
      3. **Responsável:**  
         * \[Nome do responsável\]  
         * \[Tipo da responsabilidade\]: com valores possíveis: "Titular", "Substituição (até \[DATA\_TERM\_SUBST)", "Atrib. temporária (até \[DATA\_TERM\_ATRIB\])"  
         * **Ramal**: \[Ramal do servidor no SGRH\]  
         * **E-mail**: \[Endereço de e-mail do servidor no SGRH\]  
      4. **Situação**: Informação descritiva da situação do subprocesso da unidade (ver seção [Situações de subprocessos](https://docs.google.com/document/d/1bnyhBfW5AMvCht5aaeqJ3IIWU-itApdVb98TMTf5KWE/edit?pli=1&tab=t.0#heading=h.blzodn5ksjx)).  
      5. **Localização atual**: Unidade destino da última movimentação do subprocesso da unidade.  
      6. **Prazo para conclusão da etapa atual**: Data limite da última etapa do subprocesso ainda não concluída na unidade.  
   2. Na seção ***Movimentações do processo***, título "Movimentações", é apresentada uma tabela com as movimentações que o subprocesso já teve até o momento, com os campos: Data/hora da movimentação, unidade origem, unidade destino e descrição da movimentação. As informações deverão ser apresentadas em ordem decrescente de data/hora.  
   3. Na seção ***Elementos do processo*** (sem título) serão apresentados *cards* clicáveis com informação variável em função do tipo do processo, os quais darão acesso às telas específicas de cada tema.  
      1. Se o processo for dos tipos Mapeamento ou Revisão, a seção apresentará *cards* para acesso ao cadastro de ***Atividades e conhecimentos*** (descrição “Cadastro de atividades e conhecimentos da unidade”) e ao ***Mapa de competências*** (descrição “Mapa de competências da unidade”).  
         * O card ***Atividades e conhecimentos*** estará sempre habilitado para usuários com o perfil CHEFE, inclusive com opções para alteração do cadastro (ver caso de uso [Manter cadastro de atividades e conhecimentos](#cdu-08---manter-cadastro-de-atividades-e-conhecimentos)**)** . Para os demais perfis, a habilitação acontecerá apenas após a disponibilização do cadastro pelo CHEFE.   
         * O card ***Mapa de Competências*** será habilitado inicialmente apenas para o perfil ADMIN, após a homologação do cadastro ou da revisão cadastral. Posteriormente, com a disponibilização do mapa, o card será liberado também para os demais perfis de usuários.  
      2. Se o processo for do tipo Diagnóstico, a seção apresentará estes cards:  
         * ***Diagnóstico da equipe*** (descrição “Diagnóstico das competências pelos servidores da unidade”)   
         * ***Ocupações críticas*** (descrição “Identificação das ocupações críticas da unidade”).

           OBS: Os elementos de processos de diagnóstico ainda estão sendo definidos.

## **CDU-08 \- Manter cadastro de atividades e conhecimentos** {#cdu-08---manter-cadastro-de-atividades-e-conhecimentos}

**Ator**: CHEFE

**Pré-condições**: 

* Usuário logado com perfil CHEFE  
* Processo de mapeamento ou de revisão iniciado que tenha a unidade como participante  
* Subprocesso da unidade com localização atual na própria unidade e situação ‘Não iniciado’ e ‘Cadastro em andamento’, no caso de processos de mapeamento, ou ‘Não iniciado’ e ‘Revisão do cadastro em andamento’ no caso de processos de revisão

**Fluxo principal**:

1. No **Painel**, CHEFE clica em um processo de mapeamento ou revisão da lista de processos.  
2. Sistema mostra a tela **Detalhes do subprocesso** com os dados da unidade.  
3. CHEFE clica no *card* ***Atividades e conhecimentos***.  
4. Sistema apresenta a tela **Cadastro de atividades e conhecimentos** da unidade. Se o processo for de revisão ou se o cadastro já tiver sido iniciado anteriormente, a tela já virá previamente preenchida com os dados atualmente associados ao subprocesso.  
5. Para incluir uma nova atividade, CHEFE fornece a descrição da atividade, e clica no botão de adição.  
6. Sistema adiciona atividade e mostra campo para adição de conhecimento abaixo da atividade.  
7. CHEFE fornece a descrição do conhecimento e clica no botão de adição correspondente.  
8. Sistema adiciona o conhecimento, associando-o à atividade.   
   1. Deve ser indicada claramente a associação entre o conhecimento e a atividade, indentando os conhecimentos da atividade abaixo da descrição desta.  
9. CHEFE repete o fluxo de adição de atividades/conhecimentos.   
   1. Pode-se incluir primeiro várias atividades e depois os conhecimentos correspondentes; ou trabalhar em uma atividade por vez até concluir todos os seus conhecimentos. O sistema deve permitir os dois modos de trabalho.  
10. Para cada **atividade** já cadastrada, ao passar o mouse, o sistema exibe botões de edição e remoção.  
    1. Se o usuário clicar em ***Editar***, sistema habilita a edição do nome da atividade e exibe ao lado um botão ***Salvar*** e outro ***Cancelar***.   
       1. Se usuário clicar em ***Salvar***, o sistema salva a alteração e volta a exibir os botões ***Editar*** e ***Remover*** ao lado do nome da atividade.  
       2. Se o usuário clicar em ***Cancelar***, o sistema não salva a alteração e volta a exibir o nome da atividade que estava antes da modificação com os botões ***Editar*** e ***Remover*** ao lado.  
    2. Se o usuário clicar em ***Remover***, o sistema solicita que o usuário confirme a operação. Se o usuário confirmar, a atividade e todos os conhecimentos associados a ela são removidos.  
11. De forma análoga à usada para atividades, para cada **conhecimento** já cadastrado, o sistema exibe, ao passar o mouse sobre o conhecimento, uma opção ***Editar*** e outra ***Remover***.  
    1. Se o usuário clicar em ***Editar***, o sistema habilita a edição do nome do conhecimento e exibe ao lado um botão ***Salvar*** e outro ***Cancelar***.  
       1. Se usuário clicar em ***Salvar***, o sistema salva a alteração e volta a exibir os botões ***Editar*** e ***Remover*** ao lado do nome do conhecimento.  
       2. Se usuário clicar em ***Cancelar***, o sistema não salva a alteração e volta a exibir o nome do conhecimento que estava antes da modificação com os botões ***Editar*** e ***Remover*** ao lado.  
    2. Se usuário clicar em ***Remover***, o sistema solicita que o usuário confirme a operação. Se o usuário confirmar, o conhecimento é removido.  
12. Opcionalmente, o CHEFE clica no botão ***Importar atividades***:  
    1. Sistema exibe um modal com uma lista dos processos com tipo Mapeamento ou Revisão que estejam com situação *Finalizado*.  
    2. CHEFE escolhe um processo da lista.  
    3. O sistema recupera as unidades operacionais e interoperacionais participantes do processo selecionado e expande o modal para mostrar estas unidades em uma lista (siglas).  
    4. CHEFE escolhe uma unidade da lista.  
    5. Sistema recupera as atividades/conhecimentos da unidade selecionada, e expande o modal para mostrar a lista de atividades, permitindo a seleção múltipla.  
    6. CHEFE marca uma ou mais atividades e clica em ***Importar***.  
    7. Sistema faz uma cópia das atividades selecionadas e seus respectivos conhecimentos para o cadastro de atividades da unidade atual.  
       1. Deverão ser importadas apenas as atividades cujas descrições não corresponderem a nenhuma atividade atualmente cadastrada na unidade.  
       2. Caso haja coincidência entre a descrição de uma atividade selecionada com a de alguma atividade cadastrada na unidade, Sistema informa que uma ou mais atividades não puderam ser importadas por já existirem no cadastro.  
13. Se, no momento da criação/edição/importação de qualquer informação, a situação do subprocesso da unidade ainda estiver ‘Não iniciado’, Sistema altera a situação para 'Cadastro em andamento', no caso de processo de mapeamento, ou ‘Revisão do cadastro em andamento’, no caso de processo de revisão.  
14. Quando estiver satisfeito com o cadastro das atividades e conhecimentos, o CHEFE poderá clicar em ***Disponibilizar**,* ou navegar para outra parte do sistema (as atividades e conhecimentos são sempre salvas e vinculadas ao subprocesso, depois da criação/edição/remoção, então não é necessário atuar para que sejam salvas). Dúvida: se o botão estiver sempre habilitado, o ator CHEFE poderá clicar a qualquer tempo, devendo fazer ao término do cadastramento, o que talvez seja o que está sendo dito com o quando estiver satisfeito com o cadastro das atividade e conhecimentos. Sugestão:  
15. Após finalizar o cadastro das atividades e conhecimentos, o CHEFE poderá clicar em "Disponibilizar" ou simplesmente navegar para outra área do sistema. As informações são salvas automaticamente e vinculadas ao subprocesso após cada ação de criação, edição ou exclusão, não sendo necessário realizar nenhuma ação adicional para garantir o salvamento.  
16. No caso de processos de revisão, haverá ainda um botão ***Impactos no mapa***, para verificar as competências impactadas pelas alterações feitas no cadastro.

## **CDU-09 \- Disponibilizar cadastro** {#cdu-09---disponibilizar-cadastro}

**Ator**: CHEFE

**Pré-condições**:

* Usuário logado com perfil CHEFE.  
* Subprocesso de mapeamento da unidade na situação ‘Cadastro em andamento’.

**Fluxo principal:**

1. No **Painel**, CHEFE clica no processo de mapeamento na situação ‘Em Andamento’.  
2. Sistema mostra tela **Detalhes do subprocesso** da unidade.  
3. CHEFE clica em ***Atividades e Conhecimentos***.  
4. Sistema mostra tela **Cadastro de atividades e conhecimentos** preenchida com os dados cadastrados até o momento.  
5. Se o subprocesso da unidade tiver retornado de análise pelas unidades superiores, deverá ser exibido o botão ***Histórico de análise***:  
   1. Se CHEFE clicar no botão ***Histórico de análise***, sistema exibe, em uma janela modal, os dados das análises realizadas pelas unidades superiores desde a última disponibilização. Essas informações podem ser usadas para orientar quais ajustes precisam ser feitos no cadastro antes da realização de uma nova disponibilização.  
6. CHEFE clica no botão ***Disponibilizar***.  
7. Sistema verifica se todas as atividades têm ao menos um conhecimento associado.   
8. Caso negativo, indica quais atividades estão precisando de adição de conhecimentos e impede a disponibilização.  
9. Caso positivo, sistema mostra diálogo de confirmação: título ''Disponibilização do cadastro", mensagem "Confirma finalização e disponibilização do cadastro? Essa ação bloqueia a edição e habilita a validação por unidades superiores''/ Botões ***Disponibilizar*** e ***Cancelar***.  
10. Caso CHEFE escolha ***Cancelar***, o sistema interrompe a operação de disponibilização do cadastro, permanecendo na mesma tela.  
11. CHEFE escolhe ***Disponibilizar***.  
12. Sistema altera a situação do subprocesso da unidade para 'Cadastro disponibilizado'  
13. Sistema registra uma movimentação para o subprocesso com os campos:  
    1. **Data/hora**: Data/hora atual  
    2. **Unidade origem**: \[SIGLA\_UNIDADE\_SUBPROCESSO\]  
    3. **Unidade destino**: \[SIGLA\_UNIDADE\_SUPERIOR\]  
    4. **Descrição**: ‘Disponibilização do cadastro de atividades’  
14. Sistema notifica a unidade superior hierárquica quanto à disponibilização, com e-mail no modelo abaixo:

    ***Assunto:** SGC: Cadastro de atividades e conhecimentos disponibilizado: \[SIGLA\_UNIDADE\_SUBPROCESSO\]*

    *Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUPERIOR\],*

    *A unidade \[SIGLA\_UNIDADE\_SUBPROCESSO\] disponibilizou o cadastro de atividades e conhecimentos do processo \[DESCRICAO DO PROCESSO\].*

    *A análise desse cadastro já pode ser realizada no Sistema de Gestão de Competências (URL\_SISTEMA).* 

15. Sistema cria internamente um alerta com a seguinte informação:  
    1. **Descrição:** “Cadastro de atividades e conhecimentos da unidade \[SIGLA\_UNIDADE\_SUBPROCESSO\] disponibilizado para análise”  
    2. **Processo**: \[DESCRICAO DO PROCESSO\]  
    3. **Data/hora**: Data/hora atual  
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]   
    5. **Unidade de destino:** \[SIGLA\_UNIDADE\_SUPERIOR\].  
16. Sistema define a data/hora de conclusão da etapa 1 do subprocesso da unidade como sendo a data/hora atual.  
17. Sistema exclui o histórico de análise do cadastro do subprocesso da unidade.  
18. Sistema mostra confirmação: "Cadastro de atividades disponibilizado" e redireciona para **Painel**.

## **CDU-10 \- Disponibilizar revisão do cadastro** {#cdu-10---disponibilizar-revisão-do-cadastro}

**Ator**: CHEFE

**Pré-condições**:

* Usuário logado com perfil CHEFE.  
* Subprocesso de revisão da unidade na situação ‘Revisão do cadastro em andamento’.

**Fluxo principal:**

1. No **Painel**, CHEFE clica no processo de revisão na situação ‘Em Andamento’.  
2. Sistema mostra tela **Detalhes do subprocesso** da unidade.  
3. CHEFE clica em ***Atividades e conhecimentos***.  
4. Sistema mostra tela **Cadastro de atividades e conhecimentos** preenchida com os dados cadastrados/revisados até o momento.  
5. Se o subprocesso da unidade tiver retornado de análise pelas unidades superiores, deverá ser exibido o botão ***Histórico de análise***.  
   1. Se CHEFE clicar no botão ***Histórico de análise***, sistema exibe, em uma janela modal, os dados das análises realizadas pelas unidades superiores desde a última disponibilização. Essas informações podem ser usadas para orientar quais ajustes precisam ser feitos no cadastro antes da realização de uma nova disponibilização.  
6. CHEFE clica no botão ***Disponibilizar***.  
7. Sistema verifica se todas as atividades têm ao menos um conhecimento associado.   
8. Caso negativo, indica quais atividades estão precisando de adição de conhecimentos e impede a disponibilização.  
9. Caso positivo, sistema mostra diálogo de confirmação: título ''Disponibilização da revisão do cadastro", mensagem "Confirma finalização revisão e disponibilização do cadastro? Essa ação bloqueia a edição e habilita a validação por unidades superiores''/ Botões ***Disponibilizar*** e ***Cancelar***.  
10. Caso CHEFE escolha ***Cancelar***, o sistema interrompe a operação de disponibilização da revisão do cadastro, permanecendo na mesma tela.  
11. CHEFE escolhe ***Disponibilizar***.  
12. Sistema altera a situação do subprocesso da unidade para 'Revisão do cadastro disponibilizada'  
13. Sistema registra uma movimentação para o subprocesso com os campos:  
    1. **Data/hora**: Data/hora atual  
    2. **Unidade origem**: \[SIGLA\_UNIDADE\_SUBPROCESSO\]  
    3. **Unidade destino**: \[SIGLA\_UNIDADE\_SUPERIOR\]  
    4. **Descrição**: ‘Disponibilização da revisão do cadastro de atividades’  
14. Sistema notifica unidade superior hierárquica quanto à disponibilização, com e-mail no modelo abaixo:

    ***Assunto:** SGC: Revisão do cadastro de atividades e conhecimentos disponibilizada: \[SIGLA\_UNIDADE\_SUBPROCESSO\]*

    *Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUPERIOR\],*

    *A unidade \[SIGLA\_UNIDADE\_SUBPROCESSO\] concluiu a revisão e disponibilizou seu cadastro de atividades e conhecimentos do processo \[DESCRICAO DO PROCESSO\].*

    *A análise desse cadastro já pode ser realizada no Sistema de Gestão de Competências (URL\_SISTEMA).* 

15. Sistema cria internamente um alerta com a seguinte informação:  
    1. **Descrição:** “Cadastro de atividades e conhecimentos da unidade \[SIGLA\_UNIDADE\_SUBPROCESSO\] disponibilizado para análise”  
    2. **Processo**: \[DESCRICAO DO PROCESSO\]  
    3. **Data/hora**: Data/hora atual  
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]   
    5. **Unidade de destino:** \[SIGLA\_UNIDADE\_SUPERIOR\].  
16. Sistema define a data/hora de conclusão da etapa 1 do subprocesso da unidade como sendo a data/hora atual.  
17. Sistema exclui o histórico de análise do cadastro do subprocesso da unidade.  
18. Sistema mostra confirmação: "Revisão do cadastro de atividades disponibilizada" e redireciona para **Painel**.

## **CDU-11 \- Visualizar cadastro de atividades e conhecimentos** {#cdu-11---visualizar-cadastro-de-atividades-e-conhecimentos}

**Ator**: Usuário (todos os perfis)

**Pré-condições**: 

* Processo de mapeamento ou de revisão iniciado ou finalizado, que tenha a unidade como participante  
* Subprocesso da unidade com cadastro de atividades e conhecimentos (processo de mapeamento) ou revisão do cadastro (processo de revisão) já disponibilizados.

**Fluxo principal**:

1. No **Painel**, usuário clica no processo de mapeamento ou revisão na situação ‘Em Andamento’ ou ‘Finalizado’.  
2. Se perfil logado for ADMIN ou GESTOR:  
   1. Sistema exibe a tela **Detalhes do processo**  
   2. Usuário clica em uma unidade subordinada que seja operacional ou interoperacional  
   3. Sistema exibe a tela **Detalhes do subprocesso** com os dados do subprocesso da unidade selecionada  
3. Se perfil logado for CHEFE ou SERVIDOR:  
   1. Sistema exibe a tela **Detalhes do subprocesso** com os dados do subprocesso da unidade do usuário  
4. Na tela de **Detalhes do subprocesso**, usuário clica no *card* ***Atividades e conhecimentos***.  
5. Sistema apresenta a tela **Atividades e conhecimentos** preenchida com os dados da unidade.  
6. Nesta tela, são apresentados a sigla e o nome da unidade e cada atividade é apresentada como uma tabela tendo como cabeçalho a descrição da atividade e as linhas preenchidas com os conhecimentos cadastrados para aquela atividade.

## **CDU-12 \- Verificar impactos no mapa de competências** {#cdu-12---verificar-impactos-no-mapa-de-competências}

**Ator**: CHEFE

**Pré-condições**:

* Usuário logado com perfil CHEFE.  
* Um mapa de competências já deve existir e estar associado ao subprocesso da unidade.  
* O CHEFE realizou ao menos uma alteração (adição, edição ou remoção) nas atividades ou conhecimentos da unidade.

**Fluxo principal**:

1. Na tela **Cadastro de atividades e conhecimentos**, após ter realizado modificações, o CHEFE clica no botão ***Impacto no mapa***.  
2. Sistema determina os impactos sobre as competências, seguindo estas regras:  
   1. Sistema usa como base duas fontes principais:  
      1. O **mapa de competências vigente** associado ao subprocesso da unidade, que contém a lista de competências e as atividades que estão vinculadas a cada uma.  
      2. As **adições**, **remoções** e **alterações** em atividades e conhecimentos realizadas desde a do início do subprocesso de revisão para aquela unidade.	  
   2. Se houver qualquer **alteração ou remoção de atividade**, ou qualquer **mudança em um conhecimento** de uma atividade (adição, alteração ou remoção), sistema verifica quais competências no mapa estão vinculadas à atividade e guarda a atividade correspondente. Todas as competências que possuem esse vínculo são marcadas como impactadas.  
   3. Se houver atividades criadas, estas serão listadas separadamente na seção **Atividades inseridas**. Não aparecem na seção **Competências impactadas** porque, por definição, ainda não estão associadas a nenhuma competência no mapa. A associação ocorrerá em etapa posterior, na edição do próprio mapa.  
   4. Se sistema determinar que nenhuma competência foi afetada pelas alterações, o sistema mostra a mensagem "Nenhuma competência foi impactada."  
   5. Se sistema determinar que houve impacto, mostra o modal **Impacto no Mapa de Competências**, com as seguintes seções:  
      1. **Atividades inseridas** (opcional): Se novas atividades foram adicionadas, esta seção é exibida, listando cada atividade com um ícone de adição. Abaixo de cada atividade, são listados os conhecimentos associados a ela recém-adicionados, se houver.  
      2. **Competências impactadas** (sempre exibida): Mostra as competências do mapa existente que foram afetadas pelas alterações.  
   6. Para cada competência impactada, o sistema exibe seu nome como título e, abaixo, uma lista detalhada de cada mudança que pode ter impacto sobre a competência:  
      1. **Adição**: Mostra um ícone indicando adição, e o texto "Atividade adicionada" ou "Conhecimento adicionado", seguido pela descrição do item.  
      2. **Remoção**: Mostra um ícone indicando remoção, e o texto "Atividade removida" ou "Conhecimento removido", seguido pela descrição do item.  
      3. **Alteração**: Mostra um ícone indicando alteração, e o texto "Atividade alterada" ou "Conhecimento alterado", seguido pela descrição do item e os valores antes e depois da mudança (De "X" para "Y").  
3. O CHEFE analisa as informações apresentadas no modal e, ao concluir a análise, clica em ***Fechar***.  
4. O sistema fecha o modal, retornando o usuário para a tela **Cadastro de atividades e conhecimentos**, que permanece com seu estado inalterado.

## **CDU-13 \- Analisar cadastro de atividades e conhecimentos** {#cdu-13---analisar-cadastro-de-atividades-e-conhecimentos}

**Atores:** GESTOR e ADMIN

**Pré-condição**: 

* Processo de mapeamento ou de revisão iniciado que tenha a unidade como participante  
* Subprocesso com cadastro de atividades e conhecimentos (processo de mapeamento) ou revisão do cadastro (processo de revisão) já disponibilizado, e com localização atual na unidade do usuário.

**Fluxo principal:**

1. No **Painel**, usuário clica no processo de mapeamento.  
2. Sistema exibe a tela **Detalhes do processo**  
3. Usuário clica na unidade subordinada cujo cadastro de atividades deseja validar.  
4. Sistema exibe a tela **Detalhes do subprocesso** com os dados da unidade selecionada  
5. Na tela de **Detalhes do subprocesso**, usuário clica no *card* ***Atividades e conhecimentos***.  
6. Sistema apresenta a tela **Atividades e conhecimentos** preenchida com os dados da unidade e com os botões ***Devolver para ajustes*** e ***Registrar aceite***, caso o perfil logado seja GESTOR, ou ***Devolver para ajustes*** e ***Homologar***, caso o perfil logado seja ADMIN.  
7. Sistema exibe, na tela **Atividades e conhecimentos**, o histórico da análise prévia recebida pelo cadastro desde a sua última disponibilização.  
   1. Cada linha do histórico apresentará a sigla da unidade que analisou, o resultado da análise (aceite ou devolução) e a observação fornecida pela unidade responsável pela análise.  
8. Caso o processo seja de revisão, a tela **Atividades e conhecimentos** também incluirá o botão ***Verificar impactos no mapa de competências***, a partir do qual será possível verificar as competências impactadas pela alteração realizada no cadastro de atividades e conhecimentos da unidade.  
9. Usuário analisa as informações e opta por aceitar/homologar ou devolver o cadastro para ajustes.   
10. Se optar por **devolver**:  
    1. Usuário clica em ***Devolver para ajustes***.  
    2. Sistema abre um diálogo modal (título ***Devolução***) com a pergunta ‘Confirma a devolução do cadastro de atividades?’, um campo para preenchimento de uma observação opcional e os botões ***Confirmar*** ou ***Cancelar***.  
    3. Usuário opcionalmente informa a observação e escolhe ***Confirmar***.  
    4. Sistema registra uma análise de cadastro para o subprocesso com as informações:  
       1. **Data/hora**: Data/hora atual  
       2. **Unidade**: \[SIGLA\_UNIDADE\]  
       3. **Resultado**: ‘Devolução’  
       4. **Observação**: A observação da janela modal, caso tenha sido fornecida.  
    5. Sistema identifica a **unidade de devolução** como sendo a unidade de origem da última movimentação do subprocesso.  
    6. Sistema registra uma movimentação para o subprocesso com os campos:  
       1. **Data/hora**: Data/hora atual  
       2. **Unidade origem**: \[SIGLA\_UNIDADE\]  
       3. **Unidade destino**: \[SIGLA\_UNIDADE\_DEVOLUCAO\]  
       4. **Descrição**: ‘Devolução do cadastro de atividades e conhecimentos para ajustes’  
    7. Se a unidade de devolução for a própria unidade do subprocesso, Sistema altera a situação do subprocesso para 'Cadastro em andamento' e limpa a data/hora de conclusão da etapa 1 do subprocesso da unidade.  
    8. Sistema envia notificação por e-mail para a unidade de devolução:

       ***Assunto:** SGC: Cadastro de atividades e conhecimentos da \[SIGLA\_UNIDADE\_SUBPROCESSO\] devolvido para ajustes*

       *Prezado(a) responsável pela \[*SIGLA\_UNIDADE\_DEVOLUCAO*\],*

       *O cadastro de atividades e conhecimentos da \[SIGLA\_UNIDADE\_SUBPROCESSO\] no processo \[DESCRICAO DO PROCESSO\] foi devolvido para ajustes, devendo posteriormente ser novamente submetido para análise pela \[SIGLA\_UNIDADE\].*

       *Acompanhe o processo no Sistema de Gestão de Competências: \[URL\_SISTEMA\].*

    9. Sistema cria internamente um alerta com a seguinte informação:  
       1. **Descrição:** “Cadastro de atividades e conhecimentos da unidade \[SIGLA\_UNIDADE\_SUBPROCESSO\] devolvido para ajustes”  
       2. **Processo**: \[DESCRICAO DO PROCESSO\]  
       3. **Data/hora**: Data/hora atual  
       4. **Unidade de origem:** \[SIGLA\_UNIDADE\]   
       5. **Unidade de destino:** \[SIGLA\_UNIDADE\_DEVOLUCAO\].  
    10. Sistema mostra a mensagem "Devolução efetivada" e redireciona para **Painel**.  
11. Se optar por **aceitar** (perfil GESTOR):  
    1. Usuário clica em ***Registrar aceite***.  
    2. Sistema abre um diálogo modal (título ***Aceite***) com a pergunta ‘Confirma o aceite do cadastro de atividades?’, um campo para preenchimento de uma observação opcional e os botões ***Confirmar*** ou ***Cancelar***.  
    3. Usuário opcionalmente informa a observação e escolhe ***Confirmar***.  
    4. Sistema registra uma análise de cadastro para o subprocesso com as informações:  
       1. **Data/hora**: Data/hora atual  
       2. **Unidade**: \[SIGLA\_UNIDADE\]  
       3. **Resultado**: ‘Aceite’  
       4. **Observação**: A observação da janela modal, caso tenha sido fornecida.  
    5. Sistema registra uma movimentação para o subprocesso com os campos:  
       1. **Data/hora**: Data/hora atual  
       2. **Unidade origem**: \[SIGLA\_UNIDADE\]  
       3. **Unidade destino**: \[SIGLA\_UNIDADE\_SUPERIOR\]  
       4. **Descrição**: ‘Cadastro de atividades e conhecimentos validado’  
    6. Sistema envia notificação por e-mail para a unidade superior:

       ***Assunto:** SGC: Cadastro de atividades e conhecimentos da \[SIGLA\_UNIDADE\_SUBPROCESSO\] submetido para análise*

       *Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUPERIOR\],*

       *O cadastro de atividades e conhecimentos da \[SIGLA\_UNIDADE\_SUBPROCESSO\] no processo \[DESCRICAO DO PROCESSO\] foi submetido para análise por essa unidade.*

       *A análise já pode ser realizada no Sistema de Gestão de Competências (\[URL\_SISTEMA\]).*

    7. Sistema cria internamente um alerta com a seguinte informação:  
       1. **Descrição:** “Cadastro de atividades e conhecimentos da unidade \[SIGLA\_UNIDADE\_SUBPROCESSO\] submetido para análise”  
       2. **Processo**: \[DESCRICAO DO PROCESSO\]  
       3. **Data/hora**: Data/hora atual  
       4. **Unidade de origem:** \[SIGLA\_UNIDADE\]   
       5. **Unidade de destino:** \[SIGLA\_UNIDADE\_SUPERIOR\].  
    8. Sistema mostra a mensagem "Aceite registrado" e redireciona para **Painel**.  
12. Se optar por **homologar** (perfil ADMIN):  
    1. Usuário escolhe ***Homologar***.  
    2. Sistema abre um diálogo de confirmação (título ***Homologação***) com a pergunta ‘Confirma a homologação do cadastro de atividades?’ e os botões ***Confirmar*** ou ***Cancelar***.  
    3. Usuário escolhe ***Confirmar***.  
    4. Sistema registra uma movimentação para o subprocesso com os campos:  
       1. **Data/hora**: Data/hora atual  
       2. **Unidade origem**: ‘SEDOC’  
       3. **Unidade destino**: ‘SEDOC’  
       4. **Descrição**: ‘Cadastro de atividades e conhecimentos homologado’  
    5. Sistema altera a situação do subprocesso da unidade para ‘Cadastro homologado’, caso o processo seja de mapeamento, ou  'Revisão do cadastro homologada’, caso o processo seja de revisão.  
    6. Sistema mostra a mensagem "Homologação efetivada" e redireciona para a tela **Detalhes do subprocesso**.

## **CDU-14 \- Aceitar/homologar cadastro em bloco** {#cdu-14---aceitar/homologar-cadastro-em-bloco}

**Ator**: GESTOR e ADMIN

**Pré-condições**: 

**Fluxo principal:**

## **CDU-15 \- Manter mapa de competências** {#cdu-15---manter-mapa-de-competências}

**Ator**: ADMIN 

**Pré-condições**: 

* Processo de mapeamento com ao menos uma unidade com subprocesso nas situações 'Cadastro homologado' ou ‘Mapa criado’. 

**Fluxo principal:**

1. No **Painel** ADMIN escolhe um processo na tela **Detalhes do processo** clica em uma unidade operacional ou interoperacional com subprocesso nas situações 'Cadastro homologado' ou ‘Mapa criado’.  
2. Sistema mostra a tela **Detalhes do subprocesso**.  
3. ADMIN clica no *card* ***Mapa de competências***.  
4. Se ainda não tiver sido criado um mapa de competências para a unidade no processo selecionado:  
   1. Sistema cria internamente um mapa de competências vazio (sem competências cadastradas) e o associa ao subprocesso da unidade.  
   2. Sistema altera a situação do subprocesso da unidade para ‘Mapa criado’.  
5. Sistema mostra a tela **Edição de mapa** preenchida com os dados do mapa de competências da unidade**.**  
6. A tela **Edição de mapa** deverá apresentar os seguintes elementos:  
   1. Um lista de blocos com cada um representando uma competência criada no mapa.  
      1. O cabeçalho do bloco deverá ser preenchido pela descrição da competência  
         * Ao lado da descrição da competência, deverão ser apresentados 2 botões de ação (ícones), um para editar e outro para excluir a competência.  
      2. No conteúdo do bloco serão apresentadas as descrições das atividades associadas à competência  
         * Ao lado da descrição de cada atividade haverá um *badge* com o número de conhecimentos que a atividade possui. Ao passar o mouse sobre esse *badge*, o Sistema exibirá em um *tool-tip-text* a lista de conhecimentos da atividade.  
   2. Botões ***Criar competência*** e ***Disponibilizar mapa*** alinhados no canto superior direito da lista de blocos de competências

*\[Início de fluxo de criação  de competências\]* 

7. ADMIN clica no botão ***Criar competência***.  
8. Sistema abre a janela modal **Edição de competência**, composta por um campo para informação da descrição da competência e da lista das atividades cadastradas pela unidade, assim como os botões ***Cancelar*** e ***Salvar***.  
   1. Da mesma forma que ocorre na tela **Edição de mapa**, cada atividade será representada pela sua descrição e um *badge* com o número de conhecimentos que a atividade possui, tendo este um *tool-tip-text* para exibição da lista de conhecimentos da atividade.  
9. ADMIN informa a descrição da competência que será criada e seleciona uma ou mais atividades para associar a ela.  
10. ADMIN clica em ***Salvar*** para persistir a competência.  
11. Sistema armazena a competência e o vínculo desta com a(s) atividade(s).  
12. Sistema insere a competência criada no mapa de competências.  
13. Sistema volta para a tela **Edição de mapa** e exibe a competência recém-criada como o primeiro bloco da lista de blocos de competências.

*\[Término de fluxo de criação de competências\]* 

14. ADMIN repete o [fluxo de criação de competências](#bookmark=id.4d1bdsmf0hmp) até que o mapa esteja completo.  
15. Se desejar **editar** uma competência criada:  
    1.  ADMIN clica no botão de ação de editar no cabeçalho do bloco correspondente à competência.  
    2. Sistema exibe a tela **Edição de competência** preenchida com a descrição da competência e apresenta selecionada(s) a(s) atividade(s) atualmente associada(s) à competência.  
    3. ADMIN altera a descrição ou a associação com as atividades e clica no botão ***Salvar***.  
    4. Sistema persiste a nova descrição da competência e os vínculos com as atividades.  
    5. Sistema retorna para a tela **Edição do mapa**, exibindo o bloco da competência correspondente atualizado.  
16. Se desejar **excluir** uma competência criada:  
    1.  ADMIN clica no botão de ação de excluir no cabeçalho do bloco correspondente à competência.  
    2. Sistema mostra diálogo de confirmação: título ''Exclusão de competência", mensagem "Confirma a exclusão da competência \[DESCRICAO\_COMPETENCIA\]?” / Botões Confirmar e Cancelar.  
    3. ADMIN confirma a exclusão.  
    4. Sistema remove a competência e todos os seus vínculos com as atividades da unidade.  
    5. Sistema retorna para a tela **Edição do mapa**, exibindo o bloco da competência correspondente atualizado.

## **CDU-16 \- Ajustar mapa de competências** {#cdu-16---ajustar-mapa-de-competências}

**Ator**: ADMIN

**Pré-condições**: 

* Processo de Revisão, com ao menos uma unidade com subprocesso nas situações 'Revisão do cadastro homologada' ou ‘Mapa ajustado’. 

**Fluxo principal**:

1. No **Painel** ADMIN escolhe o processo de mapeamento desejado.  
2. Sistema mostra tela **Detalhes do processo**.  
3. ADMIN clica em uma unidade operacional ou interoperacional com subprocesso nas situações  'Revisão do cadastro homologada' ou ‘Mapa ajustado’.  
4. Sistema mostra a tela **Detalhes do subprocesso**.  
5. ADMIN clica no *card* ***Mapa de competências***.  
6. Sistema mostra a tela **Edição de mapa** preenchida com os dados do mapa de competências da unidade**.**

## **CDU-17 \- Disponibilizar mapa de competências** {#cdu-17---disponibilizar-mapa-de-competências}

**Ator**: ADMIN

**Pré-condições**: 

* Usuário logado com perfil ADMIN.  
* Processo de mapeamento com ao menos uma unidade com subprocesso na situação ‘Mapa criado’ ou processo de revisão com ao menos uma unidade com subprocesso na situação ‘Mapa ajustado’.   
* Tela Painel sendo exibida.

**Fluxo principal**:

1. ADMIN escolhe o processo de mapeamento desejado.  
2. Sistema mostra tela **Detalhes do processo**.  
3. ADMIN clica em uma unidade operacional ou interoperacional com subprocesso na situação 'Mapa criado' ou ‘Mapa ajustado’.  
4. Sistema mostra a tela **Detalhes de subprocesso**.  
5. ADMIN clica no *card* ***Mapa de competências***.  
6. Sistema mostra a tela **Edição de mapa** preenchida com os dados do mapa de competências da unidade.  
7. ADMIN clica no botão ***Disponibilizar mapa*** da tela **Edição de mapa**.  
8. Sistema mostra diálogo de confirmação: título ''Disponibilização do mapa de competências", mensagem "Confirma finalização da edição e a disponibilização do mapa de competências? Essa ação bloqueia a edição e habilita a validação do mapa pela unidade''/ Botões ***Confirmar*** e ***Cancelar***.  
9. Caso ADMIN escolha ***Cancelar***, o sistema interrompe a operação de disponibilização do mapa, permanecendo na mesma tela.  
10. ADMIN confirma a disponibilização.  
11. Sistema verifica se todas as competências criadas estão associadas a pelo menos uma atividade do cadastro da unidade.  
    1. Caso negativo, interrompe a disponibilização do mapa e informa quais competências ainda permanecem sem associação.  
12. Sistema verifica se todas as atividades foram associadas a pelo menos uma competência.   
    1. Caso negativo, interrompe a disponibilização do mapa e informa quais atividades estão ainda sem associação a competências.  
13. Sistema solicita uma data limite para conclusão da validação do mapa pela unidade e o aceite desta validação pela hierarquia de unidades superiores.  
14. ADMIN fornece a data limite.  
15. Sistema registra essa data como sendo a data limite da etapa 2 do subprocesso da unidade.  
16. Sistema altera a situação do subprocesso da unidade para ‘Mapa dosponibilizado’.  
17. Sistema registra uma movimentação para o subprocesso com os campos:  
    1. **Data/hora**: Data/hora atual  
    2. **Unidade origem**: ‘SEDOC’  
    3. **Unidade destino**: \[SIGLA\_UNIDADE\_SUBPROCESSO\]  
    4. **Descrição**: ‘Disponibilização do mapa de competências’  
18. Sistema notifica a unidade do subprocesso quanto à disponibilização, com e-mail no modelo abaixo:

    ***Assunto:** SGC: Mapa de competências disponibilizado*

    *Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUBPROCESSO\],*

    *O mapa de competências de sua unidade foi disponibilizado no contexto do processo \[DESCRICAO DO PROCESSO\].*

    *A validação deste mapa já pode ser realizada no Sistema de Gestão de Competências (URL\_SISTEMA). O prazo para conclusão desta etapa do processo é \[DATA\_LIMITE\].*

19. Sistema notifica as unidades superiores da unidade do subprocesso quanto à disponibilização, com e-mail no modelo abaixo:

    ***Assunto:** SGC: Mapa de competências disponibilizado \- \[SIGLA\_UNIDADE\_SUBPROCESSO\]*

    *Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUPERIOR\],*

    *O mapa de competências da \[SIGLA\_UNIDADE\_SUBPROCESSO\] foi disponibilizado no contexto do processo \[DESCRICAO DO PROCESSO\].*

    *A validação deste mapa já pode ser realizada no Sistema de Gestão de Competências (URL\_SISTEMA). O prazo para conclusão desta etapa do processo é \[DATA\_LIMITE\].*

20. Sistema cria internamente um alerta com a seguinte informação:  
    1. **Descrição:** “Mapa de competências da unidade  \[SIGLA\_UNIDADE\_SUBPROCESSO\] disponibilizado para análise”  
    2. **Processo**: \[DESCRICAO DO PROCESSO\]  
    3. **Data/hora**: Data/hora atual  
    4. **Unidade de origem:** ‘SEDOC’  
    5. **Unidade de destino:** \[SIGLA\_UNIDADE\_SUBPROCESSO\].  
21. Sistema exclui as sugestões e  o histórico de análise do mapa de competência do subprocesso da unidade.  
22. Sistema mostra confirmação: "Revisão do cadastro de atividades disponibilizada" e redireciona para **Painel**.

## **CDU-18 \- Disponibilizar mapas de competências em bloco** {#cdu-18---disponibilizar-mapas-de-competências-em-bloco}

**Ator**: ADMIN  
**Pré-condições**:  
**Fluxo principal**:

23. ADMIN também poderá disponibilizar os mapas de competências para várias unidades em lote, informando para todas a mesma data limite para conclusão da validação.   
    1. Se o processo de disponibilização tiver sido realizado em lote, as unidades intermediárias na árvore hierárquica receberão uma única notificação consolidada com os dados de todas as unidades subordinadas. Modelo de notificação consolidada (exemplo):

    *Assunto: SGC: Mapas de Competências Disponibilizados*

    *À equipe da STIC,*

    *Comunicamos que os mapas de competências da seguintes unidades já estão disponíveis para validação:*

    ***\- \[STIC\]***  
       ***\- \[SESEL\]***  
       ***\- \[SENIC\]***  
       ***\- \[SESUP\]***

    *A validação dos mapas pode ser realizada no Sistema SGC (\[URL\_SISTEMA\]).* 

    *Informamos que a data limite para finalizar essa validação é  \[DATA\_LIMITE\].*

    2. Uma vez disponibilizado, o mapa de competências passará a ficar disponível para consulta pelo CHEFE e GESTOR.   
       1. O CHEFE tem acesso a apenas aos mapas de sua unidade.  
       2. O GESTOR tem acesso a todos os mapas de suas unidades subordinadas.

## **CDU-19 \- Visualizar mapa de competências** {#cdu-19---visualizar-mapa-de-competências}

**Ator**: GESTOR

**Pré-condições**:

* Usuário logado com perfil GESTOR.  
* O CHEFE da unidade já submeteu o mapa de competências para análise.

**Fluxo principal**:

1. No Painel, GESTOR escolhe um processo e na tela **Detalhes do processo** escolhe a unidade a ser analisada.  
2. Sistema mostra tela **Detalhes do subprocesso** para a unidade escolhida.  
3. GESTOR clica no card ***Mapa de competências***.  
4. O sistema mostra a tela **Visualização de mapa**, com as seguintes informações:  
   1. Título "Mapa de competências técnicas"  
   2. Identificação da unidade (sigla e nome).  
   3. Conjunto de competências, com cada competência mostrada em um bloco individual, contendo:  
      1. Descrição da competência como título.  
      2. Conjunto das atividades associadas àquela competência.  
      3. Para cada atividade, conjunto de conhecimentos da atividade.

## **CDU-20 \- Validar mapa de competências** {#cdu-20---validar-mapa-de-competências}

**Atores:** CHEFE

**Pré-condições**: 

* Usuário logado com perfil CHEFE  
* Processo de Revisão com a situação 'Mapa disponibilizado'

**Fluxo principal:**

1. No **Painel**, CHEFE escolhe um processo e na tela **Detalhes do subprocesso** e clica em ***Mapa de competências***.  
2. Sistema mostra a tela **Visualizar mapa de competências** (ver caso de uso [Visualizar mapa de competências](#cdu-19---visualizar-mapa-de-competências)).  
   1. Botões ***Apresentar sugestões,*** ***Validar*** e ***Histórico de análise***.  
3. Se usuário clicar em **Apresentar sugestões**:  
   1. Sistema abre um modal com um campo de texto formatado, para inclusão das sugestões.  
   2. Usuário fornece as sugestões e clica em ***Confirmar***.  
   3. Sistema muda a situação do subprocesso para 'Mapa com sugestões' e mostra a mensagem "Mapa submetido com sugestões para a unidade superior".  
   4. Sistema cria uma notificação: De: UNIDADE\_ATUAL, Para: UNIDADE\_SUPERIOR, Conteúdo: "UNIDADE\_ATUAL apresentou sugestões para o mapa de competências" e um alerta análogo.  
4. Se usuário clicar em **Validar**:  
   1. Sistema muda situação do subprocesso para 'Mapa validado'.   
   2. Sistema mostra mensagem "Mapa validado e submetido à unidade superior".  
   3. Sistema cria uma notificação: De: UNIDADE\_ATUAL, Para: UNIDADE\_SUPERIOR, Conteúdo: "UNIDADE\_ATUAL validou o mapa de competências" e um alerta análogo.  
5. Se usuário clicar em ***Histórico de análise***, sistema mostra um modal contendo uma pequena tabela com o histórico.  
6. Sistema muda a localização do processo para a unidade superior.

## **CDU-21 \- Analisar validação de mapa de competências** {#cdu-21---analisar-validação-de-mapa-de-competências}

**Ator:** GESTOR e ADMIN

**Pré-condições**: 

* Usuário logado com perfil GESTOR  
* Existe ao menos um processo de mapeamento com o mapa disponibilizado.

**Fluxo principal:**

1. No **Painel**, usuário escolhe um processo e na tela **Detalhes do processo** clica em uma unidade com situação 'Mapa disponibilizado'.  
2. Sistema mostra tela **Detalhes de subprocesso**.  
3. Usuário clica em ***Mapa de Competências***.  
4. Sistema mostra a tela ***Visualização de Mapa de Competências*** com botões ***Aceitar*** e ***Devolver para ajustes***.  
5. Se usuário clicar em ***Aceitar***:  
   1. Sistema mostra um modal com um campo de texto formatado para a inclusão de observações, *opcionais*, e botões ***Cancelar*** e **Aceitar**.  
   2. Usuário fornece um observação (ou não) e clica em ***Aceitar***.  
   3. Sistema muda a localização do subprocesso para a unidade superior.  
   4. Se unidade superior NÃO for SEDOC, sistema muda a situação do subprocesso para 'Mapa validado'.  
   5. Se unidade superior for SEDOC, sistema muda situação do subprocesso para 'Mapa homologado'.  
6. Se usuário clicar em ***Devolver para ajustes***:  
   1. Sistema muda a localização do subprocesso para a unidade subordinada.  
   2. Se a unidade inferior for a unidade do subprocesso, sistema muda situação do subprocesso para 'Mapa disponibilizado'.  
   3. Se unidade inferior NÃO for a unidade do subprocesso, sistema muda a situação para 'Mapa criado', permitindo com isso que a SEDOC faça os ajustes necessários no mapa (quando a localização do subprocesso chegar à SEDOC). 

## **CDU-22 \- Aceitar/Homologar mapa de competências em bloco** {#cdu-22---aceitar/homologar-mapa-de-competências-em-bloco}

**Ator**: GESTOR e ADMIN

**Pré-condições**: 

**Fluxo principal:**

## **CDU-23 \- Reabrir cadastro** {#cdu-23---reabrir-cadastro}

Se durante as etapas de criação e de validação do mapa de competências, alguma unidade indicar a necessidade tardia de ajuste no cadastro de atividades e conhecimentos, a SEDOC poderá, mediante solicitação da unidade em questão, **retorná-la para a etapa de cadastro** a fim de que a mesma possa realizar as alterações necessárias.

* A reabertura do cadastro de atividades e conhecimentos da unidade será **notificada para todas unidades hierarquicamente superiores**.  
* Após os ajustes realizados, o cadastro de atividades e conhecimentos da unidade precisará passar por **nova análise por todas as unidades superiores** na hierarquia.

## **CDU-24 \- Reabrir revisão de cadastro** {#cdu-24---reabrir-revisão-de-cadastro}

Se durante as etapas de ajuste e de validação do mapa de competências ajustado, alguma unidade indicar a necessidade tardia de revisão do cadastro de atividades e conhecimentos, a SEDOC poderá, mediante solicitação dessa unidade, **retorná-la para a etapa de revisão do cadastro** a fim de que a mesma possa realizar as alterações necessárias.

* A reabertura da revisão do cadastro de atividades e conhecimentos da unidade será **notificada para todas unidades hierarquicamente superiores**.  
* Após os ajustes realizados, a revisão do cadastro de atividades e conhecimentos da unidade precisará passar por **nova análise por todas as unidades superiores** na hierarquia.

O processo de revisão poderá ser concluído quando todas as unidades participantes concluírem a validação do mapa de competência.

As etapas de ajuste do mapa, e de validação do mapa ajustado, deverão ser realizadas apenas para as unidades que efetuarem alterações no cadastro de atividades e conhecimentos. As unidades que não identificarem necessidades de alteração poderão indicar a validação do mapa de competências já na primeira etapa do processo.

## **CDU-25 \- Alterar data limite de unidade** {#cdu-25---alterar-data-limite-de-unidade}

Uma unidade pode solicitar diretamente à SEDOC, fora do contexto do sistema, que sua data limite seja alterada. Na tela de Detalhes de unidade, a SEDOC navega para a tela da unidade e altera a data. A unidade recebe a notificação da mudança de data.

## **CDU-26 \- Enviar lembrete de prazo** {#cdu-26---enviar-lembrete-de-prazo}

Se houver processos próximos ao prazo final, o Sistema apresenta indicadores de alertas de prazo e permite enviar um lembrete por e-mail para uma unidade.

## **CDU-27 \- Finalizar processo** {#cdu-27---finalizar-processo}

**Ator**: ADMIN

**Pré-condição**: Usuário logado com qualquer perfil, exceto SERVIDOR

**Fluxo principal:**

## **CDU-28 \- Consultar histórico de processos** {#cdu-28---consultar-histórico-de-processos}

**Ator**: ADMIN/GESTOR/CHEFE

**Pré-condição**: Usuário logado com qualquer perfil, exceto SERVIDOR

**Fluxo principal:**

1. Em qualquer tela do sistema, na barra de navegação, usuário clica 'Histórico'.  
2. Sistema apresenta uma tabela com todos os processos com situação 'Finalizado', com:  
   1. **Processo**: Descrição do processo.  
   2. **Tipo**: Tipo do processo.  
   3. **Finalizado em**: Data de finalização do processo  
   4. **Unidades participantes**: Lista de unidades participantes, agregando pelas unidades que tiverem todas as subunidades participando.  
3. Usuário clica em um processo para detalhamento.  
4. Sistema apresenta a página **Detalhes do processo**, com um botão 'Mapa' para cada unidade do tipo operacional ou interoperacional.  
5. Usuário clica no botão ***Mapa*** para uma unidade.  
6. Sistema mostra tela **Visualização de mapa**, com o mapa da unidade.

## **CDU-29 \- Manter atribuição temporária** {#cdu-29---manter-atribuição-temporária}

**Ator Principal**: ADMIN

**Pré-condições**: Processo com situação 'iniciado' e usuário autenticado com perfil ADMIN. 

**Fluxo principal:**

1. No **Painel**, ADMIN clica no item 'Unidades' na barra de navegação.  
2. Sistema mostra tela **Unidades**.  
3. ADMIN clica em umas das unidades  
4. Sistema mostra tela **Detalhes de unidade**.  
5. No bloco 'Atribuição temporária', ADMIN clica no botão 'Criar'.  
6. Sistema apresenta:  
   1. Lista de servidores da unidade selecionada.   
   2. Campo 'Início' (data)  
   3. Campo 'Término' (data)  
   4. Campo 'Justificativa' (texto simples)  
   5. Botões 'Criar atribuição' e 'Cancelar'  
7. ADMIN seleciona servidor e define a data de término da atribuição, além de incluir justificativa.   
   1. Para fins deste sistema, a atribuição temporária terá prioridade sobre os dados de titularidade importados do SGRH.  
8. Sistema registra internamente a atribuição temporária e mostra uma confirmação "Atribuição criada".  
   1. O usuário que recebe a atribuição temporária passa a ter os mesmos direitos do perfil CHEFE.  
9. Sistema agenda remoção automática da atribuição na data definida.

## **CDU-30 \- Vincular unidades** {#cdu-30---vincular-unidades}

Em caso de alteração de nomenclatura de unidades (criação de novas e mudança de situações das antigas no SGRH), o sistema deverá permitir a **vinculação das unidades novas às antigas correspondentes para reaproveitamento das informações**.

## **CDU-31 \- Manter Administradores** {#cdu-31---manter-administradores}

**Atores**: ADMIN 

**Fluxo principal:**

## **CDU-32 \- Configurar sistema** {#cdu-32---configurar-sistema}

**Atores**: ADMIN 

**Fluxo principal:**

1. ADMIN clica no botão de configurações ('engrenagem') na barra de navegação  
2. Sistema mostra a tela **Configurações** com o valor atual das seguintes configurações, permitindo edição.  
   1. **Dias para inativação de processos** (referenciado neste documento como DIAS\_INATIVACAO\_PROCESSO): Dias depois da finalização de um processo para que seja considerado inativo. Valor inteiro, 1 ou mais.  
   2. **Dias para indicação de alerta como novo** (referenciado neste documento como DIAS\_ALERTA\_NOVO): Dias depois depois de um alerta ser enviado para uma unidade, para que deixe de ser marcado como novo.  
3. ADMIN altera os valores das configurações e clica em ***Salvar***.  
4. Sistema mostra mensagem de confirmação e guarda as configurações internamente. O efeito das configurações deve ser imediato.

## **CDU-33 \- Gerar relatórios** {#cdu-33---gerar-relatórios}
**Atores**: ADMIN
