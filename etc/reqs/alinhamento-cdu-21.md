# Alinhamento CDU-21 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-21.md`.
- Teste E2E analisado: `e2e/cdu-21.spec.ts` (5 cenários `test`, 0 `test.step`, 100 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Cenario 1: ADMIN navega para detalhes do processo
- ✅ Cenario 2: ADMIN cancela finalização - permanece na tela
- ✅ Cenario 3: ADMIN finaliza processo com sucesso
- ✅ Cenario 4: Verificar ausência de botões em processo finalizado

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O sistema define os mapas de competências dos subprocessos como os mapas de competências vigentes das respectivas unidades. (palavras-chave do requisito: define, mapas, competências, subprocessos)
- ⚠️ Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo: (palavras-chave do requisito: unidades, operacionais, interoperacionais, receber)
- ⚠️ Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das unidades operacionais e interoperacionais subordinadas a elas, segundo o modelo: (palavras-chave do requisito: unidades, intermediárias, interoperacionais, receber)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
