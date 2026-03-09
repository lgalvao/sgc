# CDU-25 - Aceitar validação de mapas de competências em bloco

**Ator:** GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR.
- Processo de mapeamento ou de revisão iniciado que tenha a unidade como participante.
- Existência de subprocessos de unidades da hierarquia do usuário (ou subordinadas, recursivamente), nas situações 'Mapa validado' ou 'Mapa com sugestões' e com localização atual na unidade do usuário.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de mapeamento ou de revisão, que esteja na situação em andamento.

2. O sistema mostra a tela `Detalhes do processo`, exibindo apenas os subprocessos das unidades que compõem a hierarquia do usuário (sua própria unidade e subordinadas recursivamente).

3. O sistema identifica que existem unidades subordinadas com subprocessos elegíveis para aceite do mapa em bloco e se houver mostra o botão `Aceitar mapas em bloco`.

4. O usuário clica no botão `Aceitar mapas em bloco`.

5. O sistema abre um modal de confirmação, com os elementos a seguir:
    - Título "Aceite de mapas em bloco";
    - Texto "Selecione as unidades para aceite dos mapas correspondentes";
    - Lista das unidades cujos mapas poderão ser aceitos (conforme pré-condições); devem ser apresentados para cada unidade um checkbox (selecionado por padrão) ao lado da sigla e nome; e
    - Botão `Cancelar` e botão `Registrar aceite`.

6. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação, permanecendo na tela `Detalhes do processo`.

7. O usuário clica em `Registrar aceite`.

8. O sistema atua, para cada unidade selecionada, da seguinte forma:
   8.1. Registra internamente uma análise de validação para o subprocesso:

    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ATUAL]
    - `Resultado`: "Aceite de mapa"
    - `Observação`: "De acordo com a validação do mapa realizada pela unidade"

   8.2. Registra internamente uma movimentação para o subprocesso:

    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]
    - `Descrição`: "Mapa de competências aceito"

   8.3. Registra internamente um alerta:

    - `Descrição`: "Validação do mapa de competências da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetida para análise"
    - `Processo`: [DESCRIÇÃO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

   8.4. Envia notificação por e-mail para a unidade superior, com o modelo a seguir:

    ```text
    Assunto: SGC: Validação de mapas de competências submetida para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    A validação dos mapas de competências das unidades [LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS] no processo [DESCRIÇÃO_PROCESSO] foi submetida para análise por essa unidade.

    As análises já podem ser realizadas no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

9. O sistema mostra mensagem de confirmação: "Mapas aceitos em bloco" e redireciona para o Painel.