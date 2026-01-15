# Módulo de Alerta


## Visão Geral

Este pacote é responsável por criar e gerenciar **alertas internos** do sistema, que são notificações exibidas na
interface do usuário. Ele funciona de maneira reativa, "escutando" eventos de domínio publicados por outros módulos (
como o `processo`) e gerando os alertas correspondentes.

O objetivo é notificar os usuários sobre eventos que exigem sua atenção, como o início de um novo processo ou a
devolução de um mapa para ajuste.

## Arquitetura e Funcionamento

A principal característica deste módulo é seu **baixo acoplamento**. Ele não é invocado diretamente. Em vez disso, o
`EventoProcessoListener` (localizado no pacote `notificacao`) ouve os eventos de domínio e invoca o `AlertaFacade` para
criar os alertas correspondentes.

```mermaid
graph TD
    subgraph "Módulo Orquestrador"
        P[processo]
    end

    subgraph "Infraestrutura Spring"
        EventBus(ApplicationEventPublisher)
    end

    subgraph "Módulo de Notificação"
        Listener(notificacao.EventoProcessoListener)
    end

    subgraph "Módulo Alerta (este pacote)"
        Service(AlertaFacade)
        Controle(AlertaController)
        Repo[AlertaRepo]
    end

    subgraph "Módulo de Integração"
        SGRH[sgrh]
    end

    P -- 1. Publica evento --> EventBus
    EventBus -- 2. Notifica --> Listener
    Listener -- 3. Invoca --> Service
    Service -- 4. Consulta --> SGRH
    Service -- 5. Persiste via --> Repo

    Controle -- Expõe API para --> Frontend
    Controle -- Utiliza --> Service
```

### Fluxo de Trabalho

1. **Publicação do Evento:** Um módulo de negócio, como o `processo`, publica um evento (ex: `ProcessoIniciadoEvento`).
2. **Escuta do Evento:** O `EventoProcessoListener` (do pacote `notificacao`) captura o evento.
3. **Criação do Alerta:** O listener invoca o `AlertaFacade` (deste pacote), que contém a lógica para:
    * Construir a mensagem de alerta apropriada.
    * Consultar o `SgrhService` para identificar os usuários destinatários (ex: chefe da unidade).
    * Persistir as entidades `Alerta` e `AlertaUsuario` no banco de dados.
4. **Interação do Usuário:** O `AlertaController` expõe uma API REST para que o frontend possa listar e marcar os
   alertas do usuário logado como lidos.

## Componentes Principais

### Controladores e Serviços

- **`AlertaFacade`**: Contém a lógica de negócio para criar, formatar e persistir os alertas, além de gerenciar sua
  leitura. É invocado pelo `EventoProcessoListener` central.
- **`AlertaController`**: Expõe endpoints REST (`GET /api/alertas`, `POST /api/alertas/{codigo}/marcar-como-lido`) para
  o frontend.

### Modelo de Dados (`model`)

- **`Alerta`**: Entidade JPA que representa o alerta em si (título, mensagem, data).
- **`AlertaUsuario`**: Entidade que associa um alerta a um usuário específico, controlando se já foi lido ou não.
- **`AlertaRepo` / `AlertaUsuarioRepo`**: Repositórios Spring Data.
- **`TipoAlerta`**: Enum que define os tipos de alerta (ex: `INFORMATIVO`, `ACAO_NECESSARIA`).

### DTOs (`dto`)

- **`AlertaDto`**: Objeto de transferência de dados utilizado para enviar informações de alertas para o frontend.
- **`AlertaMapper`**: Interface MapStruct para conversão entre entidade `Alerta`/`AlertaUsuario` e `AlertaDto`.


## Como Testar

Para executar apenas os testes deste módulo (a partir do diretório `backend`):
```bash
./gradlew test --tests "sgc.alerta.*"
```
