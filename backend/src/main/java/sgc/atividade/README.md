# Módulo de Atividade


## Visão Geral

Este módulo gerencia a entidade `Atividade`, que representa uma tarefa ou atribuição dentro de um `Mapa` de
competências.

Uma característica arquitetural importante é a **centralização da gestão de Conhecimentos** neste módulo. A entidade
`Conhecimento` é tratada como um sub-recurso de `Atividade`, e seu ciclo de vida (criação, atualização, exclusão) é
gerenciado através da API de `Atividade`. Isso reflete a relação de negócio onde um conhecimento só existe no contexto
de uma atividade.

## Estrutura Spring Modulith

Este módulo segue a convenção Spring Modulith:

### API Pública
- **`AtividadeService`** (raiz do módulo) - Facade principal para operações de CRUD de atividades e conhecimentos
- **`api/AtividadeDto`** - DTO principal de atividade
- **`api/ConhecimentoDto`** - DTO de conhecimento (sub-recurso)

### Implementação Interna
- `internal/AtividadeController` - REST endpoints
- `internal/AtividadeMapper`, `internal/ConhecimentoMapper` - Mapeadores (MapStruct)
- `internal/model/Atividade`, `internal/model/Conhecimento` - Entidades JPA
- `internal/model/AtividadeRepo`, `internal/model/ConhecimentoRepo` - Repositórios

**⚠️ Importante:** Outros módulos **NÃO** devem acessar classes em `internal/`.

## Dependências

### Módulos que este módulo depende
- `comum` - Componentes compartilhados

### Módulos que dependem deste módulo
- `mapa` - Mapas vinculam atividades a competências
- `processo` - Processo acessa dados de atividades

## Arquitetura e Componentes

A lógica de negócio reside no `AtividadeService`. O `AtividadeController` atua apenas como interface REST, delegando as
operações para a camada de serviço.

```mermaid
graph TD
    subgraph "Frontend"
        Frontend
    end

    subgraph "Módulo Atividade"
        Controle(AtividadeController)
        Service(AtividadeService)
        subgraph "Repositórios"
            AtividadeRepo
            ConhecimentoRepo
        end
        subgraph "Modelos de Dados"
            Atividade
            Conhecimento
        end
    end

    Frontend -- API REST --> Controle

    Controle -- Delega para --> Service

    Service -- Utiliza --> AtividadeRepo
    Service -- Também gerencia --> ConhecimentoRepo

    AtividadeRepo -- Gerencia --> Atividade
    ConhecimentoRepo -- Gerencia --> Conhecimento

    Atividade -- Tem um-para-muitos com --> Conhecimento
```

## Componentes Principais

### Controladores e Serviços

- **`AtividadeController`**: Expõe a API REST para gerenciar `Atividades` e seus `Conhecimentos`. Seguindo as convenções
  do projeto, operações de atualização e exclusão utilizam o verbo POST com sufixos na URL.
    - **Atividades**:
        - `GET /api/atividades`: Lista todas as atividades.
        - `GET /api/atividades/{id}`: Obtém uma atividade por ID.
        - `POST /api/atividades`: Cria uma nova atividade.
        - `POST /api/atividades/{id}/atualizar`: Atualiza uma atividade.
        - `POST /api/atividades/{id}/excluir`: Exclui uma atividade.
    - **Conhecimentos (Sub-recurso)**:
        - `GET /api/atividades/{id}/conhecimentos`: Lista conhecimentos de uma atividade.
        - `POST /api/atividades/{id}/conhecimentos`: Adiciona um conhecimento.
        - `POST /api/atividades/{id}/conhecimentos/{cid}/atualizar`: Atualiza um conhecimento.
        - `POST /api/atividades/{id}/conhecimentos/{cid}/excluir`: Remove um conhecimento.

- **`AtividadeService`**: Contém a lógica de negócio para todas as operações, garantindo a integridade dos dados e
  regras de validação.

### Modelo de Dados (`model`)

- **`Atividade`**: Entidade JPA que representa uma atividade. Possui uma relação `OneToMany` com `Conhecimento`.
- **`Conhecimento`**: Entidade JPA que representa um conhecimento necessário para realizar uma atividade.
- **`AtividadeRepo` / `ConhecimentoRepo`**: Repositórios Spring Data.

### DTOs (`dto`)

- **`AtividadeDto`**: DTO para transporte de dados de atividade.
- **`ConhecimentoDto`**: DTO para transporte de dados de conhecimento.
- **`AtividadeMapper` / `ConhecimentoMapper`**: Interfaces MapStruct para conversão Entidade <-> DTO.

## Propósito da Centralização

A decisão de gerenciar `Conhecimento` através de `Atividade` simplifica a API e reforça o modelo de domínio. Em vez de
ter endpoints soltos para conhecimentos, o acesso hierárquico `/api/atividades/{atividadeId}/conhecimentos` garante que
todo conhecimento esteja sempre corretamente vinculado à sua atividade pai.

## Endpoints REST (Internal)

- **Atividades**:
  - `GET /api/atividades`: Lista todas as atividades
  - `GET /api/atividades/{id}`: Obtém uma atividade por ID
  - `POST /api/atividades`: Cria uma nova atividade
  - `POST /api/atividades/{id}/atualizar`: Atualiza uma atividade
  - `POST /api/atividades/{id}/excluir`: Exclui uma atividade
- **Conhecimentos (Sub-recurso)**:
  - `GET /api/atividades/{id}/conhecimentos`: Lista conhecimentos de uma atividade
  - `POST /api/atividades/{id}/conhecimentos`: Adiciona um conhecimento
  - `POST /api/atividades/{id}/conhecimentos/{cid}/atualizar`: Atualiza um conhecimento
  - `POST /api/atividades/{id}/conhecimentos/{cid}/excluir`: Remove um conhecimento


## Como Testar

Para executar apenas os testes deste módulo:
```bash
./gradlew :backend:test --tests "sgc.atividade.*"
```
