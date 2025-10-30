# Backend do Sistema de Gestão de Competências (SGC)

## Visão Geral
Este diretório contém o código-fonte do backend do SGC. Ele fornece uma API REST modular para ser consumida pelo frontend. A arquitetura é organizada em pacotes que representam domínios de negócio específicos, promovendo alta coesão e baixo acoplamento. A comunicação entre os módulos centrais é realizada de forma reativa, através de eventos de domínio, o que garante a separação de responsabilidades.

## Diagrama de Arquitetura
O diagrama abaixo ilustra a arquitetura em camadas, destacando as dependências principais entre os pacotes.

```mermaid
graph TD
    subgraph "1. Frontend (Cliente)"
        Frontend[Aplicação Vue.js]
    end

    subgraph "2. Camada de API"
        API[Controladores REST]
    end

    subgraph "3. Camada de Serviço"
        Servico[Serviços de Negócio]
        Processo[Processo Orquestrador]
    end

    subgraph "4. Camada de Domínio e Dados"
        Dominio[Entidades JPA & Repositórios]
        IntegracaoRH[SGRH & Unidade]
    end

    subgraph "5. Módulos Reativos & Suporte"
        Eventos[Eventos de Domínio]
        Notificacao[Notificação]
        Alerta[Alerta]
        Comum[Comum Utilitários]
    end

    Frontend --> API
    API --> Servico
    Servico --> Dominio

    Processo -- Publica --> Eventos
    Eventos --> Notificacao
    Eventos --> Alerta

    IntegracaoRH -- Popula Dados --> Dominio

    Comum -- Suporte Geral --> Servico & Dominio & Notificacao & Alerta
```

## Módulos Principais (`src/main/java/sgc/`)

### 1. `processo` (Orquestrador)
- **Responsabilidade:** Atua como o orquestrador central. Gerencia o ciclo de vida dos processos de alto nível (ex: "Mapeamento Anual de Competências") e dispara eventos de domínio (`ProcessoIniciadoEvento`) para notificar outros módulos, mantendo o sistema desacoplado.

### 2. `subprocesso` (Máquina de Estados e Controladores Especializados)
- **Responsabilidade:** Gerencia o fluxo de trabalho detalhado para cada unidade organizacional. Funciona como uma **máquina de estados**, transitando as tarefas entre diferentes situações (ex: de `PENDENTE_CADASTRO` para `MAPA_AJUSTADO`) e mantendo um histórico imutável de todas as ações através da entidade `Movimentacao`. Para melhor organização, o controlador original foi dividido em `SubprocessoCrudControle` (operações CRUD), `SubprocessoCadastroControle` (ações de workflow da etapa de cadastro), `SubprocessoMapaControle` (operações relacionadas ao mapa) e `SubprocessoValidacaoControle` (ações de workflow da etapa de validação). O `SubprocessoMapaWorkflowService` foi introduzido para gerenciar a lógica de salvamento do mapa no contexto do workflow.

### 3. `mapa`, `competencia`, `atividade` (Domínio Principal)
- **Responsabilidade:** Gerenciam os artefatos centrais do sistema.
- **`mapa`:** Orquestra a criação, cópia e análise de impacto dos Mapas de Competências.
- **`competencia`:** Define as competências que compõem um mapa.
- **`atividade`:** Define as atividades associadas às competências. Este módulo também é responsável por gerenciar os **conhecimentos** vinculados a cada atividade.

### 4. `analise` (Auditoria e Revisão)
- **Responsabilidade:** Registra o histórico de todas as análises de "cadastro" e "validação" realizadas sobre um subprocesso, fornecendo uma trilha de auditoria das revisões.

### 5. `notificacao` e `alerta` (Comunicação Reativa)
- **Responsabilidade:** Módulos reativos que "escutam" os eventos de domínio publicados pelo `processo`.
- **`alerta`:** Cria alertas visíveis dentro da interface do sistema.
- **`notificacao`:** Envia notificações externas (como e-mails) de forma assíncrona.

### 6. `sgrh` e `unidade` (Estrutura e Integração)
- **Responsabilidade:** Fornecem os dados sobre a estrutura organizacional e os usuários.
- **`unidade`:** Modela a hierarquia organizacional (secretarias, seções, etc.). É apenas um modelo de dados, sem lógica de negócio.
- **`sgrh`:** Define os modelos internos (`Usuario`, `Perfil`) e atua como uma fachada (`SgrhService`) para consultar dados de um sistema de RH externo (atualmente simulado).

### 7. `comum`, `config` e `util` (Componentes Transversais)
- **Responsabilidade:** Estes pacotes contêm código de suporte utilizado por toda a aplicação.
- **`comum`**: Centraliza o tratador global de exceções (`RestExceptionHandler`), classes de erro, a `EntidadeBase` para entidades JPA e a lógica do `Painel`.
- **`config`**: Fornece classes de configuração do Spring, como `SecurityConfig` e `ThymeleafConfig`.
- **`util`**: Contém classes de utilidade, como `HtmlUtils`.

## Como Construir e Executar
Para construir o projeto e rodar os testes, utilize o Gradle Wrapper a partir da raiz do repositório:
```bash
./gradlew :backend:build
```

Para executar a aplicação em modo de desenvolvimento (utilizando o banco de dados em memória H2), ative o perfil `local`:

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
```

Para executar o processo em segundo plano e liberar o terminal, adicione `&` ao final do comando:

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local' &
```

A API estará disponível em `http://localhost:10000`.

## Documentação da API (Swagger UI)
A documentação da API é gerada automaticamente com SpringDoc e está acessível em:
[http://localhost:10000/swagger-ui.html](http://localhost:10000/swagger-ui.html)

A especificação OpenAPI em formato JSON pode ser encontrada em:
[http://localhost:10000/api-docs](http://localhost:10000/api-docs)

## Padrões de Design e Boas Práticas
- **Injeção de Dependências:** Utilizada extensivamente pelo Spring Framework.
- **DTOs (Data Transfer Objects):** Usados em toda a camada de controle para desacoplar a API das entidades JPA.
- **Arquitetura Orientada a Eventos:** O `ApplicationEventPublisher` do Spring é usado para desacoplar os módulos `processo`, `alerta` e `notificacao`.
- **Serviços Coesos:** Lógica de negócio complexa é dividida em serviços com responsabilidades únicas (ex: `MapaService` vs. `ImpactoMapaService`).
- **Trilha de Auditoria:** A entidade `Movimentacao` garante um registro histórico completo das ações do workflow.
