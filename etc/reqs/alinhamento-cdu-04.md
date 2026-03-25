# Alinhamento CDU-04 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-04.md`.
- Teste E2E: `e2e/cdu-04.spec.ts` (1 cenários `test`, 0 `test.step`).
- Contextos `describe`: CDU-04 - Iniciar processo.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **1**.
- Status: **0 cobertos**, **1 parciais**, **0 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- 🟡 **[PARCIAL]** 1. Fluxo principal não estruturado numericamente; validar leitura manual do requisito completo.
  - Palavras-chave usadas: `estruturado, numericamente, validar, leitura, manual, requisito`
  - Evidência (score 1): `e2e/cdu-04.spec.ts:22` -> `test('Deve iniciar um processo e validar criação de subprocessos e alertas', async ({`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **Fluxo principal não estruturado numericamente; validar leitura manual do requisito completo.** (atualmente parcial).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PENDENTE_REFINAMENTO_REQUISITO**.
- Motivos: requisito sem fluxo principal estruturado.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Completar cobertura do item: **Fluxo principal não estruturado numericamente; validar leitura manual do requisito completo.** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
