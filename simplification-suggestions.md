# Plano de Simplificação e Anti-Overengineering (SGC)

Este documento centraliza as tarefas pendentes e diretrizes arquiteturais para a simplificação contínua do projeto SGC. Como o sistema visa um ambiente de intranet (5 a 10 usuários simultâneos), o foco exclusivo é manutenibilidade, pragmatismo e entrega de valor.

## 🛡️ Princípios de Execução da Refatoração (Anti-Otimismo)

Historicamente, tentativas de simplificação falharam ou foram abandonadas no meio do caminho porque a complexidade real (regras de negócio escondidas, acoplamentos não triviais) foi subestimada. Para evitar isso, **todas as refatorações** devem seguir rigorosamente os princípios abaixo:

1. **Investigação Profunda Antes da Execução:** Nunca assumir que uma camada (ex: uma Facade ou Store) é "apenas pass-through" sem mapear 100% dos métodos e dependências. Se houver lógica de negócio escondida lá, o plano muda de "deletar" para "mover para o Service/Composable".
2. **Refatoração Incremental (Baby Steps):** Não tentar remover todas as Facades ou Stores de uma vez. A refatoração deve ser feita **Caso de Uso por Caso de Uso** ou **Endpoint por Endpoint**, garantindo que os Testes de Integração ou E2E continuem passando a cada micro-commit.
3. **Respeito ao Legado Estável (ROI de Refatoração):** Se um código é feio ou overengineered, mas funciona perfeitamente, tem alta cobertura de testes e raramente é alterado, **não mexa**. O esforço de simplificação deve focar primariamente nas áreas de código que atrapalham ativamente o desenvolvimento de novas features hoje.
4. **Fallback Rápido:** Se durante a execução for descoberto que a complexidade é muito maior que o estimado, a tarefa deve ser pausada, o plano reavaliado e a mudança revertida. Não forçar refatorações que quebram o design atual pela metade.

## 🔨 Tarefas Pendentes - Back-end

* **[ ] Remover Facades Remanescentes:**
  * **Ação:** Eliminar classes como `UsuarioFacade`, `LoginFacade`, `ProcessoFacade`, `AtividadeFacade`, `AlertaFacade`, `PainelFacade` e `RelatorioFacade`.
  * **Como:** Modificar os `Controllers` para injetarem e chamarem diretamente as dependências dos `Services`. Os Facades não agregam valor e atrasam a leitura do código agindo como pass-through.

* **[ ] Minimizar Múltiplas Transformações de DTO:**
  * **Ação:** Onde a segurança permitir, reduzir ou consolar DTOs.
  * **Como:** Para operações exclusivas de leitura, evitar o peso de várias camadas de Mapeadores. Se um Service pode retornar diretamente o formato adequado da visão para a tela listar sem expor dados internos sigilosos, utilize essa via rápida.

## 🔨 Tarefas Pendentes - Front-end

* **[ ] Remoção Progressiva dos Stores Pinia "Pass-through":**
  * **Ação:** Eliminar as stores do diretório `frontend/src/stores/` que não guardam estado global (`atividades.ts`, `mapas.ts`, `subprocessos.ts`, `processos.ts`, `usuarios.ts` etc.).
  * **Como:** Substituir nos componentes de view o uso dessas lojas por chamadas diretas aos Services usando estado local Reactivo. O Pinia agora deve ser destinado **só a variáveis globais imutáveis/compartilhadas**, como Autenticação, Tema da UI e Menu.

* **[ ] Limpar Componentes UI "Wrappers" Desnecessários:**
  * **Ação:** Identificar componentes criados pelo time que servem unicamente como ponte para componentes nativos, sem abstrair muita inteligência.
  * **Como:** Renderizar o componente ou biblioteca fundamental da UI nativamente na View, poupando uma camada visual inútil do Virtual DOM.

* **[ ] Migrar Lógica de Relatórios (Resquícios do Protótipo Vue) para o Back-end:**
  * **Ação:** O componente `RelatoriosView.vue` ainda possui dados *Mockados* (`diagnosticosGaps`) misturados com lógica pesada de filtragem por data (`isWithinInterval`) e tipo de processo no lado do cliente (`computed > processosFiltrados`).
  * **Como:** O Back-end deve prover um endpoint consolidado estritamente para relatórios (ex: `/api/relatorios/painel` aceitando `dataInicio` e `dataFim`), devolvendo os contadores prontos. O front-end não deve baixar listas completas e fazer `reduce`/`filter` na memória local.

* **[ ] Consolidar Variáveis de Permissão Globais do Frontend:**
  * **Ação:** O frontend utiliza propriedades como `perfilStore.isAdmin` ou `perfilStore.isGestor` para tomar decisões genéricas de UI (ex: exibir o botão "Criar Processo" no Menu ou no Painel).
  * **Como:** Embora o backend proteja a API, o frontend replica parte da semântica de RBAC para renderizar a interface. Devemos mover essas verificações para composables padronizadas (`useAcessoGlobal` ou similar) ou delegar a configuração do menu/dashboard inicial para o payload de login do backend (retornando as permissões de UI daquele usuário ativo), eliminando verificações locais de strings `"ADMIN"` ou `"GESTOR"` soltas por views como `PainelView.vue`.

## Impacto nos Testes (Mapeamento)
A simplificação arquitetural exigirá a adaptação das suítes de teste:

* **Backend (Remoção de Facades):**
  * Testes unitários dedicados às Facades (`UsuarioFacadeTest`, etc., se existirem) serão removidos.
  * Testes de Unidade dos **Controllers** precisarão ter seus mocks atualizados (injetando os `Services` ou `UseCases` diretamente, ao invés de `Facades`).
  * Testes de Integração (E2E/API) não devem sofrer impacto lógico, pois a interface da API (JSON/HTTP) não muda, apenas o roteamento interno do Controller para o Service.

* **Frontend (Remoção da Pinia "Pass-through"):**
  * Os **12 arquivos de teste** dentro de `frontend/src/stores/__tests__` (ex: `processos.spec.ts`, `atividades.spec.ts`) serão **deletados**, reduzindo a carga de manutenção de testes que apenas validavam repasse de dados.
  * Testes de Componentes (Views como `PainelView.spec.ts`) precisarão ser refatorados: onde antes se fazia o mock da dependência global da Pinia (`mockStore()`), passará a ser feito o mock da Composable de API ou da chamada de serviço correspondente.

* **Frontend (Refatoração de Relatórios):**
  * Testes para `RelatoriosView.spec.ts` deixarão de valer a lógica de filtro de data e mock local, focando exclusivamente na verificação de que o componente renderiza corretamente os dados fornecidos pelo novo endpoint do backend.
