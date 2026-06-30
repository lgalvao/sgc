# CDU-36 - Gerar relatório de mapas vigentes

## Atores

- ADMIN
- GESTOR

## Pré-condições

- Usuário autenticado com perfil ADMIN ou GESTOR.

## Descrição

Permite a geração dos mapas de competências vigentes, incluindo Competências, Atividades e Conhecimentos.

## Fluxo principal

1. O usuário acessa `Relatórios` na barra de navegacao.

2. O usuário aciona o card `Mapas vigentes`.

3. O sistema mostra a tela `Relatório de Mapas Vigentes`, com uma árvore de unidades, *que tenham mapas vigentes* e os
   botões `Todas`, `Limpar`,  `Gerar` e `PDF`. Se o perfil for ADMIN, serão mostradas todas as unidades. Se o perfil for
   GESTOR, serão mostrados *apenas* a unidade do usuáario e suas unidades subordinadas, recursivamente.

   Exceção visual desta tela: unidades superiores não elegíveis (como unidades `INTERMEDIARIA`) permanecem em estado
   visual **indeterminado** sempre que houver ao menos uma filha selecionada, mesmo quando todas as filhas elegíveis
   daquela subárvore estiverem selecionadas. Esta variação vale apenas para o relatório e não altera o comportamento
   padrão documentado em `design/arvoreunidades.md`.

4. O usuário escolhe as unidades na árvore para as quais quer gerar o relatório e aciona o botão `Gerar`.

5. O sistema processa os dados e mostra na tela, logo abaixo dos controles, uma prévia contendo, para cada mapa, as
   seguintes informações:

    - (Para cada unidade selecionada:)
        - Unidade (Sigla e Nome)
        - (Para cada competencia:)
            - Descricao da competência
                - Atividades da competencia
                    - Conhecimentos da atividade

6. O usuário aciona o botão `PDF`.

7. O sistema gera o arquivo PDF com os dados especificados acima, precedidos de um cabeçalho formal com brasão, nome do
   sistema, data/hora da geração e filtros aplicados.
