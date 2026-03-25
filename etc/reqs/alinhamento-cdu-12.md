# Alinhamento CDU-12 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-12.md`.
- Teste E2E analisado: `e2e/cdu-12.spec.ts` (3 cenários `test`, 0 `test.step`, 86 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Passo 3.1: Verificação pelo CHEFE na tela de Cadastro
- ✅ Passo 3.2: Verificação pelo GESTOR na tela de Visualização

## Pontos do requisito sem evidência direta no E2E
- ✅ Não foram encontradas lacunas textuais relevantes com a heurística aplicada; ainda assim recomenda-se validação manual funcional.

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
