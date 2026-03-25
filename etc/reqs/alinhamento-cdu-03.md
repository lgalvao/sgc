# Alinhamento CDU-03 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-03.md`.
- Teste E2E analisado: `e2e/cdu-03.spec.ts` (9 cenários `test`, 0 `test.step`, 280 linhas).
- Contextos `describe` identificados: CDU-03 - Manter processo.

## Cobertura observada no E2E
- ✅ Deve validar campos obrigatórios e estados dos botões
- ✅ Deve permitir selecionar raiz interoperacional independentemente das subordinadas
- ✅ Deve editar um processo existente
- ✅ Deve remover um processo
- ✅ Deve validar regras de seleção em cascata na árvore de unidades
- ✅ Deve avaliar unidades ocupadas por processos em andamento e restringi-las
- ✅ Deve validar restrições de unidades sem mapa para REVISAO e DIAGNOSTICO
- ✅ Deve validar fluxos de cancelamento e mensagens de feedback
- ✅ Deve validar fluxo alternativo (Botão iniciar invés de Salvar)

## Pontos do requisito sem evidência direta no E2E
- ⚠️ A lista de unidades **deve deixar desativadas** (não selecionáveis) as unidades que já estejam participando de (palavras-chave do requisito: lista, unidades, deixar, desativadas)
- ⚠️ Em caso de processos dos tipos 'Revisão' ou 'Diagnóstico', só poderão ser selecionadas unidades com mapas de (palavras-chave do requisito: processos, tipos, revisão, diagnóstico)
- ⚠️ O sistema valida os dados depois de editados, de acordo com as mesmas regras aplicadas no momento do primeiro (palavras-chave do requisito: valida, depois, editados, acordo)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
