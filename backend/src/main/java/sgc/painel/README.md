# Módulo Painel

## Visão Geral
O módulo `painel` é responsável por fornecer os dados agregados para o dashboard (painel de controle) da aplicação. Ele centraliza a lógica de negócio que coleta informações de diferentes módulos, como `processo` e `alerta`, para apresentar uma visão consolidada ao usuário.

## Arquitetura
Este módulo segue a arquitetura padrão da aplicação, com uma camada de Controle e uma de Serviço.

```mermaid
graph TD
    subgraph "Frontend"
        DashboardUI
    end

    subgraph "Módulo Painel"
        Controle(PainelController)
        Service(PainelService)
    end

    subgraph "Outros Módulos"
        ProcessoRepo(processo.ProcessoRepo)
        AlertaRepo(alerta.AlertaRepo)
    end

    DashboardUI -- Requisições HTTP --> Controle
    Controle -- Delega para --> Service
    Service -- Consulta --> ProcessoRepo
    Service -- Consulta --> AlertaRepo
```

## Componentes Principais

- **`PainelController`**: Expõe a API REST para o painel. Recebe as requisições do frontend e delega a busca dos dados
  para o `PainelService`.
- **`PainelService`**: Contém a lógica de negócio para buscar e agregar os dados. Ele interage com os repositórios de outros módulos para construir as visualizações necessárias para o painel.

## Fluxos de Trabalho

1. O frontend solicita os dados do painel ao `PainelController`.
2. O `PainelController` invoca o `PainelService` com os parâmetros da requisição (ex: perfil do usuário, unidade).
3.  O `PainelService` consulta o `ProcessoRepo` e o `AlertaRepo` para buscar os dados relevantes.
4.  O `PainelService` processa e agrega os dados, retornando uma página de DTOs (`ProcessoResumoDto`, `AlertaDto`).
5. O `PainelController` retorna os dados como uma resposta JSON para o frontend.
