# Alinhamento CDU-27 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-27.md`.
- Teste E2E analisado: `e2e/cdu-27.spec.ts` (4 cenários `test`, 0 `test.step`, 115 linhas).

## Cobertura observada no E2E
- ✅ Setup data
- ✅ Cenario 1: ADMIN navega para detalhes do subprocesso
- ✅ Cenario 2: ADMIN pode cancelar a alteração da data limite sem persistir mudanças
- ✅ Cenario 3: ADMIN altera data limite e recebe confirmação

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O sistema cria internamente um alerta com as seguintes informações: (palavras-chave do requisito: cria, internamente, alerta, seguintes)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
