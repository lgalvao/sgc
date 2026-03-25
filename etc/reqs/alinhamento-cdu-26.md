# Alinhamento CDU-26 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-26.md`.
- Teste E2E analisado: `e2e/cdu-26.spec.ts` (5 cenários `test`, 0 `test.step`, 101 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Cenario 1: ADMIN visualiza botão Homologar mapas em bloco
- ✅ Cenario 2: ADMIN abre modal de homologação de mapa em bloco
- ✅ Cenario 3: Cancelar homologação de mapa em bloco
- ✅ Cenario 4: ADMIN confirma homologação em bloco e é redirecionado ao painel

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O sistema atua, para cada unidade selecionada, da seguinte forma: (palavras-chave do requisito: atua, unidade, selecionada, seguinte)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
