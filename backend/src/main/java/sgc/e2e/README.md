# Pacote `e2e` do backend

## Visão geral

`sgc.e2e` fornece suporte para automação de testes de ponta a ponta quando o backend sobe com perfil `e2e`.

Esse pacote é isolado por perfil e não deve ser usado em produção.

## Ativação

- Perfil Spring: `e2e`
- Condição adicional no controller: `aplicacao.ambiente-testes=true`
- Banco obrigatório: **H2 em memória** (validado em runtime)

## Componentes principais

- `E2eController`: endpoints auxiliares para reset e fixtures.
- `E2eSecurityConfig`: libera `/e2e/**` e mantém autenticação para `/api/**`.
- `AtribuicaoTemporariaViewsE2eAspect`: sincroniza views auxiliares usadas em cenários de atribuição temporária.

## Endpoints de suporte (resumo)

Base: `/e2e`

- `POST /reset-database`
- `POST /processo/{codigo}/limpar`
- `POST /processo/{codigo}/limpar-completo`
- `POST /fixtures/*` (cenários auxiliares)

## Uso esperado

Consumido pela suíte Playwright em `/e2e` (raiz do repositório), especialmente durante preparação/limpeza de cenário.

## Segurança operacional

- Nunca habilitar perfil `e2e` fora de ambiente de teste.
- Não reutilizar endpoints de fixture em fluxos funcionais da aplicação.
