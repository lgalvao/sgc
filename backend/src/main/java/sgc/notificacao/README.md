# Módulo de Notificação e Orquestração de Eventos

## Visão Geral
Este pacote tem uma dupla responsabilidade:

1.  **Envio de Notificações:** É responsável pelo envio de **notificações por e-mail** para os usuários de forma robusta e desacoplada.
2.  **Orquestração de Eventos:** Contém o `EventoProcessoListener`, o principal listener de eventos de domínio da aplicação, que também orquestra a criação de **alertas** ao invocar o `AlertaService`.

## Arquitetura Orientada a Eventos
O `EventoProcessoListener` é o coração da arquitetura reativa do SGC. Ele se inscreve para receber eventos publicados pelo `ApplicationEventPublisher` do Spring e, em resposta, delega as tarefas para os módulos `notificacao` e `alerta`.

```mermaid
graph TD
    subgraph "Módulo de Negócio (ex: processo)"
        ProcessoService -- 1. Publica evento -->
    end

    subgraph "Infraestrutura Spring"
        EventBus(ApplicationEventPublisher)
    end

    subgraph "Módulo de Notificação (este pacote)"
        Listener(EventoProcessoListener)
        TemplateService(NotificacaoModeloEmailService)
        EmailService(NotificacaoService)
    end

    subgraph "Módulo de Alerta"
        AlertaService
    end

    subgraph "Infraestrutura de E-mail"
        MailServer
    end

    EventBus -- 2. Notifica --> Listener
    Listener -- 3. Invoca --> AlertaService
    Listener -- 4. Usa --> TemplateService
    Listener -- 5. Invoca --> EmailService
    TemplateService -- Gera HTML --> Listener
    EmailService -- 6. Envia para --> MailServer
```

### Fluxo de Trabalho:
1.  **Ação de Negócio:** O `ProcessoService` executa uma ação (ex: inicia um processo).
2.  **Publicação do Evento:** Ele publica um evento de domínio (ex: `ProcessoIniciadoEvento`).
3.  **Captura do Evento:** O `EventoProcessoListener` captura este evento.
4.  **Criação de Alertas:** O listener invoca o `AlertaService` para criar os alertas internos.
5.  **Criação do E-mail:** O listener usa o `NotificacaoModeloEmailService` para gerar o conteúdo HTML do e-mail.
6.  **Envio do E-mail:** O listener invoca o `NotificacaoService` para enviar o e-mail.

## Componentes Principais
- **`EventoProcessoListener`**: O principal ponto de entrada reativo da aplicação. Ouve os eventos de domínio e orquestra as ações de notificação e alerta.
- **`NotificacaoService`**: Serviço responsável pela lógica de envio de e-mails.
  - **Assíncrono (`@Async`):** O envio é executado em uma thread separada.
  - **Persistência e Auditoria:** Salva um registro da `Notificacao` no banco de dados.
  - **Retentativas:** Em caso de falha, tenta reenviar o e-mail.
- **`NotificacaoModeloEmailService`**: Serviço utilitário focado em construir o corpo HTML dos e-mails usando Thymeleaf.
- **`Notificacao`**: A entidade JPA que representa o registro de uma notificação enviada.

## Benefícios da Arquitetura
- **Desacoplamento:** O `ProcessoService` não sabe como as notificações ou alertas são tratados. Ele apenas anuncia que "algo aconteceu".
- **Robustez:** O envio assíncrono de e-mails torna o sistema resiliente a falhas temporárias na infraestrutura de e-mail.
- **Centralização da Lógica Reativa:** O `EventoProcessoListener` centraliza a resposta aos eventos de domínio, tornando o fluxo de trabalho reativo claro e fácil de entender.
