# AGENTS.md para o Projeto SGC

Este documento serve como um guia para agentes de codificação que trabalham no projeto Sistema de Gestão de
Competências (SGC) do TRE-PE. Ele fornece informações essenciais para entender a arquitetura,
as convenções e as ferramentas utilizadas.

## 1. Visão Geral do Projeto

O SGC é um protótipo desenvolvido em Vue 3 + Vite, com Vue Router, Bootstrap 5 e Pinia. Seu objetivo é simular os fluxos
de mapeamento, revisão e diagnóstico de competências das unidades do TRE-PE. Todos os dados são centralizados no
front-end, utilizando stores Pinia e arquivos JSON mockados.

**Princípios fundamentais:**

* **Protótipo:** Foco na funcionalidade da UX/UI, sem preocupações com validações complexas, segurança ou desempenho.
* **Dados centralizados:** Todos os dados são mockados em `src/mocks` e gerenciados por stores Pinia. Não há backend.
* **Perfis de usuários:** Determinados dinamicamente via `usePerfil` e persistidos no `localStorage`.

## 2. Estrutura de Diretórios

* `/src/components/`: Componentes Vue reutilizáveis.
* `/src/views/`: Páginas/rotas da aplicação.
* `/src/stores/`: Gerenciamento de estado com Pinia.
* `/src/mocks/`: Dados simulados em JSON.
* `/src/composables/`: Lógica reutilizável (ex: `usePerfil.ts`).
* `/spec/`: Testes End-to-End (E2E) com Playwright.
* `/docs/`: Documentação adicional.

## 3. Tecnologias utilizadas

* **Frontend:** Vue 3, Vue Router, Pinia, Bootstrap 5.
* **Build Tool:** Vite.
* **Linguagem:** TypeScript.
* **Testes Unitários:** Vitest, JSDOM, `@vue/test-utils`.
* **Testes E2E:** Playwright.

## 4. Dados e estado

Todos os dados do sistema são mockados em arquivos JSON localizados em `src/mocks/`. Esses dados são carregados e
gerenciados pelas stores Pinia (`src/stores/`). **Nunca acesse arquivos JSON diretamente nos componentes; sempre utilize
as stores Pinia.**

## 5. Perfis de usuários

O sistema possui os perfis `ADMIN`, `GESTOR`, `CHEFE` e `SERVIDOR`, cujas atribuições e acessos são baseados na
responsabilidade ou lotação em uma unidade. O `idServidor` do usuário logado é gerenciado pela store `perfil.ts` e
persistido no `localStorage`.

## 6. Testes

* **Execução via npm:**
    * Testes Unitários: `npm run test:unit`
    * Testes E2E: `npm run test:e2e`

### 6.1. Testes Unitários (Vitest)

* **Execução:** `npx vitest` ou `npm test`.
* **Localização:** Testes unitários estão localizados em `src/**/*.spec.ts` ou `src/**/*.test.ts`.
* **Boas Práticas:**
    * Use `describe` e `it` para organizar os testes.
    * Utilize `beforeEach` para configurar um ambiente limpo para cada teste.
    * **Mocks:**
        * **Vue Router:** Prefira instâncias reais (`createRouter`, `createWebHistory`). Espie métodos como
          `router.push`. Sempre aguarde `router.isReady()`.
        * **Pinia Stores:** Inicialize com `setActivePinia(createPinia())` em cada `beforeEach`. Resete o estado da
          store. Espie ações com `vi.spyOn(storeInstance, 'actionName`).
        * **`localStorage`/`sessionStorage`:** Mocke com `vi.spyOn` antes da criação da store.
    * **Assincronicidade:** Use `await wrapper.vm.$nextTick()` após interações que modificam o DOM.
    * **Isolamento:** Cada teste deve focar em uma única funcionalidade.
    * **Limpeza:** Limpe mocks e spies (`spy.mockClear()`, `vi.clearAllMocks()`) para evitar vazamento de estado.

### 6.2. Testes E2E (Playwright)

* **Execução:** Configurado via `playwright.config.ts`.
* **Localização:** Testes E2E estão no diretório `spec/`.
* **Boas Práticas:**
    * Cada arquivo `.spec.ts` testa uma funcionalidade específica.
    * Autenticação é feita via `utils/auth.ts`.
    * **Timeouts:** Mantenha timeouts curtos (máx. 5000ms) devido ao ambiente mockado.
    * **Depuração:** Verifique logs do console do navegador. Use `browser_navigate`, `browser_type`, `browser_click`
      para depuração direta.

## 7. Regras de Refatoração e Ferramentas

* **Entendimento do Contexto:** Sempre analise a arquitetura, padrões e dependências antes de refatorar.
* **Ferramenta `replace`:**
    * Extremamente sensível a espaços em branco e quebras de linha.
    * Sempre use `read_file` para obter o `old_string` exato.
    * Inclua contexto (3+ linhas antes e depois) para garantir unicidade.
    * Se falhar repetidamente, considere a abordagem de "ler, modificar em memória, escrever" com `write_file`.
* **Ferramenta `write_file`:** Ideal para criar novos arquivos ou sobrescrever conteúdo completo.

## 8. Regras de Código e Convenções

* Mantenha todo o código, comentários e dados em **português do Brasil**.
* Siga o padrão de navegação, componentização e UI/UX estabelecido.
* **Breadcrumbs:** Globais, com trilha especial para processos/unidades. O último breadcrumb não é link.
* **Botão "Voltar":** Global.
* **Contexto do Usuário:** Utilize `perfilStore.perfilSelecionado` e `perfilStore.unidadeSelecionada`.
* **Unidades do tipo `INTERMEDIARIA`:** Não devem ter `subprocessos` associados.

## 9. Endpoints Principais (Vue Router)

Veja `endpoints.md` para mais detalhes. Resumo:

* `/login`: `Login.vue`
* `/painel`: `Painel.vue`
* `/processo/:idProcesso`: `Processo.vue`
* `/processo/:idProcesso/:siglaUnidade`: `Subprocesso.vue`
* `/processo/:idProcesso/:siglaUnidade/mapa`: `CadMapa.vue`
* `/processo/:idProcesso/:siglaUnidade/cadastro`: `CadAtividades.vue`
* `/unidade/:siglaUnidade`: `Unidade.vue`
* `/unidade/:siglaUnidade/atribuicao`: `CadAtribuicao.vue`
* `/relatorios`: `Relatorios.vue`
* `/historico`: `HistoricoProcessos.vue`
* `/configuracoes`: `Configuracoes.vue`