# Plano de Melhoria de Testes

## Análise Atual

A análise da suíte de testes do backend revelou os seguintes pontos:

1.  **Testes de Integração Valiosos:** O teste `CDU02IntegrationTest.java` cobre bem os cenários de visibilidade do Painel (CDU-02), verificando que usuários ADMIN veem processos CRIADO, enquanto GESTOR/CHEFE não veem. No entanto, ele depende de resets manuais de sequência de banco de dados (`ALTER TABLE ... RESTART WITH`), o que é frágil e desnecessário dado o uso de `@Transactional`.
2.  **Testes Unitários de Facade Excessivamente Mockados:** `ProcessoFacadeTest.java` testa a orquestração corretamente, mas depende quase inteiramente de mocks (`when(...).thenReturn(...)`). Isso não garante que a consulta ao banco de dados (JPQL) esteja correta. Se a query mudar logicamente mas mantiver a assinatura, o teste unitário passará enquanto a funcionalidade quebrará.
3.  **Lacuna na Camada de Repositório:** Não existe um teste dedicado (`@DataJpaTest`) para `ProcessoRepo`. As consultas customizadas com `@Query`, especialmente `findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot`, possuem lógica complexa (joins, exclusão de status, paginação com countQuery) que precisa ser verificada isoladamente contra um banco real (H2/TestContainer) para garantir corretude sintática e lógica.

## Plano de Ação

### 1. Criar `ProcessoRepoTest.java`
O objetivo é garantir que as queries customizadas funcionem como esperado, independente da camada web ou de serviço.

-   **Tecnologia:** `@DataJpaTest` com `TestEntityManager`.
-   **Foco Principal:** Método `findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot`.
-   **Cenários a Cobrir:**
    -   Exclusão de processos com status `CRIADO` (requisito do Painel para não-admins).
    -   Inclusão de processos com outros status (`EM_ANDAMENTO`, `FINALIZADO`).
    -   Filtragem correta por lista de códigos de unidade (apenas unidades onde o usuário tem permissão).
    -   Paginação e Count Query (garantir que o total de elementos está correto mesmo com `DISTINCT`).

### 2. Refatorar `CDU02IntegrationTest.java`
O objetivo é tornar o teste mais robusto e limpo.

-   Remover os comandos SQL manuais de `ALTER TABLE ... RESTART WITH`.
-   Garantir que todos os testes utilizem IDs gerados dinamicamente pelas entidades persistidas no `@BeforeEach`, evitando "magic numbers" (ex: trocar `param(UNIDADE, "1")` por `unidadeRaiz.getCodigo().toString()`).
-   Adicionar comentários explícitos vinculando as asserções aos requisitos do CDU-02 (ex: "Processos CRIADO não devem ser visíveis para GESTOR").

## Benefícios Esperados

-   **Confiança:** Garantia de que a lógica de filtragem do banco de dados está correta.
-   **Manutenibilidade:** Testes de repositório são mais rápidos e fáceis de manter do que testes de integração full-stack para verificar lógica de query.
-   **Robustez:** Remoção de dependências de estado global do banco de dados (sequências fixas) nos testes de integração.
