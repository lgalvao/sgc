# Alinhamento CDU-35 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-35.md`.
- Teste E2E analisado: `e2e/cdu-35.spec.ts` (1 cenários `test`, 0 `test.step`, 45 linhas).

## Cobertura observada no E2E
- ✅ Cenários CDU-35: ADMIN navega e gera relatórios de andamento

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O sistema exibe o relatório em tela contendo as seguintes colunas: (palavras-chave do requisito: relatório, contendo, seguintes, colunas)
- ⚠️ Data da última movimentação (palavras-chave do requisito: data, última, movimentação)
- ⚠️ Responsável (palavras-chave do requisito: responsável)
- ⚠️ Titular (Se não for o responsavel) (palavras-chave do requisito: titular, responsavel)
- ⚠️ O usuário pode optar por exportar os dados para PDF clicando no botao `PDF`. (palavras-chave do requisito: pode, optar, exportar, clicando)
- ⚠️ O sistema gera o arquivo selecionado e o disponibiliza para download. (palavras-chave do requisito: gera, arquivo, selecionado, disponibiliza)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
