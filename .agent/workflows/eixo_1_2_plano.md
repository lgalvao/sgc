---
description: [Eixo 1.2: Plano de Ação para Blindagem e Auditação de Access Control]
---
# Plano de Ação - Eixo 1.2: Acesso/Segurança em Serviços Core

## Contexto e Objetivo
As validações de controle de acesso (`accessControlService.verificarPermissao()`) encontram-se espalhadas no código (por exemplo, em `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `AtividadeFacade`, `SubprocessoContextoService`).
Contudo, o código precisa de garantias de que, se o serviço de acesso bloquear o usuário através do lançamento da `ErroAcessoNegado`, a execução **deve abortar e relatar**, ou seja, precisamos testar explicitamente esses cenários de rejeição de segurança.

## Passos para Implementação (Workflows/Tasks)

### Todo: 1. `AtividadeFacadeTest.java`
- Adicionar um cenário de falha `deveLancarErroAcessoNegadoAoCriarAtividadeSemPermissao()`.
  - Stub do `accessControlService.verificarPermissao(..., CRIAR_ATIVIDADE, ...)` para lançar `ErroAcessoNegado`.
  - Execução validando `assertThrows(ErroAcessoNegado.class, () -> facade.criarAtividade(...))`.
- Adicionar cenário de falha semelhante para `editar`, `excluir` e `removerConhecimento`.

### Todo: 2. `ImpactoMapaServiceTest.java`
- Adicionar teste para validação de `VERIFICAR_IMPACTOS` negado, forçando o mock a disparar `ErroAcessoNegado` e asserindo que o método propagará a exceção na classe `ImpactoMapaService`.

### Todo: 3. `SubprocessoCadastroWorkflowServiceTest.java` e afins
- Validar se `disponibilizarCadastro`, `homologarCadastro` etc., que usam `verificarPermissao`, transferem-se em suites de erro apropriadas. Testaremos `deveLancarErroAcessoAoSolicitarAprovacaoSemPermissao()`.

### Todo: 4. `SubprocessoMapaWorkflowServiceTest.java`
- Adicionar casos cobrindo `HOMOLOGAR_MAPA`, `ACEITAR_MAPA`, quando `ErroAcessoNegado` forçado.

## Critério de Finalização
* Em todas as alterações, será executado `.\gradlew :backend:test --tests "NomeDaClasseDeTesteAlterada"` (completando o `[x]` Eixo 1.2 do nosso plano principal em `/c:/sgc/plano-global-melhoria-testes.md`).
