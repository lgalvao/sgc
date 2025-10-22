# Problemas Conhecidos

## Falha ao Carregar o `ApplicationContext` em Testes de Integração

**Problema:** Os testes de integração (`@SpringBootTest`) estão falhando ao carregar o `ApplicationContext` da aplicação. Isso resulta em erros de `IllegalStateException: ApplicationContext failure threshold (1) exceeded`, que por sua vez causam falhas em cascata nos testes.

**Diagnóstico:** A causa raiz do problema é uma `Unique index or primary key violation` durante a execução do script `data.sql` no ambiente de teste. Isso indica que o script está sendo executado várias vezes, o que causa a falha na inserção de dados e, consequentemente, a falha ao carregar o `ApplicationContext`.

**Tentativas de Solução:**

1.  **Refatoração do Teste:** O `CDU14IntegrationTest` foi refatorado para usar MockMvc para todas as chamadas de API e para carregar os dados de um arquivo `data.sql` em `src/test/resources`. Isso não resolveu o problema.
2.  **Configuração do Ambiente de Teste:** O arquivo `src/test/resources/application.properties` foi modificado para forçar a execução do `data.sql` (`spring.sql.init.mode=always`). Isso também não resolveu o problema.
3.  **Smoke Tests:** Um `DataSetupSmokeTest` foi criado para isolar o problema. O teste falhou com o mesmo erro de `ApplicationContext`, confirmando que o problema está na configuração do ambiente de teste, e não em um teste específico.
4.  **Limpeza de Dados:** Foram adicionados métodos `@BeforeEach` aos testes para limpar os dados antes de cada execução. Isso não resolveu o problema, pois o `ApplicationContext` é carregado antes da execução dos métodos `@BeforeEach`.
5.  **Análise do Código da Aplicação:** O código da aplicação foi revisado em busca de bugs que pudessem estar causando a falha na inicialização. Um bug foi encontrado e corrigido no `SubprocessoWorkflowService`, mas isso não resolveu o problema do `ApplicationContext`.

**Conclusão:** O problema parece estar em um nível mais profundo da configuração do projeto, possivelmente relacionado à forma como o Gradle interage com o Spring Boot no ambiente de teste. São necessárias mais investigações para identificar a causa raiz e aplicar a correção adequada.
