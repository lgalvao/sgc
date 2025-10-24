# CDU-08 - Manter cadastro de atividades e conhecimentos

Ator: CHEFE

Pré-condições:
● Usuário logado com perfil CHEFE
● Processo de mapeamento ou de revisão iniciado que tenha a unidade como participante
● Subprocesso da unidade com localização atual na própria unidade e situação 'Não iniciado' e 'Cadastro em andamento',
no caso de processos de mapeamento, ou 'Não iniciado' e 'Revisão do cadastro em andamento' no caso de processos de
revisão

Fluxo principal:

1. No Painel, CHEFE clica em um processo de mapeamento ou revisão da lista de processos.
2. O sistema mostra a tela Detalhes do subprocesso com os dados da unidade.
3. CHEFE clica no card Atividades e conhecimentos.
4. O sistema apresenta a tela Cadastro de atividades e conhecimentos da unidade. Se o mapa de competências da unidade do
   subprocesso já tiver atividades cadastradas, a tela virá previamente preenchida com essa informação.
5. Se o processo for de revisão, deverá ser exibido o botão Impacto no mapa, a partir do qual será possível avaliar o
   efeito das alterações realizadas nas atividades e/ou conhecimentos sobre o mapa de competências do subprocesso (ver
   caso de uso Verificar impactos no mapa de competências).
6. Para incluir uma nova atividade, CHEFE fornece a descrição da atividade, e clica no botão de adição.
7. O sistema adiciona atividade e mostra campo para adição de conhecimento abaixo da atividade.
8. CHEFE fornece a descrição do conhecimento e clica no botão de adição correspondente.
9. O sistema adiciona o conhecimento, associando-o à atividade.

   9.1. Deve ser indicada claramente a associação entre o conhecimento e a atividade, indentando os conhecimentos da
   atividade abaixo da descrição desta.

10. CHEFE repete o fluxo de adição de atividades/conhecimentos.

    10.1. Pode-se incluir primeiro várias atividades e depois os conhecimentos correspondentes; ou trabalhar em uma
    atividade por vez até concluir todos os seus conhecimentos. O sistema deve permitir os dois modos de trabalho.

11. Para cada atividade já cadastrada, ao passar o mouse, o sistema exibe botões de edição e remoção.

    11.1. Se o usuário clicar em Editar, o sistema habilita a edição do nome da atividade e exibe ao lado um botão
    Salvar e outro Cancelar.

    11.1.1. Se o usuário clicar em Salvar, o sistema salva a alteração e volta a exibir os botões Editar e Remover ao
    lado do nome da atividade.
    11.1.2. Se o usuário clicar em Cancelar, o sistema não salva a alteração e volta a exibir o nome da atividade que
    estava antes da modificação com os botões Editar e Remover ao lado.

    11.2. Se o usuário clicar em Remover, o sistema solicita que o usuário confirme a operação. Se o usuário confirmar,
    a atividade e todos os conhecimentos associados a ela são removidos.

12. De forma análoga à usada para atividades, para cada conhecimento já cadastrado, o sistema exibe, ao passar o mouse
    sobre o conhecimento, uma opção Editar e outra Remover.

    12.1. Se o usuário clicar em Editar, o sistema habilita a edição do nome do conhecimento e exibe ao lado um botão
    Salvar e outro Cancelar.

    12.1.1. Se o usuário clicar em Salvar, o sistema salva a alteração e volta a exibir os botões Editar e Remover ao
    lado do nome do conhecimento.
    12.1.2. Se o usuário clicar em Cancelar, o sistema não salva a alteração e volta a exibir o nome do conhecimento que
    estava antes da modificação com os botões Editar e Remover ao lado.

    12.2. Se o usuário clicar em Remover, o sistema solicita que o usuário confirme a operação. Se o usuário confirmar,
    o conhecimento é removido.

13. Opcionalmente, o CHEFE clica no botão Importar atividades:

    13.1. O sistema exibe um modal com uma lista dos processos com tipo Mapeamento ou Revisão que estejam com situação '
    Finalizado'.
    13.2. CHEFE escolhe um processo da lista.
    13.3. O sistema recupera as unidades operacionais e interoperacionais participantes do processo selecionado e
    expande o modal para mostrar estas unidades em uma lista (siglas).
    13.4. CHEFE escolhe uma unidade da lista.
    13.5. O sistema recupera as atividades/conhecimentos da unidade selecionada, e expande o modal para mostrar a lista
    de atividades, permitindo a seleção múltipla.
    13.6. CHEFE marca uma ou mais atividades e clica em Importar.
    13.7. O sistema faz uma cópia das atividades selecionadas e seus respectivos conhecimentos para o cadastro de
    atividades da unidade atual.

    13.7.1. Deverão ser importadas apenas as atividades cujas descrições não corresponderem a nenhuma atividade
    atualmente cadastrada na unidade.
    13.7.2. Caso haja coincidência entre a descrição de uma atividade selecionada com a de alguma atividade cadastrada
    na unidade, o sistema informa que uma ou mais atividades não puderam ser importadas por já existirem no cadastro.

14. Se, no momento da criação/edição/importação de qualquer informação, a situação do subprocesso da unidade ainda
    estiver 'Não iniciado', O sistema altera a situação para 'Cadastro em andamento', no caso de processo de mapeamento,
    ou 'Revisão do cadastro em andamento', no caso de processo de revisão.
15. Após finalizar o cadastro das atividades e conhecimentos, o CHEFE poderá clicar em Disponibilizar (ver caso de uso
    Disponibilizar cadastro de atividades e conhecimentos ou caso de uso Disponibilizar revisão do cadastro conforme o
    caso), ou simplesmente navegar para outra área do sistema.

    15.1. Após cada ação de criação, edição ou exclusão, as informações deverão ser salvas automaticamente e vinculadas
    ao mapa de competências do subprocesso, não sendo necessária nenhuma ação adicional para garantir a persistência
    dessa informação.