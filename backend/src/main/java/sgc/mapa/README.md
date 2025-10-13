# Módulo de Mapa de Competências - SGC

## Visão Geral
O pacote `mapa` é um módulo central do SGC, responsável pela gestão do **Mapa de Competências**. Um "Mapa" é um artefato complexo que representa o conjunto de competências e atividades de uma unidade organizacional. Este pacote orquestra operações de negócio complexas como a criação, salvamento transacional, cópia e análise de impacto de mudanças nos mapas.

## Arquitetura e Componentes
A arquitetura é orientada a serviços, com responsabilidades bem definidas.

- **`MapaControle.java`**: Controller REST que orquestra as chamadas aos diferentes serviços do módulo, expondo os endpoints da API para o frontend.
- **Serviços de Negócio**:
  - **`MapaService.java`**: Principal serviço para manipulação de mapas. Lida com a obtenção, salvamento atômico (`salvarMapaCompleto`) e validação de mapas completos.
  - **`CopiaMapaService.java`**: Serviço especializado em clonar mapas, permitindo que uma unidade reutilize um mapa existente.
  - **`ImpactoMapaService.java`**: Implementa a análise de impacto, comparando versões de um mapa para identificar diferenças.
- **`dto/`**: Contém os DTOs para a comunicação via API.
  - **`MapaMapper.java`**: Interface MapStruct para conversão entre entidades e DTOs.
  - **DTOs Notáveis**: `MapaCompletoDto`, `SalvarMapaRequest`, `ImpactoMapaDto`, e DTOs de visualização no sub-pacote `visualizacao/`.
- **`modelo/`**: Contém as entidades JPA, enums e repositórios.
  - **`Mapa.java`**: Entidade central que representa o Mapa de Competências.
  - **`UnidadeMapa.java`**: Entidade de associação que registra o status de um mapa para uma unidade.
  - **`MapaRepo.java`**: Repositório para a entidade `Mapa`.
  - **`UnidadeMapaRepo.java`**: Repositório para a entidade `UnidadeMapa`.
  - **Enums**: `TipoImpactoAtividade`, `TipoImpactoCompetencia`.

## Diagrama de Componentes
```mermaid
graph TD
    subgraph "Cliente"
        A[Cliente API]
    end

    subgraph "Módulo Mapa"
        MC(MapaControle)
        subgraph "Serviços"
            MS(MapaService)
            IS(ImpactoMapaService)
            CS(CopiaMapaService)
        end
        subgraph "Camada de Dados"
            MR(MapaRepo)
            UMR(UnidadeMapaRepo)
            M(Mapa)
            UM(UnidadeMapa)
        end
        subgraph "DTOs"
            MM(MapaMapper)
            DTOs(...)
        end
    end

    A -- Requisições HTTP --> MC
    MC -- Orquestra --> MS
    MC -- Orquestra --> IS
    MC -- Orquestra --> CS
    MC -- Usa --> MM

    MS -- Usa --> MR
    IS -- Usa --> MR
    CS -- Usa --> MR
    MS -- Usa --> UMR

    MR -- Gerencia --> M
    UMR -- Gerencia --> UM
```

## Fluxos de Operação

### Salvando um Mapa
1.  O cliente envia uma requisição com o `SalvarMapaRequest` para o `MapaControle`.
2.  O `MapaControle` invoca `MapaService.salvarMapaCompleto()`.
3.  Dentro de uma única transação (`@Transactional`), o serviço garante a consistência dos dados, atualizando o mapa e todas as suas associações.

### Verificando o Impacto de Mudanças
1.  Uma requisição invoca `ImpactoMapaService.verificarImpactos()`.
2.  O serviço compara o mapa vigente com uma nova versão e monta o `ImpactoMapaDto`.
3.  O DTO com as diferenças detalhadas é retornado.

## Notas Importantes
- **Complexidade de Negócio**: A separação em múltiplos serviços é chave para manter o código organizado.
- **Transacionalidade**: O salvamento atômico no `MapaService` é crucial para a integridade dos dados.
- **DTOs Ricos**: O uso de DTOs complexos e específicos para cada operação é fundamental para gerenciar a complexidade das interações.