# Testes End-to-End (E2E)

Este diretório contém a suite de testes automatizados de ponta a ponta, implementada com **Playwright**.

## 🎯 Objetivo

Garantir que os fluxos críticos de negócio (Casos de Uso) funcionem corretamente integrando Frontend, Backend e Banco de Dados.

## 🏗️ Estrutura do Projeto

Seguimos uma arquitetura organizada para manter os testes legíveis e manuteníveis:

### 1. Specs (`*.spec.ts`)
Arquivos de teste declarativos. Descrevem **O QUE** está sendo testado, não **COMO**.
*   Focam na narrativa do usuário.
*   Não contêm seletores CSS ou lógica de espera direta (delegam para Helpers).
*   Mapeados 1:1 com os Casos de Uso (ex: `cdu-01.spec.ts`).

### 2. Helpers (`/helpers`)
Encapsulam a complexidade de automação e interações com a página.
*   **`helpers-auth.ts`**: Login e gestão de sessão.
*   **`helpers-processos.ts`**: Navegação e ações em processos.
*   **`helpers-mapas.ts`**: Manipulação de tabelas e formulários de competências.
*   **Abstração:** Métodos semânticos como `criarCompetencia(...)` em vez de manipulação direta de seletores nos testes.

### 3. Fixtures (`/fixtures`)
Define dados de teste e extensões do objeto `test` do Playwright.
*   **`base.ts`**: Extensão base do Playwright com configurações globais e listeners de log.
*   **`fixtures-processos.ts`**: Massa de dados para testes de processos.

### 4. Setup e Hooks (`/setup`, `/hooks`)
*   **`/setup`**: Scripts de inicialização global, como `seed.sql` e configuração inicial do ambiente.
*   **`/hooks`**: Lógica executada antes ou depois dos testes, como `hooks-limpeza.ts` para garantir um estado limpo.

## 🚀 Como Executar

### Pré-requisitos
*   Backend rodando em `http://localhost:10000` (perfil `e2e` recomendado para endpoints de reset).
*   Frontend rodando em `http://localhost:5173`.

### Comandos

```bash
# Instalar dependências
npm install

# Rodar todos os testes (Headless)
npm run test:e2e

# Rodar com interface gráfica (UI Mode)
npx playwright test --ui

# Rodar um arquivo específico
npx playwright test cdu-01.spec.ts
```

## 🛠️ Suporte no Backend

O backend possui um perfil específico (`e2e`) que habilita endpoints auxiliares para facilitar os testes:

*   **Reset de Banco:** `/e2e/reset-database` (Limpa e popula o banco antes dos testes).
*   **Fixtures:** `/e2e/fixtures/*` (Cria dados complexos via API para pular etapas repetitivas na UI).

Consulte `backend/src/main/java/sgc/e2e/README.md` para mais detalhes.

## 🤝 Padrões de Contribuição

*   **Seletores Resilientes:** Use sempre `data-testid="..."` em vez de classes CSS ou XPaths frágeis.
*   **Idempotência:** Cada teste deve ser independente. Use os hooks `beforeEach` para limpar/resetar o estado.
*   **Determinismo:** Evite `page.waitForTimeout()`. Use esperas explícitas por elementos ou respostas de rede.