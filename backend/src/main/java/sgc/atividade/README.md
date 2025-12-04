# Módulo de Atividade
Última atualização: 2025-12-04 14:18:38Z

## Visão Geral
Este pacote gerencia a entidade `Atividade`, que representa uma tarefa ou atribuição dentro de um `Mapa` de competências.

Uma característica arquitetural importante é a **centralização da gestão de Conhecimentos** neste módulo. A entidade `Conhecimento` é tratada como um sub-recurso de `Atividade`, e seu ciclo de vida (criação, atualização, exclusão) é gerenciado através da API de `Atividade`. Isso reflete a relação de negócio onde um conhecimento só existe no contexto de uma atividade.

## Arquitetura e Componentes
A lógica de negócio reside no `AtividadeService`. O `AtividadeController` atua apenas como interface REST, delegando as operações para a camada de serviço.

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
- **`AtividadeController`**: Expõe a API REST para gerenciar `Atividades` e seus `Conhecimentos`. Seguindo as convenções do projeto, operações de atualização e exclusão utilizam o verbo POST com sufixos na URL.
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

- **`AtividadeService`**: Contém a lógica de negócio para todas as operações, garantindo a integridade dos dados e regras de validação.

### Modelo de Dados (`model`)
- **`Atividade`**: Entidade JPA que representa uma atividade. Possui uma relação `OneToMany` com `Conhecimento`.
- **`Conhecimento`**: Entidade JPA que representa um conhecimento necessário para realizar uma atividade.
- **`AtividadeRepo` / `ConhecimentoRepo`**: Repositórios Spring Data.

### DTOs (`dto`)
- **`AtividadeDto`**: DTO para transporte de dados de atividade.
- **`ConhecimentoDto`**: DTO para transporte de dados de conhecimento.
- **`AtividadeMapper` / `ConhecimentoMapper`**: Interfaces MapStruct para conversão Entidade <-> DTO.

## Propósito da Centralização
A decisão de gerenciar `Conhecimento` através de `Atividade` simplifica a API e reforça o modelo de domínio. Em vez de ter endpoints soltos para conhecimentos, o acesso hierárquico `/api/atividades/{atividadeId}/conhecimentos` garante que todo conhecimento esteja sempre corretamente vinculado à sua atividade pai.


## Detalhamento técnico (gerado em 2025-12-04T14:22:48Z)

Resumo detalhado dos artefatos, comandos e observações técnicas gerado automaticamente.
