# Problemas Identificados no Backend

## 1. SubprocessoCrudControllerRestAssuredTest

O teste de integração `SubprocessoCrudControllerRestAssuredTest` apresenta falhas persistentes mesmo após ajustes de limpeza de banco e inserção de perfil ADMIN.

- **deveObterDetalhes**: Retorna `404 Not Found`. Isso sugere que a entidade criada no `setupDados` não está visível para a requisição RestAssured, possivelmente devido a problemas de transação ou o ID não estar sendo persistido corretamente antes da chamada.
- **deveCriarSubprocesso**: Retorna JSON onde `unidade.sigla` é nulo, falhando na asserção `Expected: UNIT_SUB_NEW Actual: null`. O status code é 201 (Created), indicando que o subprocesso foi criado, mas o retorno não contém os dados esperados da unidade.

## 2. RelatorioControllerRestAssuredTest

O teste `RelatorioControllerRestAssuredTest` está falhando com `ConstraintViolationException` durante a limpeza do banco de dados (`deleteAll`).

- **Erro**: `Referential integrity constraint violation: "FK_SUBPROCESSO_PROCESSO...`.
- **Causa**: A ordem de exclusão no `setupDados` deste teste provavelmente não respeita as dependências de chave estrangeira, ou não utiliza a estratégia de desabilitar integridade referencial implementada em outros testes (`SubprocessoMapaControllerRestAssuredTest`, `SubprocessoCrudControllerRestAssuredTest`, etc.).

## 3. Estratégia de Limpeza de Testes

Vários testes de integração sofrem com problemas de `TransientPropertyValueException` e violações de chave estrangeira devido à complexidade do modelo de dados (muitas relações bidirecionais e dependências). A solução robusta aplicada em alguns testes (desabilitar RI, truncar tabelas, reabilitar RI dentro de uma transação) deve ser replicada para todos os testes de integração que interagem com o banco de dados (`*RestAssuredTest.java`).
