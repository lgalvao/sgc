# Tracking da refatoracao de validacao e feedback

Este arquivo registra apenas o estado atual da trilha de validacao/feedback. As diretrizes vivas ficam em [`plano-validacao.md`](plano-validacao.md) e [`ux.md`](ux.md).

## Estado geral

- **Fase atual:** trilha concluida; manter apenas vigilancia de regressao.
- **Ultima atualizacao:** 2026-04-24.
- **Diretriz vigente:** erro corrigivel aparece inline ou em `BAlert` contextual; `notify` fica para erro sistemico/integracional.

## Consolidado como concluido

- base compartilhada de validacao com `useValidacaoFormulario`;
- foco no primeiro erro invalido;
- contrato estruturado de erros (`erros` / `campo` / `mensagem`);
- convergencia dos fluxos de administracao, atribuicao temporaria, limpeza e relatorios;
- convergencia do cadastro de atividades;
- convergencia dos modais criticos de mapa, cadastro e subprocesso para evitar bloqueio preventivo silencioso;
- ajustes recentes em fluxos de mapa para manter o modal aberto em submit invalido quando ha campo obrigatorio.

## Em aberto

- Nenhuma pendencia aberta nesta trilha de validacao/feedback.

## Fora desta pilha

- o `401` espurio em `GET /api/subprocessos/contexto-edicao/buscar` foi separado desta trilha; segue como investigacao propria de sessao/rota.

## Validacoes de referencia desta rodada

- `npm run typecheck`
- `npx playwright test e2e/cdu-20.spec.ts e2e/cdu-27.spec.ts`
- `npm run test:unit --prefix frontend -- src/views/__tests__/MapaVisualizacaoView.spec.ts src/views/__tests__/VisMapa.spec.ts`
- `npm run typecheck --prefix frontend`

## Proximo passo recomendado

Manter o checklist do plano como criterio de revisao para novos ajustes e tratar separadamente o `401` espurio de `contexto-edicao`.
