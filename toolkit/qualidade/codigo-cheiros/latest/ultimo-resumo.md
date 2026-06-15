# Auditoria de cheiros de codigo

Gerado em: 2026-06-06T12:15:24.747Z
Pontuacao: 1541 (critico)

## Contagens

| Sinal | Total | Delta | Peso |
|---|---:|---:|---:|
| Backend DTOs com @Nullable | 50 | -2 | 5 |
| Backend checks explicitos de null | 302 | +70 | 2 |
| Backend Objects.isNull/nonNull | 10 | 0 | 2 |
| Frontend producao com any explicito | 0 | -1 | 4 |
| Frontend testes com any explicito | 586 | +6 | 1 |
| Frontend catch tipado como any | 2 | 0 | 3 |
| Frontend checks explicitos de null | 29 | +9 | 2 |
| Frontend fallbacks defensivos com || | 17 | +3 | 1 |

## Hotspots

| Arquivo | Pontos | Sinais |
|---|---:|---|
| frontend/src/views/__tests__/CadProcesso.spec.ts | 54 | frontend_any_testes: 54 |
| backend/src/main/java/sgc/e2e/E2eController.java | 44 | backend_null_checks: 22 |
| frontend/src/views/__tests__/ProcessoView.spec.ts | 34 | frontend_any_testes: 34 |
| frontend/src/components/__tests__/ArvoreUnidades.spec.ts | 33 | frontend_any_testes: 33 |
| backend/src/main/java/sgc/relatorio/RelatorioService.java | 32 | backend_null_checks: 15, backend_objects_null: 1 |
| backend/src/main/java/sgc/organizacao/ValidadorDadosOrganizacionais.java | 30 | backend_null_checks: 14, backend_objects_null: 1 |
| frontend/src/stores/__tests__/subprocesso.spec.ts | 28 | frontend_any_testes: 28 |
| backend/src/main/java/sgc/processo/service/ProcessoService.java | 24 | backend_null_checks: 11, backend_objects_null: 1 |
| backend/src/main/java/sgc/alerta/EnfileirarNotificacaoCommand.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/AtualizarEstadoMapaCommand.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/AtualizarMapaRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/mapa/dto/CriarMapaRequest.java | 20 | backend_nullable_dto: 4 |
| backend/src/main/java/sgc/processo/painel/PainelService.java | 20 | backend_null_checks: 10 |
| backend/src/main/java/sgc/feedback/FeedbackService.java | 18 | backend_null_checks: 9 |
| backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java | 18 | backend_null_checks: 9 |

## Escopos

- backend: 874 ponto(s)
- frontend: 81 ponto(s)
- frontend_testes: 586 ponto(s)
