# Auditoria de cheiros de codigo

Gerado em: 2026-05-15T23:46:02.008Z
Pontuacao: 1317 (critico)

## Contagens

| Sinal | Total | Delta | Peso |
|---|---:|---:|---:|
| Backend DTOs com @Nullable | 49 | 0 | 5 |
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
| frontend/src/views/__tests__/ProcessoView.spec.ts | 35 | frontend_any_testes: 35 |
| frontend/src/components/__tests__/ArvoreUnidades.spec.ts | 33 | frontend_any_testes: 33 |
| frontend/src/stores/__tests__/subprocesso.spec.ts | 31 | frontend_any_testes: 31 |
| backend/src/main/java/sgc/organizacao/ValidadorDadosOrganizacionais.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| backend/src/main/java/sgc/processo/service/ProcessoService.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| backend/src/main/java/sgc/e2e/E2eController.java | 26 | backend_null_checks: 13 |
| backend/src/main/java/sgc/relatorio/RelatorioFacade.java | 24 | backend_null_checks: 11, backend_objects_null: 1 |
| backend/src/main/java/sgc/alerta/EnfileirarNotificacaoCommand.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/AtualizarEstadoMapaCommand.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/AtualizarMapaRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/CriarMapaRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/feedback/FeedbackService.java | 18 | backend_null_checks: 9 |
| backend/src/main/java/sgc/organizacao/dto/UnidadeDto.java | 18 | backend_null_checks: 4, backend_nullable_dto: 2 |
| backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java | 18 | backend_null_checks: 9 |

## Escopos

- backend: 733 ponto(s)
- frontend: 76 ponto(s)
- frontend_testes: 508 ponto(s)
