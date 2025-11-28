# Padrões de Arquitetura e Desenvolvimento Backend

Este documento consolida os padrões arquiteturais, convenções de nomenclatura e melhores práticas identificadas no backend do Sistema de Gestão de Competências (SGC). O objetivo é servir como guia de referência para manter a consistência e qualidade do código.

## 1. Visão Geral da Arquitetura

O SGC utiliza uma arquitetura **Modular Monolith** baseada em Spring Boot. O sistema é dividido em pacotes de domínio (`processo`, `mapa`, `atividade`, etc.) que encapsulam suas regras de negócio e persistem dados.

A comunicação entre módulos ocorre de duas formas:

1. **Síncrona:** Chamadas diretas de método entre Serviços (Fachadas).
2. **Assíncrona/Reativa:** Publicação e escuta de Eventos de Domínio (Spring Events) para desacoplamento (ex: Processo -> Notificação).

### Diagrama de Arquitetura Geral

```mermaid
graph TD
    subgraph "Camada de API"
        API[Controladores REST]
    end

    subgraph "Camada de Serviço"
        Servico[Serviços de Negócio (Fachadas)]
    end

    subgraph "Camada de Domínio e Dados"
        Dominio[Entidades JPA & Repositórios]
    end

    subgraph "Módulos Reativos"
        Eventos[Eventos de Domínio]
        Notificacao[Notificação]
        Alerta[Alerta]
    end

    API --> Servico
    Servico --> Dominio
    Servico -- Publica --> Eventos
    Eventos --> Notificacao
    Eventos --> Alerta
```

## 2. Padrões de Nomenclatura

O idioma oficial do projeto é o **Português Brasileiro**.

- **Classes:** PascalCase (ex: `UsuarioService`, `MapaController`).
- **Métodos e Variáveis:** camelCase (ex: `buscarPorId`, `dataCriacao`).
- **Pacotes:** minúsculo, sem separadores, representando o domínio (ex: `sgc.processo`, `sgc.mapa`).
- **Exceções:** Prefixo `Erro` (ex: `ErroEntidadeNaoEncontrada`).
- **Sufixos Padronizados:**
  - Controladores: `Controller` (ex: `ProcessoController`).
  - Serviços: `Service` (ex: `MapaService`).
  - Repositórios: `Repo` (ex: `SubprocessoRepo`).
  - Testes: `Test` (ex: `MapaServiceTest`).
  - DTOs: `Dto`, `Req` (Request), `Resp` (Response).

## 3. Padrões de Projeto (Design Patterns)

### 3.1. Service Facade (Fachada de Serviço)

Cada módulo possui um serviço principal que atua como ponto de entrada único para operações de negócio. Este serviço orquestra serviços especializados internos e delega tarefas.

*Exemplo no módulo `mapa`:*
O `MapaService` é a fachada pública. Ele utiliza internamente `CopiaMapaService`, `ImpactoMapaService` e `MapaVisualizacaoService` para realizar tarefas complexas.

```mermaid
graph TD
    Client[Cliente (Controller ou Outro Módulo)]
    Facade[MapaService (Fachada)]
    Sub1[CopiaMapaService]
    Sub2[ImpactoMapaService]
    Sub3[CompetenciaService]

    Client --> Facade
    Facade --> Sub1
    Facade --> Sub2
    Facade --> Sub3
```

### 3.2. Arquitetura Orientada a Eventos (Event-Driven)

Para evitar acoplamento rígido em fluxos secundários (como enviar e-mail ou criar alerta), o sistema utiliza o `ApplicationEventPublisher` do Spring.

*Fluxo:*

1. `ProcessoService` publica `ProcessoIniciadoEvento`.
2. `EventoProcessoListener` (em `sgc.notificacao`) captura o evento.
3. Listener invoca `AlertaService` (para alerta visual) e `NotificacaoEmailService` (para e-mail).

## 4. Padrões de API REST

### 4.1. Verbos HTTP e Operações

O sistema adota uma convenção específica para operações de escrita, priorizando o uso de POST para ações de negócio explícitas.

- **GET:** Consultas e listagens.
- **POST:** Criação de recursos.
- **POST (com sufixo):** Atualizações e exclusões.
  - Atualizar: `/api/recurso/{id}/atualizar`
  - Excluir: `/api/recurso/{id}/excluir`
  - Ações de Workflow: `/api/processos/{id}/iniciar`, `/api/subprocessos/{id}/devolver-cadastro`

### 4.2. Tratamento de Erros

Todas as exceções de negócio herdam de `RuntimeException` e são tratadas globalmente pelo `RestExceptionHandler` (pacote `sgc.comum.erros`).
A resposta de erro segue o padrão JSON definido na classe `ErroApi`.

### 4.3. DTOs (Data Transfer Objects)

- Entidades JPA **nunca** devem ser expostas diretamente no Controller.
- Utiliza-se DTOs para entrada (`...Req` ou `...Dto`) e saída.
- O mapeamento é feito via **MapStruct**.

## 5. Tecnologias e Ferramentas

- **Java 21**: Uso de recursos modernos da linguagem.
- **Spring Boot 3.5.7**: Framework base.
- **Lombok**: Para redução de boilerplate (Getters, Setters, Builders).
- **MapStruct**: Para conversão eficiente entre Entity e DTO.
- **H2 Database**: Banco em memória para perfil de testes
- **PostgreSQL**: Banco de produção.

## 6. Desvios dos Padrões Identificados

Durante a análise, foram identificados os seguintes pontos que desviam de convenções externas comuns ou da regra estrita de idioma:

1. **Sufixos em Inglês:** Embora a regra seja "Português Brasileiro", os sufixos das classes principais estão em inglês (`Controller`, `Service`, `Repo`), provavelmente por convenção da comunidade Spring/Java. O conteúdo e lógica, porém, seguem em português.
2. **Verbos REST (POST para tudo):** A API não utiliza `PUT`, `PATCH` ou `DELETE` para operações de CRUD padrão. A opção por usar `POST` com sufixos de ação (`/atualizar`, `/excluir`) é uma escolha de design explícita do projeto, divergindo do estilo REST "puro".
3. **Segurança (Em Transição):** A configuração de segurança (`SecurityConfig`) está parcialmente desabilitada ou mockada nos perfis de desenvolvimento, indicando uma área que ainda não atingiu o padrão final de produção.
