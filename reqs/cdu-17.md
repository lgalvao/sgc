# CDU-17 - Disponibilizar mapa de competências

Ator: ADMIN

Pré-condições:

- Usuário logado com perfil ADMIN.
- Processo de mapeamento com ao menos uma unidade com subprocesso na situação 'Mapa criado' ou processo de revisão comao
  menos uma unidade com subprocesso na situação 'Mapa ajustado'.
- Tela Painel sendo exibida.

Fluxo principal:

1. ADMIN escolhe o processo de mapeamento desejado.

2. O sistema mostra tela Detalhes do processo.

3. ADMIN clica em uma unidade operacional ou interoperacional com subprocesso na situação 'Mapa criado' ou 'Mapa
   ajustado'.

4. O sistema mostra a tela Detalhes de subprocesso.

5. ADMIN clica no card Mapa de Competências.

6. O sistema mostra a tela Edição de mapa preenchida com os dados do mapa de competências da unidade.

7. ADMIN clica no botão Disponibilizar.

8. O sistema verifica se todas as competências criadas estão associadas a pelo menos uma atividade do cadastro da
   unidade.

   8.1. Caso negativo, o sistema interrompe a disponibilização do mapa e informa em mensagem de erro quais competências
   ainda permanecem sem associação.

9. O sistema verifica se todas as atividades foram associadas a pelo menos uma competência.

   9.1. Caso negativo, o sistema interrompe a disponibilização do mapa e informa em mensagem de erro quais atividades
   ainda estão sem associação a competências.

10. O sistema mostra uma janela modal com as seguintes características:

    10.1. Título: 'Disponibilização do mapa de competências'

    10.2. Campo de data, de preenchimento obrigatório, para armazenar a Data limite para a validação do mapa

    10.3. Campo de texto formatado, de preenchimento opcional, para registro de Observações

    10.4. Botões Disponibilizar e Cancelar.

11. Caso ADMIN escolha Cancelar, o sistema interrompe a operação de disponibilização do mapa, permanecendo na tela
    Edição de mapa.
12. ADMIN preenche as informações dos campos da tela modal e clica no botão Disponibilizar.
13. O sistema registra a informação do campo Observações no mapa do subprocesso e a informação do campo Data limite para
    a validação do mapa na data limite da etapa 2 do subprocesso.
14. O sistema altera a situação do subprocesso da unidade para 'Mapa disponibilizado'.
15. O sistema registra uma movimentação para o subprocesso com os campos:

    - Data/hora: Data/hora atual
    - Unidade origem: SEDOC
    - Unidade destino: [SIGLA_UNIDADE_SUBPROCESSO]
    - Descrição: 'Disponibilização do mapa de competências'

16. O sistema notifica a unidade do subprocesso quanto à disponibilização, com e-mail no modelo abaixo:

    ```text
    Assunto: SGC: Mapa de competências disponibilizado

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    O mapa de competências de sua unidade foi disponibilizado no contexto do processo [DESCRICAO_PROCESSO].

    A validação deste mapa já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]). O prazo para conclusão desta etapa do processo é [DATA_LIMITE].
    ```

17. O sistema notifica as unidades superiores da unidade do subprocesso quanto à disponibilização, com e-mail no modelo
    abaixo:

    ```text
    Assunto: SGC: Mapa de competências disponibilizado - [SIGLA_UNIDADE_SUBPROCESSO]

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    O mapa de competências da [SIGLA_UNIDADE_SUBPROCESSO] foi disponibilizado no contexto do
    processo [DESCRIÇÃO DO PROCESSO].

    A validação deste mapa já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]). O prazo para conclusão desta etapa do processo é [DATA_LIMITE].
    ```

18. O sistema cria internamente um alerta:

    - Descrição: "Mapa de competências da unidade  [SIGLA_UNIDADE_SUBPROCESSO] disponibilizado para análise"
    - Processo: [DESCRICAO_PROCESSO]
    - Data/hora: Data/hora atual
    - Unidade de origem: SEDOC
    - Unidade de destino: [SIGLA_UNIDADE_SUBPROCESSO].

19. O sistema exclui as sugestões apresentadas e o histórico de análise do mapa de competência do subprocesso da
    unidade.

20. O sistema mostra confirmação: "Revisão do cadastro de atividades disponibilizada" e redireciona para o Painel.
