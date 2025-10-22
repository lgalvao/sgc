# Problemas Conhecidos

## Falha ao Carregar o `ApplicationContext` em Testes de Integração (Resolvido)

**Problema:** Os testes de integração (`@SpringBootTest`) estavam falhando ao carregar o `ApplicationContext` da aplicação. Isso resultava em erros de `IllegalStateException: ApplicationContext failure threshold (1) exceeded`, que por sua vez causavam falhas em cascata nos testes.

**Diagnóstico:** A causa raiz do problema era uma `Unique index or primary key violation` durante a execução do script `data.sql` no ambiente de teste. Isso indicava que o script estava sendo executado várias vezes, causando a falha na inserção de dados e, consequentemente, a falha ao carregar o `ApplicationContext`. A investigação revelou que a configuração `spring.sql.init.mode=always` no `application.yml` principal, combinada com a inicialização de dados nos testes, causava a dupla execução.

**Solução:**

A solução definitiva foi configurar o `spring.jpa.hibernate.ddl-auto` para `create-drop` no arquivo `src/test/resources/application.properties` e desabilitar a inicialização de dados por padrão. Esta abordagem garante que o schema do banco de dados seja criado do zero antes de cada execução de teste e descartado após a conclusão, proporcionando um ambiente de teste limpo e consistente para cada teste.

**Configuração Final em `src/test/resources/application.properties`:**
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SGC
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=never
```

## Testes Remanescentes com Falha

A solução acima corrigiu o problema de carregamento do `ApplicationContext`, mas 8 testes ainda estão falhando devido à falta de dados de teste. Esses testes precisam ser atualizados para carregar os dados de que precisam, seja através da anotação `@Sql` ou criando as entidades necessárias em seus métodos de setup.

**Testes Falhando:**
- `sgc.integracao.CDU20IntegrationTest` (2 testes)
- `sgc.subprocesso.SubprocessoWorkflowServiceTest` (2 testes)
- `sgc.integracao.CDU14IntegrationTest` (4 testes)
