# Alinhamento CDU-31 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-31.md`.
- Teste E2E analisado: `e2e/cdu-31.spec.ts` (1 cenários `test`, 0 `test.step`, 52 linhas).

## Cobertura observada no E2E
- ✅ Cenários CDU-31: ADMIN navega, valida entradas e persiste alterações de configurações

## Pontos do requisito sem evidência direta no E2E
- ⚠️ Dias para inativação de processos (referenciado neste documento como DIAS_INATIVACAO_PROCESSO): Dias depois da (palavras-chave do requisito: dias, inativação, processos, referenciado)
- ⚠️ O sistema mostra mensagem de confirmação e guarda as configurações internamente. O efeito das configurações deve ser (palavras-chave do requisito: mensagem, confirmação, guarda, configurações)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
