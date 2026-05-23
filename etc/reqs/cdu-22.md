# CDU-22 - Aceitar cadastros em bloco

**Ator:** GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR
- Existência de processo de mapeamento ou de revisão, em andamento, com pelo menos uma unidade subordinada cujo
  subprocesso tenha localização atual na unidade do usuário com situação 'Cadastro disponibilizado', para processos de
  mapeamento (ou 'Revisão do cadastro disponibilizada', para processos de revisão).

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de mapeamento ou revisão em andamento.

2. O sistema mostra a tela `Detalhes do processo` com uma tabela um resumo de todas unidades participantes do processo,
   que sejam iguais ou subordinadas à unidade do gestor.

3. Caso existam unidades com subprocessos elegíveis para aceite em bloco do cadastro de atividades (de acordo com as
   pré-condições), o sistema habilita o botão `Aceitar cadastro em bloco`.

4. O usuário clica no botão `Aceitar cadastro em bloco`.

5. O sistema abre modal de confirmação, com os elementos a seguir:
    - Título "Aceite de cadastro em bloco";
    - Texto "Selecione as unidades cujos cadastros deverão ser aceitos"
    - Lista das unidades operacionais ou interoperacionais subordinadas **elegíveis**, sendo apresentadas com um
      checkbox (selecionado por padrão), a sigla e o nome da unidade
    - Botões `Cancelar` e `Registrar aceite`.

6. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela
   `Detalhes do processo`.

7. O usuário clica em `Registrar aceite`.

8. O sistema atua, para cada unidade selecionada, da seguinte forma:

   8.1. Registra internamente uma análise de cadastro para o subprocesso:

    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ATUAL]
    - `Resultado`: "Aceite"
    - `Observação`: "De acordo com o cadastro de atividades da unidade"

   8.2. Registra internamente uma movimentação para o subprocesso:

    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]
    - `Descrição`: "Cadastro aceito"

   8.3. Registra internamente um alerta:

    - `Data/hora`: [Data/hora atual]
    - `Descrição`: para processos de mapeamento, "Cadastro da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise"
    - `Descrição`: para processos de revisão, "Revisão do cadastro da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetida para análise"
    - `Processo`: [DESCRIÇÃO_PROCESSO]
    - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

   8.4. Envia notificação por e-mail individual para a própria unidade do subprocesso, seguindo o modelo correspondente ao tipo do processo:

    ```text
    Para processos de mapeamento:
    Assunto: SGC: Cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    O cadastro de atividades e conhecimentos da sua unidade no processo [DESCRIÇÃO_PROCESSO] foi aceito e submetido para análise pela unidade superior imediata.

    Acompanhe o processo no Sistema de Gestão de Competências ([URL_SISTEMA]).

    Para processos de revisão:
    Assunto: SGC: Revisão do cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    A revisão do cadastro de atividades e conhecimentos da sua unidade no processo [DESCRIÇÃO_PROCESSO] foi aceita e submetida para análise pela unidade superior imediata.

    Acompanhe o processo no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

   8.5. O sistema agrupa as unidades selecionadas por unidade superior imediata e, para cada unidade superior imediata que tenha ao menos uma subordinada direta selecionada, envia uma única notificação consolidada por e-mail, seguindo o modelo correspondente ao tipo do processo:

    ```text
    Para processos de mapeamento:
    Assunto: SGC: Cadastros de atividades e conhecimentos submetidos para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    Os cadastros de atividades e conhecimentos das unidades [LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS] no processo [DESCRIÇÃO_PROCESSO] foram submetidos para análise por essa unidade.

    As análises já podem ser realizadas no Sistema de Gestão de Competências ([URL_SISTEMA]).

    Para processos de revisão:
    Assunto: SGC: Revisões de cadastro de atividades e conhecimentos submetidas para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    As revisões de cadastro de atividades e conhecimentos das unidades [LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS] no processo [DESCRIÇÃO_PROCESSO] foram submetidas para análise por essa unidade.

    As análises já podem ser realizadas no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

   8.6. O agrupamento do passo anterior considera apenas a unidade superior imediata de cada subprocesso selecionado. O sistema não propaga automaticamente a consolidação para níveis hierárquicos acima.

9. O sistema redireciona para `Painel` e mostra a mensagem "Cadastros aceitos em bloco".
