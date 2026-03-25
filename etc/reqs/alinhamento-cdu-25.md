# Alinhamento CDU-25 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-25.md`.
- Teste E2E analisado: `e2e/cdu-25.spec.ts` (1 cenários `test`, 4 `test.step`, 67 linhas).

## Cobertura observada no E2E
- ✅ Cenários CDU-25: Aceite em bloco de mapas validados

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O sistema identifica que existem unidades subordinadas com subprocessos elegíveis para aceite do mapa em bloco e se houver mostra o botão `Aceitar mapas em bloco`. (palavras-chave do requisito: identifica, existem, unidades, subordinadas)
- ⚠️ O sistema atua, para cada unidade selecionada, da seguinte forma: (palavras-chave do requisito: atua, unidade, selecionada, seguinte)
- ⚠️ Registra internamente uma análise de validação para o subprocesso: (palavras-chave do requisito: registra, internamente, análise, validação)
- ⚠️ `Data/hora`: [Data/hora atual] (palavras-chave do requisito: data, hora, atual)
- ⚠️ Registra internamente uma movimentação para o subprocesso: (palavras-chave do requisito: registra, internamente, movimentação, subprocesso)
- ⚠️ `Data/hora`: [Data/hora atual] (palavras-chave do requisito: data, hora, atual)
- ⚠️ Registra internamente um alerta: (palavras-chave do requisito: registra, internamente, alerta)
- ⚠️ `Data/hora`: [Data/hora atual] (palavras-chave do requisito: data, hora, atual)
- ⚠️ Envia notificação por e-mail para a unidade superior, com o modelo a seguir: (palavras-chave do requisito: envia, notificação, e-mail, unidade)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
