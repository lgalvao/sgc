# Guia para Execução do Backend SGC

Este documento detalha os passos e as lições aprendidas para conseguir executar o backend do SGC em um ambiente de desenvolvimento limpo.

## O Problema Original: Falha na Conexão com o Banco de Dados

A tentativa inicial de executar o backend com `./gradlew :backend:bootRun` falhava com um erro de `Connection refused` para o PostgreSQL. A aplicação, por padrão, está configurada para usar um banco de dados PostgreSQL, que não está disponível no ambiente.

## A Solução em Etapas: Do Erro à Execução

A jornada para fazer o backend rodar envolveu a correção de múltiplos problemas, desde a configuração do ambiente até a inconsistência dos dados de teste.

### 1. Ativando o Perfil `local` com H2

A primeira e mais crucial etapa foi criar um perfil Spring para o ambiente de desenvolvimento local que utiliza um banco de dados em memória (H2), eliminando a dependência do PostgreSQL.

**Ação:** Criação do arquivo `backend/src/main/resources/application-local.yml`.

**Conteúdo Essencial:**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:sgcdb;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SGC
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    defer-datasource-initialization: true
  sql:
    init:
      mode: always
```

### 2. Disponibilizando o Driver H2 em Tempo de Execução

Mesmo com a configuração do perfil, a aplicação falhava ao iniciar com o erro `Cannot load driver class: org.h2.Driver`. Isso ocorria porque a dependência do H2 no `backend/build.gradle.kts` estava com o escopo `testImplementation`, o que a tornava indisponível para a execução principal da aplicação.

**Ação:** Alterar o escopo da dependência do H2.

**De:**
```kotlin
testImplementation("com.h2database:h2")
```

**Para:**
```kotlin
runtimeOnly("com.h2database:h2")
```

### 3. Corrigindo o Script de Seed (`data.sql`)

Após resolver os problemas de configuração, a aplicação começou a falhar na inicialização do banco de dados devido a um `data.sql` severamente desatualizado e inconsistente com o modelo de entidades JPA atual. Os erros foram:

- **Nomes de Colunas Incorretos:** O script usava `ID` como chave primária, mas a `EntidadeBase` define a chave como `codigo`. Outras colunas de chave estrangeira também estavam com nomes errados (ex: `UNIDADE_SUPERIOR_ID` em vez de `unidade_superior_codigo`).
- **Valores de Enum Inválidos:** O script tentava inserir o valor `'ADMINISTRATIVA'` na coluna `TIPO` da tabela `UNIDADE`, mas este valor não existe no enum `TipoUnidade`.
- **Ordem de Inserção Incorreta:** O script tentava inserir registros filhos antes de seus pais na mesma tabela, violando as restrições de integridade referencial.
- **Nomes de Tabela Incorretos:** O script tentava inserir perfis na tabela `USUARIO_PERFIS`, mas a anotação `@CollectionTable` na entidade `Usuario` definia o nome como `USUARIO_PERFIL`.
- **Modelo de Dados Desatualizado:** O erro mais grave foi a tentativa de inserir dados na tabela `MAPA` usando colunas (`NOME`, `DESCRICAO`) que não existem mais na entidade `Mapa.java`. Isso revelou que todo o esquema de dados para mapas, processos, atividades, etc., no `data.sql` era obsoleto.

**Ação:** Foi necessário remover drasticamente todos os `INSERT`s do `data.sql` que dependiam do modelo de dados antigo (`MAPA`, `PROCESSO`, `ATIVIDADE`, etc.), mantendo apenas os `INSERT`s para `UNIDADE`, `USUARIO`, e `USUARIO_PERFIL`, após corrigir seus nomes de colunas e valores.

## Comando Final para Execução

Após todas as correções, o comando para iniciar o backend com sucesso é:

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
```

**Nota:** A tarefa `bootRun` manterá o processo em execução. Em ambientes automatizados, pode ocorrer um *timeout*, o que, neste caso, indica que o servidor iniciou com sucesso e está aguardando conexões.