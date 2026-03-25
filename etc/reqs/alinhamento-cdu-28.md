# Alinhamento CDU-28 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-28.md`.
- Teste E2E analisado: `e2e/cdu-28.spec.ts` (3 cenários `test`, 0 `test.step`, 60 linhas).

## Cobertura observada no E2E
- ✅ Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição
- ✅ Cenario 2: Campos obrigatórios devem ser validados
- ✅ Cenario 3: ADMIN cria atribuição temporária com sucesso

## Pontos do requisito sem evidência direta no E2E
- ⚠️ Sistema apresenta um modal com estes campos: (palavras-chave do requisito: apresenta, modal, estes)
- ⚠️ Dropdown pesquisável `Servidores` com os nomes dos servidores da unidade (palavras-chave do requisito: dropdown, pesquisável, servidores, nomes)
- ⚠️ Botões `Confirmar` e `Cancelar` (palavras-chave do requisito: botões, confirmar, cancelar)
- ⚠️ `Processo`: (Vazio) (palavras-chave do requisito: processo, vazio)
- ⚠️ `Usuário destino`: [USUARIO_SERVIDOR] (palavras-chave do requisito: destino, usuario_servidor)
- ⚠️ Ausência de cenário explícito para perfil CHEFE no E2E. (palavras-chave do requisito: chefe)
- ⚠️ Ausência de cenário explícito para perfil SERVIDOR no E2E. (palavras-chave do requisito: servidor)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
