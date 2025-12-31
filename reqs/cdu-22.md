# CDU-22 - Aceitar cadastros em bloco

**Ator:** GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR
- Existência de processo de mapeamento ou revisão em andamento com pelo menos uma unidade subordinada cujo subprocesso tenha localização atual na unidade do usuário e a situação:
  - 'Cadastro disponibilizado', para processos de mapeamento; ou
  - 'Revisão do cadastro disponibilizada', para processos de revisão.

## Fluxo principal

1. No Painel, o usuário acessa um processo de mapeamento ou revisão em andamento.

2. O sistema mostra a tela Detalhes do processo.

3. O sistema identifica que existem unidades subordinadas com subprocessos elegíveis para aceitação em bloco do cadastro de atividades (de acordo com as pré-condições do caso de uso) e exibe, na seção Unidades Participantes, abaixo da árvore de unidades, o botão `Aceitar cadastro em bloco`.

4. O usuário clica no botão `Aceitar cadastro em bloco`.

5. O sistema abre modal de confirmação, com os elementos a seguir:

   - Título "Aceite de cadastro em bloco";
   - Texto "Selecione abaixo as unidades cujos cadastros deverão ser aceitos:";
   - Lista das unidades operacionais ou interoperacionais subordinadas cujos cadastros poderão ser aceitos, sendo apresentados, para cada unidade, um checkbox (selecionado por padrão), a sigla e o nome; e
   - Botões `Cancelar` e `Registrar aceite`.

6. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela Detalhes do processo.

7. O usuário clica em `Registrar aceite`.

8. O sistema atua, para cada unidade selecionada, da seguinte forma:

   8.1. O sistema registra uma análise de cadastro para o subprocesso:

        - `Data/hora`: [Data/hora atual]
        - `Unidade`: [SIGLA_UNIDADE_ATUAL]
        - `Resultado`: "Aceite"
        - `Observação`: "De acordo com o cadastro de atividades da unidade"

   8.2. O sistema registra uma movimentação para o subprocesso:

        - `Data/hora`: [Data/hora atual]
        - `Unidade origem`: [SIGLA_UNIDADE_ATUAL]
        - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]
        - `Descrição`: "Cadastro de atividades e conhecimentos aceito"

   8.3. O sistema cria internamente um alerta:

        - `Descrição`: "Cadastro de atividades da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise"
        - `Processo`: [DESCRIÇÃO_PROCESSO]
        - `Data/hora`: [Data/hora atual]
        - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
        - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

   8.4. O sistema envia esta notificação por e-mail para a unidade superior:

    ```text
    Assunto: SGC: Cadastros de atividades e conhecimentos submetidos para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    Os cadastros de atividades e conhecimentos das unidades [LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS] no processo [DESCRIÇÃO_PROCESSO] foram submetidos para análise por essa unidade.

    As análises já podem ser realizadas no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

9. O sistema mostra mensagem de confirmação: "Cadastros aceitos em bloco" e redireciona para o Painel.
