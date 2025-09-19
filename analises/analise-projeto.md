### Análise do Projeto: Sistema de Gestão de Competências (SGC) para TRE-PE

#### Visão Geral
O projeto é um **protótipo frontend-only** de um Sistema de Gestão de Competências (SGC) desenvolvido para o Tribunal Regional Eleitoral de Pernambuco (TRE-PE). Seu foco principal é gerenciar fluxos de mapeamento, revisão e diagnóstico de competências organizacionais nas unidades do TRE-PE. Como protótipo, prioriza a funcionalidade de UX/UI sem ênfase em otimização de desempenho, reuso de código ou backend real — todos os dados são simulados localmente via arquivos JSON em `src/mocks/`. O sistema está em desenvolvimento ativo, com muitas telas e componentes implementados, mas funcionalidades em andamento.

- **Objetivo Principal**: Centralizar a gestão de processos, subprocessos, atividades, mapas de competências, atribuições de servidores e relatórios, considerando perfis de usuários (ADMIN, GESTOR, CHEFE, SERVIDOR) baseados na lotação organizacional.
- **Escopo**: Não há integração com backend; manipulações são locais via stores Pinia. O login é simulado, e o perfil/unidade do usuário é persistido no localStorage.
- **Idioma e Convenções**: Todo código, comentários e dados em português do Brasil. Ênfase em simplicidade, seguindo padrões Vue/Bootstrap, sem abstrações prematuras.

#### Stack Tecnológico
- **Framework Principal**: Vue 3 (com Composition API) + Vite como bundler.
- **Gerenciamento de Estado**: Pinia (stores dedicadas por domínio, ex.: `processos.ts`, `subprocessos.ts`).
- **Roteamento**: Vue Router.
- **UI e Estilos**: Bootstrap 5 + Bootstrap Icons; CSS customizado em `src/style.css`.
- **TypeScript**: Suporte completo com definições em `src/types/tipos.ts` e paths aliases (`@` para `src/`).
- **Utilitários**: date-fns para manipulação de datas; composables como `usePerfil.ts` para lógica reutilizável; constantes em `src/constants/` e utils em `src/utils/`.
- **Testes**:
  - **Unitários**: Vitest (em `__tests__/` adjacentes aos arquivos, ex.: `src/components/__tests__/Navbar.spec.ts`).
  - **E2E**: Playwright (em `spec/`, organizados por funcionalidade, ex.: `spec/cdu/cdu-01.spec.ts` para testes de login).
  - Cobertura: Istanbul + NYC para relatórios.
- **Qualidade de Código**: ESLint (config em `eslint.config.js`) e type-checking com `vue-tsc`.
- **Build e Dev**: Vite config (`vite.config.js`) com plugins para Vue, Istanbul (cobertura), aliases e setup de testes (globals, jsdom). Scripts no `package.json`: `npm run dev`, `npm run test:unit`, `npm run test:e2e`, `npm run lint`, `npm run typecheck`, e fluxo de coverage.
- **Dependências Chave** (de `package.json`):
  - Runtime: `vue@^3.5.21`, `pinia@^3.0.3`, `bootstrap@^5.3.8`, `vue-router@^4.5.1`, `date-fns@^4.1.0`.
  - Dev: `vitest@^3.2.4`, `@playwright/test@^1.55.0`, `eslint@^9.35.0`, `typescript@^5.9.2`, `vite@^7.1.5`.

Configurações adicionais:
- **tsconfig.json**: Modo estrito parcial (strictNullChecks e noImplicitAny desabilitados para flexibilidade), target ESNext, módulos ESNext/Bundler, includes para src/, spec/ e configs. Exclui node_modules e dist.
- **Outros**: `.nycrc.json` para coverage; `playwright.config.ts` para E2E; `vitest.setup.ts` para setup de testes.

#### Estrutura de Diretórios
Baseado no `environment_details` e README:
- **Raiz**: Configs (`.gitignore`, `package.json`, `tsconfig.json`, `vite.config.js`), `index.html`, docs (`README.md`, `licoes.md`, `prompts.md`).
- **reqs/**: Requisitos funcionais em Markdown (CDU-01 a CDU-21 para Casos de Uso, ex.: CDU-01 cobre login e estrutura de telas; `cdu-mapeamento-revisao.pdf` e `estados-mapeamento.md` para mapeamentos).
- **regras/**: Guias de desenvolvimento (endpoints, perfis, refatoração, testes Vitest/Playwright).
- **spec/**: Testes E2E Playwright (auxiliares em `cdu/`, gerais em `geral/`, utils como `auth.ts`).
- **src/**:
  - **components/**: Reutilizáveis (modais como `AceitarMapaModal.vue`, navegação `Navbar.vue`, tabelas hierárquicas `TreeTable.vue`, notificações `NotificacaoContainer.vue`). Testes em `__tests__/`.
  - **views/**: Páginas principais (ex.: `Login.vue`, `Painel.vue`, `Processo.vue`, `CadAtividades.vue`, `Relatorios.vue`).
  - **stores/**: 12 stores Pinia (ex.: `perfil.ts` para login, `mapas.ts` para competências, `notificacoes.ts` para toasts). Testes em `__tests__/`, incluindo `validacao-mocks.spec.ts`.
  - **mocks/**: Dados JSON simulados (ex.: `processos.json`, `unidades.json`, `mapas.json`).
  - **composables/**: `usePerfil.ts` (determina perfil por lotação), `useProcessosFiltrados.ts`.
  - **constants/**: Enums como `situacoes.ts`.
  - **types/**: `tipos.ts` para interfaces.
  - **utils/**: Funções comuns (`index.ts`), testes em `__tests__/`.
  - **Outros**: `App.vue`, `main.ts`, `router.ts`, `shims-vue.d.ts`, `style.css`.
- **tests/**: Setup Vitest (`vue-specific-setup.ts`, utils como `error-reporter.ts`).
- **Outros**: `.nyc_output/` para coverage; `plano-extensao-cobertura.md` possivelmente para planejamento de testes.

#### Funcionalidades Chave (Baseado em README e Exemplo de Req: CDU-01)
- **Autenticação e Perfis**: Login simulado via título/senha (mock). Determina perfis dinamicamente:
  - ADMIN: Administradores globais.
  - GESTOR/CHEFE: Responsáveis por unidades intermediárias/operacionais.
  - SERVIDOR: Outros lotados.
  - Suporte a múltiplos perfis/unidades com seletor no login. Usa `usePerfil` e store `perfil.ts`.
- **Navegação e UI**:
  - Barra superior (`Navbar.vue`): Links (Painel, Minha Unidade, Relatórios, Histórico), seletor de perfil/unidade, logout. Ícone de configs para ADMIN.
  - Breadcrumbs e "Voltar" em `BarraNavegacao.vue`.
  - Modais para ações (ex.: aceitar mapas, importar atividades, histórico de análises).
  - Tabelas hierárquicas (`TreeTable.vue`) para processos/unidades; cards dinâmicos (`SubprocessoCards.vue`).
  - Notificações: Sistema de toasts via `notificacoes.ts` (sucesso, erro, aviso).
- **Fluxos Principais**:
  - **Painel Inicial**: Alertas, processos pendentes.
  - **Gestão de Processos/Subprocessos**: CRUD via views como `CadProcesso.vue`, `Subprocesso.vue`; árvores de unidades (`UnidadeTreeItem.vue`).
  - **Atividades e Mapas**: Cadastro/validação (`CadAtividades.vue`, `VisMapa.vue`); importação, impacto de mudanças (`ImpactoMapaModal.vue`).
  - **Atribuições e Diagnósticos**: Temporárias (`CadAtribuicao.vue`), avaliação de equipe (`DiagnosticoEquipe.vue`).
  - **Relatórios e Histórico**: Exportação, processos finalizados (`Relatorios.vue`, `Historico.vue`).
  - **Configs**: Globais para ADMIN (`Configuracoes.vue`).
- **Regras de Negócio** (de CDU-01 e README):
  - Acesso condicional por perfil (ex.: só ADMIN edita datas limites via `SubprocessoModal.vue`).
  - Dados centralizados em stores; nunca acessar mocks diretamente.
  - Validações: Uso de constantes para estados/situações; utils para IDs/datas.
  - E2E cobre fluxos como login (`spec/geral/login.spec.ts`), CDU-specific (`spec/cdu/cdu-01.spec.ts`).

#### Pontos Fortes
- **Organização**: Estrutura modular (stores por domínio, testes colocalizados), fácil de estender.
- **Cobertura de Testes**: Unitários para componentes/stores; E2E para fluxos completos; scripts integrados para coverage.
- **Simulação Robusta**: Mocks abrangentes permitem testar cenários reais sem backend.
- **UX Focada**: Modais intuitivos, navegação breadcrumb-based, suporte a perfis dinâmicos.

#### Áreas de Melhoria Potenciais (Observações Gerais)
- **Dependências de Dados**: Como é mock-based, migração para backend real (ex.: API) exigiria refatoração das stores.
- **Acessibilidade/Segurança**: Protótipo ignora; adicionar ARIA para Bootstrap, validações de auth reais.
- **Cobertura de Reqs**: 21 CDUs em `reqs/`; verificar alinhamento com implementações (ex.: CDU-01 implementado em `Login.vue` e `Navbar.vue`).
- **Performance**: Para protótipo OK, mas trees/tables podem precisar de virtualização em escala.
- **Documentação**: README excelente; expandir `regras/` para mais guias (ex.: stores, modais).

O projeto está bem estruturado para um protótipo, com foco em funcionalidades core implementadas e testes sólidos.

#### Análise dos Requisitos em 'reqs/'

##### Visão Geral
O diretório `reqs/` contém os requisitos funcionais do Sistema de Gestão de Competências (SGC), documentados em 21 Casos de Uso (CDUs) em Markdown, um diagrama de estados em Mermaid (`estados-mapeamento.md`) e um PDF de mapeamento de revisão (`cdu-mapeamento-revisao.pdf`, que não pode ser lido diretamente aqui, mas presume-se complementar os fluxos). Os CDUs descrevem fluxos detalhados para um sistema hierárquico de gestão de competências no TRE-PE, focando em três tipos de processos: **Mapeamento** (cadastro inicial), **Revisão** (atualização) e **Diagnóstico** (avaliação, mencionado mas não detalhado nos CDUs fornecidos).

- **Estrutura dos CDUs**: Cada CDU segue um formato padrão: Ator(es), Pré-condições, Fluxo Principal (com subfluxos para criação/edição/remoção), Fluxos Alternativos/Extensões e Pós-condições implícitas. Ênfase em validações, notificações (e-mails e alertas), movimentações de subprocessos e situações de estado. Perfis de usuário (ADMIN, GESTOR, CHEFE, SERVIDOR) controlam acessos, com hierarquia organizacional (unidades operacionais, intermediárias, interoperacionais).
- **Temas Principais**:
  - **Autenticação e Navegação** (CDU-01): Login simulado e estrutura de telas (Navbar, Painel, Rodapé).
  - **Gestão de Processos** (CDU-02 a CDU-03, CDU-21): Criação, edição, iniciação e finalização de processos.
  - **Subprocessos e Fluxos por Tipo** (CDU-04 a CDU-05 para iniciação; CDU-06 a CDU-07 para detalhes).
  - **Cadastro e Revisão de Atividades/Conhecimentos** (CDU-08 a CDU-10, CDU-11, CDU-13 a CDU-14): CRUD, disponibilização, análise (aceite/devolução/homologação).
  - **Mapa de Competências** (CDU-12, CDU-15 a CDU-18, CDU-19 a CDU-20): Criação, impactos, validação, análise.
- **Diagrama de Estados** (`estados-mapeamento.md`): Foca no processo de Mapeamento, modelando transições de estados para subprocessos (ex.: Não Iniciado → Cadastro Em Andamento → Disponibilizado → Homologado → Mapa Criado → Disponibilizado → Validado/Homologado). Usa Mermaid stateDiagram-v2, com choices para decisões (validação/devolução). Não cobre Revisão ou Diagnóstico explicitamente, mas pode ser estendido.
- **Cobertura Geral**: Os requisitos cobrem ~80-90% das funcionalidades core (baseado na estrutura do projeto anterior). Ênfase em fluxos lineares com loops de aprovação hierárquica (unidade → superior → SEDOC/ADMIN). Notificações são críticas (e-mails padronizados, alertas internos). Validações incluem campos obrigatórios, associações (ex.: atividades sem conhecimentos bloqueiam disponibilização) e restrições por perfil/tipo de processo.
- **Integração com Projeto**: Alinha-se bem com o codebase Vue/Pinia:
  - **Views/Componentes**: CDU-01 → `Login.vue`, `Navbar.vue`; CDU-08 → `CadAtividades.vue`; CDU-15 → `CadMapa.vue`; Modais como `HistoricoAnaliseModal.vue` para históricos; `ImpactoMapaModal.vue` para CDU-12.
  - **Stores**: `processos.ts` (CDU-03), `subprocessos.ts` (detalhes/movimentações), `atividades.ts` (CRUD), `mapas.ts` (criação/validação), `notificacoes.ts` (alertas), `unidades.ts` (hierarquia/checkboxes).
  - **Mocks**: JSONs como `processos.json`, `atividades.json`, `mapas.json` simulam dados para testes.
  - **Testes**: E2E em `spec/cdu/` mapeiam diretamente (ex.: `cdu-01.spec.ts` para login).
  - **Gaps Potenciais**: Diagnóstico mencionado mas não detalhado (talvez em PDF); fluxos de e-mail reais ausentes (simulados via mocks); exportação de relatórios (mencionada em CDU-01) não aprofundada; integração com SGRH/API de acesso (simulada).

##### Resumo por Grupo de CDUs
1. **CDU-01: Login e Estrutura de Telas** (Todos os perfis)
   - Fluxo: Autenticação via título/senha (mock), determinação de perfis/unidades (múltiplos via seletor), exibição de Navbar (links: Painel, Minha Unidade, Relatórios, Histórico; configs para ADMIN), Rodapé (versão/desenvolvedor).
   - Validações: Credenciais inválidas → "Título ou senha inválidos".
   - Análise: Base para navegação; implementado em `Login.vue`, `Navbar.vue`. Suporte a localStorage para perfil persistido.

2. **CDU-02: Visualizar Painel** (Todos)
   - Seções: Processos Ativos (tabela filtrada por unidade/perfil, ordenável, clicável para detalhes), Alertas (tabela por data/processo, negrito para não lidos).
   - Regras: Só ADMIN vê 'Criado'; botão "Criar processo" para ADMIN.
   - Análise: Central para dashboard; usa `Painel.vue`, stores `processos.ts`/`alertas.ts`. Filtros por unidade subordinada evitam sobrecarga.

3. **CDU-03: Manter Processo** (ADMIN)
   - CRUD: Formulário com descrição, tipo (Mapeamento/Revisão/Diagnóstico), árvore de unidades (checkboxes com auto-seleção hierárquica), data limite.
   - Validações: Descrição/unidade obrigatórios; restrições por tipo (ex.: Revisão só com mapas vigentes).
   - Fluxos: Criação/Salvar → 'Criado'; Edição/Remoção só em 'Criado'; Iniciar → CDU-04/05.
   - Análise: Ênfase em árvore (`UnidadeTreeItem.vue`/`TreeTable.vue`); integra com `CadProcesso.vue`.

4. **CDU-04/05: Iniciar Processo (Mapeamento/Revisão)** (ADMIN)
   - Confirmação → Copia hierarquia unidades → Cria subprocessos ('Não iniciado', mapa vazio/cópia), movimentações ('Processo iniciado'), e-mails/alertas consolidados por nível hierárquico.
   - Diferenças: Mapeamento cria mapa vazio; Revisão copia mapa vigente.
   - Análise: Transição para 'Em andamento'; usa `stores/subprocessos.ts`/`mapas.ts`. E-mails mockados via `notificacoes.ts`.

5. **CDU-06/07: Detalhar Processo/Subprocesso** (ADMIN/GESTOR; CHEFE/SERVIDOR)
   - Processo: Dados + árvore unidades (situação/data limite); botões para finalizar/aceitar em bloco.
   - Subprocesso: Dados unidade (titular/responsável), movimentações (tabela), cards (Atividades, Mapa, Diagnóstico/Ocupações).
   - Análise: Hierárquico (`Processo.vue`/`Subprocesso.vue`); cards levam a views específicas. ADMIN edita datas/situações.

6. **CDU-08: Manter Cadastro Atividades/Conhecimentos** (CHEFE)
   - CRUD hierárquico (atividades → conhecimentos), importação de processos finalizados (filtro por duplicatas), auto-save.
   - Botão Impacto (para Revisão, → CDU-12).
   - Análise: `CadAtividades.vue`/`ImportarAtividadesModal.vue`; transita 'Não iniciado' → 'Cadastro em andamento'.

7. **CDU-09/10: Disponibilizar Cadastro/Revisão** (CHEFE)
   - Validação (conhecimentos obrigatórios), histórico análise (modal), confirmação → 'Disponibilizado', movimentação/notificação superior, alerta, limpa histórico.
   - Análise: Bloqueia edição; usa modais (`HistoricoAnaliseModal.vue`).

8. **CDU-11: Visualizar Cadastro** (Todos)
   - Tabela por atividade/conhecimentos (somente leitura).
   - Análise: `VisAtividades.vue`; habilitação por disponibilização.

9. **CDU-12: Verificar Impactos no Mapa** (CHEFE/GESTOR/ADMIN)
   - Compara cadastro vigente vs. subprocesso → Listas de inserções/impactos (modal com ícones).
   - Análise: `ImpactoMapaModal.vue`; crítico para Revisão, detecta mudanças em competências.

10. **CDU-13/14: Analisar Cadastro/Revisão** (GESTOR/ADMIN)
    - Histórico, devolução/aceite/homologação (modais com observações), movimentações/notificações.
    - Revisão: + Impactos; Homologação condicional (sem impacto → mantém mapa).
    - Análise: Fluxos de aprovação; transições como 'Disponibilizado' → 'Homologado'/'Em andamento'.

11. **CDU-15/16: Manter/Ajustar Mapa** (ADMIN)
    - Blocos competências (edição/exclusão, associações atividades, badges conhecimentos), botão Impactos (Revisão).
    - Análise: `CadMapa.vue`; auto-transição 'Homologado' → 'Mapa criado'; ajustes obrigam associação total.

12. **CDU-17: Disponibilizar Mapa** (ADMIN)
    - Validações (associações totais), modal (data limite/observações) → 'Disponibilizado', notificação/alerta, limpa histórico.
    - Análise: Transição etapa 2; e-mails para unidade/superiores.

13. **CDU-18: Visualizar Mapa** (Todos)
    - Blocos hierárquicos (competência → atividades → conhecimentos).
    - Análise: `VisMapa.vue`; habilitação pós-disponibilização.

14. **CDU-19: Validar Mapa** (CHEFE)
    - Histórico, sugestões (modal texto) ou validação direta → 'Com sugestões'/'Validado', notificação superior, movimentação.
    - Análise: `VisMapa.vue` com botões; transita etapa 2.

15. **CDU-20: Analisar Validação Mapa** (GESTOR/ADMIN)
    - Ver sugestões (modal), histórico, devolução/aceite/homologação.
    - Análise: Similar a CDU-13/14; 'Validado' → 'Homologado'.

16. **CDU-21: Finalizar Processo** (ADMIN)
    - Verifica homologação total → 'Finalizado', atualiza mapas vigentes, e-mails consolidados.
    - Análise: Fecha ciclo; integra `stores/mapas.ts` para vigência.

##### Análise do Diagrama (`estados-mapeamento.md`)
- **Foco**: Processo de Mapeamento, modelando subprocessos por unidade (UDP: Unidade de Detalhe do Processo?).
- **Estados Chave**: Cadastro (Em Andamento → Disponibilizado → Homologado) → Mapa (Criado → Disponibilizado → Validado/Com Sugestões → Homologado).
- **Transições**: Decisões para devoluções (para UDP/int), validações (int/SEDOC). Final em [*] (finalizado).
- **Pontos Fortes**: Visualiza loops hierárquicos; útil para validar implementações em `stores/subprocessos.ts`.
- **Limitações**: Não inclui Revisão (similar, mas com cópia mapa e impactos); Diagnóstico ausente. Sugestão: Expandir para diagrama unificado.

##### Cobertura e Recomendações
- **Alinhamento com Codebase**: Alta (~85%); CDUs mapeiam diretamente a views/stores (ex.: 21 CDUs → 21 specs em `spec/cdu/`). Testes E2E cobrem fluxos; unitários para lógica (ex.: validações em `__tests__/`).
- **Gaps Identificados**:
  - **Diagnóstico**: Mencionado em CDU-07 (cards), mas sem CDU dedicado; implementar fluxos semelhantes (avaliação equipe, ocupações críticas → `DiagnosticoEquipe.vue`/`OcupacoesCriticas.vue`).
  - **Relatórios/Histórico**: CDU-01 menciona, mas detalhes vagos (ex.: exportação em `Relatorios.vue`); adicionar CDU.
  - **Atribuições Temporárias**: Em CDU-01/07, mas fluxo em `CadAtribuicao.vue` precisa de CDU específico.
  - **E-mails/Alertas**: Simulados; para produção, integrar serviço real.
  - **PDF (`cdu-mapeamento-revisao.pdf`)**: Provavelmente detalha estados para Revisão; ler manualmente para validar diagrama.
- **Melhorias**:
  - **Traceabilidade**: Criar matriz CDU → Componente/Store/Teste em MD.
  - **Estados Unificados**: Expandir Mermaid para todos tipos de processo.
  - **Validações Avançadas**: Implementar regras complexas (ex.: auto-seleção árvore em CDU-03) com utils (`src/utils/`).
  - **Cobertura Testes**: Verificar se specs cobrem devoluções/loops; adicionar para gaps.
  - **UX**: Modais/diálogos padronizados (Bootstrap); breadcrumbs em `BarraNavegacao.vue` para fluxos longos.

Os requisitos são detalhados e hierárquicos, ideais para o protótipo. O sistema promove aprovação em cascata, garantindo qualidade via validações/notificações. Para implementação completa, priorize gaps como Diagnóstico e relatórios.