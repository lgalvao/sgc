# Pacote `processo`

## Visão Geral

O pacote `processo` é o coração do backend do SGC, atuando como o principal orquestrador dos fluxos de negócio. Ele é responsável por gerenciar o ciclo de vida de um **Processo**, que representa uma iniciativa de alto nível, como um "Mapeamento de Competências" ou uma "Revisão Anual".

A arquitetura deste pacote é notável pelo seu design desacoplado, utilizando um sistema de eventos do Spring (`ApplicationEventPublisher`) para comunicar marcos importantes (como o início e a finalização de um processo) a outros módulos, como `alerta` e `notificacao`, sem criar dependências diretas.

## Diagrama de Fluxo: Iniciação de um Processo

O diagrama abaixo ilustra o fluxo de trabalho quando um novo processo de mapeamento é iniciado.

```mermaid
sequenceDiagram
    participant Frontend as Usuário (Frontend)
    participant ProcessoControle as API (ProcessoControle)
    participant ProcessoService as Serviço (ProcessoService)
    participant Database as Banco de Dados
    participant EventPublisher as Publicador de Eventos
    participant AlertaService as Módulo de Alertas
    participant NotificacaoService as Módulo de Notificações

    Frontend->>+ProcessoControle: POST /api/processos/{id}/iniciar
    ProcessoControle->>+ProcessoService: iniciarProcessoMapeamento(id, unidades)

    ProcessoService->>ProcessoService: Inicia Transação
    ProcessoService->>Database: Valida Processo (Situação='CRIADO')
    ProcessoService->>Database: Valida Unidades (Não estão em processo ativo)

    loop Para cada Unidade
        ProcessoService->>Database: Cria Snapshot (UnidadeProcesso)
        ProcessoService->>Database: Cria Mapa
        ProcessoService->>Database: Cria Subprocesso
        ProcessoService->>Database: Cria Movimentação inicial
    end

    ProcessoService->>Database: Atualiza Processo (Situação='EM_ANDAMENTO')

    ProcessoService->>+EventPublisher: Publica ProcessoIniciadoEvento
    EventPublisher-->>-AlertaService: Notifica evento
    AlertaService->>Database: Cria Alertas para gestores

    EventPublisher-->>-NotificacaoService: Notifica evento
    NotificacaoService->>Database: Enfileira E-mails para envio

    ProcessoService-->>-ProcessoControle: Retorna ProcessoDto
    ProcessoControle-->>-Frontend: Resposta 200 OK
```

## Componentes Principais

### `ProcessoControle.java`

A camada de API REST do pacote. Expõe endpoints para as operações CRUD (`criar`, `atualizar`, `apagar`) e, mais importante, para as ações que disparam os fluxos de negócio, como `iniciar` e `finalizar` um processo.

### `ProcessoService.java`

O cérebro do módulo. Contém toda a lógica de negócio para gerenciar o ciclo de vida de um processo. Seus métodos são transacionais, garantindo a consistência dos dados.

- **`iniciarProcessoMapeamento()` / `iniciarProcessoRevisao()`**: Orquestram a complexa lógica de iniciar um processo, que inclui:
    1.  Validação de regras de negócio.
    2.  Criação de "snapshots" das unidades participantes (`UnidadeProcesso`).
    3.  Criação de `Subprocessos` e `Mapas`.
    4.  Publicação de um `ProcessoIniciadoEvento` para notificar outros módulos.
- **`finalizar()`**: Orquestra a finalização, validando se todos os subprocessos foram homologados, tornando os mapas vigentes e notificando os usuários.

### `modelo/Processo.java`

A entidade JPA que representa um processo no banco de dados. Contém informações como tipo, situação, descrição e datas.

### `eventos/`

Este subpacote contém as classes de eventos de domínio, como `ProcessoIniciadoEvento`. Esses eventos são registros imutáveis (`record`) que carregam dados essenciais sobre o que aconteceu, permitindo que outros componentes reajam de forma desacoplada.

## Fluxo Principal (Detalhado)

1.  **Requisição**: Uma chamada é feita para o endpoint `POST /api/processos/{id}/iniciar`.
2.  **Controle**: `ProcessoControle` recebe a chamada e a delega para o `ProcessoService`.
3.  **Serviço (Transacional)**: `ProcessoService` executa a lógica de negócio dentro de uma transação.
    - **Validação**: Verifica se o processo está na situação "CRIADO" e se as unidades selecionadas não estão participando de outro processo ativo.
    - **Criação de Entidades**: Itera sobre as unidades participantes, criando um `Subprocesso`, um `Mapa` associado e um `UnidadeProcesso` (snapshot) para cada uma.
    - **Mudança de Estado**: Atualiza a situação do `Processo` para "EM_ANDAMENTO".
    - **Publicação de Evento**: Cria e publica um `ProcessoIniciadoEvento` usando o `ApplicationEventPublisher` do Spring.
4.  **Reação ao Evento (Assíncrona)**:
    - O `AlertaService` "escuta" o evento e cria os alertas necessários para os gestores das unidades.
    - O `NotificacaoService` "escuta" o mesmo evento e prepara os e-mails a serem enviados.
5.  **Resposta**: Se todas as operações no `ProcessoService` forem bem-sucedidas, a transação é confirmada e uma resposta de sucesso é enviada ao cliente. Se ocorrer qualquer erro, a transação é revertida, garantindo que o sistema permaneça em um estado consistente.