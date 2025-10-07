# Módulo de Subprocessos - SGC

## Visão Geral
O pacote `subprocesso` é o motor do fluxo de trabalho (workflow) do SGC. Enquanto o pacote `processo` gerencia a iniciativa de alto nível, o `subprocesso` gerencia a jornada detalhada que cada unidade organizacional individual percorre dentro desse processo.

Cada `Subprocesso` representa a tarefa de uma única unidade (ex: a "Zona Eleitoral 001" preenchendo seu mapa de competências). Este pacote gerencia o estado, as transições, as validações e o histórico de cada uma dessas tarefas individuais.

## Arquivos e Componentes Principais

### Entidades Core

#### 1. `Subprocesso.java`
**Localização:** `backend/src/main/java/sgc/subprocesso/Subprocesso.java`
- **Descrição:** A entidade JPA central que representa a tarefa de uma unidade dentro de um processo.
- **Associações:**
  - `ManyToOne` com `Processo` (seu processo pai).
  - `ManyToOne` com `Unidade` (a unidade executora).
  - `ManyToOne` com `Mapa` (o mapa de competências sendo trabalhado).
- **Gerenciamento de Estado:** O campo `situacaoId` é a chave para o workflow, armazenando o estado atual do subprocesso (ex: `PENDENTE`, `CADASTRO_DISPONIBILIZADO`, `MAPA_HOMOLOGADO`).

#### 2. `Movimentacao.java`
**Localização:** `backend/src/main/java/sgc/subprocesso/Movimentacao.java`
- **Descrição:** Uma entidade de log/auditoria. Para **cada ação** realizada em um subprocesso (disponibilizar, devolver, aprovar), um novo registro de `Movimentacao` é criado.
- **Propósito:** Garante uma trilha de auditoria completa, registrando o que aconteceu, quando, e quem enviou de qual unidade para qual unidade.

### Serviço de Workflow

#### 3. `SubprocessoService.java`
**Localização:** `backend/src/main/java/sgc/subprocesso/SubprocessoService.java`
- **Descrição:** Este serviço é o coração do pacote e funciona como um **mecanismo de state machine**. Ele contém a lógica de negócio para cada uma das ações possíveis no ciclo de vida de um subprocesso.
- **Padrão de Ação:** Cada método de ação (ex: `devolverCadastro`, `validarMapa`, `homologarCadastro`) segue um padrão rigoroso dentro de uma transação:
  1.  **Buscar o Subprocesso**: Carrega a entidade do banco de dados.
  2.  **Validar Estado Atual**: Garante que a ação é permitida no estado atual do subprocesso (ex: não se pode "homologar" algo que não foi "disponibilizado").
  3.  **Atualizar Estado**: Altera o `situacaoId` do subprocesso para o novo estado.
  4.  **Criar Movimentação**: Registra a ação na trilha de auditoria criando uma nova `Movimentacao`.
  5.  **Gravar Análise (se aplicável)**: Salva justificativas ou observações em entidades como `AnaliseCadastro` ou `AnaliseValidacao`.
  6.  **Enviar Notificações**: Interage com `NotificationService` e `AlertaRepository` para notificar os usuários relevantes por e-mail e através de alertas no sistema.

### DTOs e Controller

#### 4. `SubprocessoController.java`
**Localização:** `backend/src/main/java/sgc/subprocesso/SubprocessoController.java`
- **Descrição:** O controlador REST que expõe os endpoints para o frontend interagir com o workflow. Cada endpoint geralmente corresponde a um botão de ação na interface do usuário (ex: "Devolver", "Aprovar").

#### 5. DTOs Específicos de Ação
**Localização:** `backend/src/main/java/sgc/subprocesso/`
- **Descrição:** O pacote utiliza uma variedade de DTOs de requisição muito específicos, como `DevolverCadastroRequest.java` e `DisponibilizarMapaRequest.java`. Isso cria um contrato de API claro e robusto para cada ação do workflow.

## Exemplo de Fluxo: Devolução de um Cadastro

1.  **Ação do Usuário**: Um gestor clica no botão "Devolver" na tela de um cadastro que está sendo analisado, preenchendo um motivo.
2.  **Requisição**: O frontend envia uma `POST` para `/api/subprocessos/{id}/devolver-cadastro` com o `DevolverCadastroRequest` no corpo.
3.  **Controller**: O `SubprocessoController` recebe a requisição e chama `subprocessoService.devolverCadastro(...)`.
4.  **Serviço (em uma transação)**:
    a. O serviço carrega o `Subprocesso` com o ID fornecido.
    b. Ele verifica se a `situacaoId` atual é `CADASTRO_DISPONIBILIZADO`. Se não for, lança uma exceção.
    c. Ele muda a `situacaoId` para `CADASTRO_EM_ELABORACAO`.
    d. Cria uma `Movimentacao` registrando a devolução da unidade superior para a unidade original.
    e. Salva o motivo da devolução em uma nova entidade `AnaliseCadastro`.
    f. Envia um e-mail e cria um alerta para o responsável da unidade original, informando sobre a devolução.
5.  **Resposta**: O controller retorna uma resposta de sucesso. Se qualquer passo falhar, a transação é revertida, e o estado do subprocesso permanece inalterado.

## Notas Importantes
- **Motor do Negócio**: Este pacote é o verdadeiro motor que impulsiona o fluxo de trabalho principal do SGC. A lógica aqui contida é a representação mais fiel das regras de negócio do sistema.
- **Trilha de Auditoria Imutável**: O uso da entidade `Movimentacao` para registrar cada passo garante uma trilha de auditoria completa e imutável, que é essencial para a transparência e a responsabilidade no processo.
- **Complexidade Gerenciada**: Embora a lógica seja complexa, ela é bem gerenciada através da separação de responsabilidades entre o serviço (regras de negócio), a entidade (estado) e a `Movimentacao` (histórico).