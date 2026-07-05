# CDU-25 - Aceitar validação de mapas em bloco

## Atores

- GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR.
- Processo de mapeamento ou de revisão iniciado que tenha a unidade como participante.
- Existência de subprocessos de unidades que estejam na hierarquia do usuário, nas situações 'Mapa validado' ou 'Mapa
  com sugestões' e com localização atual na unidade do usuário.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de mapeamento ou de revisão, que esteja na situação em andamento.

2. O sistema mostra a tela `Detalhes do processo`, exibindo apenas os subprocessos das unidades que compõem a hierarquia
   do usuário (sua própria unidade e subordinadas recursivamente).

3. O sistema verifica se existem unidades subordinadas com subprocessos elegíveis para aceite do mapa em bloco; se
   houver habilita a ação `Aceitar mapas em bloco`.

4. O usuário aciona `Aceitar mapas em bloco`.

5. O sistema abre um modal de confirmação, com:
    - Título: `Aceite de mapas em bloco`;
    - Texto: "Selecione as unidades para aceite dos mapas correspondentes";
    - Lista das unidades cujos mapas poderão ser aceitos; devem ser apresentados para cada unidade um checkbox
      (selecionado por padrão) ao lado da sigla e nome da unidade;
    - Botões `Cancelar` e `Registrar aceite`.

6. O usuário clica em `Registrar aceite`.

7. O sistema atua, para cada unidade selecionada, da seguinte forma:

   7.1. Registra uma análise de validação para o subprocesso:
    - `Data/hora`: :DATA_HORA:
    - `Unidade`: :SIGLA_UNIDADE_ATUAL:
    - `Resultado`: "Aceite de mapa"

   7.2. Registra uma movimentação para o subprocesso:
    - `Data/hora`: :DATA_HORA:
    - `Unidade origem`: :SIGLA_UNIDADE_ATUAL:
    - `Unidade destino`: :SIGLA_UNIDADE_SUPERIOR:
    - `Descrição`: "Validação do mapa aceita"

   7.3. Registra um alerta:
    - `Descrição`: "Validação do mapa da unidade :SIGLA_UNIDADE_SUBPROCESSO: submetida para análise"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: :DATA_HORA:
    - `Unidade de origem`: :SIGLA_UNIDADE_ATUAL:
    - `Unidade de destino`: :SIGLA_UNIDADE_SUPERIOR:

   7.4. Envia notificação por e-mail individual para a própria unidade do subprocesso, com o modelo a seguir:

    ```text
    Assunto: SGC: Validação do mapa de competências da :SIGLA_UNIDADE_SUBPROCESSO: submetida para análise

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,

    A validação do mapa de competências da sua unidade no processo :DESCRICAO_PROCESSO: foi aceita e submetida
    para análise pela unidade superior imediata.

    Acompanhe o processo no Sistema de Gestão de Competências (:URL_SISTEMA:).
    ```

   7.5. O sistema agrupa as unidades selecionadas por unidade superior imediata e envia, para cada unidade superior
   imediata que tenha ao menos uma subordinada direta selecionada, uma única notificação consolidada por e-mail, com o
   modelo a seguir:

    ```text
    Assunto: SGC: Validação de mapas de competências submetida para análise

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUPERIOR:,

    A validação dos mapas de competências das unidades :LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS: 
    no processo :DESCRICAO_PROCESSO: foi submetida para análise por essa unidade.

    As análises já podem ser realizadas no Sistema de Gestão de Competências (:URL_SISTEMA:).
    ```

   7.6. O agrupamento do passo anterior considera apenas a unidade superior imediata de cada subprocesso selecionado. O
   sistema não propaga automaticamente a consolidação para níveis hierárquicos acima.

8. O sistema redireciona para o Painel e mostra *toast*: "Mapas aceitos em bloco".
