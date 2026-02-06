# Diretório de Test Utils

Utilitários e helpers para facilitar a escrita de testes unitários e de integração com Vitest.

## Utilitários Disponíveis

* **`mockFactories.ts`**: Fábricas de objetos para criar dados fake consistentes (ex: `criarProcessoMock()`).
* **`componentTestHelpers.ts`**: Wrappers em torno do Vue Test Utils para montar componentes com plugins (Pinia, Router) pré-configurados.
* **`storeTestHelpers.ts`**: Auxiliares para testar stores Pinia de forma isolada.
* **`serviceTestHelpers.ts`**: Utilitários para mockar chamadas Axios em testes de serviço.
* **`a11yTestHelpers.ts`**: Integração com `vitest-axe` para verificação de acessibilidade.
* **`uiHelpers.ts`**: Helpers para simular interações de interface (cliques, preenchimento de formulários).
* **`helpers.ts`**: Funções genéricas de apoio a testes.

## Uso Sugerido

Sempre utilize as `mockFactories` para garantir que as alterações nos tipos/interfaces sejam propagadas automaticamente para todos os testes.
