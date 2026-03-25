# Alinhamento CDU-36 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-36.md`.
- Teste E2E analisado: `e2e/cdu-36.spec.ts` (1 cenários `test`, 0 `test.step`, 47 linhas).

## Cobertura observada no E2E
- ✅ Cenários CDU-36: ADMIN navega e gera relatórios de mapas

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O usuário acessa Relatórios na barra de navegacao. (palavras-chave do requisito: acessa, relatórios, barra, navegacao)
- ⚠️ O usuário define os filtros: (palavras-chave do requisito: define, filtros)
- ⚠️ Unidade (Opcional - se vazio, considera todas as unidades do processo) (palavras-chave do requisito: unidade, opcional, vazio, considera)
- ⚠️ Para cada competencia: (palavras-chave do requisito: competencia)
- ⚠️ Atividades da competencia (palavras-chave do requisito: atividades, competencia)
- ⚠️ Conhecimentos da atividade (palavras-chave do requisito: conhecimentos, atividade)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
