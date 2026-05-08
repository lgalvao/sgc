# Relatório de Defensividade — SGC

> Atualizado em 2026-05-08 (UTC). Escopo: backend Java + frontend Vue/TypeScript.

## Diagnóstico atual

A avaliação anterior estava **direcionalmente correta**: o principal excesso de defensividade segue concentrado no frontend, sobretudo em pontos com `try/catch` repetitivos que apenas logam e devolvem fallback silencioso, ou duplicam tratamento em camadas próximas.

Nesta rodada, o foco foi reduzir:

- fallback silencioso em store crítica;
- fragmentação de tratamento de erro em composable;
- acoplamento indevido entre Vitest e Playwright no frontend unitário.

## Limpezas aplicadas nesta rodada

1. **Store de histórico sem swallow em falha de carga**
   - Arquivo: `frontend/src/stores/historico.ts`
   - `garantirDados()` deixou de capturar erro só para limpar lista.
   - Efeito: erro volta a propagar para o chamador; fluxo de retry e feedback fica centralizável no nível da view/composable.

2. **Vitest desacoplado de Playwright no frontend**
   - Arquivo: `frontend/vitest.config.ts`
   - Removido projeto `storybook` com execução em browser Playwright.
   - Mantido apenas projeto jsdom para testes unitários.

3. **Dependências pesadas removidas do frontend de testes unitários**
   - Arquivo: `frontend/package.json`
   - Removidas dependências: `@storybook/addon-vitest`, `@vitest/browser`, `@vitest/browser-playwright`, `@vitest/browser-preview`, `playwright`.

4. **Tipos Vitest simplificados**
   - Arquivo: `frontend/vitest.shims.d.ts`
   - Removida referência a `@vitest/browser-playwright`.

## Resultado prático

- Menos defensividade “silenciosa” em carregamento de dados.
- Menos fragmentação de error handling no fluxo de histórico (erro não é mais absorvido na store).
- Testes unitários mais leves e previsíveis (sem boot de provider browser/playwright).

## Próxima rodada ampla sugerida

1. Consolidar padrões repetidos de `try/catch + logger.error + notify(...)` em composables de mapa/cadastro com helper único por contexto.
2. Revisar stores com fallback implícito (`[]`, `null`, `false`) para converter em contrato explícito (propagar erro ou expor estado de erro tipado).
3. Remover catches que apenas relançam sem enriquecer contexto.
