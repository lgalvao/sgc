# Status do Refatoramento dos Comentários TODO

Este documento resume o progresso feito na tarefa de resolver os comentários `// TODO` no código-fonte e detalha os problemas restantes.

## Progresso e Tarefas Concluídas

Foi realizado um refatoramento extenso em várias partes do sistema para abordar os `TODOs` existentes. As principais melhorias incluem:

1.  **Refatoração de DTOs:**
    *   DTOs nos pacotes `alerta` e `analise` foram convertidos de `records` para classes com o padrão `@Builder`, proporcionando mais flexibilidade na criação de objetos.

2.  **Melhora da Segurança de Tipos (Type Safety):**
    *   O uso de `String`s para representar tipos como `Perfil` e `TipoProcesso` foi substituído por enums, tornando o código mais robusto e menos propenso a erros.

3.  **Refatoração do Serviço de Notificação:**
    *   O `SubprocessoNotificacaoService` foi completamente refatorado para utilizar templates **Thymeleaf** para a geração de e-mails, eliminando strings fixas no código.
    *   A criação de `Alerta`s foi padronizada com o uso do padrão `@Builder`.
    *   Essa mudança exigiu a criação de uma configuração de teste compartilhada (`TestThymeleafConfig`) para mockar o `TemplateEngine` e a correção de um grande número de testes de integração que falharam devido à nova dependência.

4.  **Simplificação da Arquitetura:**
    *   A classe `ProcessoSeguranca`, que continha apenas um método de verificação, foi eliminada. Sua lógica foi movida para dentro do `ProcessoService`, simplificando a arquitetura do módulo `processo`.
    *   A configuração de `Executor` assíncrono customizado (`ConfigAsync`) foi removida em favor do executor padrão do Spring Boot, eliminando um caso de "overengineering".

5.  **Exceções de Negócio Customizadas:**
    *   Foi introduzida uma exceção específica, `ErroNegocio`, para representar violações de regras de negócio.
    *   Exceções genéricas como `IllegalStateException` e `IllegalArgumentException` foram substituídas por `ErroNegocio` em vários serviços, tornando o tratamento de erros mais claro e a API mais consistente.

## Problemas Restantes (Testes Falhando)

Apesar do progresso, **7 testes de integração continuam falhando**.

*   **Testes Afetados:**
    *   `CDU17IntegrationTest` (5 falhas)
    *   `CDU19IntegrationTest` (2 falhas)

*   **Diagnóstico do Problema:**
    *   A causa raiz parece ser um conflito sutil no ciclo de vida do contexto de teste do Spring, especificamente entre a inicialização do banco de dados H2, a configuração do `SecurityContext` por anotações customizadas (ex: `@WithMockChefe`), e a execução dos métodos de setup (`@BeforeEach`).
    *   O erro mais comum é `Table "USUARIO" not found (this database is empty)`. Isso ocorre porque a anotação de segurança tenta carregar um usuário do banco de dados **antes** que o `@BeforeEach` tenha a chance de popular as tabelas.
    *   As tentativas de corrigir isso alterando a ordem de importação das configurações de teste ou ajustando as anotações não foram bem-sucedidas e levaram a outros erros em cascata (compilação, outros erros de runtime).

Conforme instruído, o trabalho de refatoramento não será desfeito. A resolução desses testes restantes será abordada em uma sessão futura.
