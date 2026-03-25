# Alinhamento CDU-33 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-33.md`.
- Teste E2E analisado: `e2e/cdu-33.spec.ts` (2 cenários `test`, 0 `test.step`, 91 linhas).

## Cobertura observada no E2E
- ✅ Setup UI
- ✅ Cenários CDU-33: ADMIN reabre revisão de cadastro

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O sistema altera a situação do subprocesso para `REVISAO_CADASTRO_EM_ANDAMENTO`. (palavras-chave do requisito: altera, situação, subprocesso, revisao_cadastro_em_andamento)
- ⚠️ `Data/hora`: Data/hora atual (palavras-chave do requisito: data, hora, atual)
- ⚠️ O sistema envia notificações por e-mail para a unidade solicitante e unidades superiores. (palavras-chave do requisito: envia, notificações, e-mail, unidade)
- ⚠️ Para a unidade solicitante (operacional/interoperacional): (palavras-chave do requisito: unidade, solicitante, operacional, interoperacional)
- ⚠️ Para as unidades superiores: (palavras-chave do requisito: unidades, superiores)
- ⚠️ `Data/hora`: Data/hora atual (palavras-chave do requisito: data, hora, atual)
- ⚠️ Para as unidades superiores: (palavras-chave do requisito: unidades, superiores)
- ⚠️ `Data/hora`: Data/hora atual (palavras-chave do requisito: data, hora, atual)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
