# Módulo de Competência

## Visão Geral
Este pacote gerencia a entidade `Competencia` e sua associação com `Atividades`. Uma competência representa um conjunto de habilidades ou conhecimentos, e este módulo fornece a API REST para seu gerenciamento completo, incluindo a criação, leitura, atualização e exclusão de competências, bem como a gestão dos vínculos com as atividades que as compõem.

## Arquitetura e Componentes
Seguindo o padrão arquitetural da aplicação, a lógica de negócio foi encapsulada no `CompetenciaService`. O `CompetenciaControle` atua como a camada de API, recebendo as requisições HTTP e delegando a execução para o serviço.

```mermaid
graph TD
    subgraph "Frontend"
        Frontend
    end

    subgraph "Módulo Competência"
        Controle(CompetenciaControle)
        Service(CompetenciaService)
        subgraph "Repositórios"
            CompetenciaRepo
            CompetenciaAtividadeRepo
        end
        subgraph "Modelos de Dados"
            Competencia
            CompetenciaAtividade
        end
    end

    Frontend -- API REST --> Controle

    Controle -- Delega para --> Service

    Service -- Utiliza --> CompetenciaRepo & CompetenciaAtividadeRepo

    CompetenciaRepo -- Gerencia --> Competencia
    CompetenciaAtividadeRepo -- Gerencia --> CompetenciaAtividade
```

## Componentes Principais
- **`CompetenciaControle`**: Expõe a API REST para gerenciar `Competencias` e seus vínculos com `Atividades`.
  - **Endpoints de Competência**:
    - `POST /api/competencias`: Cria uma nova competência.
    - `PUT /api/competencias/{id}`: Atualiza uma competência.
    - `DELETE /api/competencias/{id}`: Exclui uma competência.
  - **Endpoints de Vínculo (Sub-recurso)**:
    - `POST /api/competencias/{competenciaId}/atividades`: Associa uma atividade a uma competência.
    - `DELETE /api/competencias/{competenciaId}/atividades/{atividadeId}`: Desassocia uma atividade.
- **`CompetenciaService`**: Contém a lógica de negócio para todas as operações, garantindo a integridade dos dados e as regras de validação antes de interagir com os repositórios.
- **`Competencia`**: Entidade JPA que representa uma competência.
- **`CompetenciaAtividade`**: Entidade de associação (tabela `competencia_atividade`) que representa a relação N-para-N entre `Competencia` e `Atividade`. Utiliza uma chave primária composta.
- **`CompetenciaRepo` / `CompetenciaAtividadeRepo`**: Repositórios Spring Data para a persistência das entidades.

## Propósito e Uso
O módulo permite que os usuários definam competências de forma granular e, em seguida, as componham associando-as a múltiplas atividades. A API aninhada para o gerenciamento de vínculos (`/api/competencias/{id}/atividades`) segue as melhores práticas RESTful, tornando a intenção da operação clara e semântica.