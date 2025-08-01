# Sobre o projeto

Este projeto é um protótipo de Sistema de Gestão de Competências (SGC) para o TRE-PE, desenvolvido em Vue 3 + Vite, com
Vue Router, Bootstrap 5 e Pinia. O objetivo é simular os fluxos de mapeamento, revisão e diagnóstico de competências das
unidades
do TRE, centralizando todos os dados no front-end. O sistema está em desenvolvimento ativo, com várias funcionalidades
já implementadas e outras em andamento.

## Antes de qualquer coisa

- Este é um protótipo. Não faremos validações, a não ser que eu solicite especificamente uma validação para ilustrar
  algum aspecto do sistema.
- Não vamos implementar segurança nem controles de acesso. No entando, o perfil atual é importante para a navegação e
  exibição de dados, mesmo no protótipo.
- Não vamos nos preocupar com desempenho; o foco é no funcionamento da UX/UI do sistema
- Considerando os pontos acima, não antecipé otimizações e abstrações que possam ser necessárias no futuro.
- O código deve ser simples e direto, seguindo as convenções do Vue e do Bootstrap, mas sem complexidade desnecessária.

## Visão geral do projeto

- **Dados Centralizados**: Todos os dados (processos, unidades, atividades, conhecimentos, mapas de competências,
  atribuições temporárias etc.) são mantidos em stores Pinia, alimentados por arquivos JSON em `src/mocks`. Não há
  backend;
  toda manipulação é local e reativa.
- **Perfis de Usuário**: O perfil de usuário (ADMIN, GESTOR, CHEFE, SERVIDOR) é determinado dinamicamente com base na
  lotação do servidor logado, através do composable `usePerfil`. O `servidorId` do usuário logado é gerenciado pela
  store `perfil.js` e persistido no localStorage.
- **Login**: A tela de login permite ao usuário "logar" como qualquer servidor cadastrado, definindo o `servidorId`
  globalmente. Não há autenticação real.

## Estrutura de Diretórios:

- `/src/components/`: Componentes Vue reutilizáveis
- `/src/views/`: Páginas/rotas da aplicação
- `/src/stores/`: Gerenciamento de estado com Pinia
- `/src/mocks/`: Dados simulados em JSON
- `/src/composables/`: Lógica reutilizável, como o `usePerfil.js` que calcula o perfil do usuário.

## Telas e Componentes Principais

- **Painel**: Tela inicial que exibe uma lista de processos e uma seção de alertas. A lista de processos é alimentada pela `processos.js` store, enquanto os alertas são dados mockados da `painel.js` store.
- **Detalhes de processo**: Exibe árvore de unidades participantes de um processo, mostrando a `dataLimite` do processo para cada unidade. Unidades folha são destacadas e levam à tela de
  atividades/conhecimentos.
- **Atividades e conhecimentos**: Cadastro de atividades e conhecimentos para uma unidade folha, em tela única e
  dinâmica.
- **Mapa de competências**: Para unidades com cadastro de atividades/conhecimentos finalizado, permite criar, editar,
  visualizar e disponibilizar mapas de
  competências, associando atividades a competências.
- **Atribuição temporária**: Tela para criação/edição de atribuições temporárias de servidores a unidades.
- **Histórico de processos**: Consulta de processos finalizados.
- **Navbar**: Barra de navegação principal com links para todas as áreas, incluindo um seletor de servidor para simular
  o login de diferentes usuários.

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
    - `processos.js`: Gerencia o estado dos processos
    - `mapas.js`: Gerencia os mapas de competência
    - `atividadesConhecimentos.js`: Gerencia atividades e conhecimentos
    - `atribuicaoTemporaria.js`: Gerencia atribuições temporárias
    - `perfil.js`: Gerencia o `servidorId`, o perfil e a unidade selecionados do usuário logado, persistindo-os no localStorage.
    - `servidores.js`: Gerencia os dados dos servidores.
    - `unidades.js`: Gerencia as unidades organizacionais
    - `painel.js`: Fornece dados mockados para o painel (ex: alertas).
- **Componentes**:
    - `TreeTable.vue`: Tabela hierárquica dinâmica que renderiza cabeçalhos e linhas com base nas colunas fornecidas.
    - `TreeRow.vue`: Linha de uma tabela hierárquica que renderiza suas células dinamicamente.
    - `Navbar.vue`: Barra de navegação que exibe o perfil e a unidade selecionados, com um seletor de perfil oculto.
      **Views**
    - `Login.vue`: Tela de login que permite ao usuário "logar" como qualquer servidor cadastrado, apresentando um único seletor para pares "perfil - unidade" quando múltiplas opções estão disponíveis.
    - `Painel.vue`: Painel que exibe processos e alertas, utilizando os stores `processos.js` e `alertas.js`. O botão "Criar processo" é exibido condicionalmente com base no `perfilSelecionado`.
    - `CadProcesso.vue`: Cadastro de processos (CRUD)
    - `DetalhesProcesso.vue`: Detalhes de um processo, incluindo a árvore de unidades do processo com a `dataLimite` correta.
    - `DetalhesUnidadeProcesso.vue`: Detalhes de um processo para uma unidade.
    - `CadAtividades.vue`: Cadastro de atividades/conhecimentos.
    - `CadAtribuicao.vue`: Cadastro de atribuições temporárias (CRUD).
    - `CadMapa.vue`: Edição/criação de mapa de competências.
    - `CadMapaVisualizacao.vue`: Visualização de mapa de competências.
    - `CadMapaFinalizacao.vue`: Finalização/disponibilização de mapa.
    - `HistoricoProcessos.vue`: Lista de processos finalizados.
    - `Unidades.vue`: Listagem hierárquica de unidades.
    - `DetalhesUnidade.vue`: Detalhes de uma unidade (sem vínculo com processo).

## Regras Importantes

- Sempre centralize dados e lógica de acesso a esses dados nos stores do Pinia.
- Nunca acesse arquivos JSON diretamente nos componentes.
- Mantenha todo o código, comentários e dados em português.
- Siga o padrão de navegação, componentização e UI/UX já estabelecido.
- Mantenha a consistência do código seguindo as convenções existentes.
- Para o contexto do usuário logado (perfil e unidade), utilize sempre `perfilStore.perfilSelecionado` e `perfilStore.unidadeSelecionada`.

## Próximos Passos e Considerações

- **Refatoração da lógica de `getPerfisEUnidades`**: A função em `Login.vue` pode ser refatorada para melhorar a legibilidade e manutenção, especialmente a função `findUnit`.
- **Consistência da unidade no Navbar**: Atualmente, o seletor de perfil no Navbar ainda exibe a unidade de lotação principal do servidor (`servidor.unidade`) e não a unidade selecionada no login. Avaliar se o link "Minha unidade" deve refletir a unidade selecionada no login ou a unidade de lotação principal.
- **Tratamento de erros**: Adicionar tratamento de erros mais robusto para cenários onde o servidor não é encontrado ou não há pares perfil/unidade disponíveis.
