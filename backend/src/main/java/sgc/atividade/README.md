# Módulo de Atividade

## Visão Geral
Este pacote gerencia a entidade `Atividade`, que representa uma tarefa ou atribuição dentro de um `Mapa` de competências.

Uma mudança arquitetural importante foi a **centralização da gestão de Conhecimentos** neste módulo. Agora, a entidade `Conhecimento` é tratada como um recurso filho de `Atividade`, e seu ciclo de vida (criação, atualização, exclusão) é gerenciado através da API de `Atividade`. Isso reflete a relação de negócio onde um conhecimento só existe no contexto de uma atividade.

## Arquitetura e Componentes
A lógica de negócio foi movida do `AtividadeControle` para o `AtividadeService`, alinhando este módulo com a arquitetura padrão da aplicação. O `AtividadeControle` agora é responsável apenas por expor a API REST e delegar as operações para a camada de serviço.

```mermaid
graph TD
    subgraph "Frontend"
        Frontend
    end

    subgraph "Módulo Atividade"
        Controle(AtividadeControle)
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
- **`AtividadeControle`**: Expõe a API REST para gerenciar `Atividades` e seus `Conhecimentos` aninhados.
  - **Endpoints de Atividade**:
    - `POST /api/atividades`: Cria uma nova atividade.
    - `POST /api/atividades/{id}/atualizar`: Atualiza uma atividade.
    - `POST /api/atividades/{id}/excluir`: Exclui uma atividade.
  - **Endpoints de Conhecimento (Sub-recurso)**:
    - `POST /api/atividades/{atividadeId}/conhecimentos`: Adiciona um conhecimento a uma atividade.
    - `POST /api/atividades/{atividadeId}/conhecimentos/{conhecimentoId}/atualizar`: Atualiza um conhecimento.
    - `POST /api/atividades/{atividadeId}/conhecimentos/{conhecimentoId}/excluir`: Remove um conhecimento de uma atividade.
- **`AtividadeService`**: Contém a lógica de negócio para todas as operações, garantindo a integridade dos dados e as regras de validação.
- **`Atividade`**: Entidade JPA que representa uma atividade. Possui uma relação `OneToMany` com `Conhecimento`.
- **`Conhecimento`**: Entidade JPA que representa um conhecimento necessário para realizar uma atividade. Está sempre associada a uma `Atividade`.
- **`AtividadeRepo` / `ConhecimentoRepo`**: Repositórios Spring Data para a persistência das entidades.

## Propósito da Centralização
A decisão de gerenciar `Conhecimento` através de `Atividade` simplifica a API e reforça o modelo de domínio. Em vez de ter um endpoint `POST /api/conhecimentos` que exigiria a passagem manual do ID da atividade, o novo modelo `POST /api/atividades/{atividadeId}/conhecimentos` é mais semântico, seguro e alinhado com as melhores práticas de design de APIs RESTful.
