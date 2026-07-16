# CDU-37 - Gerar relatório de unidades sem mapas vigentes

## Atores

- ADMIN

## Descrição

Permite ao administrador identificar, visualizar e exportar a relação de unidades que ainda não possuem mapa de
competências vigente, para apoiar o planejamento de futuros mapeamentos.

## Pré-condições

- Usuário autenticado com perfil ADMIN.

## Fluxo principal

1. O usuário acessa `Relatórios` na barra de navegação.

2. O sistema mostra os cards de relatórios disponíveis.

3. O usuário aciona o card `Unidades sem mapas vigentes`.

4. O sistema mostra a tela `Unidades sem mapas vigentes`, com os botões `Visualizar` e `PDF`.

5. O usuário clica em `Visualizar`.

6. O sistema busca todas as unidades cadastradas e identifica aquelas que não possuem mapa vigente.

7. O sistema apresenta uma prévia organizada em árvore hierárquica contendo apenas os ramos que possuem ao menos uma
   unidade sem mapa vigente.

8. Para cada agrupamento exibido, o sistema mostra:
    - a sigla e o nome da unidade agrupadora;
    - abaixo, a árvore das unidades sem mapa vigente pertencentes àquele ramo, exibindo sigla e nome.

9. Se não houver nenhuma unidade sem mapa vigente, o sistema mostra a mensagem `Não há unidades sem mapa vigente.`

10. O usuário clica em `PDF`.

11. O sistema gera um arquivo PDF contendo a relação completa das unidades sem mapa vigente, precedida de cabeçalho
    formal com brasão, nome do sistema, data/hora da geração, escopo `Todas as unidades` e quantidade total de
    registros. O download é iniciado automaticamente.

12. O nome do arquivo gerado segue o padrão `sgc-rel-unidades-sem-mapas-vigentes-YYYY-MM-DD.pdf`.
