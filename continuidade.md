# Sobre o Projeto SGC

Este projeto é um protótipo de Sistema de Gestão de Competências (SGC) para o TRE-PE, desenvolvido em Vue 3 + Vite, com
Vue Router, Bootstrap 5 e Pinia. O objetivo é simular os fluxos de mapeamento, revisão e diagnóstico de competências das unidades
do TRE, centralizando todos os dados no front-end. O sistema está em desenvolvimento ativo, com várias funcionalidades já implementadas e outras em andamento.

## Antes de qualquer coisa
- Este é um protótipo. Não faremos validações, a não ser que eu solicite especificamente uma validação para ilustrar algum aspecto do sistema.
- Não vamos implementar segurança nem controles de acesso. Novamente, este é um protótipo de telas!
- Não vamos nos preocupar com performance. Não temos sequer um backend, nem pretendemos criar um backend para esse protótipo. O foco é no funcionamento da UX/UI do sistema.

## Visão geral do projeto
- **Dados Centralizados**: Todos os dados (processos, unidades, atividades, conhecimentos, mapas de competências,
  atribuições temporárias) são mantidos em stores Pinia, alimentados por arquivos JSON em `src/mocks`. Não há backend;
  toda manipulação é local e reativa.
- **Perfis de Usuário**: O perfil global (SEDOC, CHEFE, GESTOR, SERVIDOR) é gerenciado via Pinia e persistido em localStorage. 
  perfil determina a configuração do painel e a navegação, mas não há controle real de permissões.
- **Situações Padronizadas**: Processos, unidades e mapas possuem situações calculadas pelo sistema: 'Não iniciado', 'Em
  andamento', 'Finalizado', 'Disponível para validação'.
- **Login**: Tela de login sem autenticação real e sem validação obrigatória; o acesso é sempre permitido.
- **Estado Atual**: A maior parte das funcionalidades básicas de CRUD estão implementadas, mas ainda faltam validações, fluxos de aprovação e melhorias de usabilidade.

## Estrutura de Diretórios:
- /src/components/: Componentes Vue reutilizáveis
- /src/views/: Páginas/rotas da aplicação
- /src/stores/: Gerenciamento de estado com Pinia
- /src/mocks/: Dados simulados em JSON
- /src/composables/: Lógica reutilizável

## Telas e Componentes Principais
- **Painel**: Tela inicial parametrizada por perfil, exibindo cartões, listas, alertas e ações rápidas conforme arquivos
  JSON em `src/mocks/painel`. Inclui tabela de processos (para SEDOC e GESTOR) e cartões de alertas/notificações.
- **Detalhes de processo**: Exibe árvore de unidades participantes de um processo. Unidades folha são destacadas e levam à tela de
  atividades/conhecimentos.
- **Atividades/Conhecimentos**: Cadastro de atividades e conhecimentos para uma unidade folha, em tela única e dinâmica.
- **Mapa de Competências**: Para unidades com cadastro de atividades/conhecimentos finalizado, permite criar, editar, visualizar e disponibilizar mapas de
  competências, associando atividades a competências.
- **Atribuição Temporária**: Tela para criação/edição de atribuições temporárias de servidores a unidades.
- **Histórico**: Consulta de processos finalizados.
- **Navbar**: Barra de navegação principal com links para todas as áreas, incluindo seletor global de perfil.

## UI/UX e Padrões
- **Bootstrap 5**: Layout responsivo e visual padronizado.
- **Bootstrap Icons**: Padronização de ícones.
- **Formulários**: Implementados com validação básica, podendo ser aprimorados.
- **Alertas e Notificações**: Exibidos no painel conforme configuração do perfil.
- **Navegação**: Sempre via Vue Router; componentes acessam dados exclusivamente via stores do Pinia.
- **Feedback ao Usuário**: Mensagens de sucesso/erro básicas implementadas, podendo ser expandidas.
- **Responsividade**: Layout adaptável a diferentes tamanhos de tela.

## Arquitetura e Componentização
- **Dados**: Mockados em JSON, importados apenas nos stores do Pinia.
- **Stores**: Cada domínio (processo, mapa, unidade, perfil etc.) possui um store dedicado.
  - `processos.js`: Gerencia o estado dos processos
  - `mapas.js`: Gerencia os mapas de competência
  - `atividadesConhecimentos.js`: Gerencia atividades e conhecimentos
  - `atribuicaoTemporaria.js`: Gerencia atribuições temporárias
  - `perfil.js`: Gerencia o perfil do usuário
  - `unidades.js`: Gerencia as unidades organizacionais
- **Componentes**:
  - `TreeTable.vue`: Tabela hierarquica de unidades.
  - `TreeRow.vue`: Linha de uma tabela hierárquica. 
  - `Navbar.vue`: Barra de navegação e seletor de perfil.
  **Views**
  - `Login.vue`: Tela padrão de login.
  - `Painel.vue`: Painel, ajustado dinamicamente de acordo com o perfil atual.
  - `CadProcesso.vue`: Cadastro de processos (CRUD)
  - `DetalhesProcesso.vue`: Detalhes de um processo, incluindo a árvore de unidades do processo.
  - `DetalhesUnidade.vue`: Detalhes de um processo para uma unidade.
  - `CadAtividadesConhecimentos.vue`: Cadastro de atividades/conhecimentos.
  - `CadAtribuicao.vue`: Cadastro de atribuições temporárias (CRUD).
  - `CadMapa.vue`: Edição/criação de mapa de competências.
  - `Mapas.vue`: Listagem e gerenciamento de mapas.
  - `MapaVisualizacao.vue`: Visualização de mapa de competências.
  - `MapaFinalizacao.vue`: Finalização/disponibilização de mapa.
  - `HistoricoProcessos.vue`: Lista de processos finalizados.
  - `Unidades.vue`: Listagem hierárquica de unidades.'

## Regras Importantes
- Sempre centralize dados e lógica de acesso a esses dadosnos stores do Pinia.
- Nunca acesse arquivos JSON diretamente nos componentes.
- Mantenha todo o código, comentários e dados em português.
- Siga o padrão de navegação, componentização e UI/UX já estabelecido.
- Mantenha a consistência do código seguindo as convenções existentes