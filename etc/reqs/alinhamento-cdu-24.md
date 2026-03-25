# Alinhamento CDU-24 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-24.md`.
- Teste E2E analisado: `e2e/cdu-24.spec.ts` (3 cenários `test`, 0 `test.step`, 72 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ ADMIN mantém botão disponibilizar desabilitado enquanto existir atividade sem competência
- ✅ ADMIN disponibiliza mapas em bloco após associar todas as atividades

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O sistema identifica que existem unidades com subprocessos com mapas criados ou ajustados mas ainda não (palavras-chave do requisito: identifica, existem, unidades, subprocessos)
- ⚠️ Na seção de unidades participantes, abaixo da árvore de unidades, o sistema mostra o botão (palavras-chave do requisito: unidades, participantes, abaixo, árvore)
- ⚠️ O sistema abre modal de confirmação, com os elementos a seguir: (palavras-chave do requisito: abre, modal, confirmação, elementos)
- ⚠️ Lista das unidades operacionais ou interoperacionais cujos mapas de competências poderão ser disponibilizados, (palavras-chave do requisito: lista, unidades, operacionais, interoperacionais)
- ⚠️ Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela Detalhes do (palavras-chave do requisito: escolha, botão, cancelar, interrompe)
- ⚠️ Caso negativo, o sistema interrompe a operação de disponibilização em bloco, permanece na tela Detalhes do processo e (palavras-chave do requisito: negativo, interrompe, operação, disponibilização)
- ⚠️ Caso positivo, o sistema atua, para cada unidade selecionada, da seguinte forma: (palavras-chave do requisito: positivo, atua, unidade, selecionada)
- ⚠️ O sistema agrupa as unidades selecionadas com suas unidades superiores em todos os níveis da hierarquia, (palavras-chave do requisito: agrupa, unidades, selecionadas, superiores)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
