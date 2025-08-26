## **CDU-14 \- Aceitar/homologar cadastro em bloco**

**Ator**: GESTOR e ADMIN

**Pré-condições**:

* Usuário logado com perfil GESTOR ou ADMIN
* Existência de processo de mapeamento ou revisão em andamento
* Pelo menos uma unidade subordinada com subprocesso na situação apropriada:
    * Para processos de mapeamento: 'Cadastro disponibilizado'
    * Para processos de revisão: 'Revisão do cadastro disponibilizada'

**Fluxo principal:**

1. No **Painel**, usuário com perfil GESTOR ou ADMIN acessa um processo em andamento.
2. Sistema mostra tela **Detalhes do processo**.
3. Sistema identifica que existem unidades subordinadas com subprocessos elegíveis para aceitação/homologação em bloco:
    * Para GESTOR: Unidades com subprocesso na situação 'Cadastro disponibilizado' (mapeamento) ou 'Revisão do cadastro disponibilizada' (revisão) cuja unidade atual seja a do próprio usuário.
    * Para ADMIN: Mesmas condições acima, mas considerando todas as unidades.
4. Na seção de unidades participantes, abaixo da tabela:
    * Se perfil ADMIN, sistema mostra botão ***Homologar em bloco***.
    * Se perfil GESTOR, sistema mostra botão ***Aceitar em bloco***.
5. Usuário clica no botão ***Aceitar em bloco** ou **Homologar em bloco***, conforme o caso.
6. Sistema abre modal de confirmação com título "Aceitar cadastros em bloco" ou "Homologar cadastros em bloco".
7. Sistema lista as unidades cujos cadastros poderão ser aceitos/homologados, para cada unidade: checkbox (selecionado por padrão), sigla, nome e situação atual do subprocesso.
8. Sistema apresenta botões ***Cancelar*** e ***Aceitar*** ou ***Homologar*** (conforme o caso).
9. Usuário pode optar por:
    * Clicar em ***Cancelar***: Sistema fecha o modal e retorna à tela **Detalhes do processo** sem realizar nenhuma ação.
    * Clicar em ***Aceitar*** ou ***Homologar***: Sistema processa a aceitação/homologação em bloco apenas para as unidades cujo checkbox esteja marcado, conforme as regras abaixo.

**Processamento em bloco para cada unidade selecionada:**

10. Para cada unidade com subprocesso elegível cujo checkbox esteja marcado:
    * Se usuário for GESTOR:
        * Sistema registra análise de cadastro para o subprocesso com as informações:
            * **Data/hora**: Data/hora atual
            * **Unidade**: Sigla da unidade do usuário logado
            * **Resultado**: 'Aceite'
        * Sistema registra movimentação para o subprocesso com os campos:
            * **Data/hora**: Data/hora atual
            * **Unidade origem**: Sigla da unidade do usuário logado
            * **Unidade destino**: Unidade superior hierárquica à unidade do usuário logado
            * **Descrição**: 'Cadastro de atividades e conhecimentos validado em bloco'
        * Sistema envia notificação por e-mail para a unidade superior:

          ***Assunto:** SGC: Cadastro de atividades e conhecimentos da \[SIGLA\_UNIDADE\_SUBPROCESSO\] submetido para análise  
          Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUPERIOR\],*
          O cadastro de atividades e conhecimentos da \[SIGLA\_UNIDADE\_SUBPROCESSO\] no processo \[DESCRICAO DO PROCESSO\] foi submetido para análise por essa unidade.*
          A análise já pode ser realizada no Sistema de Gestão de Competências (\[URL\_SISTEMA\]).

        * Sistema cria internamente um alerta com as seguintes informações:
            * **Descrição:** "Cadastro de atividades da unidade \[SIGLA\_UNIDADE\_SUBPROCESSO\] submetido para análise"
            * **Processo**: \[DESCRICAO DO PROCESSO\]
            * **Data/hora**: Data/hora atual
            * **Unidade de origem:** \[SIGLA\_UNIDADE\_USUARIO\_LOGADO\]
            * **Unidade de destino:** \[SIGLA\_UNIDADE\_SUPERIOR\]

    * Se usuário for ADMIN:  
      * Sistema registra movimentação para o subprocesso com os campos:  
        * **Data/hora**: Data/hora atual  
        * **Unidade origem**: 'SEDOC'  
        * **Unidade destino**: 'SEDOC'  
        * **Descrição**: 'Cadastro de atividades e conhecimentos homologado em bloco'  
      * Sistema altera a situação do subprocesso:  
        * Para processo de mapeamento: 'Cadastro homologado'  
        * Para processo de revisão: 'Revisão do cadastro homologada'  
      * Sistema envia notificação por e-mail para a unidade do subprocesso:   

        Prezado(a) responsável pela \[SIGLA\_UNIDADE\_SUBPROCESSO\],  
        O cadastro de atividades e conhecimentos da sua unidade no processo \[DESCRICAO DO PROCESSO\] foi homologado pela SEDOC.
        A próxima etapa será a criação/ajuste do mapa de competências.* *Mais informações no Sistema de Gestão de Competências (\[URL\_SISTEMA\]).

      * Sistema cria internamente um alerta com a seguinte informação:  
        * **Descrição:** "Cadastro de atividades e conhecimentos da unidade \[SIGLA\_UNIDADE\_SUBPROCESSO\] homologado"  
        * **Processo**: \[DESCRICAO DO PROCESSO\]  
        * **Data/hora**: Data/hora atual  
        * **Unidade de origem:** 'SEDOC'  
        * **Unidade de destino:** \[SIGLA\_UNIDADE\_SUBPROCESSO\]

11. Sistema mostra mensagem de confirmação: "Cadastros aceitos/homologados em bloco com sucesso" e redireciona para **Painel**.