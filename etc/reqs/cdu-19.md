# CDU-19 - Validar mapa de competências

Ator: CHEFE

## Pré-condições

- Usuário logado com perfil CHEFE
- Processo de mapeamento ou revisão com subprocesso na situação 'Mapa disponibilizado'

## Fluxo principal

1. No `Painel`, o usuário escolhe um processo e, na tela `Detalhes do subprocesso`, clica no card `Mapa de competências`.

2. O sistema mostra a tela `Visualização de mapa` com os botões `Apresentar sugestões` e `Validar`.

3. Se o subprocesso tiver retornado de análise pelas unidades superiores, deverá ser exibido também o botão `Histórico de análise`.

   3.1. Se o usuário clicar no botão `Histórico de análise`, o sistema mostra, em tela modal, os dados das análises do mapa realizadas pelas unidades superiores à unidade do subprocesso desde a última disponibilização. As análises deverão ser apresentadas em uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações. Essas informações poderão ser usadas como subsídio para a realização da nova validação do mapa.

4. Se o usuário clicar em `Apresentar sugestões`:

   4.1. O sistema abre um modal "Apresentar sugestões" com um campo de **texto formatado** (obrigatório), para inclusão das sugestões.

    - Se já houver um registro de sugestões para o mapa no subprocesso da unidade, o sistema traz o campo preenchido com essa informação.

   4.2. Usuário fornece as sugestões e clica em `Confirmar`.

   4.3. O sistema armazena as sugestões registradas no mapa do subprocesso da unidade e altera a situação do subprocesso para 'Mapa com sugestões'.

   4.4. O sistema notifica a unidade superior hierárquica da apresentação de sugestões para o mapa, com e-mail no modelo abaixo:

      ```text
      Assunto: SGC: Sugestões apresentadas para o mapa de competências da [SIGLA_UNIDADE_SUBPROCESSO]

      Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],
      A unidade [SIGLA_UNIDADE_SUBPROCESSO] apresentou sugestões para o mapa de competências elaborado no processo [DESCRICAO_PROCESSO].

      A análise dessas sugestões já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).
      ```

   4.5. O sistema cria internamente um alerta com:

    - `Descrição`: "Sugestões para o mapa de competências da [SIGLA_UNIDADE_SUBPROCESSO] aguardando análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR].

   4.6. O sistema mostra a mensagem "Mapa submetido com sugestões para análise da unidade superior".

5. Se usuário clicar em **Validar**:

   5.1. O sistema mostra diálogo de confirmação, com título ''Validação do mapa" e mensagem "Confirma a validação do mapa de competências? Essa ação habilita a análise por unidades superiores''/ Botões `Cancelar` e `Validar`.

   5.1.1. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de validação, permanecendo na mesma tela.

   5.2. O usuário escolhe `Validar`.

   5.3. O sistema altera a situação do subprocesso da unidade para 'Mapa validado'.

   5.4. O sistema notifica a unidade superior hierárquica da validação do mapa, com e-mail no modelo abaixo:

      ```text
      Assunto: SGC: Validação do mapa de competências da [SIGLA_UNIDADE_SUBPROCESSO] submetida para análise

      Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

      A unidade [SIGLA_UNIDADE_SUBPROCESSO] validou o mapa de competências elaborado no processo [DESCRICAO_PROCESSO].

      A análise dessa validação já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).
      ```

   5.5. O sistema cria internamente um alerta com:

    - `Descrição`: "Validação do mapa de competências da [SIGLA_UNIDADE_SUBPROCESSO] aguardando análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR].

   5.6. O sistema mostra a mensagem "Mapa validado e submetido para análise à unidade superior".

6. O sistema registra uma movimentação para o subprocesso com:

    - `Data/hora`: Data/hora atual
    - `Unidade origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]
    - `Descrição`: "Apresentação de sugestões para o mapa", ou "Validação do mapa", conforme o caso.

7. O sistema define a data/hora de conclusão da Etapa 2 do subprocesso da unidade como sendo a atual.

8. O sistema redireciona para o `Painel`.