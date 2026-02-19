# Plano de Melhoria de Testes

## Análise Atual

A análise da suíte de testes do backend revelou os seguintes pontos:

1.  **Testes de Integração Valiosos:** O teste `CDU02IntegrationTest.java` cobre bem os cenários de visibilidade do Painel (CDU-02), verificando que usuários ADMIN veem processos CRIADO, enquanto GESTOR/CHEFE não veem. No entanto, ele depende de resets manuais de sequência de banco de dados (`ALTER TABLE ... RESTART WITH`), o que é frágil e desnecessário dado o uso de `@Transactional`.
2.  **Testes Unitários de Facade Excessivamente Mockados:** `ProcessoFacadeTest.java` testa a orquestração corretamente, mas depende quase inteiramente de mocks (`when(...).thenReturn(...)`). Isso não garante que a consulta ao banco de dados (JPQL) esteja correta. Se a query mudar logicamente mas mantiver a assinatura, o teste unitário passará enquanto a funcionalidade quebrará.
3.  **Segurança em Importação de Atividades:** A funcionalidade de importação de atividades (`SubprocessoAtividadeService.importarAtividades`) apresenta uma vulnerabilidade crítica: verifica permissão apenas no subprocesso de destino, permitindo importar dados de *qualquer* subprocesso de origem sem verificação de acesso.

## Status das Melhorias

- [x] **Criar `ProcessoRepoTest.java`**: Implementado com `@SpringBootTest` e `@Transactional` para validar queries customizadas como `findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot`.
- [ ] **Refatorar `CDU02IntegrationTest.java`**: Ainda pendente. Remover SQL manual e usar IDs dinâmicos.
- [x] **Corrigir Vulnerabilidade de Importação**: Adicionar verificação de permissão na origem e testes correspondentes.

## Plano de Ação (Próximos Passos)

### 1. Corrigir Vulnerabilidade em `SubprocessoAtividadeService`
- **Ação:** Adicionar verificação de permissão `VISUALIZAR_SUBPROCESSO` no subprocesso de origem.
- **Teste:** Atualizar `SubprocessoAtividadeServiceTest` para garantir que a exceção `ErroAcessoNegado` seja lançada quando o usuário não tem permissão na origem.

### 2. Refatorar `CDU02IntegrationTest.java`
O objetivo é tornar o teste mais robusto e limpo.

-   Remover os comandos SQL manuais de `ALTER TABLE ... RESTART WITH`.
-   Garantir que todos os testes utilizem IDs gerados dinamicamente pelas entidades persistidas no `@BeforeEach`, evitando "magic numbers" (ex: trocar `param(UNIDADE, "1")` por `unidadeRaiz.getCodigo().toString()`).
-   Adicionar comentários explícitos vinculando as asserções aos requisitos do CDU-02 (ex: "Processos CRIADO não devem ser visíveis para GESTOR").

## Benefícios Esperados

-   **Segurança:** Prevenção de acesso não autorizado a dados sensíveis via importação.
-   **Confiança:** Garantia de que a lógica de filtragem do banco de dados está correta.
-   **Manutenibilidade:** Testes de repositório são mais rápidos e fáceis de manter do que testes de integração full-stack para verificar lógica de query.
-   **Robustez:** Remoção de dependências de estado global do banco de dados (sequências fixas) nos testes de integração.
