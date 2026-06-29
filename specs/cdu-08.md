# CDU-08 - Manter cadastro de atividades e conhecimentos

## Atores

- CHEFE

## Pré-condições

- Usuário logado com perfil CHEFE
- Processo de mapeamento ou de revisão iniciado, que tenha a unidade como participante
- Subprocesso da unidade com localização atual na unidade e situação 'Não iniciado', 'Cadastro em andamento',
  (mapeamento), ou 'Revisão do cadastro em andamento' (revisão)

## Fluxo principal

1. No `Painel`, o usuário aciona um processo de mapeamento ou de revisão da lista de processos.

2. O sistema mostra a tela `Detalhes de subprocesso` para a unidade.

3. O usuário aciona o card `Atividades e conhecimentos`.

4. O sistema apresenta a tela `Cadastro de atividades e conhecimentos` da unidade. Se o mapa de competências da unidade
   já tiver atividades cadastradas, a tela virá preenchida com as informações atuais do mapa.

5. Se o processo for de **Revisão**, deverá ser exibido o botão `Impacto no mapa`, a partir do qual será possível
   avaliar o efeito das alterações realizadas nas atividades/conhecimentos sobre o mapa da unidade (ver caso de uso
   `Verificar impactos no mapa de competências`).

6. Para incluir uma atividade, o usuário fornece a descrição da atividade e clica no botão de adição.

7. O sistema adiciona a atividade e mostra o campo para adição de conhecimento abaixo da atividade.

8. O usuário fornece a descrição do conhecimento e clica no botão de adição correspondente.

9. O sistema adiciona o conhecimento, associando-o à atividade.
    - Deve ser indicada claramente a associação entre o conhecimento e a atividade, indentando os conhecimentos da
      atividade abaixo da descrição desta.

10. O usuário repete o fluxo de adição de atividades/conhecimentos.
    - O usuário pode incluir primeiro várias atividades e depois os conhecimentos correspondentes; ou trabalhar em uma
      atividade por vez até concluir todos os seus conhecimentos. O sistema deve permitir os dois modos de trabalho.

11. Para cada atividade já cadastrada, o sistema apresenta permanentemente os botões de edição e remoção. As ações devem
    ser facilmente identificáveis e operáveis via teclado e leitor de tela, sem dependência de eventos de movimento do
    mouse (hover).

    11.1. Se o usuário acionar `Editar`, o sistema habilita a edição do nome da atividade e exibe ao lado os controles
    para `Salvar` e `Cancelar`.

    11.1.1. Se o usuário acionar `Salvar`, o sistema salva a alteração e retorna ao estado de visualização com as opções
    de edição e remoção disponíveis.

    11.1.2. Se o usuário acionar `Cancelar`, o sistema descarta as alterações e restaura o conteúdo anterior.

    11.2. Se o usuário acionar `Remover`, o sistema solicita confirmação. Se confirmado, a atividade e seus
    conhecimentos são removidos.

12. De forma análoga, para cada conhecimento cadastrado, o sistema disponibiliza permanentemente as opções de editar e
    remover, garantindo operabilidade plena por teclado e tecnologias assistivas.

13. Opcionalmente, o usuário aciona `Importar atividades`:

    13.1. O sistema exibe um modal com uma lista dos processos com tipo Mapeamento ou Revisão que estejam com situação '
    Finalizado'.
    - **IMPORTANTE**: Todos os processos finalizados, de **todas as unidades**, devem ser mostrados para importação.

    13.2. O usuário escolhe um processo da lista.

    13.3. O sistema recupera as unidades operacionais e interoperacionais participantes do processo selecionado e
    expande o modal para mostrar estas unidades em uma lista com apenas as siglas.

    13.4. O usuário escolhe uma unidade da lista.

    13.5. O sistema recupera as atividades/conhecimentos da unidade selecionada, e expande o modal para mostrar a lista
    de atividades, permitindo a seleção múltipla.

    13.6. O usuário marca uma ou mais atividades e clica em `Importar`.

    13.7. O sistema faz uma cópia das atividades selecionadas e seus respectivos conhecimentos para o cadastro de
    atividades da unidade atual.

    13.7.1. Deverão ser importadas apenas as atividades cujas descrições não corresponderem a nenhuma atividade
    atualmente cadastrada na unidade.

    13.7.2. Caso haja coincidência entre a descrição de uma atividade selecionada com a de alguma atividade cadastrada
    na unidade, o sistema informa que uma ou mais atividades não puderam ser importadas por já existirem no cadastro,
    mas procede sem levantar erros.

14. Se, no momento da criação/edição/importação de qualquer informação, a situação do subprocesso da unidade ainda
    estiver 'Não iniciado', o sistema altera a situação para 'Cadastro em andamento', no caso de processo de mapeamento;
    ou a situação 'Revisão do cadastro em andamento', no caso de processo de revisão.

15. Após finalizar o cadastro de atividades e conhecimentos, o usuário poderá clicar em `Disponibilizar` (ver caso de
    uso `Disponibilizar cadastro de atividades e conhecimentos`, ou o caso de uso `Disponibilizar revisão do cadastro`);
    o usuário pode também simplesmente navegar para outra área do sistema, sem a necessidade de gravar as informações de
    alguma maneira, pois a gravação do cadastro é feita automaticamente, a cada mudança.

    15.1. Após cada ação de criação, edição ou exclusão, as informações deverão ser salvas automaticamente e vinculadas
    ao mapa de competências do subprocesso, não sendo necessária nenhuma ação adicional para garantir a persistência
    dessa informação.