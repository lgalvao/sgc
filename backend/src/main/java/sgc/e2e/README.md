# Pacote E2E (Suporte a Testes End-to-End)

## Visão Geral
Este pacote contém classes de infraestrutura e suporte específicas para a execução de **Testes End-to-End (E2E)** no backend.

Estas classes **não fazem parte da lógica de produção** e são ativadas apenas quando o perfil Spring `e2e` está ativo. Elas permitem que o ambiente de teste manipule o estado da aplicação de forma que não seria permitida em produção (ex: resetar o banco de dados, criar dados de teste via API).

## Arquitetura

```mermaid
graph TD
    subgraph "Ambiente de Produção"
        direction TB
        AppProd[Aplicação SGC]
        DBProd[PostgreSQL]
        AppProd -- Usa --> DBProd
    end

    subgraph "Ambiente E2E"
        direction TB
        AppE2E[Aplicação SGC (Perfil 'e2e')]
        DBE2E[H2 In-Memory]
        TestController[E2eTestController]

        AppE2E -- Usa --> DBE2E
        AppE2E -- Expõe --> TestController
    end

    Playwright -- Chama API de Teste --> TestController
    TestController -- Reseta/Popula --> DBE2E
```

## Componentes Principais

- **`E2eTestController`**: Um controlador REST especial, disponível apenas no perfil `e2e`. Ele expõe endpoints que permitem aos scripts de teste (Playwright) controlar o ambiente:
    - Resetar o banco de dados.
    - Popular o banco com cenários específicos.
    - Limpar o cache.
- **`E2eDatabaseConfig`**: Configurações de banco de dados específicas para o ambiente de teste (geralmente H2 em memória).
- **`E2eTestContext`**: Contexto auxiliar para manter estado durante a execução dos testes.
- **`E2eTestDatabaseService`**: Serviço que executa os scripts SQL de limpeza e população de dados.

## Segurança
Este pacote é estritamente isolado. As classes aqui contidas devem ser anotadas com `@Profile("e2e")` para garantir que nunca sejam carregadas ou expostas em ambientes de produção ou homologação.
