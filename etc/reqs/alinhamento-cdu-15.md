# Alinhamento CDU-15 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-15.md`.
- Teste E2E analisado: `e2e/cdu-15.spec.ts` (2 cenários `test`, 0 `test.step`, 100 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Cenários CDU-15: Fluxo completo de manutenção do mapa pelo ADMIN

## Pontos do requisito sem evidência direta no E2E
- ⚠️ Um bloco para cada competência criada, cujo título é a descrição da competência (palavras-chave do requisito: bloco, competência, criada, cujo)
- ⚠️ Botões `Cancelar` e `Salvar`. (palavras-chave do requisito: botões, cancelar, salvar)
- ⚠️ O usuário informa a descrição da competência que será criada, seleciona uma ou mais atividades para associar a ela e clica em `Salvar` para confirmar as mudanças. (palavras-chave do requisito: informa, descrição, competência, será)
- ⚠️ O usuário altera a descrição ou a associação com as atividades e clica no botão `Salvar`. (palavras-chave do requisito: altera, descrição, associação, atividades)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
