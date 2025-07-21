# Prompt de Continuidade – Projeto SGC

Este projeto é um protótipo de Sistema de Gestão de Competências (SGC) para o TRE-PE, desenvolvido em Vue 3 + Vite, com Vue Router, Bootstrap 5 e Pinia. O objetivo é simular o fluxo de mapeamento de competências das unidades organizacionais, centralizando todos os dados no front-end.

## Visão Geral do Funcionamento

- **Dados Centralizados**: Todos os dados (processos, unidades, atividades, conhecimentos, mapas de competências, atribuições temporárias) são mantidos em stores Pinia, alimentados por arquivos JSON em `src/mocks`. Não há backend; toda manipulação é local e reativa.
- **Perfis de Usuário**: O perfil global (SEDOC, CHEFE, GESTOR) é gerenciado via Pinia e persistido em localStorage. O perfil determina a configuração do painel e a navegação, mas não há controle real de permissões.
- **Situações Padronizadas**: Processos, unidades e mapas possuem situações calculadas pelo sistema: Não iniciado, Em andamento, Finalizado, Disponível para validação.
- **Login**: Tela de login sem autenticação real e sem validação obrigatória; o acesso é sempre permitido.

## Telas e Fluxos Principais

- **Painel**: Tela inicial parametrizada por perfil, exibindo cartões, listas, alertas e ações rápidas conforme arquivos JSON em `src/mocks/painel`. Inclui tabela de processos (para SEDOC e GESTOR) e cartões de alertas/notificações.
- **Processos**: Lista de processos com navegação para unidades participantes em árvore colapsável. Cadastro de novo processo via formulário com seleção hierárquica de unidades.
- **Unidades do Processo**: Exibe árvore de unidades participantes. Unidades folha são destacadas e levam à tela de atividades/conhecimentos.
- **Atividades/Conhecimentos**: Cadastro de atividades e conhecimentos por unidade folha, em tela única e dinâmica.
- **Mapa de Competências**: Para unidades finalizadas, permite criar, editar, visualizar e disponibilizar mapas de competências, associando atividades a competências.
- **Atribuição Temporária**: Tela para simular atribuições temporárias de servidores a unidades, com cadastro e listagem.
- **Histórico**: Tela dedicada para consulta de processos finalizados, exibidos em tabela ordenável.
- **Navbar**: Barra de navegação principal com links para todas as áreas, incluindo seletor global de perfil.

## UI/UX e Padrões

- **Bootstrap 5**: Layout responsivo e visual padronizado.
- **Árvore de Unidades**: Todos os nós começam expandidos; folhas são totalmente clicáveis e destacadas ao hover.
- **Formulários**: Simples, sem validação real.
- **Alertas/Notificações**: Exibidos no painel conforme configuração do perfil.
- **Navegação**: Sempre via Vue Router; componentes acessam dados exclusivamente via stores Pinia.

## Arquitetura e Componentização

- **Dados**: Mockados em JSON, importados apenas nos stores Pinia.
- **Stores**: Cada domínio (processos, unidades, perfil, etc.) possui um store dedicado.
- **Componentes**:
  - `TreeNode.vue`: Árvore recursiva de unidades.
  - `Navbar.vue`: Barra de navegação e seletor de perfil.
  - `Painel.vue`: Painel dinâmico por perfil.
  - `Processos.vue`: Lista de processos.
  - `FormProcesso.vue`: Cadastro de processo.
  - `UnidadesProcesso.vue`: Árvore de unidades do processo.
  - `AtividadesConhecimentos.vue`: Cadastro de atividades/conhecimentos.
  - `MapaCompetencias.vue`: Edição/criação de mapa de competências.
  - `FinalizacaoMapa.vue`: Finalização/disponibilização de mapa.
  - `AtribuicaoTemporariaForm.vue`: Atribuição temporária de servidores.
  - `Historico.vue`: Consulta de processos finalizados.

## Regras Importantes

- Sempre centralize dados e lógica nos stores Pinia.
- Nunca acesse arquivos JSON diretamente nos componentes.
- Mantenha todo o código, comentários e dados em português.
- Siga o padrão de navegação, componentização e UI já estabelecido.
- Comente o código explicando simulações e limitações do protótipo.
- Atualize README e documentação ao evoluir o sistema.
