# Relatório de Análise: Adoção do Spring Modulith 2.0

**Data:** 25/12/2025
**Assunto:** Análise de viabilidade para adoção do Spring Modulith 2.0 no backend do SGC.

## 1. Introdução

Este relatório analisa a arquitetura atual do backend do Sistema de Gestão de Competências (SGC) e avalia a viabilidade, benefícios e impactos da adoção do framework **Spring Modulith 2.0** (já disponível e compatível com Spring Boot 4.0.1+).

O objetivo é responder se o sistema se beneficiaria dessa migração para reforçar a modularidade, documentação e testabilidade.

## 2. Diagnóstico da Arquitetura Atual

A análise do código fonte revelou os seguintes pontos sobre a arquitetura atual:

*   **Estrutura de Pacotes:** O projeto já está organizado por domínios funcionais (`processo`, `subprocesso`, `unidade`, etc.), o que é um pré-requisito essencial para o Modulith.
*   **Acoplamento:** Existe um acoplamento direto entre camadas de persistência de diferentes módulos.
    *   *Exemplo:* O `ProcessoService` (pacote `processo`) injeta diretamente `UnidadeRepo` (pacote `unidade`) e `SubprocessoRepo` (pacote `subprocesso`).
    *   *Impacto:* Isso quebra o encapsulamento do módulo. Alterações internas na forma como `Unidade` é persistida podem quebrar o `ProcessoService`, pois ele conhece detalhes de implementação (o Repositório) em vez de usar uma API pública (o Service).
*   **Uso de Eventos:** O sistema já utiliza `ApplicationEventPublisher` (ex: `EventoProcessoCriado`), o que demonstra maturidade para uma arquitetura orientada a eventos, facilitando a adoção do `EventPublicationRegistry` do Modulith.
*   **Verificação Arquitetural:** O projeto utiliza `ArchUnit` (`ArchConsistencyTest`) para garantir algumas regras (ex: Controller não acessa Repo). No entanto, não há regras impedindo que um Módulo A acesse classes internas do Módulo B, permitindo o acoplamento descrito acima.

## 3. O que é o Spring Modulith 2.0?

O Spring Modulith é uma extensão do Spring Boot focada na construção de "Monolitos Modulares". A versão 2.0 (lançada no final de 2025) é construída sobre o **Spring Boot 4** e **Spring Framework 7**, trazendo melhorias significativas no registro de eventos.

### Principais Recursos:
1.  **Verificação de Estrutura:** Garante que módulos só acessem classes expostas (API) de outros módulos. Acesso a classes internas falha o build.
2.  **Testes Modulares (`@ApplicationModuleTest`):** Permite rodar testes de integração carregando apenas o contexto de um módulo específico, mockando automaticamente dependências de outros módulos.
3.  **Documentação Viva:** Gera diagramas C4 (Container, Component) e relatórios de dependências automaticamente a partir do código.
4.  **Registro de Eventos (Event Registry):** Garante que eventos publicados sejam persistidos e entregues mesmo em caso de falha (padrão *Transactional Outbox*), agora com novos estados (`published`, `processing`, `failed`, `resubmitted`) na v2.0.

## 4. Benefícios para o SGC

A adoção traria benefícios claros e imediatos:

### 4.1. Reforço de Limites (Encapsulamento)
Atualmente, nada impede que o código "vaze" lógica entre domínios. O Spring Modulith forçaria que `ProcessoService` parasse de usar `UnidadeRepo` diretamente.
*   **Benefício:** Redução de acoplamento e efeitos colaterais em refatorações.

### 4.2. Performance de Testes
Os testes de integração atuais (`@SpringBootTest`) carregam o contexto inteiro da aplicação e o banco H2 completo.
*   **Benefício:** Com `@ApplicationModuleTest`, poderíamos testar o módulo `processo` isoladamente. O Spring Modulith detectaria que ele depende de `unidade` e permitiria mockar essa interação facilmente, reduzindo drasticamente o tempo de execução do CI.

### 4.3. Documentação Automática
Como o projeto SGC preza por documentação (vários READMEs e diagramas solicitados), o Modulith automatizaria a criação de diagramas de componentes atualizados a cada build.
*   **Benefício:** Fim de diagramas desatualizados no `README.md`.

### 4.4. Robustez em Eventos
O sistema já usa eventos, mas sem garantia de entrega (se o listener falhar, o evento é perdido).
*   **Benefício:** O Modulith 2.0 traz um registro de eventos robusto (sobre JPA/JDBC) que garante que notificações (emails, alertas) não sejam perdidas mesmo se o serviço de email estiver fora do ar momentaneamente.

## 5. Desafios e Esforço de Migração

A migração não é trivial devido ao acoplamento existente.

1.  **Refatoração de Injeções Cruzadas (Médio/Alto Esforço):**
    *   Será necessário refatorar classes como `ProcessoService`. Em vez de injetar `UnidadeRepo`, ele deverá usar `UnidadeService` (ou uma interface dedicada exportada pelo módulo `unidade`).
    *   Alternativamente, mover DTOs e Interfaces para pacotes que o Modulith considere "API pública" do módulo.
2.  **Ajuste de Testes:**
    *   Substituir testes puramente `@SpringBootTest` por `@ApplicationModuleTest` onde fizer sentido.
3.  **Compatibilidade:**
    *   Como o projeto já está no **Spring Boot 4.0.1** (conforme `build.gradle.kts`), ele é **totalmente compatível** com o Spring Modulith 2.0.

## 6. Recomendação

**Recomenda-se a adoção do Spring Modulith 2.0.**

O sistema SGC já possui a estrutura de pacotes correta e a fundação (Java 21, Spring Boot 4) necessária. A adoção resolverá o problema de acoplamento "silencioso" entre repositórios e elevará a maturidade da arquitetura de eventos, além de acelerar o feedback dos testes.

### Plano de Ação Sugerido:
1.  Adicionar a dependência `spring-modulith-starter-core` e `spring-modulith-starter-test`.
2.  Criar um teste base `SgcApplicationModulesTest` para verificar a estrutura e gerar documentação.
3.  Refatorar gradualmente os módulos, começando pelos centrais (`unidade`, `seguranca`) para expor apenas Serviços/Interfaces, ocultando Repositórios (tornando-os package-private).
