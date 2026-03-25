# Alinhamento CDU-20 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-20.md`.
- Teste E2E analisado: `e2e/cdu-20.spec.ts` (13 cenários `test`, 0 `test.step`, 251 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Cenario 0: modal de aceite exibe campo opcional de observação
- ✅ Cenario 1: GESTOR SECRETARIA_1 analisa e aceita
- ✅ Cenario 2: ADMIN homologa final
- ✅ Setup data
- ✅ GESTOR visualiza botão "Ver sugestões" e acessa modal com conteúdo
- ✅ Setup data
- ✅ CHEFE apresenta sugestões e GESTOR registra aceite
- ✅ Setup data
- ✅ ADMIN não vê card de edição de mapa quando situação é Mapa com sugestões
- ✅ Setup data
- ✅ CHEFE apresenta sugestões e GESTOR registra aceite
- ✅ ... +1 cenários adicionais no mesmo arquivo

## Pontos do requisito sem evidência direta no E2E
- ⚠️ `Observação`: A observação caso tenha sido fornecida. (palavras-chave do requisito: observação, tenha, sido, fornecida)
- ⚠️ Descrição: "Cadastro da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes" (palavras-chave do requisito: descrição, cadastro, unidade, sigla_unidade_subprocesso)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
