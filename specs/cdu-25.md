# CDU-25 - Aceitar validação de mapas em bloco

## Atores

- GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR.
- Processo de mapeamento ou de revisão iniciado que tenha a unidade como participante.
- Existência de subprocessos de unidades que estejam na hierarquia do usuário, nas situações 'Mapa validado' ou 'Mapa
  com sugestões' e com localização atual na unidade do usuário.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de mapeamento ou de revisão, que esteja em andamento.

2. O sistema mostra a tela `Detalhes do processo`, mostrando apenas as unidades subordinadas à unidade do usuário, além
   do botão `Aceitar mapas em bloco`.

4. O usuário aciona `Aceitar mapas em bloco`.

5. O sistema verifica se há unidades subordinadas com subprocessos nas situações 'Mapa validado' ou 'Mapa com
   sugestões', e abre um modal com título "Aceite de mapas em bloco" e texto "Selecione as unidades para aceite dos
   mapas correspondentes", além dos elementos:
    - lista das unidades aptas, com um checkbox (selecionado por padrão) por unidade, além de sigla e nome da unidade;
    - botões `Cancelar` e `Aceitar em bloco`.

6. O usuário aciona `Aceitar em bloco`.

7. O sistema atua, para cada unidade selecionada, da seguinte forma:

   7.1. Registra uma análise de validação para o subprocesso:
    - `Resultado`: "Aceite de mapa"
    - `Data/hora`: :DATA_HORA:
    - `Unidade`: :UNIDADE_ANALISE:

   7.2. Registra uma movimentação para o subprocesso:
    - `Descrição`: "Validação do mapa aceita"
    - `Data/hora`: :DATA_HORA:
    - `Unidade origem`: :UNIDADE_ANALISE:
    - `Unidade destino`: :UNIDADE_SUPERIOR:

   7.3. Registra um alerta:
    - `Descrição`: "Validação do mapa da unidade :SIGLA_UNIDADE_SUBPROCESSO: submetida para análise"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: :DATA_HORA:
    - `Unidade de origem`: :UNIDADE_ANALISE:
    - `Unidade de destino`: :UNIDADE_SUPERIOR:

   7.4. O sistema agrupa as unidades selecionadas por unidade superior e envia, para cada unidade superior que tenha ao
   menos uma subordinada direta selecionada (referenciada como :LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS:), uma única
   notificação consolidada por e-mail, com o modelo a seguir:

    ```text
    Assunto: SGC: Validação de mapas de competências submetida para análise

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUPERIOR:,

    A validação dos mapas de competências das unidades :LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS: 
    no processo :DESCRICAO_PROCESSO: foi submetida para análise por essa unidade.

    As análises já podem ser realizadas no Sistema de Gestão de Competências (:URL_SISTEMA:).
    ```

   NOTA: O agrupamento do passo anterior considera apenas a unidade superior *imediata* de cada subprocesso selecionado.

8. O sistema redireciona para o `Painel` e mostra um *toast* "Mapas aceitos em bloco".