# Auditoria de cheiros de codigo

Gerado em: 2026-04-05T15:43:49.619Z
Pontuacao: 1355 (critico)

## Contagens

| Sinal | Total | Delta | Peso |
|---|---:|---:|---:|
| Backend DTOs com @Nullable | 82 | +2 | 5 |
| Backend checks explicitos de null | 195 | +1 | 2 |
| Backend Objects.isNull/nonNull | 5 | 0 | 2 |
| Frontend producao com any explicito | 38 | 0 | 4 |
| Frontend testes com any explicito | 319 | 0 | 1 |
| Frontend catch tipado como any | 4 | 0 | 3 |
| Frontend checks explicitos de null | 19 | 0 | 2 |
| Frontend fallbacks defensivos com || | 24 | 0 | 1 |

## Hotspots

| Arquivo | Pontos | Sinais |
|---|---:|---|
| backend/src/main/java/sgc/organizacao/dto/UnidadeDto.java | 45 | backend_nullable_dto: 7, backend_null_checks: 5 |
| backend/src/main/java/sgc/e2e/E2eController.java | 30 | backend_null_checks: 15 |
| backend/src/main/java/sgc/processo/service/ProcessoService.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| backend/src/main/java/sgc/processo/dto/ProcessoDetalheDto.java | 29 | backend_nullable_dto: 5, backend_null_checks: 2 |
| backend/src/main/java/sgc/subprocesso/dto/SubprocessoResumoDto.java | 29 | backend_nullable_dto: 5, backend_null_checks: 2 |
| backend/src/main/java/sgc/organizacao/ValidadorDadosOrganizacionais.java | 28 | backend_null_checks: 12, backend_objects_null: 2 |
| backend/src/main/java/sgc/subprocesso/dto/SubprocessoListagemDto.java | 25 | backend_nullable_dto: 5 |
| backend/src/main/java/sgc/mapa/dto/AtualizarMapaRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/CriarMapaRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/MapaResumoDto.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/subprocesso/dto/AtualizarSubprocessoRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java | 20 | backend_null_checks: 10 |
| frontend/src/views/__tests__/ProcessoViewCoverage.spec.ts | 17 | frontend_any_testes: 17 |
| backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java | 16 | backend_null_checks: 8 |
| frontend/src/test-utils/serviceTestHelpers.ts | 16 | frontend_any_producao: 4 |

## Escopos

- backend: 810 ponto(s)
- frontend: 226 ponto(s)
- frontend_testes: 319 ponto(s)
