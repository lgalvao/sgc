# Sobre o projeto

Este projeto é um protótipo de Sistema de Gestão de Competências (SGC) para o TRE-PE, **desenvolvido** em Vue 3 e Vite, com TypeScript, Vue Router, Bootstrap 5 e Pinia. O objetivo do sistema será gerir os fluxos de mapeamento, revisão e diagnóstico de competências das unidades do TRE-PE, centralizando todos os dados no front-end via mocks em JSON. O sistema está em desenvolvimento ativo, com muitas funcionalidades já implementadas e outras em andamento.

## Antes de qualquer coisa

- Este é um **protótipo**. Não vamos nos preocupar com desempenho ou reuso; o foco é no funcionamento da UX/UI do sistema.
- **Não** antecipe otimizações e abstrações; faça só o necessário para o momento.
- O código deve ser simples e direto, seguindo as convenções do Vue e do Bootstrap, mas sem complexidade desnecessária.
- O código, comentários e dados devem estar sempre em **português do Brasil**.

## Testes e Qualidade de Código

O projeto utiliza Vitest para testes unitários e Playwright para testes de integração (e2e). ESLint é usado para garantir a consistência do código.

### Arquitetura dos Testes E2E

Os testes E2E seguem uma arquitetura semântica em 3 camadas para garantir que sejam legíveis, robustos e fáceis de manter:

1.  **Dados e Constantes (`/e2e/cdu/helpers/dados`):** Centraliza todos os seletores (`data-testid`), textos da UI e URLs, eliminando "strings mágicas" dos testes.
2.  **Helpers - Ações e Verificações (`/e2e/cdu/helpers`):** Camada que encapsula toda a lógica de interação e verificação com o Playwright. As funções são nomeadas de forma semântica (ex: `criarProcessoCompleto`, `verificarProcessoFinalizadoNoPainel`) e são o único local onde `expect` e seletores complexos são permitidos.
3.  **Especificações (`/e2e/cdu/*.spec.ts`):** Os arquivos de teste em si, que devem ser lidos como uma narrativa de usuário, orquestrando chamadas aos helpers sem conter detalhes técnicos.

Esta estrutura é reforçada pelo uso extensivo de atributos `data-testid` no código-fonte para criar seletores resilientes. Para uma documentação mais detalhada sobre a arquitetura e os helpers, consulte o [README dos testes E2E](./e2e/cdu/README.md).

### Localização dos Testes

- **Testes Unitários (Vitest)**: Ficam localizados em diretórios `__tests__/` adjacentes aos arquivos que estão testando (ex: `src/components/__tests__/Navbar.spec.ts`).
- **Testes E2E (Playwright)**: Todos os testes de ponta a ponta ficam no diretório `/e2e`, organizados por funcionalidade (ex: `e2e/cdu/cdu-01.spec.ts`).

### Comandos

- **Executar testes unitários**:
  
  ```bash
  npm run test:unit
  ```
- **Executar testes end-to-end**:
  
  ```bash
  npm run test:e2e
  ```
- **Verificar o estilo do código (Lint)**:
  
  ```bash
  npm run lint
  ```
- **Verificar os tipos do TypeScript**:
  
  ```bash
  npm run typecheck
  ```

## Estrutura de Diretórios:

- `/src/components/`: Componentes Vue reutilizáveis
- `/src/views/`: Páginas/rotas da aplicação
- `/src/stores/`: Gerenciamento de estado com Pinia
- `/src/mocks/`: Dados simulados em JSON
- `/src/composables/`: Lógica reutilizável, como o `usePerfil.ts`
- `/src/constants/`: Constantes e enums centralizados
- `/src/utils/`: Utilitários auxiliares para funcionalidades comuns
- `/src/types/`: Definições de tipos TypeScript

## Visão geral de design

- **Dados Centralizados**: Todos os dados (processos, unidades, atividades, etc.) são mantidos em stores Pinia, alimentados por arquivos JSON em `src/mocks`. Não há backend; toda manipulação é local.
- **Perfis de Usuário**: O perfil (`ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`) é determinado dinamicamente com base na lotação do servidor logado, através do composable `usePerfil`. O `idServidor` logado é gerenciado pela store `perfil.ts` e persistido no localStorage.
- **Login**: A tela de login permite ao usuário "logar" como qualquer servidor cadastrado, com um seletor para pares "perfil - unidade" quando houver múltiplas opções.

## Arquitetura e Componentes

### Stores (`/src/stores/`)

Cada domínio possui um store dedicado:

- `processos.ts`: Gerencia o estado dos processos.
- `subprocessos.ts`: Gerencia os dados e a lógica de negócio dos subprocessos.
- `mapas.ts`: Gerencia os mapas de competência.
- `atividades.ts`: Gerencia atividades e conhecimentos.
- `atribuicoes.ts`: Gerencia atribuições temporárias de servidores.
- `perfil.ts`: Gerencia o `idServidor`, perfil e unidade do usuário logado.
- `servidores.ts`: Gerencia os dados dos servidores.
- `unidades.ts`: Gerencia as unidades organizacionais.
- `revisao.ts`: Gerencia o estado das mudanças durante a revisão de um mapa.
- `alertas.ts`: Fornece dados mockados de alertas para o painel.
- `notificacoes.ts`: Gerencia o sistema de notificações da aplicação.
- `configuracoes.ts`: Gerencia as configurações globais do sistema.

### Componentes (`/src/components/`)

- `AceitarMapaModal.vue`: Modal para GESTORES/CHEFES aceitarem um mapa ou para o ADMIN homologá-lo.
- `AcoesEmBlocoModal.vue`: Modal para aceitar ou homologar cadastros de atividades em lote.
- `BarraNavegacao.vue`: Agrupa o botão "Voltar" e breadcrumbs dinâmicos.
- `CriarCompetenciaModal.vue`: Modal para criar ou editar uma competência.
- `DisponibilizarMapaModal.vue`: Modal para o ADMIN disponibilizar um mapa para validação.
- `HistoricoAnaliseModal.vue`: Modal que exibe o histórico de análises e observações de um mapa.
- `ImpactoMapaModal.vue`: Exibe o impacto das alterações de atividades em um mapa em revisão.
- `ImportarAtividadesModal.vue`: Permite importar atividades de processos finalizados.
- `Navbar.vue`: Barra de navegação superior principal, com links, seletor de perfil e logout.
- `NotificacaoContainer.vue`: Container global para exibição de notificações (toasts).
- `SistemaNotificacoesModal.vue`: Central que exibe o histórico de todas as notificações.
- `SubprocessoCards.vue`: Apresenta cards de navegação dinâmicos na tela de subprocesso.
- `SubprocessoHeader.vue`: Cabeçalho da página de subprocesso com informações de contexto.
- `SubprocessoModal.vue`: Modal para o ADMIN alterar a data limite de uma etapa.
- `TabelaProcessos.vue`: Componente reutilizável para exibir listas de processos com ordenação.
- `TreeRow.vue` e `TreeTable.vue`: Componentes para criar tabelas hierárquicas expansíveis.
- `UnidadeTreeItem.vue`: Componente recursivo para renderizar uma árvore de unidades com checkboxes.

### Views (`/src/views/`)

- `Login.vue`: Tela de login simulado.
- `Painel.vue`: Painel inicial com processos e alertas.
- `Processo.vue`: Detalhes de um processo e sua árvore de unidades.
- `Subprocesso.vue`: Detalhes do subprocesso de uma unidade.
- `CadProcesso.vue`: Formulário para CRUD de processos.
- `CadAtividades.vue`: Tela para cadastro de atividades e conhecimentos de uma unidade.
- `CadAtribuicao.vue`: Formulário para criar atribuições temporárias.
- `CadMapa.vue`: Tela para criação e edição de mapas de competências.
- `Configuracoes.vue`: Página para o ADMIN ajustar configurações globais.
- `DiagnosticoEquipe.vue`: Tela para avaliação de competências pela equipe.
- `Historico.vue`: Exibe a lista de processos finalizados.
- `OcupacoesCriticas.vue`: Tela para cadastrar ocupações críticas da unidade.
- `Relatorios.vue`: Painel para visualização e exportação de relatórios.
- `Unidade.vue`: Detalhes de uma unidade organizacional.
- `VisAtividades.vue`: Tela para visualização e validação do cadastro de atividades.
- `VisMapa.vue`: Tela para visualização e validação de um mapa de competências.

## Regras Importantes ao Codificar

- **Stores Pinia**: Sempre centralize dados e a lógica de acesso nos stores. Nunca acesse arquivos JSON diretamente de componentes.
- **Idioma**: Mantenha todo o código, comentários e dados em português do Brasil.
- **Padrões**: Siga os padrões de navegação, componentização e UI/UX já estabelecidos no projeto.
- **Contexto do Usuário**: Para obter o perfil e a unidade do usuário, utilize `perfilStore.perfilSelecionado` e `perfilStore.unidadeSelecionada`.
- **Utilitários**: Use as funções centralizadas em `/src/utils/index.ts` para operações comuns (datas, IDs, etc.).
- **Constantes**: Utilize as constantes em `/src/constants/` para evitar o uso de strings "mágicas".
- **Notificações**: Use a store `notificacoes.ts` (via `useNotificacoesStore()`) para disparar feedbacks ao usuário com os métodos `sucesso()`, `erro()`, `aviso()`, etc.