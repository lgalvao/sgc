# Testes End-to-End (E2E)

Este diretório contém a suite de testes automatizados de ponta a ponta, implementada com **Playwright**.

## 🎯 Objetivo

Garantir que os fluxos críticos de negócio (Casos de Uso) funcionem corretamente integrando Frontend, Backend e Banco de Dados.

## 🏗️ Estrutura do Projeto

Seguimos uma arquitetura em 3 camadas para manter os testes legíveis e manuteníveis:

### 1. Specs (`*.spec.ts`)
Arquivos de teste declarativos. Descrevem **O QUE** está sendo testado, não **COMO**.
*   Focam na narrativa do usuário.
*   Não contêm seletores CSS ou lógica de espera direta (delegam para Helpers).
*   Mapeados 1:1 com os Casos de Uso (ex: `cdu-01.spec.ts`).

### 2. Helpers (`/helpers`)
Encapsulam a complexidade de automação e interações com a página.
*   **`LoginHelper`**: Realiza login, seleção de perfil e logout.
*   **`ProcessoHelper`**: Navegação e ações em processos.
*   **`MapaHelper`**: Manipulação de tabelas e formulários de competências.
*   **Abstração:** Métodos semânticos como `criarProcesso(...)` em vez de `click('#btn-criar')`.

### 3. Dados (`/dados`)
Centralizam constantes e seletores.
*   **`constantes-teste.ts`**: Seletores `data-testid`, mensagens de erro esperadas, URLs.
*   Evita "magic strings" espalhadas pelos testes.

## 🚀 Como Executar

### Pré-requisitos
*   Backend rodando em `http://localhost:10000` (perfil `e2e` recomendado para endpoints de reset).
*   Frontend rodando em `http://localhost:5173`.

### Comandos

```bash
# Instalar dependências (na raiz do projeto ou dentro de e2e se for separado)
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
