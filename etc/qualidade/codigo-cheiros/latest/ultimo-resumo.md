# Auditoria de cheiros de codigo

Gerado em: 2026-05-03T21:54:26.074Z
Pontuacao: 1636 (critico)

## Contagens

| Sinal | Total | Delta | Peso |
|---|---:|---:|---:|
| Backend DTOs com @Nullable | 123 | 0 | 5 |
| Backend checks explicitos de null | 235 | 0 | 2 |
| Backend Objects.isNull/nonNull | 8 | 0 | 2 |
| Frontend producao com any explicito | 0 | 0 | 4 |
| Frontend testes com any explicito | 471 | 0 | 1 |
| Frontend catch tipado como any | 0 | 0 | 3 |
| Frontend checks explicitos de null | 23 | 0 | 2 |
| Frontend fallbacks defensivos com || | 18 | 0 | 1 |

## Hotspots

| Arquivo | Pontos | Sinais |
|---|---:|---|
| backend/src/main/java/sgc/alerta/dto/NotificacaoDto.java | 51 | backend_nullable_dto: 9, backend_null_checks: 3 |
| backend/src/main/java/sgc/organizacao/dto/UnidadeDto.java | 48 | backend_nullable_dto: 8, backend_null_checks: 4 |
| backend/src/main/java/sgc/subprocesso/dto/SubprocessoResumoDto.java | 36 | backend_nullable_dto: 6, backend_null_checks: 3 |
| backend/src/main/java/sgc/organizacao/ValidadorDadosOrganizacionais.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| backend/src/main/java/sgc/processo/service/ProcessoService.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| backend/src/main/java/sgc/subprocesso/dto/AtualizarSubprocessoRequest.java | 30 | backend_nullable_dto: 6 |
| backend/src/main/java/sgc/processo/dto/ProcessoDetalheDto.java | 29 | backend_nullable_dto: 5, backend_null_checks: 2 |
| backend/src/main/java/sgc/e2e/E2eController.java | 26 | backend_null_checks: 13 |
| backend/src/main/java/sgc/relatorio/RelatorioFacade.java | 26 | backend_null_checks: 11, backend_objects_null: 2 |
| frontend/src/stores/__tests__/subprocesso.spec.ts | 26 | frontend_any_testes: 26 |
| frontend/src/views/__tests__/ProcessoViewCoverage.spec.ts | 26 | frontend_any_testes: 26 |
| backend/src/main/java/sgc/alerta/dto/AlertaDto.java | 25 | backend_nullable_dto: 5 |
| backend/src/main/java/sgc/subprocesso/dto/SubprocessoListagemDto.java | 25 | backend_nullable_dto: 5 |
| frontend/src/components/__tests__/ArvoreUnidades.spec.ts | 25 | frontend_any_testes: 25 |
| backend/src/main/java/sgc/alerta/EnfileirarNotificacaoCommand.java | 20 | backend_nullable_dto: 4 |

## Escopos

- backend: 1101 ponto(s)
- frontend: 64 ponto(s)
- frontend_testes: 471 ponto(s)
