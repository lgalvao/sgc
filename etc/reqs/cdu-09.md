# CDU-09 - Disponibilizar cadastro de atividades e conhecimentos

Ator: CHEFE

## Pré-condições

- Usuário logado com perfil CHEFE.
- Subprocesso de mapeamento da unidade na situação 'Cadastro em andamento'.

## Fluxo principal

1. No painel, o usuário escolhe um processo de mapeamento na situação 'Em andamento'.

2. O sistema mostra tela `Detalhes do subprocesso` da unidade.

3. O usuário clica no card `Atividades e conhecimentos`.

4. O sistema mostra a tela `Cadastro de atividades e conhecimentos`, preenchida com os dados cadastrados até o momento.

5. Se o subprocesso já tiver sido disponibilizado anteriormente e estiver localizado em unidade diferente da unidade ativa do usuário, o sistema mostra um alerta fixo no topo da tela com o texto: "Cadastro disponibilizado para análise pelas unidades superiores.", não permitindo edição ou disponibilização.

6. Se o subprocesso tiver retornado de análise pelas unidades superiores, deverá ser exibido, além dos botões fixos da
   tela, o botão `Histórico de análise`.

   6.1. Se o usuário clicar no botão `Histórico de análise`, o sistema mostra, em tela modal, os dados das análises do
   cadastro realizadas pelas unidades superiores desde a última disponibilização.

   6.1.1. As análises deverão ser apresentadas em uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações. Essas informações poderão ser usadas como subsídio para ajustes no cadastro pelo usuário, antes da realização de nova disponibilização.

7. O usuário escolhe `Disponibilizar`.

8. O sistema verifica se todas as atividades têm ao menos um conhecimento associado.

   8.1. Se houver esses problemas de validação, o sistema indica quais atividades estão precisando de adição de conhecimentos e interrompe a operação de disponibilização, permanecendo na mesma tela.

9. O sistema mostra um diálogo de confirmação com título "Disponibilização do cadastro", com mensagem "Confirma a finalização e a disponibilização do cadastro? Essa ação bloqueia a edição e habilita a análise do cadastro por unidades superiores", além dos botões `Confirmar` e `Cancelar`.

   9.1. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de disponibilização, permanecendo na mesma tela.

10. O usuário escolhe `Confirmar`.

11. O sistema altera a situação do subprocesso da unidade para 'Cadastro disponibilizado'

12. O sistema registra uma movimentação para o subprocesso com os campos:

    - `Data/hora`: Data/hora atual
    - `Unidade origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]
    - `Descrição`: 'Disponibilização do cadastro de atividades'

13. O sistema notifica a unidade superior hierárquica quanto à disponibilização, com e-mail no modelo abaixo:

    ```text
    Assunto: SGC: Cadastro de atividades e conhecimentos disponibilizado: [SIGLA_UNIDADE_SUBPROCESSO]

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],
    A unidade [SIGLA_UNIDADE_SUBPROCESSO] disponibilizou o cadastro de atividades e conhecimentos do processo [DESCRICAO_PROCESSO].

    A análise desse cadastro já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

14. O sistema cria internamente um alerta:

    - `Descrição`: "Cadastro da unidade [SIGLA_UNIDADE_SUBPROCESSO] disponibilizado para
      análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR].

15. O sistema define a data/hora de conclusão da etapa 1 do subprocesso da unidade como sendo a data/hora atual.

16. O sistema redireciona para o Painel, mostrando a mensagem "Cadastro de atividades disponibilizado".