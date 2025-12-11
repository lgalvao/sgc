**Sistema de Gestão de Competências**

# **Especificação de Casos de Uso**

## **CDU-01 – Iniciar Processo de Diagnóstico**  {#cdu-01-–-iniciar-processo-de-diagnóstico}

Ator: ADMIN

**Pré-condições:**

* Login realizado com perfil ADMIN
* Existência de processo de Diagnóstico de Competências na situação ‘Criado’

**Fluxo principal:**

1. No Painel, o usuário clica em um processo de Diagnóstico de Competências que esteja na situação 'Criado'.
2. O sistema muda para a tela Cadastro de processo com os campos preenchidos com as informações do processo selecionado.
3. O usuário clica no botão *Iniciar processo*.
4. O sistema mostra diálogo de confirmação: "Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas
   as unidades participantes serão notificadas por e-mail.", botões *Confirmar* e *Cancelar*.
5. Caso o usuário escolha *Cancelar*, o sistema interrompe a operação de iniciação do processo, permanecendo na mesma
   tela.
6. O usuário confirma.
7. O sistema armazena internamente uma cópia da árvore de unidades participantes, incluindo os seus servidores, e a
   vincula com o processo, a fim de preservar a hierarquia de unidades vigente, no momento da iniciação do processo.
8. O sistema muda a situação do processo de Diagnóstico de Competências para 'Em andamento';
9. O sistema cria internamente um subprocesso para cada unidade operacional ou interoperacional participante, com os
   seguintes campos e valores iniciais:
    1. Data limite etapa 1: Data copiada da data limite da etapa inicial do processo.
    2. Situação: 'Não iniciado'
    3. Observações: Campo de texto formatado reservado para registro de informações futuras pela SEDOC.
    4. Sugestões: Campo de texto formatado reservado para registro de sugestões futuras pelas unidades.
10. O sistema registra uma movimentação para cada subprocesso criado com os campos:
    1. Data/hora: Data/hora atual
    2. Unidade origem: ‘SEDOC’
    3. Unidade destino: \[SIGLA\_UNIDADE\_SUBPROCESSO\]
    4. Descrição: ‘Processo iniciado’
11. O sistema envia notificações por e-mail para todas as unidades participantes.
    1. As notificações devem seguir para os endereços de e-mail das unidades (ex. *sesel@tre-pe.jus.br*) e dos
       servidores responsáveis por elas.
    2. Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo:  
       Assunto: SGC: Início de processo de diagnóstico de competências  
       *Prezado(a) responsável pela \[*SIGLA\_UNIDADE\_SUBPROCESSO\]*\],*  
       Comunicamos o início do processo \[DESCRICAO DO PROCESSO\] para a sua unidade.  
       Já é possível realizar o diagnóstico no Sistema de Gestão de Competências (\[URL\_SISTEMA\]).  
       O prazo para conclusão desta etapa do processo é \[DATA\_LIMITE\].
3. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das unidades
   operacionais e interoperacionais subordinadas a elas, segundo o modelo:

   Assunto: SGC: Início de processo de diagnóstico de competências em unidades subordinadas

   Prezado(a) responsável pela \[SIGLA\_UNIDADE\],

   Comunicamos o início do processo \[DESCRICAO DO PROCESSO\] nas unidades
   \[LISTA\_DE\_SIGLAS\_DAS\_UNIDADES\_SUBORDINADAS\]. Essas unidades já podem iniciar o diagnóstico de competências. À
   medida que esses diagnósticos forem sendo disponibilizados, será possível visualizar e realizar a sua validação.

   O prazo para conclusão desta etapa do processo é \[DATA\_LIMITE\].

   Acompanhe o processo no Sistema de Gestão de Competências: \[URL\_SISTEMA\].

13. O sistema cria internamente alertas para todas as unidades participantes. Esses alertas devem ser exibidos para
    todos os servidores integrantes das unidades
    1. Para cada unidade operacional será criado um alerta com a seguinte informação:
        1. Descrição: “Início do processo”
        2. Processo: \[DESCRICAO DO PROCESSO\]
        3. Data/hora: Data/hora atual
        4. Unidade de origem: SEDOC
        5. Unidade de destino: \[SIGLA\_UNIDADE\].
    2. Para cada unidade intermediária será criado um alerta com a seguinte informação:
        1. Descrição: “Início do processo em unidade(s) subordinada(s)”
        2. Processo: \[DESCRICAO DO PROCESSO\]
        3. Data/hora: Data/hora atual
        4. Unidade de origem: SEDOC
        5. Unidade de destino: \[SIGLA\_UNIDADE\].
    3. Para cada unidade interoperacional serão criados dois alertas: um de unidade operacional e outro de unidade
       intermediária, como especificado acima.

## **CDU-02 – Realizar autoavaliação de Diagnóstico** {#cdu-02-–-realizar-autoavaliação-de-diagnóstico}

**Ator:** SERVIDOR

**Pré-condições:**

* Login realizado com perfil SERVIDOR
* Processo de diagnóstico iniciado e SERVIDOR notificado

**Fluxo principal:**

1. No **Painel**, o usuário clica no processo de Diagnóstico de Competências na situação ‘Em andamento’.
2. O sistema mostra tela **Detalhes do subprocesso** da unidade.
3. O usuário acessa o card **Diagnóstico** **da equipe**.
4. O sistema mostra o **formulário com as competências da unidade** preenchida com os dados cadastrados até o momento.
   Para cada competência da unidade, deve ser possível ao usuário detalhar quais as atividades e conhecimentos
   associados a ela.
5. O usuário informa os campos **Importância** e **Domínio** com valores **NA (Não se aplica), 1, 2, 3, 4, 5 ou 6\.**
6. O usuário clica em **Salvar**.
7. O sistema mostra a mensagem *"Autoavaliação salva."*.
8. O usuário clica no botão em **Concluir autoavaliação**.
9. O sistema altera a situação do servidor para *Autoavaliação concluída*.
10. O sistema envia notificação por e-mail para a CHEFIA  
    ***Assunto:** SGC: Autoavaliação de* \[NOME\_SERVIDOR\] *submetida para análise”.*

    *Prezado(a) responsável pela \[*SIGLA\_UNIDADE\_SUBPROCESSO*,*   
    O SERVIDOR \[NOME\_SERVIDOR\] concluiu a autoavaliação no processo \[DESCRICAO DO PROCESSO\] para análise.  
    A análise já pode ser realizada no Sistema de Gestão de Competências (\[URL\_SISTEMA\]).
11. O sistema cria internamente um alerta, a ser exibido para a CHEFIA, com as seguintes informações:
    1. **Descrição:** "*Autoavaliação de* \[NOME\_SERVIDOR\] *submetida para análise*"
    2. **Processo**: \[DESCRIÇÃO\_PROCESSO\]
    3. **Data/hora**: \[Data/hora atual\]
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]
    5. **Unidade de destino:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]

**Fluxo alternativo:**

* Caso o usuário preencha a autoavaliação após o prazo, o sistema permite envio, mas exige preenchimento de campo *
  *Justificativa de atraso**, exibindo mensagem *"Informe a justificativa para envio fora do prazo."*.

## **CDU-03 – Monitorar autoavaliação de servidores** {#cdu-03-–-monitorar-autoavaliação-de-servidores}

**Ator:** CHEFE

**Pré-condições:**

* Login realizado com perfil CHEFE
* Processo de diagnóstico em andamento

**Fluxo principal:**

1. CHEFE acessa o card **Diagnóstico da equipe**.
2. Sistema lista servidores da unidade e a situação de cada um conforme abaixo ~~situações~~:
    * *Autoavaliação não realizada*
    * *Autoavaliação concluída*
    * *Avaliação de consenso criada*
    * *Avaliação de consenso aprovada*
    * *Avaliação impossibilitada*

## **CDU-04 – Criar/editar avaliação de consenso** {#cdu-04-–-criar/editar-avaliação-de-consenso}

**Ator:** CHEFE

**Pré-condições:**

* Login realizado com perfil CHEFE
* Existência de Servidor com autoavaliação na situação *Autoavaliação concluída*

**Fluxo principal:**

1. O usuário seleciona um servidor na situação *Autoavaliação concluída* e clica no botão **Criar/Editar avaliação de
   consenso**.
2. O sistema mostra formulário idêntico ao da autoavaliação, preenchido inicialmente com os dados do SERVIDOR.
3. O usuário edita os valores conforme necessário e clica em **Salvar**.
4. O sistema mostra a mensagem *"Avaliação de consenso salva."*.
5. O sistema envia notificação por e-mail para o SERVIDOR  
   ***Assunto:** SGC: Consenso da Autoavaliação de* \[NOME\_SERVIDOR\] *submetido para validação”.*

   *Prezado(a) \[NOME\_SERVIDOR\],*   
   A chefia concluiu a análise da sua autoavaliação e disponibilizou a avaliação de consenso para validação.  
   O consenso já pode ser validado no Sistema de Gestão de Competências (\[URL\_SISTEMA\]).
6. Sistema cria internamente um alerta para o SERVIDOR com as seguintes informações:
    1. **Descrição:** "*Consenso da Autoavaliação de* \[NOME\_SERVIDOR\] *submetido para validação”.*"
    2. **Processo**: \[DESCRIÇÃO\_PROCESSO\]
    3. **Data/hora**: \[Data/hora atual\]
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]
    5. **Unidade de destino:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]

**Fluxo alternativo:**

7. Se CHEFE editar após aprovação do SERVIDOR, o sistema obriga preenchimento do campo **Motivo da reabertura**.

## **CDU-05 – Consultar avaliação de consenso** {#cdu-05-–-consultar-avaliação-de-consenso}

**Ator principal:** SERVIDOR

**Pré-condições:**

* Login realizado com perfil SERVIDOR
* Avaliação de consenso criada para o SERVIDOR

**Fluxo principal:**

1. SERVIDOR acessa o card **Diagnóstico da equipe**.
2. Sistema mostra a avaliação de consenso.
3. SERVIDOR visualiza Importância e Domínio atribuídos.

## **CDU-06 – Aprovar avaliação de consenso** {#cdu-06-–-aprovar-avaliação-de-consenso}

**Ator principal:** SERVIDOR

**Pré-condições:**

* Login realizado com perfil SERVIDOR
* Avaliação de consenso disponível

**Fluxo principal:**

1. SERVIDOR acessa sua avaliação de consenso.
2. Se concordar, clica em **Aprovar**.
3. Sistema altera a situação para *Avaliação de consenso aprovada* e mostra a mensagem *"Avaliação aprovada."*.
4. Sistema envia notificação por e-mail para a CHEFIA

   ***Assunto:** SGC: “A avaliação de consenso de* \[NOME\_SERVIDOR\] *foi aprovada”.*  
   *Prezado(a) responsável pela \[*SIGLA\_UNIDADE\_SUBPROCESSO*,*   
   O SERVIDOR \[NOME\_SERVIDOR\] aprovou a avaliação de consenso no processo \[DESCRICAO DO PROCESSO\].  
   A verificação já pode ser realizada no Sistema de Gestão de Competências (\[URL\_SISTEMA\]).

5. Sistema cria internamente um alerta com as seguintes informações:
    1. **Descrição:** "*A avaliação de consenso de* \[NOME\_SERVIDOR\] *foi aprovada”*"
    2. **Processo**: \[DESCRIÇÃO\_PROCESSO\]
    3. **Data/hora**: \[Data/hora atual\]
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]
    5. **Unidade de destino:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]

**Fluxo alternativo:**

* Se discordar, SERVIDOR informa **Motivo de discordância** e clica em **Solicitar ajuste**.
* Sistema registra a discordância e retorna a avaliação ao CHEFE.
* Sistema envia notificação por e-mail para a CHEFIA  
  ***Assunto:** SGC: “A avaliação de consenso de* \[NOME\_SERVIDOR\] não *foi aprovada”.*

  *Prezado(a) responsável pela \[*SIGLA\_UNIDADE\_SUBPROCESSO*,*   
  O SERVIDOR \[NOME\_SERVIDOR\] não aprovou a avaliação de consenso no processo \[DESCRICAO DO PROCESSO\].  
  A verificação já pode ser realizada no Sistema de Gestão de Competências (\[URL\_SISTEMA\]).
* Sistema cria internamente um alerta com as seguintes informações:
    * **Descrição:** *“A avaliação de consenso de* \[NOME\_SERVIDOR\] não *foi aprovada”*
    * **Processo**: \[DESCRIÇÃO\_PROCESSO\]
    * **Data/hora**: \[Data/hora atual\]
    * **Unidade de origem:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]
    * **Unidade de destino:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]

## **CDU-07 – Preencher ocupações críticas** {#cdu-07-–-preencher-ocupações-críticas}

**Ator principal:** CHEFE

**Pré-condições:**

* Login realizado com perfil CHEFE
* Diagnóstico em andamento

**Fluxo principal:**

1. CHEFE acessa o card **Ocupações críticas**.
2. CHEFE informa a situação de capacitação de cada servidor para cada competência: **NA (Não se aplica), AC (A
   capacitar), EC (Em capacitação), C (Capacitado) e I (Instrutor).**
3. CHEFE clica em **Salvar**.
4. Sistema mostra mensagem *"Informações salvas."*.

## **CDU-08 – Indicar impossibilidade de avaliação de servidor
** {#cdu-08-–-indicar-impossibilidade-de-avaliação-de-servidor}

**Ator principal:** CHEFE

**Pré-condições:**

* Login realizado com perfil CHEFE
* Servidor impedido de iniciar/concluir autoavaliação ou consenso

**Fluxo principal:**

1. CHEFE clica o card **Diagnóstico da equipe**.
2. Sistema lista servidores da unidade com as situações:
    1. *Autoavaliação não realizada*
    2. *Autoavaliação concluída*
    3. *Avaliação de consenso criada*
    4. *Avaliação de consenso aprovada*
    5. *Avaliação impossibilitada*
3. CHEFE seleciona servidor na situação de “Autoavaliação não realizada” e **indica impossibilidade de participação do
   processo de avaliação.**
4. Sistema abre campo obrigatório **Justificativa**.
5. CHEFE confirma.
6. Sistema altera situação para *Avaliação impossibilitada* e mostra a mensagem *"Servidor marcado como impossibilitado:
   \[Justificativa\]."*.

## **CDU-09 – Concluir Diagnóstico de unidade** {#cdu-09-–-concluir-diagnóstico-de-unidade}

**Ator principal:** CHEFE

**Pré-condições:**

* Login realizado com perfil CHEFE
* Todos os servidores concluíram/foram impossibilitados
* Ocupações críticas preenchidas

**Fluxo principal:**

1. CHEFE acessa o card **Diagnóstico de Competências da unidade**.
2. CHEFE clica no botão **Concluir diagnóstico**.
3. Sistema valida pendências.
    * Se houver servidores não aprovados no prazo, exige justificativa *"Informe justificativa para conclusão com
      pendência."*.
4. Sistema altera a situação do processo para ‘Diagnóstico finalizado’.
5. Sistema notifica a unidade superior hierárquica quanto à finalização do diagnóstico da Unidade, com e-mail no modelo
   abaixo:  
   ***Assunto:** SGC: Finalização do Diagnóstico da Unidade: \[SIGLA\_UNIDADE\_SUBPROCESSO\]*  
   *Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUPERIOR\],*  
   *A unidade \[SIGLA\_UNIDADE\_SUBPROCESSO\] finalizou o processo \[DESCRICAO DO PROCESSO\].*  
   *A análise desse processo já pode ser realizada no Sistema de Gestão de Competências (URL\_SISTEMA).*
2. Sistema cria internamente um alerta:
    1. **Descrição:** “*Finalização do Diagnóstico da Unidade: \[SIGLA\_UNIDADE\_SUBPROCESSO\] \-* disponível para
       análise”.
    2. **Processo**: \[DESCRICAO DO PROCESSO\]
    3. **Data/hora**: Data/hora atual
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]
    5. **Unidade de destino:** \[SIGLA\_UNIDADE\_SUPERIOR\].

## **CDU-10 – Ajustar Diagnóstico** {#cdu-10-–-ajustar-diagnóstico}

**Ator principal:** CHEFE

**Pré-condições:**

* Login realizado com perfil CHEFE
* Diagnóstico devolvido pela unidade superior

**Fluxo principal:**

1. CHEFE acessa devolução.
2. CHEFE visualiza justificativa de devolução.
3. CHEFE ajusta avaliações e ocupações críticas.
4. CHEFE solicita novamente aprovação dos servidores se necessário.
5. CHEFE conclui diagnóstico da unidade.

## **CDU-11 – Monitorar Diagnóstico de unidades subordinadas
** {#cdu-11-–-monitorar-diagnóstico-de-unidades-subordinadas}

**Ator principal:** GESTOR

**Pré-condições:**

* Login realizado com perfil GESTOR

**Fluxo principal:**

1. GESTOR acessa a tela **Detalhes do processo**.
2. Sistema mostra árvore de unidades subordinadas com situações:
    * *Não iniciado, Diagnóstico em andamento, Diagnóstico finalizado, Diagnóstico homologado*.
3. GESTOR detalha unidade para visualizar subprocessos e servidores.

## **CDU-12 – Validar Diagnóstico de unidade subordinada** {#cdu-12-–-validar-diagnóstico-de-unidade-subordinada}

**Ator principal:** GESTOR

**Pré-condições:**

* Login realizado com perfil GESTOR
* Unidade subordinada com diagnóstico concluído

**Fluxo principal:**

1. GESTOR acessa diagnóstico da unidade e clica em **Validar**.
2. Sistema altera a situação do processo para ‘Diagnóstico Validado’.
3. Sistema notifica a unidade superior hierárquica quanto à validação do diagnóstico da Unidade, com e-mail no modelo
   abaixo:  
   ***Assunto:** SGC: Validação do Diagnóstico da Unidade: \[SIGLA\_UNIDADE\_SUBPROCESSO\]*  
   *Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUPERIOR\],*  
   *A unidade \[SIGLA\_UNIDADE\_SUBPROCESSO\] Validou o processo \[DESCRICAO DO PROCESSO\].*  
   *A análise desse processo já pode ser realizada no Sistema de Gestão de Competências (URL\_SISTEMA).*
3. Sistema cria internamente um alerta:
    1. **Descrição:** “*Validação do Diagnóstico da Unidade: \[SIGLA\_UNIDADE\_SUBPROCESSO\] \-* disponível para
       análise”.
    2. **Processo**: \[DESCRICAO DO PROCESSO\]
    3. **Data/hora**: Data/hora atual
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]
    5. **Unidade de destino:** \[SIGLA\_UNIDADE\_SUPERIOR\].

## **CDU-13 – Devolver Diagnóstico para ajustes** {#cdu-13-–-devolver-diagnóstico-para-ajustes}

**Ator principal:** GESTOR

**Pré-condições:**

* Login realizado com perfil GESTOR
* Unidade subordinada com diagnóstico concluído

**Fluxo principal:**

1. No **Painel**, GESTOR clica no processo de Diagnóstico de Competências desejado.
2. GESTOR clica em **Devolver**.
3. Sistema mostra tela modal solicitando campo obrigatório **Justificativa da devolução**.
4. GESTOR confirma.
5. Sistema altera a situação do processo para ‘Diagnóstico finalizado’ e notifica a unidade subordinada quanto à
   devolução, com e-mail no modelo abaixo:  
   ***Assunto:** SGC: Devolução do Diagnóstico da Unidade: \[SIGLA\_UNIDADE\_SUBPROCESSO\]*  
   *Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUBPROCESSO\]*   
   *A unidade \[SIGLA\_UNIDADE\_SUPERIOR\], devolveu o processo \[DESCRICAO DO PROCESSO\] para ajustes.*  
   *Os ajustes desse processo já podem ser realizados no Sistema de Gestão de Competências (URL\_SISTEMA).*
4. Sistema cria internamente um alerta:
    1. **Descrição:** “*Devolução do Diagnóstico da Unidade: \[SIGLA\_UNIDADE\_SUBPROCESSO\] \-* para ajustes”.
    2. **Processo**: \[DESCRICAO DO PROCESSO\]
    3. **Data/hora**: Data/hora atual
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUPERIOR\].
    5. **Unidade de destino:**\[SIGLA\_UNIDADE\_SUBPROCESSO\]

## **CDU-14 – Validar Diagnósticos em bloco**  {#cdu-14-–-validar-diagnósticos-em-bloco}

**Ator principal:** GESTOR

**Pré-condições:**

* Login realizado com perfil GESTOR
* Mais de uma unidade subordinada com diagnóstico concluído

**Fluxo principal:**

1. GESTOR seleciona múltiplas unidades e clica em **Validar em bloco**.
2. Sistema gera uma única notificação consolidada para a unidade superior hierárquica quanto à validação das unidades,
   com e-mail no modelo abaixo:  
   ***Assunto:** SGC: Validação do Diagnóstico das Unidades \[SIGLAS\_UNIDADES\].*  
   *Prezado(a) responsável pela(s) \[SIGLA\_UNIDADE\_SUPERIOR\],*  
   *As unidades \[SIGLA\_UNIDADE\_SUBPROCESSO\] , \[SIGLA\_UNIDADE\_SUBPROCESSO\], etc.,validaram os processos
   \[DESCRICAO DO PROCESSO\].*  
   *Esses processos já podem ser acessados no Sistema de Gestão de Competências (URL\_SISTEMA).*
5. Sistema cria internamente um alerta com a seguinte informação:
    1. **Descrição:** “*Validação do Diagnóstico das Unidades: \[SIGLA\_UNIDADE\_SUBPROCESSO\],
       \[SIGLA\_UNIDADE\_SUBPROCESSO\], etc.” \-* disponível para análise”.
    2. **Processo**: \[DESCRICAO DO PROCESSO\]
    3. **Data/hora**: Data/hora atual
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]
    5. **Unidade de destino:** \[SIGLA\_UNIDADE\_SUPERIOR\].

## **CDU-15 – Monitorar andamento de Diagnóstico** {#cdu-15-–-monitorar-andamento-de-diagnóstico}

**Ator principal:** ADMIN

**Pré-condições:**

* Login realizado com perfil ADMIN
* Processo de Diagnóstico de competência em andamento

**Fluxo principal:**

1. ADMIN acessa o **Painel de monitoramento**.
2. Sistema mostra andamento por unidade.
3. ADMIN pode filtrar por prazo, status ou unidade e enviar notificações de lembrete.
4. \[Faltando passos e mais especificidade\]

## **CDU-16 – Alterar prazo de diagnóstico da unidade** {#cdu-16-–-alterar-prazo-de-diagnóstico-da-unidade}

**Ator principal:** ADMIN

**Pré-condições:**

* Login realizado com perfil ADMIN

**Fluxo principal:**

1. No **Painel**, ADMIN clica em um processo de Diagnóstico de Competências.
2. Sistema mostra tela **Detalhes do processo**.
3. ADMIN acessa a unidade desejada e na tela **Detalhes do subprocesso** clica em **Alterar prazo**.
4. Sistema solicita nova data.
5. ADMIN fornece a data e clica em ***Alterar***.
6. Sistema notifica a unidade subordinada quanto à alteração do prazo de diagnóstico da Unidade, com e-mail no modelo
   abaixo:  
   ***Assunto:** SGC: Alteração do Prazo de Diagnóstico da Unidade: \[SIGLA\_UNIDADE\_SUBPROCESSO\]*  
   *Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUBPROCESSO\]*   
   *A unidade \[SIGLA\_UNIDADE\_SUPERIOR\], alterou o prazo do processo \[DESCRICAO DO PROCESSO\].*  
   *Os detalhes desse processo já podem ser vizualidados no Sistema de Gestão de Competências (URL\_SISTEMA).*
6. Sistema cria internamente um alerta:
    1. **Descrição:** “*Alteração do Prazo de Diagnóstico da Unidade: \[SIGLA\_UNIDADE\_SUBPROCESSO\]”.*
    2. **Processo**: \[DESCRICAO DO PROCESSO\]
    3. **Data/hora**: Data/hora atual
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUPERIOR\].
    5. **Unidade de destino:**\[SIGLA\_UNIDADE\_SUBPROCESSO\]

## **CDU-17 – Homologar Diagnóstico** {#cdu-17-–-homologar-diagnóstico}

**Ator principal:** ADMIN

**Pré-condições:**

* Login realizado com perfil ADMIN
* Todas as validações da cadeia concluídas

**Fluxo principal:**

1. No **Painel**, ADMIN acessa a tela de **Diagnóstico da unidade raiz**  e clica em ***Homologar diagnóstico***.
2. \[Faltando passos do ADMIN aqui\]
3. Sistema altera a situação para *Diagnóstico homologado*.
4. Sistema notifica a unidade subordinada quanto à homologação do processo da Unidade, com e-mail no modelo abaixo:  
   ***Assunto:** SGC: Homologação do Diagnóstico da Unidade: \[SIGLA\_UNIDADE\_SUBPROCESSO\]*  
   *Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUBPROCESSO\]*   
   *A unidade \[SIGLA\_UNIDADE\_SUPERIOR\], homologou o processo \[DESCRICAO DO PROCESSO\].*  
   *Esse processo já pode ser vizualidado no Sistema de Gestão de Competências (URL\_SISTEMA).*
7. Sistema cria internamente um alerta:
    1. **Descrição:** “*Alteração do Prazo de Diagnóstico da Unidade: \[SIGLA\_UNIDADE\_SUBPROCESSO\]”.*
    2. **Processo**: \[DESCRICAO DO PROCESSO\]
    3. **Data/hora**: Data/hora atual
    4. **Unidade de origem:** \[SIGLA\_UNIDADE\_SUPERIOR\].
    5. **Unidade de destino:**\[SIGLA\_UNIDADE\_SUBPROCESSO\]

## **CDU-18 – Gerar Relatório de Gaps e Ocupações Críticas** {#cdu-18-–-gerar-relatório-de-gaps-e-ocupações-críticas}

**Ator principal:** ADMIN

**Pré-condições:**

* Login realizado com perfil ADMIN
* Diagnóstico de competências homologado

**Fluxo principal:**

1. \[Faltando os passos do ADMIN para disparar o relatório\]
2. Sistema calcula:
    * Gaps de competências por servidor.
    * Quantidade de ocupações críticas por unidade.
3. Sistema disponibiliza relatórios em múltiplos níveis de consolidação.
4. Sistema permite exportação nos formatos **PDF, Excel e CSV**.

## **Situações** {#situações}

### **Situações de processo** {#situações-de-processo}

* **Criado** → Processo registrado, mas ainda não iniciado. Permite edição ou exclusão.
* **Em andamento** → Processo iniciado. Subprocessos gerados para unidades participantes. Não é mais possível excluir ou
  editar.
* **Concluído** → Todas as unidades participantes finalizaram seus diagnósticos (ou marcaram impossibilidade
  justificada).
* **Homologado** → Diagnóstico homologado pela SEDOC. Relatórios e cálculos de gaps disponíveis.

### **Situações do Subprocesso** {#situações-do-subprocesso}

* **Não iniciado** → Nenhum servidor da unidade começou a autoavaliação.
* **Diagnóstico em andamento** → Há pelo menos uma autoavaliação ou consenso iniciado.
* **Diagnóstico concluído** → Todos os servidores da unidade concluíram suas avaliações ou foram marcados como
  impossibilitados, e o CHEFE preencheu as ocupações críticas.
* **Devolvido** → Diagnóstico concluído, mas devolvido pela unidade superior para retificação (obrigatória
  justificativa).
* **Validado** → Diagnóstico aceito pela unidade superior.
* **Homologado** → Diagnóstico da unidade validado em todos os níveis superiores e homologado pela SEDOC.

### **Situações relacionadas ao servidor** {#situações-relacionadas-ao-servidor}

* **Autoavaliação não realizada** → Servidor não iniciou o preenchimento.
* **Autoavaliação concluída** → Servidor finalizou e enviou autoavaliação.
* **Avaliação de consenso criada** → CHEFE registrou avaliação de consenso para o servidor.
* **Avaliação de consenso aprovada** → Servidor aprovou a avaliação de consenso.
* **Avaliação impossibilitada** → CHEFE registrou justificativa de impossibilidade de avaliação para o servidor.

**Três níveis de situações:**

* **Processo (global, gerido pela SEDOC)**
* **Subprocesso (unidade, gerido por CHEFE e validado por GESTOR)**
* **Servidor (etapas da avaliação individual)**

**Como cada nível evolui:**

* **Processo (SEDOC)**: Criado → Em andamento → Concluído → Homologado
* **Unidade (CHEFE/GESTOR)**: Não iniciado → Em andamento → Concluído → Devolvido/Validado → Homologado
* **Servidor (CHEFE/SERVIDOR)**: Autoavaliação não realizada → Autoavaliação concluída → Consenso criado → Consenso
  aprovado / Impossibilitado