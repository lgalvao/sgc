# CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos

Ator: CHEFE

## Pré-condições:

- Usuário logado com perfil CHEFE.
- Subprocesso de revisão da unidade na situação 'Revisão do cadastro em andamento'.

## Fluxo principal:

1. No `Painel`, o usuário clica no processo de revisão na situação 'Em andamento'.

2. O sistema mostra tela `Detalhes do subprocesso` para a unidade.

3. O usuário clica em `Atividades e conhecimentos`.

4. O sistema mostra a tela `Cadastro de atividades e conhecimentos` preenchida com os dados cadastrados/revisados até o momento.
   4.1. Além dos dados do cadastro, será mostrada a checkbox `Dipsonibilização sem mudanças`.

5. Se o subprocesso tiver retornado de análise pelas unidades superiores, deverá ser habilitado, além dos botões fixos da tela, o botão `Histórico de análise`.

    5.1. Se o usuário clicar no botão `Histórico de análise`, o sistema mostra, em tela modal, os dados das análises do cadastro realizadas pelas unidades superiores desde a última disponibilização.
        - As análises deverão ser apresentadas em uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações. Essas informações serão usadas como subsídio para ajustes no cadastro, antes de nova disponibilização.

6. O usuário faz as mudanças necessárias no cadastro.  

7. Durante as mudanças no cadastro, o sistema verifica continuamente se todas as atividades têm ao menos um conhecimento associado. Caso negativo, indica quais atividades precisam de conhecimentos e bloqueia a disponibilização.

8. Se o usuário decidir disponibilizar sem mudanças, marca o checkbox `Disponibilização sem mudanças`. Este checkbox só deve estar habilitado caso realmente não tenham sido feitas mudanças no cadastro.   

8. Se houver mudanças no cadastro, ou se o usuário clicar no checkbox, o sistema muda a situação do subprocesso para 'Revisão do cadastro em andamento' e habilita o botão `Disponibilizar`.

    8.1. Se o usuário, desmarcar o checkbox, o sistema muda a situação do subprocesso de volta a 'Não iniciado' e desativa o botão `Disponibilizar`, até que sejam feitas mudanças no cadastro (e o cadastro esteja válido).   

9. O usuário clica no botão `Disponibilizar`.

10. O sistema mostra um diálogo de confirmação: título "Disponibilização da revisão do cadastro", mensagem "Confirma a disponibilização do cadastro? Essa ação bloqueia a edição e habilita a análise do cadastro por unidades superiores" / Botões `Confirmar` e `Cancelar`.

10.1. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de disponibilização, permanecendo na mesma tela.

11. O usuário escolhe `Confirmar`.

12. O sistema altera a situação do subprocesso da unidade para 'Revisão do cadastro disponibilizada'

13. O sistema registra uma movimentação para o subprocesso com os campos:

    - Data/hora: Data/hora atual
    - Unidade origem: [SIGLA_UNIDADE_SUBPROCESSO]
    - Unidade destino: [SIGLA_UNIDADE_SUPERIOR]
    - Descrição: 'Disponibilização da revisão do cadastro'

14. O sistema notifica unidade superior hierárquica quanto à disponibilização, com e-mail no modelo abaixo:

    ```text
    Assunto: SGC: Revisão do cadastro de atividades e conhecimentos disponibilizada: [SIGLA_UNIDADE_SUBPROCESSO]

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],
    
    A unidade [SIGLA_UNIDADE_SUBPROCESSO] concluiu a revisão e disponibilizou seu cadastro de atividades e conhecimentos do processo [DESCRICAO_PROCESSO].
    
    A análise desse cadastro já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

15. O sistema cria internamente um alerta com:

    - Descrição: "Cadastro de atividades e conhecimentos da unidade [SIGLA_UNIDADE_SUBPROCESSO] disponibilizado para análise"
    - Processo: [DESCRICAO_PROCESSO]
    - Data/hora: [Data/hora atual]
    - Unidade de origem: [SIGLA_UNIDADE_SUBPROCESSO]
    - Unidade de destino: [SIGLA_UNIDADE_SUPERIOR].

16. O sistema define a data/hora de conclusão da Etapa 1 do subprocesso da unidade como sendo a data/hora atual.

17. O sistema redireciona para o `Painel`, e mostra a mensagem "Revisão do cadastro disponibilizada".