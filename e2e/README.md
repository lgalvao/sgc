# Testes End-to-End (E2E)

Este diretório contém a suite de testes automatizados de ponta a ponta, implementada com **Playwright 1.58.2**.

## 🎯 Objetivo

Garantir que os fluxos críticos de negócio (Casos de Uso) funcionem corretamente integrando Frontend, Backend e Banco de
Dados.

## 🏗️ Estrutura do Projeto

Seguimos uma arquitetura organizada para manter os testes legíveis e manuteníveis:

### 1. Specs (`*.spec.ts`)

Arquivos de teste declarativos. Descrevem **O QUE** está sendo testado, não **COMO**.

* Focam na narrativa do usuário.
* Não contêm seletores CSS ou lógica de espera direta (delegam para Helpers).
* Mapeados 1:1 com os Casos de Uso (ex: `cdu-01.spec.ts`).

### 2. Helpers (`/helpers`)

Encapsulam a complexidade de automação e interações com a página.

* **`helpers-auth.ts`**: Login e gestão de sessão.
* **`helpers-processos.ts`**: Navegação e ações em processos.
* **`helpers-mapas.ts`**: Manipulação de tabelas e formulários de competências.
* **Abstração:** Métodos semânticos como `criarCompetencia(...)` em vez de manipulação direta de seletores nos testes.

### 3. Fixtures (`/fixtures`)

Define dados de teste e extensões do objeto `test` do Playwright.

* **`base.ts`**: Extensão base do Playwright com configurações globais e listeners de log/erro.
* **`auth-fixtures.ts`**: ⭐ Fixtures de autenticação prontas para uso. Elimina duplicação de código de login.
* **`fixtures-processos.ts`**: Helpers para criação de processos via API (em desenvolvimento).

**💡 Uso de Auth Fixtures:**

```typescript
import {test, expect} from './fixtures/auth-fixtures';

// ❌ ANTES (duplicação)
test.beforeEach(async ({page}) => {
  await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
});

test('Deve criar processo', async ({page}) => {
  // teste...
});

// ✅ DEPOIS (usando fixture)
test('Deve criar processo', async ({page, autenticadoComoAdmin}) => {
  // Já está logado como ADMIN!
  // teste...
});
```

**Fixtures Disponíveis:**

* `autenticadoComoAdmin` - Admin único perfil (191919)
* `autenticadoComoGestor` - Gestor COORD_11 (222222)
* `autenticadoComoChefeSecao111` - Chefe Seção 111 (333333)
* `autenticadoComoChefeSecao211` - Chefe Seção 211 (101010)
* `autenticadoComoChefeSecao212` - Chefe Seção 212 (181818)
* `autenticadoComoChefeSecao221` - Chefe Seção 221 (141414)
* `autenticadoComoChefeAssessoria11` - Chefe Assessoria 11 (555555)
* E outras (veja `auth-fixtures.ts` para lista completa)

### 4. Setup e Hooks (`/setup`, `/hooks`)

* **`/setup`**: Scripts de inicialização global, como `seed.sql` e configuração inicial do ambiente.
* **`/hooks`**: Lógica executada antes ou depois dos testes, como `hooks-limpeza.ts` para garantir um estado limpo.

## 🚀 Como Executar

### Pré-requisitos

* Não é necessário subir frontend/backend manualmente para a suíte Playwright padrão.
* Os testes iniciam a infraestrutura E2E automaticamente via `playwright.config.ts` e `e2e/lifecycle.js`.
* Para paralelismo isolado, usamos frontend único e 1 backend por worker.

### Comandos

```bash
# Instalar dependências
npm install

# Rodar todos os testes (Headless)
npm run test:e2e

# Rodar com 2 workers isolados (configuração atualmente validada)
npm run test:e2e:2w

# Rodar captura de telas
npm run test:e2e:captura

# Rodar com interface gráfica (UI Mode)
npx playwright test --ui

# Rodar um arquivo específico
npx playwright test e2e/cdu-01.spec.ts
```

## 🛠️ Suporte no Backend

O backend possui um perfil específico (`e2e`) que habilita endpoints auxiliares para facilitar os testes:

* **Reset de Banco:** `/e2e/reset-database` (Limpa e popula o banco antes dos testes).
* **Fixtures:** `/e2e/fixtures/*` (Cria dados complexos via API para pular etapas repetitivas na UI).

Consulte `backend/src/main/java/sgc/e2e/README.md` para mais detalhes.

## ♻️ Isolamento Por Worker

Para paralelismo com isolamento de estado, a infraestrutura E2E suporta:

* 1 frontend único compartilhado.
* 1 backend por worker.
* Banco H2 em memória dedicado por worker (`sgc-e2e-w{index}`).
* Portas dedicadas por worker:
  * Backend: `10000 + workerIndex`
  * Frontend único: `5173`
* Configuração validada até aqui: `2 workers`.

## 🤝 Padrões de Contribuição

* **Seletores Resilientes:** Use sempre `data-testid="..."` em vez de classes CSS ou XPaths frágeis.
* **Idempotência:** Cada teste deve ser independente. Use os hooks `beforeEach` para limpar/resetar o estado.
* **Determinismo:** Evite `page.waitForTimeout()`. Use esperas explícitas por elementos ou respostas de rede.
* **Auth Fixtures:** Use sempre as fixtures de autenticação (`autenticadoComoAdmin`, etc.) em vez de chamar `login()`
  manualmente. Isso reduz duplicação e torna os testes mais legíveis.
