# Módulo de Processos - SGC

## Visão Geral
O pacote `processo` é o principal orquestrador de fluxos de trabalho do sistema. Ele gerencia o ciclo de vida de um **Processo**, que é uma iniciativa de alto nível, como um "Mapeamento de Competências" ou uma "Revisão Anual".

Este módulo é o ponto de partida para as operações mais importantes do SGC. Ele não apenas gerencia a entidade `Processo`, mas também coordena a criação de `Subprocessos`, `Mapas`, `Alertas` e `Notificações`, utilizando uma arquitetura orientada a eventos para manter o baixo acoplamento com outros módulos.

## Arquivos e Componentes Principais

### Entidades

#### 1. `Processo.java`
**Localização:** `backend/src/main/java/sgc/processo/Processo.java`
- **Descrição:** A entidade JPA que representa um processo.
- **Campos Importantes:**
  - `tipo`: O tipo do processo (ex: `MAPEAMENTO`, `REVISAO`).
  - `situacao`: O estado atual do processo no seu ciclo de vida (`CRIADO`, `EM_ANDAMENTO`, `FINALIZADO`).
  - `dataLimite`: A data limite para a conclusão de etapas.

#### 2. `UnidadeProcesso.java`
**Localização:** `backend/src/main/java/sgc/processo/UnidadeProcesso.java`
- **Descrição:** Uma entidade de "snapshot" (fotografia). Quando um processo é iniciado, os dados das unidades participantes são copiados para esta tabela. Isso garante que, mesmo que os dados da unidade original mudem, o processo mantenha um registro histórico de como a unidade era no momento de sua execução.

### Serviço de Negócio

#### 3. `ProcessoService.java`
**Localização:** `backend/src/main/java/sgc/processo/ProcessoService.java`
- **Descrição:** O cérebro do pacote. Este serviço contém a lógica de negócio complexa para gerenciar o ciclo de vida dos processos.
- **Funcionalidades Chave:**
  - **Criação e Atualização**: Métodos para criar e atualizar um processo enquanto ele está no estado `CRIADO`.
  - **Iniciação de Processo**: Métodos como `iniciarProcessoMapeamento` e `startRevisionProcess` que executam uma série de ações em uma única transação:
    - Validam regras de negócio (ex: a unidade já está em outro processo ativo?).
    - Criam os `Subprocessos` para cada unidade participante.
    - Criam ou copiam os `Mapas` de competências.
    - Criam os "snapshots" em `UnidadeProcesso`.
    - Publicam um `EventoProcessoIniciado`.
  - **Finalização de Processo**: O método `finalizeProcess` orquestra a conclusão de um processo:
    - Valida se todos os `Subprocessos` foram homologados.
    - Promove os mapas dos subprocessos a "mapas vigentes" (uma ação crítica).
    - Altera o status do processo para `FINALIZADO`.
    - Envia e-mails de notificação customizados.
    - Publica um `EventoProcessoFinalizado`.
  - **Controle de Acesso**: Valida se o usuário tem permissão para ver os detalhes de um processo com base em seu perfil (`ADMIN` vs. `GESTOR`).

### Arquitetura de Eventos

#### 4. `EventoProcessoIniciado.java` e `EventoSubprocessoDisponibilizado.java`
**Localização:** `backend/src/main/java/sgc/processo/`
- **Descrição:** Classes de registro (records) que representam os eventos de domínio.
- **Funcionamento:** O `ProcessoService` publica esses eventos usando o `ApplicationEventPublisher` do Spring. Outros componentes em pacotes diferentes (como `AlertaService`) podem "escutar" esses eventos e executar ações (como criar um alerta) sem que o `ProcessoService` precise conhecê-los diretamente. Isso resulta em um design limpo e desacoplado.

### DTOs

#### 5. `dto/`
**Localização:** `backend/src/main/java/sgc/processo/dto/`
- **Descrição:** Contém os Data Transfer Objects para a comunicação com a API.
- **DTOs Notáveis:**
  - `ReqCriarProcesso.java`: DTO para a criação de um novo processo.
  - `ProcessoDetalheDTO.java`: Um DTO rico que agrega todas as informações de um processo, incluindo seus subprocessos e os snapshots das unidades, para ser exibido na tela de detalhes.

## Fluxo de Iniciação de um Processo
1.  **Requisição**: O usuário, através da interface, solicita o início de um processo criado.
2.  **Controller**: O `ProcessoController` recebe a requisição e chama o `ProcessoService`.
3.  **Serviço (em uma transação)**:
    a. `ProcessoService` valida as regras de negócio.
    b. Para cada unidade participante, o serviço cria um `Subprocesso`, um `Mapa` e uma `Movimentacao`.
    c. O serviço cria um "snapshot" da unidade em `UnidadeProcesso`.
    d. O status do `Processo` é alterado para `EM_ANDAMENTO`.
    e. No final, o serviço publica um `EventoProcessoIniciado`.
4.  **Listeners de Evento**:
    a. Um listener no pacote `alerta` recebe o evento e cria os alertas para os usuários das unidades.
    b. Um listener no pacote `notificacao` recebe o evento e enfileira os e-mails a serem enviados.
5.  **Resposta**: O controller retorna a resposta ao cliente. Se qualquer passo falhar, a transação é revertida, e nenhuma das ações (criação de subprocessos, mapas, etc.) é efetivada.

## Notas Importantes
- **Orquestrador Central**: Este pacote é o principal orquestrador da aplicação. Entender seu funcionamento é fundamental para entender o fluxo de negócio do SGC.
- **Design Desacoplado**: O uso de eventos é um ponto forte da arquitetura deste pacote, permitindo que novas funcionalidades (como um novo tipo de notificação) sejam adicionadas sem modificar o `ProcessoService`.
- **Importância do Snapshot**: A tabela `UnidadeProcesso` é crucial para a auditoria e a integridade histórica dos processos, garantindo que os dados do processo permaneçam consistentes mesmo que as unidades organizacionais mudem ao longo do tempo.