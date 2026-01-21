# CDU-24 - Disponibilizar mapas de competências em bloco

**Ator:** ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.
- Processo de mapeamento com ao menos uma unidade com subprocesso na situação 'Mapa criado' ou processo de revisão com ao menos uma unidade com subprocesso na situação 'Mapa ajustado'.

## Fluxo principal

1. No Painel, ADMIN acessa um processo de mapeamento ou revisão em andamento.

2. O sistema mostra tela `Detalhes do processo`.

3. O sistema identifica que existem unidades com subprocessos com mapas criados ou ajustados mas ainda não disponibilizados.

4. Na seção de unidades participantes, abaixo da árvore de unidades, o sistema mostra o botão `Disponibilizar mapas de competência em bloco`.

5. O sistema abre modal de confirmação, com os elementos a seguir:

   - Título "Disponibilização de mapa em bloco";
   - Texto "Selecione abaixo as unidades cujos mapas deverão ser disponibilizados:";
   - Lista das unidades operacionais ou interoperacionais cujos mapas de competências poderão ser disponibilizados, sendo apresentados, para cada unidade, um checkbox (selecionado por padrão), a sigla e o nome;
   - Campo de data, de preenchimento obrigatório, para armazenar a `Data limite` para a validação dos mapas de competências; e
   - Botões `Cancelar` e `Disponibilizar`.

6. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela Detalhes do processo.

7. O usuário clica em `Disponibilizar`.

8. O sistema verifica se todas as competências dos mapas de competências dos subprocessos das unidades selecionadas estão associadas a pelo menos uma atividade dos cadastros das unidades, e, em sentido oposto, se todas as atividades foram associadas a pelo menos uma competência do mapa da unidade.

9. Caso negativo, o sistema interrompe a operação de disponibilização em bloco, permanece na tela Detalhes do processo e informa a mensagem de erro: "Não é possível realizar a disponibilização em bloco dos mapas de competências das unidades [LISTA_UNIDADES_SELECIONADAS]. Realize a disponibilização individual do mapa de cada unidade para obter maiores detalhes."

10. Caso positivo, o sistema atua, para cada unidade selecionada, da seguinte forma:

    10.1. O sistema registra a informação "Mapa disponibilizado em bloco" na observação de disponibilização do mapa do subprocesso e a informação do campo Data limite para a validação dos mapas de competências na data limite da etapa 2 do subprocesso da unidade.

    10.2. O sistema altera a situação do subprocesso da unidade para 'Mapa disponibilizado'.

    10.3. O sistema registra uma movimentação para o subprocesso com os campos:

    - `Data/hora`: Data/hora atual
    - `Unidade origem`: SEDOC
    - `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Descrição`: 'Disponibilização do mapa de competências'

    10.4. O sistema notifica a unidade do subprocesso quanto à disponibilização, com e-mail no modelo abaixo:

    ```text
    Assunto: SGC: Mapa de competências disponibilizado

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    O mapa de competências de sua unidade foi disponibilizado no contexto do processo [DESCRIÇÃO_PROCESSO].

    A validação deste mapa já pode ser realizada no Sistema de Gestão de Competências (URL_SISTEMA). O prazo para conclusão desta etapa do processo é [DATA_LIMITE].
    ```

    10.5. O sistema cria internamente um alerta:

    - `Descrição`: "Mapa de competências da unidade [SIGLA_UNIDADE_SUBPROCESSO] disponibilizado para análise"
    - `Processo`: [DESCRIÇÃO DO PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: SEDOC
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO].

    10.6. O sistema exclui as sugestões apresentadas e o histórico de análise do mapa de competência do subprocesso da unidade.

    10.7. O sistema agrupa as unidades selecionadas com suas unidades superiores em todos os níveis da hierarquia, notificando estas unidades superiores, com e-mail no modelo abaixo:

    ```text
    Assunto: SGC: Mapas de competências disponibilizados

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    Os mapas de competências das unidades [LISTA_UNIDADE_SUBORDINADAS_SELECIONADAS] foram disponibilizados no contexto do processo [DESCRIÇÃO DO PROCESSO].

    A validação destes mapas já pode ser realizada no Sistema de Gestão de Competências (URL_SISTEMA). O prazo para conclusão desta etapa do processo é [DATA_LIMITE].
    ```

11. O sistema mostra a mensagem de confirmação: "Mapas de competências disponibilizados em bloco" e redireciona para o Painel.
