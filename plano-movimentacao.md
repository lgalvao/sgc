# Plano de Refatoração: Geração de Movimentações via Eventos

## 1. Análise de Viabilidade

A proposta de utilizar o mecanismo de eventos para gerar registros de `Movimentacao` e disparar notificações é **altamente viável** e alinha o sistema aos princípios de arquitetura orientada a eventos já utilizados em outras partes do projeto (como no `EventoProcessoIniciado`).

### Benefícios Identificados
1.  **Desacoplamento (Separation of Concerns):**
    - Os serviços de domínio (`SubprocessoWorkflowService`, `SubprocessoMapaWorkflowService`) deixam de ser responsáveis por detalhes de auditoria (`Movimentacao`) e notificações (E-mails/Alertas).
    - Eles focam exclusivamente nas regras de negócio de transição de estado e validação.
2.  **Consistência:**
    - Centraliza a lógica de "o que acontece quando o estado muda" em ouvintes (`listeners`), evitando código duplicado ou esquecido em novos fluxos.
3.  **Extensibilidade:**
    - Facilita a adição de novas reações a eventos (ex: logs de segurança, integrações externas) sem modificar o código core do workflow.

### Riscos e Mitigações
-   **Transacionalidade:** A criação da `Movimentacao` é parte crítica da auditoria.
    -   *Mitigação:* Os listeners devem ser síncronos e participar da mesma transação do serviço (`@Transactional`). Se a gravação da movimentação falhar, a mudança de estado deve ser revertida.
-   **Granularidade:** Eventos genéricos podem não carregar contexto suficiente.
    -   *Mitigação:* Criar eventos específicos para cada transição de negócio relevante (ex: `EventoCadastroDisponibilizado`, `EventoMapaValidado`) em vez de um único evento de "Mudança de Estado".

## 2. Passos para Implementação

### Passo 1: Definição dos Eventos de Domínio
Criar novas classes de eventos no pacote `sgc.processo.eventos` (ou criar `sgc.subprocesso.eventos`) para representar cada ação de workflow que gera movimentação.

Eventos sugeridos (baseados nas ações atuais):
-   **Cadastro:**
    -   `EventoSubprocessoCadastroDisponibilizado`
    -   `EventoSubprocessoCadastroDevolvido`
    -   `EventoSubprocessoCadastroAceito`
    -   `EventoSubprocessoCadastroHomologado`
-   **Revisão:**
    -   `EventoSubprocessoRevisaoDisponibilizada`
    -   `EventoSubprocessoRevisaoDevolvida`
    -   `EventoSubprocessoRevisaoAceita`
    -   `EventoSubprocessoRevisaoHomologada`
-   **Mapeamento:**
    -   `EventoSubprocessoMapaIniciado` (Transição para `MAPA_CRIADO`)
    -   `EventoSubprocessoMapaDisponibilizado`
    -   `EventoSubprocessoMapaComSugestoes` (Sugestões apresentadas)
    -   `EventoSubprocessoMapaValidado`
    -   `EventoSubprocessoMapaDevolvido` (Devolução da validação)
    -   `EventoSubprocessoMapaHomologado`
    -   `EventoSubprocessoMapaAjustadoSubmetido`

**Estrutura Base do Evento:**
Todos os eventos devem conter os dados necessários para construir a `Movimentacao` e processar notificações:
-   `codSubprocesso` (Long)
-   `usuario` (Usuario - autor da ação)
-   `observacoes` / `motivo` (String - opcional)
-   `unidadeOrigem` (Unidade - opcional, se não for inferível do subprocesso)
-   `unidadeDestino` (Unidade - opcional)

### Passo 2: Criação do Listener de Movimentação
Criar a classe `MovimentacaoListener` no pacote `sgc.subprocesso.service` (ou `sgc.subprocesso.listeners`).

**Responsabilidades:**
-   Anotar métodos com `@EventListener` para capturar os eventos definidos.
-   Para cada evento:
    1.  Construir a entidade `Movimentacao` com a descrição adequada.
    2.  Persistir usando `SubprocessoMovimentacaoRepo`.
    3.  (Opcional nesta fase) Invocar o `SubprocessoNotificacaoService` para enviar e-mails, removendo essa responsabilidade do workflow service.

### Passo 3: Refatoração dos Serviços de Workflow
Alterar `SubprocessoWorkflowService` e `SubprocessoMapaWorkflowService`.

**Para cada método que altera estado:**
1.  Remover a criação explícita de `Movimentacao`.
2.  Remover a chamada explícita para `subprocessoNotificacaoService` (se movida para o listener).
3.  Instanciar e publicar o evento correspondente usando `ApplicationEventPublisher`.

*Exemplo de Refatoração:*

**Antes:**
```java
sp.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
repositorioSubprocesso.save(sp);
repositorioMovimentacao.save(new Movimentacao(sp, origem, destino, "Validação do mapa", usuario));
notificacaoService.notificarValidacao(sp);
```

**Depois:**
```java
sp.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
repositorioSubprocesso.save(sp);
eventPublisher.publishEvent(new EventoSubprocessoMapaValidado(sp, usuario));
```

### Passo 4: Integração com Alertas e Notificações
Garantir que o novo listener (ou o existente `EventoProcessoListener`) trate também o disparo de **Alertas** (`AlertaService`) e **E-mails** (`NotificacaoEmailService` via `SubprocessoNotificacaoService`), centralizando toda a comunicação assíncrona/reativa.

### Passo 5: Verificação e Testes
1.  **Testes Unitários:** Atualizar os testes dos serviços (`SubprocessoWorkflowServiceTest`) para verificar se o evento correto foi publicado (usando mocks do `ApplicationEventPublisher`), em vez de verificar a persistência no repositório de movimentação.
2.  **Testes de Integração:** Verificar se, ao executar uma ação via API/Controller, a `Movimentacao` é corretamente persistida no banco de dados através do listener.
