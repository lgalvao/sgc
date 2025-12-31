# CDU-25 - Aceitar validação de mapas de competências em bloco

**Ator:** GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR.
- Processo de mapeamento ou de revisão iniciado que tenha a unidade como participante.
- Subprocesso nas situações 'Mapa validado' ou 'Mapa com sugestões' e com localização atual na unidade do usuário.

## Fluxo principal

1. No Painel, o usuário acessa um processo de mapeamento ou revisão em andamento.

2. O sistema mostra tela Detalhes do processo.

3. O sistema identifica que existem unidades subordinadas com subprocessos elegíveis para aceitação em bloco do mapa de competências (de acordo com as pré-condições).

4. Na seção de unidades participantes, abaixo da árvore de unidades, sistema mostra o botão `Aceitar mapa de competências em bloco`.

5. O usuário clica no botão `Aceitar mapa de competências em bloco`.

6. O sistema abre modal de confirmação, com os elementos a seguir:
   - Título "Aceite de mapa em bloco";
   - Texto "Selecione abaixo as unidades cujos mapas deverão ser aceitos:";
   - Lista das unidades operacionais ou interoperacionais subordinadas cujos mapas poderão ser aceitos, sendo apresentados, para cada unidade, um checkbox (selecionado por padrão), a sigla e o nome; e
   - Botão `Cancelar` e botão `Registrar aceite`.

7. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela Detalhes do processo.

8. O usuário clica em `Registrar aceite`.

9. O sistema atua, para cada unidade selecionada, da seguinte forma:
   9.1. O sistema registra uma análise de validação para o subprocesso:
        - `Data/hora`: [Data/hora atual]
        - `Unidade`: [SIGLA_UNIDADE_ATUAL]
        - `Resultado`: "Aceite"
        - `Observação`: "De acordo com a validação do mapa realizada pela unidade"

   9.2. O sistema registra uma movimentação para o subprocesso:
        - `Data/hora`: [Data/hora atual]
        - `Unidade origem`: [SIGLA_UNIDADE_ATUAL]
        - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]
        - `Descrição`: "Mapa de competências aceito"

   9.3. O sistema cria internamente um alerta:
        - `Descrição`: "Validação do mapa de competências da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetida para análise"
        - `Processo`: [DESCRIÇÃO_PROCESSO]
        - `Data/hora`: [Data/hora atual]
        - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
        - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

   9.4. O sistema envia esta notificação por e-mail para a unidade superior:

    ```text
    Assunto: SGC: Validação de mapas de competências submetida para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    A validação dos mapas de competências das unidades [LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS] no processo [DESCRIÇÃO_PROCESSO] foi submetida para análise por essa unidade.

    As análises já podem ser realizadas no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

10. O sistema mostra mensagem de confirmação: "Mapas de competências aceitos em bloco" e redireciona para o Painel.
