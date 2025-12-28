# Relatório de Arquitetura: Evolução Modular e Desacoplamento

**Data:** 25/12/2025
**Assunto:** Análise do estado atual da arquitetura modular do backend do SGC após refatoração.

## 1. Introdução

Este documento descreve a arquitetura atual do backend do Sistema de Gestão de Competências (SGC), detalhando as melhorias estruturais implementadas para reduzir o acoplamento entre domínios e garantir a manutenibilidade a longo prazo.

Diferente da proposta anterior que visava a adoção da biblioteca *Spring Modulith*, a refatoração seguiu uma abordagem de **Modularização Manual com Convenções Fortes**, utilizando recursos nativos do Spring Framework e garantias estáticas via ArchUnit.

## 2. Visão Geral da Arquitetura

O sistema opera como um **Monolito Modular**. O código é organizado em pacotes de domínio bem definidos, onde cada pacote funciona como um módulo lógico.

### 2.1. Módulos Principais

*   **`sgc.processo`**: Orquestrador central dos fluxos de avaliação (Mapeamento, Revisão, Diagnóstico).
*   **`sgc.subprocesso`**: Gerencia o ciclo de vida da atividade de uma unidade específica dentro de um processo.
*   **`sgc.unidade`**: Mantém a estrutura organizacional e hierarquia.
*   **`sgc.notificacao` / `sgc.alerta`**: Módulos reativos que respondem a eventos de domínio para gerar feedbacks aos usuários.
*   **`sgc.atividade` / `sgc.competencia`**: Núcleo de dados das avaliações.

## 3. Estratégias de Desacoplamento Implementadas

A refatoração recente eliminou o "acoplamento oculto" (onde serviços acessavam tabelas de outros módulos diretamente) através de três pilares:

### 3.1. Fachadas de Serviço (Service Facades)
Serviços de um módulo **nunca** injetam Repositórios (`Repository`) de outro módulo.
*   *Antes:* `ProcessoService` injetava `UnidadeRepo`.
*   *Agora:* `ProcessoService` injeta `UnidadeService`.
*   *Benefício:* O módulo `unidade` pode alterar sua persistência (nomes de tabelas, cache, queries) sem quebrar o módulo `processo`.

### 3.2. Arquitetura Orientada a Eventos (Event-Driven)
Para operações que são "efeitos colaterais" (como enviar e-mail ou criar alerta), o módulo emissor não conhece o consumidor.
*   *Mecanismo:* Uso de `ApplicationEventPublisher` do Spring.
*   *Exemplo:*
    1.  `ProcessoService` finaliza um processo e publica `EventoProcessoFinalizado`.
    2.  `EventoProcessoListener` (no pacote `notificacao`) escuta o evento.
    3.  O listener orquestra o envio de e-mails e criação de alertas.
*   *Benefício:* `ProcessoService` não precisa conhecer a lógica de e-mails, nem ter dependência de bibliotecas de template ou correio.

### 3.3. Garantia de Conformidade (ArchUnit)
Para impedir a degradação da arquitetura, regras de teste automatizadas foram implementadas em `sgc.arquitetura.ArchConsistencyTest`:

```java
services_should_not_access_other_modules_repositories
```

Esta regra falha o build se, por exemplo, um desenvolvedor tentar injetar `AlertaRepo` dentro de `UsuarioService`. Isso força o uso das APIs públicas (Services) de cada módulo.

## 4. Comparativo: Proposta Original vs. Implementação Real

| Característica | Proposta Spring Modulith | Implementação Real (Atual) |
| :--- | :--- | :--- |
| **Definição de Módulos** | Automática (baseada em pacotes) | Baseada em pacotes (sgc.*) |
| **Controle de Acesso** | `package-info.java` / Visibilidade | ArchUnit (`ArchConsistencyTest`) |
| **Comunicação** | Interfaces explícitas | Injeção de Services (Facade) |
| **Eventos** | Event Registry / Outbox | Spring Events (`@EventListener`) |
| **Dependência** | `spring-modulith-starter` | Sem dependência extra (Nativo Spring) |

## 5. Conclusão

A refatoração foi bem-sucedida em atingir os objetivos de **baixo acoplamento** e **alta coesão** sem a necessidade de introduzir a complexidade ou dependências adicionais do framework Spring Modulith.

A arquitetura atual é robusta, testável e protegida por testes de arquitetura (ArchUnit), garantindo que as fronteiras entre os módulos sejam respeitadas durante a evolução do sistema.
