## Documentação da Navegação para Detalhes da Unidade

Este documento descreve o fluxo de navegação implementado para visualizar os detalhes de uma unidade a partir de tabelas hierárquicas na aplicação.

### Componentes Envolvidos

- **`src/views/DetalhesProcesso.vue`**: Exibe as unidades participantes de um processo específico.
- **`src/views/Unidades.vue`**: Exibe a estrutura organizacional completa de todas as unidades.
- **`src/views/DetalhesUnidade.vue`**: A página de destino, que mostra os detalhes de uma unidade selecionada.
- **`src/components/TreeTable.vue`**: Componente reutilizável que renderiza a tabela de dados hierárquicos.
- **`src/components/TreeRow.vue`**: Componente que renderiza cada linha da tabela e lida com a interação do usuário (clique e hover).
- **`src/router.js`**: Arquivo de configuração do Vue Router, onde a rota `/unidade/:sigla` é definida para carregar a view `DetalhesUnidade.vue`.

### Fluxo de Interação

O processo de navegação é consistente entre as páginas `DetalhesProcesso.vue` e `Unidades.vue`.

1.  **Interação do Usuário**: 
    - O usuário posiciona o mouse sobre uma linha da tabela de unidades.
    - A linha é destacada com a cor primária do Bootstrap e o cursor muda para "pointer", indicando que é um elemento clicável. Esta estilização é definida em `TreeRow.vue`.

2.  **Evento de Clique**:
    - O usuário clica na linha (`<tr>`) da unidade desejada no componente `TreeRow.vue`.
    - O `TreeRow.vue` emite um evento `row-click` com os dados do item (unidade) correspondente.

3.  **Propagação do Evento**:
    - O componente pai, `TreeTable.vue`, captura o evento `row-click` do `TreeRow.vue` e o reemite para seu próprio componente pai (a view que o está utilizando).

4.  **Navegação**:
    - A view (`DetalhesProcesso.vue` ou `Unidades.vue`) escuta o evento `@row-click` em sua instância do `TreeTable`.
    - Ao receber o evento, a view aciona uma função de navegação (`abrirDetalhesUnidade` ou `navigateToUnit`).
    - Essa função utiliza o Vue Router para navegar programaticamente para a rota `/unidade/:sigla`, onde `:sigla` é o ID (a sigla) da unidade que foi clicada.

5.  **Renderização da View de Destino**:
    - O Vue Router, com base na configuração em `router.js`, identifica a rota e renderiza o componente `DetalhesUnidade.vue`.
    - A view `DetalhesUnidade.vue` então utiliza o parâmetro `:sigla` da rota para buscar e exibir as informações detalhadas da unidade correspondente.

Este padrão permite uma navegação consistente e reutilizável para os detalhes da unidade a partir de diferentes contextos da aplicação.
