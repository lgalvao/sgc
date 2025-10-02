# Guia Definitivo de Testes E2E com Playwright

**Status: Atualizado e Consolidado.**

## 1. Filosofia e Arquitetura

O princípio fundamental dos nossos testes E2E é que cada arquivo de teste (`spec.ts`) deve ser uma **narrativa de usuário legível**, livre de detalhes técnicos. A complexidade, a lógica de interação e a robustez são encapsuladas em camadas de abstração (helpers).

Adotamos uma **arquitetura semântica em 3 camadas**:

1.  **Dados e Constantes (`/e2e/cdu/helpers/dados`):**
    *   É a fonte única da verdade para todos os dados estáticos da UI.
    *   Centraliza seletores (`data-testid`), textos visíveis (`TEXTOS`) e URLs (`URLS`).
    *   **Regra de Ouro:** Nenhuma "string mágica" deve existir fora deste diretório.

2.  **Helpers - A Linguagem de Domínio (`/e2e/cdu/helpers`):**
    *   Esta camada traduz as intenções de negócio em código executável. É onde a lógica e a complexidade dos testes residem.
    *   Encapsula **toda** a interação com o Playwright (`page.locator`, `expect`, etc.).
    *   **Estrutura:** `acoes/`, `verificacoes/`, `navegacao/` e `utils/`.

3.  **Especificações (`/e2e/cdu/*.spec.ts`):**
    *   Cada arquivo de teste representa um cenário de usuário, escrito de forma declarativa.
    *   **Regra de Ouro:** Specs são **proibidos** de conter qualquer lógica de controle (`if/else`, `try/catch`) ou chamadas diretas ao Playwright (`expect`, `page.locator`, `page.getBy...`). Eles apenas orquestram chamadas aos helpers.

## 2. Guia Prático: Criando um Novo Teste

1.  **Planejamento:**
    *   Identifique o fluxo do usuário (Pré-condição → Ação → Verificação).
    *   Liste os textos e seletores necessários. Se não existirem, adicione-os primeiro ao `constantes-teste.ts` e, se for um seletor novo, adicione o `data-testid` correspondente no código-fonte da aplicação.

2.  **Criação de Helpers (Ações e Verificações):**
    *   Crie funções semânticas nos diretórios `acoes/` ou `verificacoes/`. Ex: `registrarAceiteRevisao(page, observacao)`, `verificarCadastroDevolvidoComSucesso(page)`.
    *   Dentro do helper, utilize os utilitários de `utils/` para interagir com a página de forma robusta.

3.  **Reexportação nos Índices:**
    *   Após criar um helper, reexporte-o no `index.ts` do seu respectivo diretório (`acoes/`, `verificacoes/`) e, consequentemente, no `helpers/index.ts`.

4.  **Escrita do Spec:**
    *   Crie o arquivo `cdu-XX.spec.ts`.
    *   Use `test.describe()` para agrupar os testes e `test.beforeEach()` para a configuração inicial (ex: login).
    *   Escreva o teste chamando a sequência de helpers que narram a história do usuário.

## 3. Boas Práticas e Padrões

### Seletores e `data-testid`

- **Use `data-testid` sempre.** É o padrão do projeto para garantir testes resilientes.
- Todos os `data-testid`s devem ter uma constante correspondente no objeto `SELETORES`.

### Helpers e a Nova Camada de Utilitários

A complexidade de encontrar elementos foi abstraída em `refactoring-utils.ts`. **Não use mais `if/await/count` ou `try/catch` para encontrar elementos.**

- **`localizarElemento(locators: Locator[])`**: Recebe um array de possíveis seletores e retorna o primeiro que for encontrado.
- **`clicarElemento(locators: Locator[])`**: Encontra e clica no elemento, usando uma lista de seletores de fallback.
- **`preencherCampo(locators: Locator[], valor: string)`**: Encontra e preenche um campo, usando uma lista de seletores de fallback.

**Exemplo de Uso (a forma correta):**

```typescript
// Dentro de um helper de ação

// Forma ANTIGA e INCORRETA:
// if ((await page.getByTestId('btn-a').count()) > 0) {
//   await page.getByTestId('btn-a').click();
// } else {
//   await page.getByRole('button', { name: 'Texto B' }).click();
// }

// Forma NOVA e CORRETA:
await clicarElemento([
    page.getByTestId(SELETORES.BTN_A),
    page.getByRole('button', { name: TEXTOS.TEXTO_B })
]);
```

### Dados Mockados

- Entenda os mocks em `src/mocks/` antes de escrever asserções.
- Garanta que os mocks suportam o cenário de teste. Se um botão depende de uma condição, o mock deve simular essa condição.

## 4. Correção e Depuração de Testes

1.  **Reproduza Localmente:** Use `npx playwright test <arquivo> --headed` para ver o teste rodando.
2.  **Analise o Contexto:** O Playwright gera um diretório `test-results/`. Dentro dele, o arquivo `error-context.md` (ou similar) contém um snapshot do DOM no momento da falha, que é essencial para o diagnóstico.
3.  **Use `page.pause()`:** Se precisar inspecionar a página em um ponto específico, adicione `await page.pause()` no seu helper para pausar a execução e abrir o inspetor do Playwright.
4.  **Verifique as Camadas:**
    *   O seletor está errado? → `constantes-teste.ts`.
    *   A lógica de interação falhou? → Helper de `acoes/` ou `verificacoes/`.
    *   O fluxo está errado? → `spec.ts`.

## 5. Comandos Úteis

- **Rodar todos os testes:**
  ```bash
  npm run test:e2e
  ```
- **Rodar um arquivo específico:**
  ```bash
  npx playwright test e2e/cdu/cdu-08.spec.ts
  ```
- **Rodar o último teste que falhou:**
  ```bash
  npx playwright test --last-failed
  ```
- **Rodar em modo de depuração (headed):**
  ```bash
  npx playwright test --headed
  ```
