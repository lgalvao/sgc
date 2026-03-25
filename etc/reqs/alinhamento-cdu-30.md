# Alinhamento CDU-30 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-30.md`.
- Teste E2E analisado: `e2e/cdu-30.spec.ts` (3 cenários `test`, 0 `test.step`, 79 linhas).

## Cobertura observada no E2E
- ✅ Cenário 1: ADMIN navega para página de administradores e visualiza lista
- ✅ Cenário 2: ADMIN adiciona novo administrador
- ✅ Cenário 3: ADMIN remove administrador adicionado

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O usuário clica em Configurações (ícone de engrenagem) e escolhe `Administradores`. (palavras-chave do requisito: clica, configurações, ícone, engrenagem)
- ⚠️ O sistema apresenta opções para: (palavras-chave do requisito: apresenta, opções)
- ⚠️ O usuário informa o título eleitoral e clica em "Adicionar". (palavras-chave do requisito: informa, título, eleitoral, clica)
- ⚠️ **<<Início de fluxo de remoção de administrador>>** O usuário aciona o ícone de exclusão em um registro da lista. (palavras-chave do requisito: início, remoção, administrador, aciona)
- ⚠️ O sistema exibe um modal com título "Confirmar remoção" e a mensagem "Deseja realmente (palavras-chave do requisito: modal, título, confirmar, remoção)
- ⚠️ O sistema valida se a exclusão é permitida: (palavras-chave do requisito: valida, exclusão, permitida)
- ⚠️ Verifica se o usuário está tentando remover a si mesmo. (palavras-chave do requisito: verifica, está, tentando, remover)
- ⚠️ Se a validação falhar, o sistema exibe mensagem de erro correspondente. (palavras-chave do requisito: validação, falhar, mensagem, erro)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
