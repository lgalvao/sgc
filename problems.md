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

A solução acima corrigiu o problema de carregamento do `ApplicationContext`, mas 7 testes ainda estão falhando.

**Testes Falhando:**
- `sgc.integracao.CDU20IntegrationTest` (1 teste)
- `sgc.integracao.CDU14IntegrationTest` (4 testes)
- `sgc.integracao.CDU09IntegrationTest` (1 teste)
- `sgc.integracao.CDU10IntegrationTest` (1 teste)

### `sgc.integracao.CDU20IntegrationTest`

O teste `devolucaoEaceiteComVerificacaoHistorico` está falhando com um `AssertionError`. O teste espera 2 alertas, mas encontra 3. Isso indica que a lógica de negócios está criando um alerta extra em algum lugar do fluxo.

### `sgc.integracao.CDU14IntegrationTest`

Todos os 4 testes nesta classe estão falhando com um erro de status HTTP 400 (Bad Request) durante a fase de setup (`@BeforeEach`). Isso aponta para uma falha de validação na chamada da API `/api/subprocessos/{id}/disponibilizar-revisao-cadastro`. A causa raiz parece ser uma falha na cópia do mapa de competências, que resulta em um mapa inválido sendo submetido para revisão.

### `sgc.integracao.CDU09IntegrationTest` e `sgc.integracao.CDU10IntegrationTest`

Ambos os testes falham com um erro de status HTTP 422 (Unprocessable Entity). Isso indica que a validação da entidade falhou. A causa provável é que os testes não estão associando corretamente as entidades `Atividade` e `Conhecimento`, o que leva a uma falha na validação da lógica de negócios.
