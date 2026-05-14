# Auditoria de cheiros de codigo

Gerado em: 2026-05-14T23:14:40.505Z
Pontuacao: 1617 (critico)

## Contagens

| Sinal | Total | Delta | Peso |
|---|---:|---:|---:|
| Backend DTOs com @Nullable | 109 | 0 | 5 |
| Backend checks explicitos de null | 235 | 0 | 2 |
| Backend Objects.isNull/nonNull | 9 | 0 | 2 |
| Frontend producao com any explicito | 0 | 0 | 4 |
| Frontend testes com any explicito | 508 | 0 | 1 |
| Frontend catch tipado como any | 0 | 0 | 3 |
| Frontend checks explicitos de null | 28 | 0 | 2 |
| Frontend fallbacks defensivos com || | 20 | 0 | 1 |

## Hotspots

| Arquivo | Pontos | Sinais |
|---|---:|---|
| frontend/src/views/__tests__/CadProcesso.spec.ts | 44 | frontend_any_testes: 44 |
| backend/src/main/java/sgc/subprocesso/dto/SubprocessoResumoDto.java | 36 | backend_nullable_dto: 6, backend_null_checks: 3 |
| frontend/src/views/__tests__/ProcessoView.spec.ts | 35 | frontend_any_testes: 35 |
| frontend/src/components/__tests__/ArvoreUnidades.spec.ts | 33 | frontend_any_testes: 33 |
| frontend/src/stores/__tests__/subprocesso.spec.ts | 31 | frontend_any_testes: 31 |
| backend/src/main/java/sgc/organizacao/ValidadorDadosOrganizacionais.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| backend/src/main/java/sgc/processo/service/ProcessoService.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| backend/src/main/java/sgc/subprocesso/dto/AtualizarSubprocessoRequest.java | 30 | backend_nullable_dto: 6 |
| backend/src/main/java/sgc/processo/dto/ProcessoDetalheDto.java | 29 | backend_nullable_dto: 5, backend_null_checks: 2 |
| backend/src/main/java/sgc/e2e/E2eController.java | 26 | backend_null_checks: 13 |
| backend/src/main/java/sgc/alerta/dto/AlertaDto.java | 25 | backend_nullable_dto: 5 |
| backend/src/main/java/sgc/subprocesso/dto/SubprocessoListagemDto.java | 25 | backend_nullable_dto: 5 |
| backend/src/main/java/sgc/relatorio/RelatorioFacade.java | 24 | backend_null_checks: 11, backend_objects_null: 1 |
| backend/src/main/java/sgc/alerta/EnfileirarNotificacaoCommand.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/AtualizarEstadoMapaCommand.java | 20 | backend_nullable_dto: 4 |

## Escopos

- backend: 1033 ponto(s)
- frontend: 76 ponto(s)
- frontend_testes: 508 ponto(s)
