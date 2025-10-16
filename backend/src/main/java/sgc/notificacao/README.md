# Módulo de Notificação

## Visão Geral
Este pacote é responsável pelo envio de **notificações por e-mail** para os usuários. Ele é projetado para ser robusto e desacoplado, funcionando de maneira reativa a eventos de domínio para garantir que as falhas no envio de e-mails não afetem os fluxos de negócio principais.

## Arquitetura Orientada a Eventos
O módulo é um exemplo clássico de arquitetura orientada a eventos. Ele não é chamado diretamente por outros serviços. Em vez disso, o `EventoProcessoListener` se inscreve para receber eventos publicados pelo `ApplicationEventPublisher` do Spring.

```mermaid
graph TD
    subgraph "Módulo de Negócio (ex: processo)"
        ProcessoService -- 1. Publica evento -->
    end

    subgraph "Infraestrutura Spring"
        EventBus(ApplicationEventPublisher)
    end

    subgraph "Módulo de Notificação"
        Listener(EventoProcessoListener)
        TemplateService(NotificacaoModeloEmailService)
        EmailService(NotificacaoService)
    end

    subgraph "Infraestrutura de E-mail"
        MailServer
    end

    EventBus -- 2. Notifica --> Listener
    Listener -- 3. Usa --> TemplateService
    Listener -- 4. Invoca --> EmailService
    TemplateService -- Gera HTML --> Listener
    EmailService -- 5. Envia para --> MailServer
```

### Fluxo de Trabalho:
1.  **Ação de Negócio:** O `ProcessoService` executa uma ação (ex: inicia um processo).
2.  **Publicação do Evento:** Ele publica um evento de domínio (ex: `ProcessoIniciadoEvento`).
3.  **Captura do Evento:** O `EventoProcessoListener` captura este evento.
4.  **Criação do Template:** O listener usa o `NotificacaoModeloEmailService` para gerar o conteúdo HTML do e-mail a partir dos dados do evento.
5.  **Envio:** O listener invoca o `NotificacaoService` para enviar o e-mail.

## Componentes Principais
- **`EventoProcessoListener`**: O ponto de entrada do módulo. Ouve os eventos de domínio e orquestra o envio da notificação correspondente.
- **`NotificacaoService`**: Serviço responsável pela lógica de envio de e-mails.
  - **Assíncrono (`@Async`):** O envio é executado em uma thread separada para não bloquear a operação principal.
  - **Persistência e Auditoria:** Salva um registro da `Notificacao` no banco de dados antes do envio.
  - **Retentativas:** Em caso de falha, tenta reenviar o e-mail automaticamente.
- **`NotificacaoModeloEmailService`**: Um serviço utilitário focado em construir o corpo HTML dos e-mails usando o Thymeleaf. Cada método corresponde a um tipo de notificação.
- **`Notificacao`**: A entidade JPA que representa o registro de uma notificação enviada.

## Benefícios da Arquitetura
- **Desacoplamento:** O `ProcessoService` não sabe nada sobre como as notificações são enviadas. Ele apenas anuncia que "algo aconteceu". Outros módulos poderiam ouvir o mesmo evento para diferentes propósitos (ex: logging, analytics) sem nenhuma alteração no `ProcessoService`.
- **Robustez:** O envio assíncrono e com retentativas torna o sistema resiliente a falhas temporárias na infraestrutura de e-mail. Uma falha no envio não causa a falha da transação de negócio principal.