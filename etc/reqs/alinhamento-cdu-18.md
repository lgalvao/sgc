# Alinhamento CDU-18 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-18.md`.
- Teste E2E analisado: `e2e/cdu-18.spec.ts` (2 cenários `test`, 9 `test.step`, 93 linhas).
- Contextos `describe` identificados: CDU-18: Visualizar mapa de competências.

## Cobertura observada no E2E
- ✅ Cenário 1: ADMIN visualiza mapa via detalhes do processo
- ✅ Cenário 2: CHEFE visualiza mapa da própria unidade

## Pontos do requisito sem evidência direta no E2E
- ⚠️ Ausência de cenário explícito para perfil SERVIDOR no E2E. (palavras-chave do requisito: servidor)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
