# Diretório de Testes End-to-End (E2E)

Este diretório contém os testes _end-to-end_ (ponta a ponta) da aplicação, desenvolvidos com [Playwright](https://playwright.dev/).

## Objetivo

O principal objetivo dos testes E2E é simular o comportamento de um usuário real interagindo com a aplicação em um ambiente que se aproxima ao máximo do de produção. Eles verificam fluxos de trabalho completos, garantindo que a integração entre os componentes de frontend e as APIs de backend funciona como esperado.

## Estrutura

- **`**/*.spec.ts`**: Arquivos de suíte de testes. Cada arquivo agrupa testes para uma funcionalidade ou fluxo de usuário específico.
- **`fixtures.ts`**: Arquivo para configuração de _fixtures_ customizadas do Playwright, permitindo a criação de estados de teste reutilizáveis.
- **`mocks.ts`**: Definições de dados mockados específicos para os cenários de teste E2E.

## Como Executar

Para executar os testes E2E, utilize o seguinte comando a partir do diretório raiz do frontend:

```bash
npm run test:e2e
```

Os testes são configurados para rodar em um navegador _headless_ por padrão. Consulte o arquivo `playwright.config.ts` para mais opções de configuração.