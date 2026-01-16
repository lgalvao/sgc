# Módulo de Análise


## Visão Geral

Este pacote é responsável por registrar o **histórico de decisões** tomadas durante o ciclo de vida de um `Subprocesso`.
Ele funciona como uma trilha de auditoria, armazenando as justificativas, observações e ações (aceite, devolução) que
ocorrem durante as etapas de revisão de um mapa de competências.

A implementação utiliza um **modelo de dados genérico**, onde uma única entidade `Analise` é usada para registrar todos
os tipos de análise. Um enum, `TipoAnalise`, é usado para diferenciar os contextos (ex: `CADASTRO` vs. `VALIDACAO`).

## Arquitetura e Componentes

O `AnaliseFacade` é invocado pelo `SubprocessoFacade` (ou diretamente pelo `AnaliseController` para operações
específicas) sempre que uma transição de estado que requer uma justificativa ocorre (ex: `devolverCadastro`). A API
exposta pelo `AnaliseController` permite ao frontend registrar novas análises e consultar o histórico de um subprocesso,
usando endpoints distintos para cada tipo de análise.

```mermaid
graph TD
    subgraph "Módulo de Workflow"
        SP[SubprocessoFacade]
    end

    subgraph "Módulo de Análise"
        Service(AnaliseFacade)
        Controle(AnaliseController)
        subgraph "Modelo de Dados Genérico"
            Analise[Analise]
            TipoAnalise[TipoAnalise Enum]
        end
        subgraph "Repositório"
            AnaliseRepo
        end
    end

    SP -- Invoca --> Service

    Controle -- Expõe API para --> Frontend
    Controle -- Utiliza --> Service

    Service -- Persiste --> AnaliseRepo
    AnaliseRepo -- Gerencia --> Analise
    Analise -- Usa --> TipoAnalise
```

## Componentes Principais

### Controladores e Serviços

- **`AnaliseFacade`**: Centraliza a lógica de negócio para criar os registros de análise. É chamado por serviços de
  nível superior para garantir que as ações de workflow sejam devidamente auditadas.
- **`AnaliseController`**: Expõe endpoints REST para o frontend criar e consultar o histórico de cada tipo de análise.
    - `GET /api/subprocessos/{codigo}/analises-cadastro`
    - `POST /api/subprocessos/{codigo}/analises-cadastro`
    - `GET /api/subprocessos/{codigo}/analises-validacao`
    - `POST /api/subprocessos/{codigo}/analises-validacao`

### Modelo de Dados (`model`)

- **`Analise`**: Entidade JPA genérica que modela um registro de análise. Está vinculada a um `Subprocesso` e armazena a
  ação realizada, as observações, o autor e o `TipoAnalise`.
- **`AnaliseRepo`**: Repositório Spring Data.
- **`TipoAnalise`**: Enum que diferencia os contextos de análise (`CADASTRO`, `VALIDACAO`).
- **`TipoAcaoAnalise`**: Enum que define as ações possíveis em uma análise (ex: `DEVOLUCAO`, `ACEITE`).

### DTOs (`dto`)

- **`CriarAnaliseRequest`**: DTO utilizado para transportar os dados necessários para a criação de uma nova análise.
- **`AnaliseMapper`**: Interface MapStruct para conversão de entidades (embora o controller atualmente retorne entidades
  diretamente em alguns casos, a infraestrutura de mapeamento existe para uso futuro).

## Propósito e Uso

A principal função deste módulo é fornecer **rastreabilidade**. A abordagem de modelo genérico com um campo de "tipo"
foi utilizada para registrar o histórico de decisões em diferentes etapas do workflow sem duplicar estruturas de dados.


## Como Testar

Para executar apenas os testes deste módulo (a partir do diretório `backend`):
```bash
./gradlew test --tests "sgc.analise.*"
```
