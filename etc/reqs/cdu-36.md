# CDU-36 - Gerar relatório de mapas vigentes

**Atores:** ADMIN, GESTOR

## Descrição

Permite a geração dos mapas de competências vigentes, incluindo Competências, Atividades e Conhecimentos.

## Fluxo principal

1. O usuário acessa `Relatórios` na barra de navegacao.

2. O usuário aciona o card `Mapas vigentes`.

3. O sistema mostra a tela `Relatório de Mapas Vigentes`, com uma árvore de unidades, *que tenham mapas vigentes* e os botões `Todas`, `Limpar`,  `Gerar` e `PDF`. Se o perfil for ADMIN, serão mostradas todas as unidades. Se o perfil for GESTOR, serão mostrados *apenas* a unidade do usuáario e suas unidades subordinadas, recursivamente.

4. O usuário escolhe as unidades na árvore para as quais quer gerar o relatório e aciona o botão `Gerar`.

6. O sistema processa os dados e mostra na tela, logo abaixo dos controles, uma prévia contendo, para cada mapa, as seguintes informações:

    - (Para cada unidade selecionada:)
        - Unidade (Sigla e Nome)
        - (Para cada competencia:)
            - Descricao da competência
                - Atividades da competencia
                    - Conhecimentos da atividade

7. O usuário aciona o botão `PDF`.

8. O sistema gera o arquivo PDF com os dados especificados acima, precedidos de um cabeçalho formal com brasão, nome do sistema, data/hora da geração e filtros aplicados.