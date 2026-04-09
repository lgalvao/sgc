# Auditoria de cheiros de codigo

Gerado em: 2026-04-08T23:51:44.741Z
Pontuacao: 1250 (critico)

## Contagens

| Sinal | Total | Delta | Peso |
|---|---:|---:|---:|
| Backend DTOs com @Nullable | 89 | 0 | 5 |
| Backend checks explicitos de null | 200 | 0 | 2 |
| Backend Objects.isNull/nonNull | 5 | 0 | 2 |
| Frontend producao com any explicito | 0 | 0 | 4 |
| Frontend testes com any explicito | 327 | 0 | 1 |
| Frontend catch tipado como any | 0 | 0 | 3 |
| Frontend checks explicitos de null | 22 | 0 | 2 |
| Frontend fallbacks defensivos com || | 24 | 0 | 1 |

## Hotspots

| Arquivo | Pontos | Sinais |
|---|---:|---|
| backend/src/main/java/sgc/organizacao/dto/UnidadeDto.java | 45 | backend_nullable_dto: 7, backend_null_checks: 5 |
| backend/src/main/java/sgc/processo/service/ProcessoService.java | 32 | backend_null_checks: 15, backend_objects_null: 1 |
| backend/src/main/java/sgc/e2e/E2eController.java | 30 | backend_null_checks: 15 |
| backend/src/main/java/sgc/organizacao/ValidadorDadosOrganizacionais.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| backend/src/main/java/sgc/processo/dto/ProcessoDetalheDto.java | 29 | backend_nullable_dto: 5, backend_null_checks: 2 |
| backend/src/main/java/sgc/subprocesso/dto/SubprocessoResumoDto.java | 29 | backend_nullable_dto: 5, backend_null_checks: 2 |
| backend/src/main/java/sgc/subprocesso/dto/SubprocessoListagemDto.java | 25 | backend_nullable_dto: 5 |
| backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java | 22 | backend_null_checks: 11 |
| backend/src/main/java/sgc/mapa/dto/AtualizarEstadoMapaCommand.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/AtualizarMapaRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/CriarMapaRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/MapaResumoDto.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/subprocesso/dto/AtualizarSubprocessoRequest.java | 20 | backend_nullable_dto: 4 |
| frontend/src/views/__tests__/ProcessoViewCoverage.spec.ts | 17 | frontend_any_testes: 17 |
| backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java | 16 | backend_null_checks: 8 |

## Escopos

- backend: 855 ponto(s)
- frontend: 68 ponto(s)
- frontend_testes: 327 ponto(s)
