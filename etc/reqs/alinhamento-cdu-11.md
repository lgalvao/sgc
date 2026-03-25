# Alinhamento CDU-11 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-11.md`.
- Teste E2E analisado: `e2e/cdu-11.spec.ts` (5 cenários `test`, 0 `test.step`, 84 linhas).
- Contextos `describe` identificados: Em processo em Andamento, Em processo finalizado.

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Fluxo ADMIN/GESTOR: Navega via Detalhes do Processo (Passo 2)
- ✅ Fluxo CHEFE/SERVIDOR: Navega direto para Detalhes do Subprocesso (Passo 3)
- ✅ Setup data
- ✅ Fluxo ADMIN: Visualizar em processo finalizado

## Pontos do requisito sem evidência direta no E2E
- ✅ Não foram encontradas lacunas textuais relevantes com a heurística aplicada; ainda assim recomenda-se validação manual funcional.

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
