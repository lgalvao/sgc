# Módulo de Competência

## Visão Geral
Este pacote gerencia a entidade `Competencia` e sua associação com `Atividades`. Uma competência representa um conjunto de habilidades ou conhecimentos, e este módulo fornece a API REST para seu gerenciamento completo, incluindo a criação, leitura, atualização e exclusão de competências, bem como a gestão dos vínculos com as atividades que as compõem.

## Arquitetura e Componentes
A lógica de negócio para o gerenciamento de competências está encapsulada no `CompetenciaService`. No entanto, este módulo não possui um controlador próprio. A API REST para `Competencia` é exposta pelo `SubprocessoMapaControle`, localizado no pacote `subprocesso`.

```mermaid
graph TD
    subgraph "Frontend"
        Frontend
    end

    subgraph "Módulo Subprocesso"
        Controle(SubprocessoMapaControle)
    end

    subgraph "Módulo Competência"
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
- **`CompetenciaService`**: Contém a lógica de negócio para todas as operações, garantindo a integridade dos dados e as regras de validação antes de interagir com os repositórios.
- **`SubprocessoMapaControle`**: (no pacote `subprocesso`) Expõe a API REST para gerenciar `Competencias` e seus vínculos com `Atividades`.
- **`Competencia`**: Entidade JPA que representa uma competência.
- **`CompetenciaAtividade`**: Entidade de associação (tabela `competencia_atividade`) que representa a relação N-para-N entre `Competencia` e `Atividade`. Utiliza uma chave primária composta.
- **`CompetenciaRepo` / `CompetenciaAtividadeRepo`**: Repositórios Spring Data para a persistência das entidades.

## Propósito e Uso
O módulo permite que os usuários definam competências de forma granular e, em seguida, as componham associando-as a múltiplas atividades. A API aninhada para o gerenciamento de vínculos (`/api/competencias/{id}/atividades`) segue as melhores práticas RESTful, tornando a intenção da operação clara e semântica.