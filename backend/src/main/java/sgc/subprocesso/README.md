# Módulo de Subprocesso

## Visão Geral
Este pacote é o **motor do workflow** do SGC. Ele gerencia a entidade `Subprocesso`, que representa a tarefa de uma única unidade organizacional dentro de um `Processo` maior. Ele funciona como uma **máquina de estados**, controlando o ciclo de vida de cada tarefa, desde sua criação até a homologação.

A principal responsabilidade deste módulo é garantir que as transições de estado (`situacao`) sigam as regras de negócio e que cada ação seja registrada em uma trilha de auditoria imutável (`Movimentacao`).

## Arquitetura de Serviços (Padrão Fachada)
A complexidade do workflow é gerenciada através de uma arquitetura de serviços coesa, usando o padrão **Service Facade**. O `SubprocessoService` atua como o ponto de entrada, orquestrando as operações e delegando a lógica para serviços mais especializados.

```mermaid
graph TD
    subgraph "Frontend"
        Controle(SubprocessoControle)
    end

    subgraph "Módulo Subprocesso"
        Facade(SubprocessoService - Fachada)

        subgraph "Serviços Especializados"
            Workflow(SubprocessoWorkflowService)
            DtoBuilder(SubprocessoDtoService)
            Mapa(SubprocessoMapaService)
            Notificacao(SubprocessoNotificacaoService)
        end

        Repos(Repositórios JPA)
    end

    Controle -- Utiliza --> Facade

    Facade -- Orquestra e delega para --> Workflow
    Facade -- Orquestra e delega para --> DtoBuilder
    Facade -- Orquestra e delega para --> Mapa
    Facade -- Orquestra e delega para --> Notificacao

    Workflow & DtoBuilder & Mapa & Notificacao -- Acessam --> Repos
```

## Componentes Principais

### Camada de Fachada
- **`SubprocessoService`**: O ponto de entrada do módulo. Ele expõe métodos de alto nível (ex: `devolverCadastro`, `disponibilizarMapa`) e orquestra os serviços especializados para executar a ação.

### Serviços Especializados
- **`SubprocessoWorkflowService`**: O coração da máquina de estados. Contém a lógica para todas as transições de estado, validando a situação atual, atualizando-a e criando o registro de `Movimentacao`.
- **`SubprocessoDtoService`**: Responsável por construir os DTOs de visualização complexos (ex: `SubprocessoCadastroDto`), que agregam dados de múltiplas fontes.
- **`SubprocessoMapaService`**: Contém a lógica de negócio relacionada à interação entre o subprocesso e o mapa de competências.
- **`SubprocessoNotificacaoService`**: Gerencia o envio de notificações (e-mails, alertas) específicas para os eventos do subprocesso.

### Outros Componentes
- **`SubprocessoControle`**: Expõe a API REST para o frontend. Cada endpoint corresponde a uma ação do usuário no workflow.
- **`modelo/`**: Contém as entidades JPA `Subprocesso` e `Movimentacao`.
- **`SituacaoSubprocesso`**: Enum que define todos os estados possíveis do workflow.

## Diagrama da Máquina de Estados
O fluxo de trabalho do subprocesso segue o diagrama de estados abaixo:

```mermaid
stateDiagram-v2
    direction LR

    [*] --> PENDENTE_CADASTRO: Processo iniciado

    PENDENTE_CADASTRO --> CADASTRO_DISPONIBILIZADO: disponibilizarCadastro()
    CADASTRO_DISPONIBILIZADO --> PENDENTE_AJUSTES_CADASTRO: devolverCadastro()
    PENDENTE_AJUSTES_CADASTRO --> CADASTRO_DISPONIBILIZADO: disponibilizarCadastro()

    CADASTRO_DISPONIBILIZADO --> MAPA_EM_ANALISE: aceitarCadastro()
    MAPA_EM_ANALISE --> MAPA_AJUSTADO: submeterMapaAjustado()

    MAPA_AJUSTADO --> PENDENTE_AJUSTES_MAPA: devolverMapa()
    PENDENTE_AJUSTES_MAPA --> MAPA_AJUSTADO: submeterMapaAjustado()

    MAPA_AJUSTADO --> MAPA_VALIDADO: validarMapa()
    MAPA_VALIDADO --> MAPA_HOMOLOGADO: homologarMapa()
    MAPA_HOMOLOGADO --> [*]: Processo finalizado
```

## Trilha de Auditoria (`Movimentacao`)
Para cada transição de estado no diagrama acima, uma nova entidade `Movimentacao` é criada e persistida. Isso cria um histórico imutável e detalhado de todas as ações realizadas em um subprocesso, garantindo total rastreabilidade.