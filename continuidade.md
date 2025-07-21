# Prompt de Continuidade – Projeto SGC

Este projeto é um protótipo de Sistema de Gestão de Competências (SGC) para o TRE-PE, desenvolvido em Vue 3 + Vite, com
Vue Router, Bootstrap 5 e Pinia. O objetivo é simular os fluxos de mapeamento, revisão e diagnóstico de competências das unidades
do TRE, centralizando todos os dados no front-end.

## Visão geral do projeto

- **Dados Centralizados**: Todos os dados (processos, unidades, atividades, conhecimentos, mapas de competências,
  atribuições temporárias) são mantidos em stores Pinia, alimentados por arquivos JSON em `src/mocks`. Não há backend;
  toda manipulação é local e reativa.
- **Perfis de Usuário**: O perfil global (SEDOC, CHEFE, GESTOR) é gerenciado via Pinia e persistido em localStorage. O
  perfil determina a configuração do painel e a navegação, mas não há controle real de permissões.
- **Situações Padronizadas**: Processos, unidades e mapas possuem situações calculadas pelo sistema: Não iniciado, Em
  andamento, Finalizado, Disponível para validação.
- **Login**: Tela de login sem autenticação real e sem validação obrigatória; o acesso é sempre permitido.

## Telas e Fluxos Principais

- **Painel**: Tela inicial parametrizada por perfil, exibindo cartões, listas, alertas e ações rápidas conforme arquivos
  JSON em `src/mocks/painel`. Inclui tabela de processos (para SEDOC e GESTOR) e cartões de alertas/notificações.
- **Unidades do Processo**: Exibe árvore de unidades participantes. Unidades folha são destacadas e levam à tela de
  atividades/conhecimentos.
- **Atividades/Conhecimentos**: Cadastro de atividades e conhecimentos por unidade folha, em tela única e dinâmica.
- **Mapa de Competências**: Para unidades com cadastro de atividades/conhecimentos finalizado, permite criar, editar, visualizar e disponibilizar mapas de
  competências, associando atividades a competências.
- **Atribuição Temporária**: Tela para criação/edição de atribuições temporárias de servidores a unidades.
- **Histórico**: Consulta de processos finalizados.
- **Navbar**: Barra de navegação principal com links para todas as áreas, incluindo seletor global de perfil.

## UI/UX e Padrões

- **Bootstrap 5**: Layout responsivo e visual padronizado.
- **Árvore de Unidades**: Todos os nós começam expandidos; folhas são totalmente clicáveis e destacadas ao hover.
- **Formulários**: Simples, sem validação real.
- **Alertas e Notificações**: Exibidos no painel conforme configuração do perfil.
- **Navegação**: Sempre via Vue Router; componentes acessam dados exclusivamente via stores do Pinia.

## Arquitetura e Componentização

- **Dados**: Mockados em JSON, importados apenas nos stores do Pinia.
- **Stores**: Cada domínio (processos, unidades, perfil etc.) possui um store dedicado.
- **Componentes**:
  - `TreeNode.vue`: Árvore recursiva de unidades.
  - `Navbar.vue`: Barra de navegação e seletor de perfil.
- **Views**
  - `Painel.vue`: Painel dinâmico por perfil.
  - `CadProcesso.vue`: Cadastro de processo.
  - `DetalhesProcesso.vue`: Árvore de unidades do processo.
  - `CadAtividadesConhecimentos.vue`: Cadastro de atividades/conhecimentos.
  - `CadMapa.vue`: Edição/criação de mapa de competências.
  - `MapaFinalizacao.vue`: Finalização/disponibilização de mapa.
  - `CadAtribuicao.vue`: Atribuição temporária de servidores.
  - `HistoricoProcessos.vue`: Consulta de processos finalizados.

## Regras Importantes
- Sempre centralize dados e lógica nos stores do Pinia.
- Nunca acesse arquivos JSON diretamente nos componentes.
- Mantenha todo o código, comentários e dados em português.
- Siga o padrão de navegação, componentização e UI já estabelecido.