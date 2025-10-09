# Documentação da Arquitetura do Backend - SGC

## Visão Geral

Este documento serve como um guia central para a arquitetura do backend do Sistema de Gestão de Competências (SGC). O sistema é construído em Java com o framework Spring Boot e segue uma arquitetura modular, onde cada pacote de negócio possui responsabilidades bem definidas.

A arquitetura é projetada em torno de três conceitos principais:

1.  **Orquestração de Processos (`processo`)**: Gerencia as iniciativas de alto nível, como "Mapeamento Anual" ou "Revisão de Competências". É o ponto de partida para os principais fluxos de trabalho.
2.  **Máquina de Estados de Subprocessos (`subprocesso`)**: Controla o ciclo de vida detalhado das tarefas de cada unidade organizacional dentro de um processo. Funciona como o motor do workflow, gerenciando estados, transições e uma trilha de auditoria completa (`Movimentacao`).
3.  **Gestão de Mapas de Competências (`mapa`)**: Lida com a criação, manipulação, cópia e análise de impacto dos mapas de competências, que são os artefatos centrais gerados pelo sistema.

A comunicação entre os módulos é, em grande parte, **desacoplada** através de um sistema de eventos de domínio (`ApplicationEventPublisher`), permitindo que pacotes de suporte como `alerta` e `notificacao` reajam a eventos de negócio sem criar dependências diretas.

## Documentação Detalhada por Módulo

Para uma análise aprofundada de cada componente da arquitetura, consulte os documentos abaixo:

- **Modelo de Dados**
    - [Diagrama de Entidade-Relacionamento (ER)](./docs/entidades.md) - *Visão consolidada do modelo de dados principal.*

- **Módulos de Negócio Principais**
    - [Pacote `processo`](./docs/processo.md) - *O orquestrador de alto nível.*
    - [Pacote `subprocesso`](./docs/subprocesso.md) - *O motor do workflow e da máquina de estados.*
    - [Pacote `mapa`](./docs/mapa.md) - *Gerenciamento dos mapas de competências.*

- **Módulos de Suporte**
    - [Pacote `alerta`](./docs/alerta.md) - *Sistema de notificações internas.*
    - [Pacote `notificacao`](./docs/notificacao.md) - *Sistema de notificações externas (e-mail).*

- **Camada de Integração**
    - [Pacote `sgrh`](./docs/sgrh.md) - *Camada de integração com o sistema de RH (atualmente com dados mock).*