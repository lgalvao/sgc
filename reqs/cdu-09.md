# CDU-09 - Disponibilizar cadastro de atividades e conhecimentos

Ator: CHEFE

## Pré-condições

- Usuário logado com perfil CHEFE.
- Subprocesso de mapeamento da unidade na situação 'Cadastro em andamento'.

## Fluxo principal

1. No Painel, CHEFE clica no processo de mapeamento na situação 'Em andamento'.

2. O sistema mostra tela `Detalhes do subprocesso` da unidade.

3. CHEFE clica em `Atividades e Conhecimentos`.

4. O sistema mostra tela `Cadastro de atividades e conhecimentos`, preenchida com os dados cadastrados até o momento.

5. Se o subprocesso tiver retornado de análise pelas unidades superiores, deverá ser exibido, além dos botões fixos da
   tela, o botão `Histórico de análise`.

   5.1. Se CHEFE clicar no botão `Histórico de análise`, o sistema mostra, em tela modal, os dados das análises do
   cadastro realizadas pelas unidades superiores desde a última disponibilização.

   5.1.1. As análises deverão ser apresentadas em uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações. Essas informações poderão ser usadas como subsídio para ajustes no cadastro, antes da realização de nova disponibilização.

6. CHEFE escolhe `Disponibilizar`.

7. O sistema verifica se todas as atividades têm ao menos um conhecimento associado.

   7.1. Caso negativo, indica quais atividades estão precisando de adição de conhecimentos e interrompe a operação de
   disponibilização, permanecendo na mesma tela.

8. O sistema mostra diálogo de confirmação: título `Disponibilização do cadastro`, mensagem "Confirma a finalização e a
   disponibilização do cadastro? Essa ação bloqueia a edição e habilita a análise do cadastro por unidades superiores"
   e botões `Confirmar` e `Cancelar`.

   8.1. Caso CHEFE escolha `Cancelar`, o sistema interrompe a operação de disponibilização, permanecendo na mesma tela.

9. CHEFE escolhe `Confirmar`.

10. O sistema altera a situação do subprocesso da unidade para 'Cadastro disponibilizado'

11. O sistema registra uma movimentação para o subprocesso com os campos:

    - `Data/hora`: Data/hora atual
    - `Unidade origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]
    - `Descrição`: 'Disponibilização do cadastro de atividades'

12. O sistema notifica a unidade superior hierárquica quanto à disponibilização, com e-mail no modelo abaixo:

    ```text
    Assunto: SGC: Cadastro de atividades e conhecimentos disponibilizado: [SIGLA_UNIDADE_SUBPROCESSO]

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],
    A unidade [SIGLA_UNIDADE_SUBPROCESSO] disponibilizou o cadastro de atividades e conhecimentos do processo [DESCRICAO_PROCESSO].

    A análise desse cadastro já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

13. O sistema cria internamente um alerta:

    - `Descrição`: "Cadastro de atividades/conhecimentos da unidade [SIGLA_UNIDADE_SUBPROCESSO] disponibilizado para análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR].

14. O sistema define a data/hora de conclusão da etapa 1 do subprocesso da unidade como sendo a data/hora atual.

15. O sistema exclui o histórico de análise do cadastro do subprocesso da unidade.

16. O sistema redireciona para o Painel, mostrando a mensagem "Cadastro de atividades disponibilizado".
