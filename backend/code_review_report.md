# Relatório de Revisão de Código - SGC

**Data da Revisão:** 2025-10-11

## Resumo Executivo

A revisão do código-fonte do backend do SGC identificou pontos de melhoria em quatro áreas principais: **Segurança**, **Arquitetura & Performance**, **Melhores Práticas** e **Convenções do Projeto**.

As vulnerabilidades de segurança são de baixa criticidade, mas devem ser corrigidas para fortalecer a aplicação. As questões de arquitetura e performance, especialmente os problemas de consulta N+1, são mais críticas e podem impactar a escalabilidade e a responsividade do sistema.

As recomendações seguem as diretrizes do `AGENTS.md` e as melhores práticas para aplicações Spring Boot.

---

## 1. Segurança

### 1.1. Configuração de CORS Permissiva (Criticidade: Baixa)

-   **Arquivo:** `sgc/comum/config/ConfigWeb.java`
-   **Observação:** A configuração do CORS permite os métodos `PUT`, `DELETE` e `OPTIONS`. Embora funcional, essa configuração é mais permissiva do que o necessário para a maioria das aplicações web e pode aumentar a superfície de ataque se as permissões nos endpoints não forem rigorosamente controladas.
-   **Recomendação:** Restringir os `allowedMethods` para `GET` e `POST`, adicionando outros métodos apenas se forem estritamente necessários para a funcionalidade do frontend. Métodos como `DELETE` devem ser usados com cautela.

### 1.2. Tratamento de Exceções Genéricas (Criticidade: Baixa)

-   **Arquivo:** `sgc/comum/erros/RestExceptionHandler.java`
-   **Observação:** Os handlers para `IllegalStateException` e `IllegalArgumentException` retornam mensagens de erro genéricas ao cliente. Embora isso evite o vazamento de informações internas, dificulta a depuração e o tratamento de erros no lado do cliente.
-   **Recomendação:** Alterar as mensagens para serem um pouco mais descritivas, sem expor detalhes da implementação. Por exemplo, em vez de "Ocorreu um erro de estado na aplicação", poderia ser "A operação não pode ser executada no estado atual do recurso".

### 1.3. Política de Sanitização de HTML (Criticidade: Baixa)

-   **Arquivo:** `sgc/comum/erros/RestExceptionHandler.java`
-   **Observação:** A política de sanitização do OWASP (`Sanitizers.FORMATTING.and(Sanitizers.LINKS)`) é permissiva. Se a intenção é não permitir HTML nas mensagens de erro, essa política é inadequada.
-   **Recomendação:** Se HTML não é esperado nas mensagens de erro, mudar para uma política mais restritiva, como `Sanitizers.NONE`, para uma proteção mais robusta contra XSS.

---

## 2. Arquitetura & Performance

### 2.1. Problema de Consulta N+1 em Notificações (Criticidade: Alta)

-   **Arquivo:** `sgc/processo/ProcessoService.java`
-   **Método:** `enviarNotificacoesDeFinalizacao`
-   **Observação:** Este método itera sobre uma lista de subprocessos e, dentro do loop, faz chamadas individuais ao `sgrhService` para buscar responsáveis e dados de usuários. Isso resulta em múltiplas viagens ao banco de dados (ou serviço externo), degradando a performance significativamente com o aumento do número de unidades.
-   **Recomendação:** Refatorar o método para usar as operações em lote já disponíveis no `SgrhService`. Buscar todos os responsáveis e usuários necessários com duas chamadas (`buscarResponsaveisUnidades` e `buscarUsuariosPorTitulos`) antes de iniciar o loop.

### 2.2. Lógica de Autorização Misturada com Lógica de Negócio (Criticidade: Média)

-   **Arquivo:** `sgc/processo/ProcessoService.java`
-   **Método:** `obterDetalhes`
-   **Observação:** O método contém lógica de autorização (verificação de `ROLE_GESTOR`, `ROLE_ADMIN` e participação da unidade no processo) misturada com a lógica de busca de dados. Isso viola o princípio de separação de responsabilidades (SoC) e torna o código mais difícil de manter.
-   **Recomendação:** Mover a lógica de autorização para a camada de controle (`ProcessoControle.java`) ou, preferencialmente, utilizar anotações de segurança do Spring (`@PreAuthorize`) no método do serviço para uma abordagem mais declarativa e limpa.

---

## 3. Melhores Práticas

### 3.1. Anotação de Cache em Serviço Mock (Criticidade: Baixa)

-   **Arquivo:** `sgc/sgrh/SgrhService.java`
-   **Observação:** A classe `SgrhService`, que é um mock, possui uma anotação `@Cacheable` em nível de classe. Isso é enganoso, pois sugere que o cache está ativo para todos os métodos, e pode causar comportamento inesperado quando a implementação real for desenvolvida. A memória do agente indicava que isso foi removido, mas a análise do arquivo mostra que a anotação ainda está presente.
-   **Recomendação:** Remover a anotação `@Cacheable` no nível da classe. As anotações de cache por método, que já estão presentes, são a abordagem correta e suficiente.

---

## 4. Próximos Passos (Plano de Ação)

-   [X] **Refatorar `ProcessoService`**:
    -   [X] Mover a lógica de autorização de `obterDetalhes` para a camada de segurança/controle.
    -   **Observação:** O problema de consulta N+1 em `enviarNotificacoesDeFinalizacao` já havia sido corrigido em uma versão anterior do código.
-   [X] **Ajustar `ConfigWeb`**:
    -   [X] Limitar os métodos CORS para `GET`, `POST` e `PUT`.
-   [X] **Melhorar `RestExceptionHandler`**:
    -   [X] Refinar as mensagens de erro para `IllegalStateException` (agora retorna 409 Conflict) e `IllegalArgumentException`.
    -   [X] Ajustar a política de sanitização do OWASP para remover todo o HTML.
-   [X] **Limpar `SgrhService`**:
    -   [X] Remover as anotações `@Cacheable` e `@Transactional` no nível da classe.
-   [X] **Verificação Final**:
    -   [X] Rodar todos os testes de integração para garantir que as refatorações não introduziram regressões.
    -   [X] Atualizar este relatório com o status final das correções.