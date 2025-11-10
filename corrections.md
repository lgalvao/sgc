# Correções Implementadas nos Testes de Backend

Este documento resume as soluções aplicadas para corrigir as falhas em várias classes de testes de integração.

## 1. `CDU09IntegrationTest`

- **Problema:** Todos os testes falhavam com o status `400 Bad Request`.
- **Causa Raiz:** A URL do endpoint utilizada nos testes estava incorreta. A chamada `POST` estava sendo feita para `/api/subprocessos/{id}/disponibilizar`, mas o endpoint correto no `SubprocessoCadastroController` é `/api/subprocessos/{id}/cadastro/disponibilizar`. A ausência do segmento `/cadastro` impedia o Spring de encontrar um handler para a requisição.
- **Solução:** A URL em todas as chamadas `mockMvc.perform(post(...))` foi corrigida para o caminho correto. Adicionalmente, foi incluído o token CSRF (`with(csrf())`) para garantir a conformidade com as políticas de segurança da aplicação para requisições `POST`.

## 2. `CDU12IntegrationTest`

- **Problema:** Todos os testes falhavam na fase de setup (`@BeforeEach`) com uma `ConstraintViolationException`, indicando que a coluna `mapa_vigente_codigo` não podia ser nula na tabela `unidade_mapa`.
- **Causa Raiz:** A entidade `UnidadeMapa` utiliza uma estratégia de mapeamento duplo para suas chaves estrangeiras: um campo `Long` para o ID (`mapaVigenteCodigo`) e uma associação de objeto `@ManyToOne` (`mapaVigente`) para a mesma coluna, com a associação marcada como `insertable=false`. O código do teste estava definindo apenas a referência do objeto (`setMapaVigente()`), mas não o campo de ID primitivo, que é o único considerado pelo JPA durante a inserção.
- **Solução:** O método `setUp` foi modificado para primeiro salvar as entidades `Unidade` e `Mapa` para garantir que elas possuam IDs. Em seguida, ao criar a instância de `UnidadeMapa`, os IDs foram definidos explicitamente usando `setUnidadeCodigo()` and `setMapaVigenteCodigo()`, garantindo que os valores corretos fossem persistidos no banco de dados.

## 3. `ImpactoCompetenciaServiceTest` (Correção Inicial)

- **Problema:** Um teste de unidade falhava devido a um `NullPointerException` dentro do serviço.
- **Causa Raiz:** Os dados de mock estavam incompletos. A relação bidirecional entre as entidades `Competencia` e `Atividade` não estava sendo corretamente estabelecida no setup do teste. O código do serviço dependia dessa relação para funcionar corretamente.
- **Solução:** O setup do teste foi ajustado para garantir que a associação `@ManyToMany` fosse configurada em ambos os lados: a atividade foi adicionada à lista de atividades da competência, e a competência foi adicionada à lista de competências da atividade. Isso garantiu que o mock se comportasse como a entidade real.
