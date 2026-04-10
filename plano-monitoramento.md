# Plano de monitoramento full stack

## Objetivo

Instrumentar frontend e backend para permitir diagnostico de desempenho ponta a ponta, com correlacao entre chamadas e ativacao sob demanda, sem deixar a instrumentacao pesada ligada por padrao.

## Diretrizes

- Manter baixo acoplamento e centralizar a logica de monitoramento.
- Deixar tudo desligado por padrao.
- Permitir ativacao via `e2e/lifecycle.js`.
- Permitir ativacao sob demanda no navegador para testes manuais e E2E.
- Correlacionar logs do frontend com a requisicao HTTP e com os spans internos do backend.

## Escopo inicial

### Frontend

- Instrumentar `frontend/src/axios-setup.ts`.
- Gerar `correlacaoId` por chamada monitorada.
- Medir duracao total percebida no navegador.
- Enviar headers `X-Correlacao-Id` e `X-Monitoramento-Ativo` quando aplicavel.
- Permitir ativacao por:
  - `VITE_MONITORAMENTO_MODO=full`
  - `sessionStorage.setItem('sgc.monitoramento.ativo', 'true')`
  - parametro de URL `?monitoramento=1`

### Backend

- Evoluir `MonitoramentoAspect` para respeitar chave `sgc.monitoramento.ativo`.
- Criar filtro HTTP para:
  - gerar/propagar `X-Correlacao-Id`
  - colocar correlacao em `MDC`
  - medir tempo total da requisicao
  - devolver `X-Tempo-Servidor-Ms` e `Server-Timing`
  - ativar log detalhado por header, trace completo ou amostragem
- Propagar o mesmo `traceId` nos erros quando houver correlacao ativa.

### Lifecycle E2E/manual

- Aproveitar `e2e/lifecycle.js` como ponto oficial para subir a aplicacao em modo diagnostico.
- Adicionar modos:
  - `off`
  - `lento`
  - `sob-demanda`
  - `full`
- Repassar configuracoes para backend e frontend via argumentos/variaveis de ambiente.

## Validacao

- Ajustar testes unitarios do `axios-setup`.
- Ajustar testes unitarios do `MonitoramentoAspect`.
- Adicionar teste do filtro HTTP de monitoramento.
- Executar ao menos:
  - `npm run test:unit -- --run frontend/src/__tests__/axios-setup.spec.ts`
  - `./gradlew :backend:test --tests sgc.comum.util.*`

## Proximos passos apos esta base

- Expor um atalho visual de diagnostico no frontend apenas para ambiente de desenvolvimento/E2E.
- Considerar instrumentacao SQL sob perfil de diagnostico.
- Avaliar integracao futura com Micrometer/OpenTelemetry se o nivel atual de logs nao bastar.
