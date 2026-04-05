# Auditoria de cheiros de codigo

Gerado em: 2026-04-05T13:47:59.250Z
Pontuacao: 1683 (critico)

## Contagens

| Sinal | Total | Delta | Peso |
|---|---:|---:|---:|
| Backend DTOs com @Nullable | 91 | 0 | 5 |
| Backend checks explicitos de null | 196 | 0 | 2 |
| Backend Objects.isNull/nonNull | 5 | 0 | 2 |
| Frontend producao com any explicito | 66 | 0 | 4 |
| Frontend testes com any explicito | 475 | -108 | 1 |
| Frontend catch tipado como any | 9 | 0 | 3 |
| Frontend checks explicitos de null | 16 | 0 | 2 |
| Frontend fallbacks defensivos com || | 28 | 0 | 1 |

## Hotspots

| Arquivo | Pontos | Sinais |
|---|---:|---|
| backend/src/main/java/sgc/subprocesso/dto/SubprocessoListagemDto.java | 39 | backend_nullable_dto: 7, backend_null_checks: 2 |
| backend/src/main/java/sgc/subprocesso/dto/SubprocessoResumoDto.java | 39 | backend_nullable_dto: 7, backend_null_checks: 2 |
| frontend/src/views/__tests__/AtividadesCadastroView.spec.ts | 37 | frontend_any_testes: 37 |
| backend/src/main/java/sgc/organizacao/dto/UnidadeDto.java | 36 | backend_nullable_dto: 6, backend_null_checks: 3 |
| backend/src/main/java/sgc/processo/service/ProcessoService.java | 32 | backend_null_checks: 15, backend_objects_null: 1 |
| frontend/src/views/__tests__/SubprocessoView.spec.ts | 31 | frontend_any_testes: 31 |
| backend/src/main/java/sgc/e2e/E2eController.java | 30 | backend_null_checks: 15 |
| backend/src/main/java/sgc/organizacao/ValidadorDadosOrganizacionais.java | 28 | backend_null_checks: 12, backend_objects_null: 2 |
| frontend/src/test-utils/uiHelpers.ts | 28 | frontend_any_producao: 7 |
| backend/src/main/java/sgc/processo/dto/ProcessoDetalheDto.java | 27 | backend_nullable_dto: 5, backend_null_checks: 1 |
| frontend/src/views/__tests__/CadastroVisualizacaoView.spec.ts | 26 | frontend_any_testes: 26 |
| frontend/src/views/ProcessoDetalheView.vue | 26 | frontend_any_producao: 4, frontend_fallback_or: 4, frontend_catch_any: 2 |
| frontend/src/utils/apiError.ts | 23 | frontend_any_producao: 5, frontend_null_checks: 1, frontend_fallback_or: 1 |
| frontend/src/views/__tests__/AtribuicaoTemporariaView.spec.ts | 21 | frontend_any_testes: 21 |
| frontend/src/views/ProcessoCadastroView.vue | 21 | frontend_any_producao: 4, frontend_fallback_or: 2, frontend_catch_any: 1 |

## Escopos

- backend: 857 ponto(s)
- frontend: 351 ponto(s)
- frontend_testes: 475 ponto(s)
