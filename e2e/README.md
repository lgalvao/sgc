# Ferramentas E2E do SGC

Este workspace contém os testes Playwright do SGC e os wrappers específicos da auditoria de acessibilidade. Eles não
fazem parte da CLI reutilizável do toolkit.

```bash
npm --prefix e2e run acessibilidade:crawler
npm --prefix e2e run acessibilidade:processar
npm --prefix e2e run testes
npm --prefix e2e run typecheck
```

O crawler usa `e2e/a11y/crawler.spec.ts` e `e2e/playwright.config.ts`. Quando `SGC_RELATORIO_A11Y=sim`, o Playwright
produz `a11y-scan-results.json`; o processador grava o Markdown em `e2e/artefatos/acessibilidade/relatorio.md` por
padrão. Os caminhos podem ser substituídos por `--base`, `--entrada` e `--saida`.
