# Status de Simplificação

## Escopo atual

- **Área escolhida:** fluxo de disponibilização de cadastro/revisão em `Subprocesso`.
- **Objetivo do recorte:** remover regra de negócio da camada web e centralizar no service.

## Progresso

- [x] Investigação inicial do repositório e identificação de pontos de sobreengenharia.
- [x] Atualização de `simplification-suggestions.md` com diagnóstico contextualizado e backlog acionável.
- [x] Remoção da validação de atividades sem conhecimento do `SubprocessoController`.
- [x] Centralização da validação no `SubprocessoService` no fluxo de disponibilização.
- [x] Ajuste de testes de controller para refletir delegação ao service.
- [ ] Expandir simplificação para outros fluxos duplicados no módulo `subprocesso`.
- [ ] Planejar remoção incremental de facades pass-through priorizadas.

## Aprendizados

1. A principal fonte de complexidade local é a duplicação de regra entre Controller e Service.
2. O módulo `subprocesso` concentra grande parte da complexidade estrutural (controller e service muito extensos).
3. Simplificações pequenas e orientadas por fluxo reduzem risco e facilitam validação incremental.

## Validação executada nesta etapa

- `npm run lint` ✅
- `npm run typecheck` ✅
- `npm run test` (frontend unitário) ✅
- `./gradlew :backend:test` ⚠️ não executado no ambiente por incompatibilidade de JDK local (`invalid source release: 21`).
