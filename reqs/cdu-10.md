# CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos

Ator: CHEFE

Pré-condições:
● Usuário logado com perfil CHEFE.
● Subprocesso de revisão da unidade na situação 'Revisão do cadastro em andamento'.

Fluxo principal:

1. No Painel, CHEFE clica no processo de revisão na situação 'Em andamento'.
2. O sistema mostra tela Detalhes do subprocesso da unidade.
3. CHEFE clica em Atividades e conhecimentos.
4. O sistema mostra a tela Cadastro de atividades e conhecimentos preenchida com os dados cadastrados/revisados até o
   momento.
5. Se o subprocesso tiver retornado de análise pelas unidades superiores, deverá ser exibido, além dos botões fixos da
   tela, o botão Histórico de análise.

   5.1. Se CHEFE clicar no botão Histórico de análise, o sistema mostra, em tela modal, os dados das análises do
   cadastro realizadas pelas unidades superiores desde a última disponibilização. As análises deverão ser apresentadas
   em uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações. Essas
   informações poderão ser usadas como subsídio para ajustes no cadastro, antes da realização de nova disponibilização.

6. CHEFE clica no botão Disponibilizar.
7. O sistema verifica se todas as atividades têm ao menos um conhecimento associado.

   7.1. Caso negativo, indica quais atividades estão precisando de adição de conhecimentos e interrompe a operação de
   disponibilização, permanecendo na mesma tela.

8. O sistema mostra diálogo de confirmação: título ''Disponibilização da revisão do cadastro", mensagem "Confirma a
   finalização da revisão e a disponibilização do cadastro? Essa ação bloqueia a edição e habilita a análise do cadastro
   por unidades superiores''/ Botões Confirmar e Cancelar.

   8.1. Caso CHEFE escolha Cancelar, o sistema interrompe a operação de disponibilização, permanecendo na mesma tela.

9. CHEFE escolhe Confirmar.
10. O sistema altera a situação do subprocesso da unidade para 'Revisão do cadastro disponibilizada'
11. O sistema registra uma movimentação para o subprocesso com os campos:

    11.1. Data/hora: Data/hora atual
    11.2. Unidade origem: [SIGLA_UNIDADE_SUBPROCESSO]
    11.3. Unidade destino: [SIGLA_UNIDADE_SUPERIOR]
    11.4. Descrição: 'Disponibilização da revisão do cadastro de atividades'

12. O sistema notifica unidade superior hierárquica quanto à disponibilização, com e-mail no modelo abaixo:
    Assunto: SGC: Revisão do cadastro de atividades e conhecimentos disponibilizada: [SIGLA_UNIDADE_SUBPROCESSO]
    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],
    A unidade [SIGLA_UNIDADE_SUBPROCESSO] concluiu a revisão e disponibilizou seu cadastro de atividades e conhecimentos
    do processo [DESCRICAO_PROCESSO].
    A análise desse cadastro já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).
13. O sistema cria internamente um alerta:

    13.1. Descrição: "Cadastro de atividades e conhecimentos da unidade [SIGLA_UNIDADE_SUBPROCESSO] disponibilizado para
    análise"
    13.2. Processo: [DESCRICAO_PROCESSO]
    13.3. Data/hora: Data/hora atual
    13.4. Unidade de origem: [SIGLA_UNIDADE_SUBPROCESSO]
    13.5. Unidade de destino: [SIGLA_UNIDADE_SUPERIOR].

14. O sistema define a data/hora de conclusão da etapa 1 do subprocesso da unidade como sendo a data/hora atual.
15. O sistema exclui o histórico de análise do cadastro do subprocesso da unidade.
16. O sistema mostra a mensagem: "Revisão do cadastro de atividades disponibilizada" e redireciona para o Painel.