# Alinhamento CDU-05 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-05.md`.
- Teste E2E analisado: `e2e/cdu-05.spec.ts` (14 cenários `test`, 0 `test.step`, 298 linhas).

## Cobertura observada no E2E
- ✅ Fase 1.1: ADMIN cria e inicia processo de Mapeamento
- ✅ Fase 1.2: CHEFE adiciona atividades e conhecimentos
- ✅ Fase 1.3: CHEFE disponibiliza cadastro
- ✅ Fase 1.3b: GESTOR da SECRETARIA_2 registra aceite
- ✅ Fase 1.4: ADMIN homologa cadastro
- ✅ Fase 1.5: ADMIN adiciona competências e disponibiliza mapa
- ✅ Fase 1.6: CHEFE valida mapa
- ✅ Fase 1.6b: GESTOR da SECRETARIA_2 aceita validação do mapa
- ✅ Fase 1.7: ADMIN homologa e finaliza processo
- ✅ Fase 2: Iniciar processo de Revisão
- ✅ Fase 2.1: Verificar alertas do processo de Revisão
- ✅ Fase 2.2: CHEFE acessa o cadastro editavel da Revisão pelo card do subprocesso
- ✅ ... +2 cenários adicionais no mesmo arquivo

## Pontos do requisito sem evidência direta no E2E
- ✅ Não foram encontradas lacunas textuais relevantes com a heurística aplicada; ainda assim recomenda-se validação manual funcional.

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
