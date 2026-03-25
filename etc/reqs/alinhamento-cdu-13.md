# Alinhamento CDU-13 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-13.md`.
- Teste E2E analisado: `e2e/cdu-13.spec.ts` (2 cenários `test`, 0 `test.step`, 131 linhas).

## Cobertura observada no E2E
- ✅ 1. Setup: Preparar processo e devoluções iniciais
- ✅ Cenarios CDU-13: Hierarquia aceita e ADMIN homologa

## Pontos do requisito sem evidência direta no E2E
- ⚠️ `Histórico de análise` (palavras-chave do requisito: histórico, análise)
- ⚠️ Se o usuário clicar no botão `Histórico de análise`, o sistema mostra, em tela modal, os dados das análises prévias (palavras-chave do requisito: botão, histórico, análise, modal)
- ⚠️ Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de devolução do cadastro, permanecendo na (palavras-chave do requisito: escolha, cancelar, interrompe, operação)
- ⚠️ O usuário opcionalmente informa a observação e escolhe `Confirmar`. (palavras-chave do requisito: opcionalmente, informa, observação, escolhe)
- ⚠️ `Data/hora`: Data/hora atual (palavras-chave do requisito: data, hora, atual)
- ⚠️ `Observação`: A observação da janela modal, caso tenha sido fornecida. (palavras-chave do requisito: observação, janela, modal, tenha)
- ⚠️ O sistema identifica a unidade de devolução como sendo a unidade de origem da última movimentação do (palavras-chave do requisito: identifica, unidade, devolução, sendo)
- ⚠️ `Data/hora`: Data/hora atual (palavras-chave do requisito: data, hora, atual)
- ⚠️ O sistema envia notificação por e-mail para a unidade de devolução: (palavras-chave do requisito: envia, notificação, e-mail, unidade)
- ⚠️ `Data/hora`: Data/hora atual (palavras-chave do requisito: data, hora, atual)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
