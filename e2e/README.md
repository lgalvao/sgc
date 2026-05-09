# Testes End-to-End (E2E)

## Visão geral

A suíte E2E usa **Playwright** para validar fluxos completos do SGC, integrando frontend e backend.

Os testes ficam neste diretório e são executados com infraestrutura iniciada por `e2e/lifecycle.js`.

## Estrutura

- `cdu-*.spec.ts`: cenários por caso de uso.
- `helpers/`: abstrações de interação.
- `fixtures/`: fixtures de autenticação e dados.
- `hooks/`: limpeza e preparação.
- `setup/`: artefatos de banco e configuração.

## Execução

Da raiz do repositório:

```bash
npm run test:e2e
```

Comandos úteis adicionais:

```bash
pnpm exec playwright test --ui
pnpm exec playwright test e2e/cdu-01.spec.ts
```

## Lifecycle local

Para subir frontend + backend fora da execução do Playwright:

```bash
node e2e/lifecycle.js
```

Variáveis úteis:

- `SGC_PERFIL=e2e|hom`
- `SGC_MONITORAMENTO=sim|nao`
- `SGC_LIFECYCLE_REUTILIZAR_EXISTENTE=on|off`
- `E2E_BACKEND_BASE_PORT`
- `E2E_FRONTEND_PORT`

## Perfil `hom`

`SGC_PERFIL=hom` sobe a aplicação para homologação sem os endpoints auxiliares `/e2e/*`.

A suíte padrão não deve rodar nesse modo, porque depende de reset/fixtures do perfil `e2e`.

## Boas práticas

- Preferir `data-testid` para seletores.
- Evitar `waitForTimeout`.
- Manter testes idempotentes.
- Usar fixtures de autenticação para reduzir duplicação.

## Referências

- [README raiz](../README.md)
- [Suporte E2E no backend](../backend/src/main/java/sgc/e2e/README.md)
- [Regras E2E](../etc/docs/regras-e2e.md)
