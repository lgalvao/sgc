# Alinhamento CDU-16 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-16.md`.
- Teste E2E analisado: `e2e/cdu-16.spec.ts` (2 cenários `test`, 4 `test.step`, 73 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Cenários CDU-16: ADMIN ajusta mapa e visualiza impactos

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O usuário usa como base as informações de impactos mostradas nesta tela para alterar o mapa, podendo alterar descrições de competências, de atividades e de conhecimentos; remover ou criar novas competências; e ajustar a associação das atividades às competências do mapa, conforme descrito no caso de uso `Manter mapa de competências`. (palavras-chave do requisito: base, informações, impactos, mostradas)
- ⚠️ Quando concluir os ajustes, o usuário clica em `Disponibilizar` e o sistema segue o fluxo descrito no caso de uso `Disponibilizar mapa de competências`. (palavras-chave do requisito: concluir, ajustes, clica, disponibilizar)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
