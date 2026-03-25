# Alinhamento CDU-06 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-06.md`.
- Teste E2E analisado: `e2e/cdu-06.spec.ts` (3 cenários `test`, 0 `test.step`, 154 linhas).
- Contextos `describe` identificados: CDU-06 - Detalhar processo.

## Cobertura observada no E2E
- ✅ Fase 1: Deve exibir detalhes do processo para ADMIN e ações de unidade
- ✅ Fase 1b: Deve exibir detalhes do processo para GESTOR e ocultar ações ADMIN
- ✅ Fase 2: Verificar botões de ação em bloco [Step 2.2.2]

## Pontos do requisito sem evidência direta no E2E
- ⚠️ A tela será composta pelas seções Dados do processo e Unidades participantes. (palavras-chave do requisito: será, composta, pelas, processo)
- ⚠️ Seção unidades participantes: (palavras-chave do requisito: unidades, participantes)
- ⚠️ Subárvore das unidades hierarquicamente inferiores. (palavras-chave do requisito: subárvore, unidades, hierarquicamente, inferiores)
- ⚠️ Para cada unidade operacional e interoperacional da subárvore são exibidas, em linha, as informações da situação (palavras-chave do requisito: unidade, operacional, interoperacional, subárvore)
- ⚠️ O usuário poderá clicar nas unidades operacionais e interoperacionais para visualizar a tela Detalhes do (palavras-chave do requisito: poderá, unidades, operacionais, interoperacionais)
- ⚠️ Caso existam unidades subordinadas cujos subprocessos estejam localizados na unidade do usuário, os seguintes (palavras-chave do requisito: existam, unidades, subordinadas, cujos)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
