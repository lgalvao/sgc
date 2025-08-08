# Sobre o projeto

Este projeto é um protótipo de Sistema de Gestão de Competências (SGC) para o TRE-PE, desenvolvido em Vue 3 + Vite, com Vue Router, Bootstrap 5 e Pinia. 

O objetivo é simular os fluxos de mapeamento, revisão e diagnóstico de competências das unidades do TRE-PE, centralizando todos os dados no front-end. O sistema está em desenvolvimento ativo, com várias funcionalidades já implementadas e outras em andamento.

## Antes de qualquer coisa

- Este é um protótipo. Não faremos validações, a não ser que eu solicite especificamente uma validação para ilustrar algum aspecto do sistema.
- Não vamos implementar segurança nem controles de acesso. No entando, o perfil atual é importante para a navegação e exibição de dados, mesmo no protótipo.
- Não vamos nos preocupar com desempenho; o foco é no funcionamento da UX/UI do sistema; não antecipe otimizações e abstrações; faça só o necessário para o momento.
- O código deve ser simples e direto, seguindo as convenções do Vue e do Bootstrap, mas sem complexidade desnecessária.

## Visão geral do projeto

- **Dados Centralizados**: Todos os dados (processos, unidades, atividades, conhecimentos, mapas de competências, atribuições temporárias, processosUnidades etc.) são mantidos em stores Pinia, alimentados por arquivos JSON em `src/mocks`. Não há backend; toda manipulação é local e reativa.
- **Perfis de Usuário**: O perfil de usuário (ADMIN, GESTOR, CHEFE, SERVIDOR) é determinado dinamicamente com base na lotação do servidor logado, através do composable `usePerfil`. O `servidorId` do usuário logado é gerenciado pela store `perfil.js` e persistido no localStorage.
- **Login**: A tela de login permite ao usuário "logar" como qualquer servidor cadastrado, apresentando um único seletor para pares "perfil - unidade" quando múltiplas opções estão disponíveis.

## Estrutura de Diretórios:

- `/src/components/`: Componentes Vue reutilizáveis
- `/src/views/`: Páginas/rotas da aplicação
- `/src/stores/`: Gerenciamento de estado com Pinia
- `/src/mocks/`: Dados simulados em JSON
- `/src/composables/`: Lógica reutilizável, como o `usePerfil.js` que calcula o perfil do usuário.

## Telas e Componentes Principais

- **Painel**: Tela inicial que exibe uma lista de processos e uma seção de alertas. A lista de processos é alimentada pela `processos.js` store, enquanto os alertas são dados mockados da `alertas.js` store. As datas dos alertas são formatadas.
- **Processo**: Exibe árvore de unidades participantes de um processo, mostrando a `dataLimite` do processo para cada unidade. Unidades folha são destacadas e levam à tela de atividades/conhecimentos. Inclui botão "Finalizar processo" para ADMINs e colunas da tabela ajustadas.
- **Atividades e conhecimentos**: Cadastro de atividades e conhecimentos para uma unidade folha, em tela única e dinâmica.
- **Mapa de competências**: Para unidades com cadastro de atividades/conhecimentos finalizado, permite criar, editar,
  visualizar e disponibilizar mapas de
  competências, associando atividades a competências.
- **Atribuição temporária**: Tela para criação/edição de atribuições temporárias de servidores a unidades.
- **Histórico de processos**: Consulta de processos finalizados.
- **Navbar**: Barra de navegação principal com links para todas as áreas, incluindo um seletor de servidor para simular
  o login de diferentes usuários. Exibe o perfil e a unidade selecionados no login.

## UI/UX e Padrões

- **Bootstrap 5**: Layout responsivo e visual padronizado.
- **Bootstrap Icons**: Padronização de ícones.
- **Alertas e Notificações**: Exibidos no painel a partir de dados mockados no `painel.js` store.
- **Navegação**: Sempre via Vue Router; componentes acessam dados exclusivamente via stores do Pinia.
- **Feedback ao Usuário**: Mensagens de sucesso/erro básicas implementadas.
- **Responsividade**: Layout adaptável a diferentes tamanhos de tela.

## Arquitetura e Componentização

- **Dados**: Mockados em JSON, importados apenas nos stores do Pinia.
- **Stores**: Cada domínio (processo, mapa, unidade, perfil etc.) possui um store dedicado.
    - `processos.js`: Gerencia o estado dos processos, incluindo a relação com as unidades através de `ProcessoUnidade` e o acesso a `processosUnidades.json`.
    - `mapas.js`: Gerencia os mapas de competência, incluindo a busca por unidade e processo (`getMapaByUnidadeId`) e a busca por mapa vigente (`getMapaVigentePorUnidade`).
    - `atividades.js`: Gerencia atividades e conhecimentos.
    - `atribuicaoTemporaria.js`: Gerencia atribuições temporárias.
    - `perfil.js`: Gerencia o `servidorId`, o perfil e a unidade selecionados do usuário logado, persistindo-os no localStorage.
    - `servidores.js`: Gerencia os dados dos servidores.
    - `unidades.js`: Gerencia as unidades organizacionais e inclui a função `pesquisarUnidade` para busca hierárquica.
    - `alertas.js`: Fornece dados mockados para o painel (ex: alertas).

- **Componentes**:
    - `TreeTable.vue`: Tabela hierárquica dinâmica que renderiza cabeçalhos e linhas com base nas colunas fornecidas. Suporta ocultar cabeçalhos e larguras de coluna dinâmicas. Eventos de clique em itens aninhados são propagados corretamente.
    - `TreeRow.vue`: Linha de uma tabela hierárquica que renderiza suas células dinamicamente. Propaga eventos de clique corretamente.
    - `Navbar.vue`: Barra de navegação que exibe o perfil e a unidade selecionados, com um seletor de perfil oculto.
      **Views**
    - `Login.vue`: Tela de login que permite ao usuário "logar" como qualquer servidor cadastrado, apresentando um único seletor para pares "perfil - unidade" quando múltiplas opções estão disponíveis.
    - `Painel.vue`: Painel que exibe processos e alertas, utilizando os stores `processos.js` e `alertas.js`. O botão "Criar processo" é exibido condicionalmente com base no `perfilSelecionado`. Datas de alerta formatadas.
    - `CadProcesso.vue`: Cadastro de processos (CRUD)
    - `Processo.vue`: Detalhes de um processo, incluindo a árvore de unidades do processo com a `dataLimite` correta. O botão "Finalizar processo" é exibido para ADMINs. Largura das colunas da tabela ajustada. Ao clicar em uma unidade, navega para `ProcessoUnidade.vue` se for uma unidade participante direta, ou para `ProcessosSubordinadas.vue` se for uma unidade intermediária com subordinadas participantes.
    - `ProcessosSubordinadas.vue`: Exibe as unidades subordinadas participantes de uma unidade pai dentro do contexto de um processo específico. Permite navegar para `ProcessoUnidade.vue` ou para outra `ProcessosSubordinadas.vue`.
    - `ProcessoUnidade.vue`: Detalhes de um processo para uma unidade. Exibe informações detalhadas do responsável e cards dinâmicos e clicáveis baseados no tipo de processo. O card "Atribuição temporária" foi removido.
    - `CadAtividades.vue`: Cadastro de atividades/conhecimentos.
    - `CadAtribuicao.vue`: Cadastro de atribuições temporárias (CRUD).
    - `CadMapa.vue`: Edição/criação de mapa de competências.
    - `CadMapaVisualizacao.vue`: Visualização de mapa de competências.
    - `CadMapaFinalizacao.vue`: Finalização/disponibilização de mapa.
    - `HistoricoProcessos.vue`: Lista de processos finalizados.
    - `Unidade.vue`: Detalhes de uma unidade (sem vínculo com processo). Exibe informações detalhadas do responsável, status do mapa vigente e uma `TreeTable` de unidades subordinadas (sem coluna de situação e sem cabeçalho de colunas, com largura total).

## Regras Importantes

- Sempre centralize dados e lógica de acesso a esses dados nos stores do Pinia.
- Nunca acesse arquivos JSON diretamente nos componentes.
- Mantenha todo o código, comentários e dados em português.
- Siga o padrão de navegação, componentização e UI/UX já estabelecido.
- Mantenha a consistência do código seguindo as convenções existentes.
- Para o contexto do usuário logado (perfil e unidade), utilize sempre `perfilStore.perfilSelecionado` e `perfilStore.unidadeSelecionada`.
- Unidades do tipo `INTERMEDIARIA` (como a COSIS) não devem ter `processosUnidade` associados a elas.

## Testes E2E (Playwright)

Os testes end-to-end (E2E) são implementados utilizando o Playwright e estão localizados no diretório `spec/`. Eles garantem que os fluxos críticos da aplicação funcionem corretamente do ponto de vista do usuário.

- **Localização**: Todos os testes E2E estão no diretório `spec/`.
- **Estrutura**: Cada arquivo `.spec.ts` contém um conjunto de testes relacionados a uma funcionalidade específica (ex: `login.spec.ts` para o fluxo de login, `cad-atividades.spec.ts` para o cadastro de atividades).
- **Autenticação**: O login é realizado uma vez antes de cada teste ou suíte de testes, utilizando a função auxiliar `login` de `utils/auth.ts`, garantindo que os testes operem em um estado autenticado.
