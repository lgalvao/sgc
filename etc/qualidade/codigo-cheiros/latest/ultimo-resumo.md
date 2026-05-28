# Auditoria de cheiros de codigo

Gerado em: 2026-05-28T13:57:35.182Z
Pontuacao: 1413 (critico)

## Contagens

| Sinal | Total | Delta | Peso |
|---|---:|---:|---:|
| Backend DTOs com @Nullable | 52 | +52 | 5 |
| Backend checks explicitos de null | 259 | +259 | 2 |
| Backend Objects.isNull/nonNull | 9 | +9 | 2 |
| Frontend producao com any explicito | 1 | +1 | 4 |
| Frontend testes com any explicito | 557 | +557 | 1 |
| Frontend catch tipado como any | 2 | +2 | 3 |
| Frontend checks explicitos de null | 18 | +18 | 2 |
| Frontend fallbacks defensivos com || | 14 | +14 | 1 |

## Hotspots

| Arquivo | Pontos | Sinais |
|---|---:|---|
| frontend/src/views/__tests__/CadProcesso.spec.ts | 54 | frontend_any_testes: 54 |
| backend/src/main/java/sgc/relatorio/RelatorioFacade.java | 42 | backend_null_checks: 20, backend_objects_null: 1 |
| frontend/src/views/__tests__/ProcessoView.spec.ts | 34 | frontend_any_testes: 34 |
| frontend/src/components/__tests__/ArvoreUnidades.spec.ts | 33 | frontend_any_testes: 33 |
| backend/src/main/java/sgc/organizacao/ValidadorDadosOrganizacionais.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| backend/src/main/java/sgc/processo/service/ProcessoService.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| frontend/src/stores/__tests__/subprocesso.spec.ts | 28 | frontend_any_testes: 28 |
| backend/src/main/java/sgc/e2e/E2eController.java | 26 | backend_null_checks: 13 |
| backend/src/main/java/sgc/alerta/EnfileirarNotificacaoCommand.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/AtualizarEstadoMapaCommand.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/AtualizarMapaRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/CriarMapaRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/feedback/FeedbackService.java | 18 | backend_null_checks: 9 |
| backend/src/main/java/sgc/organizacao/dto/UnidadeDto.java | 18 | backend_null_checks: 4, backend_nullable_dto: 2 |
| backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java | 18 | backend_null_checks: 9 |

## Escopos

- backend: 796 ponto(s)
- frontend: 60 ponto(s)
- frontend_testes: 557 ponto(s)
