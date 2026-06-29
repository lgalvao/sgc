# CDU-22 - Aceitar cadastros em bloco

## Atores

- GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR
- Existência de processo de mapeamento/revisão em andamento, com pelo menos uma unidade subordinada com subprocesso 
- localizado na unidade do usuário e com situação:
    - 'Cadastro disponibilizado' (mapeamento) ou 'Revisão do cadastro disponibilizada' (revisão).

## Fluxo principal

1. No `Painel`, o usuário escolhe um processo de mapeamento ou revisão em andamento.

2. O sistema mostra a tela `Detalhes do processo` com uma tabela um resumo de todas unidades participantes do processo,
   que sejam iguais ou subordinadas à unidade do gestor.

3. Caso existam unidades com subprocessos que possam ser aceitas em bloco do cadastro de atividades (de acordo com as
   pré-condições), o sistema habilita o botão `Aceitar cadastros em bloco`.

4. O usuário clica no botão `Aceitar cadastros em bloco`.

5. O sistema abre modal de confirmação, com os elementos a seguir:
    - Título: `Aceite de cadastros em bloco`;
    - Texto: "Selecione as unidades para aceite de cadastros"
    - Lista das unidades operacionais ou interoperacionais subordinadas **elegíveis**, sendo apresentadas com um
      checkbox (selecionado por padrão), sigla e nome da unidade
    - Botões `Cancelar` e `Registrar aceite`.

6. O usuário clica em `Registrar aceite`.

7. O sistema atua, para cada unidade selecionada, da seguinte forma:

   7.1. Registra uma análise de cadastro para o subprocesso:
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: :SIGLA_UNIDADE_ATUAL:
    - `Resultado`: "Aceite"
    - `Observação`: "De acordo com o cadastro de atividades da unidade"

   7.2. Registra uma movimentação para o subprocesso:
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: :SIGLA_UNIDADE_ATUAL:
    - `Unidade destino`: :SIGLA_UNIDADE_SUPERIOR:
    - `Descrição`: "Cadastro aceito"

   7.3. Registra um alerta:
    - `Data/hora`: [Data/hora atual]
    - `Descrição`: 
      - para mapeamento, "Cadastro da unidade :SIGLA_UNIDADE_SUBPROCESSO: submetido para análise"
      - para revisão, "Revisão do cadastro da unidade :SIGLA_UNIDADE_SUBPROCESSO: submetida para análise"
    - `Processo`: [DESCRIÇÃO_PROCESSO]
    - `Unidade de origem`: :SIGLA_UNIDADE_ATUAL:
    - `Unidade de destino`: :SIGLA_UNIDADE_SUPERIOR:

   7.4. Envia notificação por e-mail para a unidade do subprocesso, seguindo o modelo correspondente ao tipo do processo:

   Para processos de **mapeamento**:
    ```text
    Assunto: SGC: Cadastro de atividades e conhecimentos da :SIGLA_UNIDADE_SUBPROCESSO: submetido para análise

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,

    O cadastro de atividades e conhecimentos da sua unidade no processo :DESCRICAO_PROCESSO:
    foi aceito e submetido para análise pela unidade superior.

    Acompanhe o processo no Sistema de Gestão de Competências (:URL_SISTEMA:).
    ```

   Para processos de **revisão**:
    ```text
    Assunto: SGC: Revisão do cadastro de atividades e conhecimentos da :SIGLA_UNIDADE_SUBPROCESSO: submetido para análise

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,

    A revisão do cadastro de atividades e conhecimentos da sua unidade no processo :DESCRICAO_PROCESSO: 
    foi aceita e submetida para análise pela unidade superior.

    Acompanhe o processo no Sistema de Gestão de Competências (:URL_SISTEMA:).
    ```

   8.5. O sistema agrupa as unidades selecionadas por unidade superior imediata e, para cada unidade superior imediata
   que tenha ao menos subordinada direta selecionada, envia uma única notificação consolidada por e-mail, seguindo o
   modelo correspondente ao tipo do processo:

   Para processos de **mapeamento**:

    ```text
    Assunto: SGC: Cadastros de atividades e conhecimentos submetidos para análise

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUPERIOR:,

    Os cadastros de atividades e conhecimentos das unidades :LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS: 
    no processo :DESCRICAO_PROCESSO: foram submetidos para análise por essa unidade.

    As análises já podem ser realizadas no Sistema de Gestão de Competências (:URL_SISTEMA:).
    ```

   Para processos de **revisão**:

    ```text
    Assunto: SGC: Revisões de cadastro de atividades e conhecimentos submetidas para análise

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUPERIOR:,

    As revisões de cadastro de atividades e conhecimentos das unidades :LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS: 
    no processo :DESCRICAO_PROCESSO: foram submetidas para análise por essa unidade.

    As análises já podem ser realizadas no Sistema de Gestão de Competências (:URL_SISTEMA:).
    ```

9. O sistema redireciona para `Painel` e mostra a mensagem "Cadastros aceitos em bloco".
