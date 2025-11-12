# Guia para Agentes de Desenvolvimento

Este documento estabelece diretrizes e boas práticas para agentes de desenvolvimento que trabalham no projeto SGC. O objetivo é garantir consistência, eficiência e alinhamento com as convenções do projeto.

## 1. Conhecimento do Projeto

É fundamental que o agente se familiarize com a estrutura e as especificidades de cada módulo do projeto.

-   **Visão Geral:** Consulte o `README.md` na raiz do repositório para uma visão geral do projeto.
-   **Backend:** Cada pacote principal do backend possui um `README.md` detalhando suas responsabilidades e tecnologias específicas.
-   **Frontend:** Cada diretório principal do frontend também contém um `README.md` com informações sobre sua finalidade, tecnologias e como interagir com ele.

## 2. Regras Gerais de Desenvolvimento

### 2.1. Idioma

-   Todo o sistema, incluindo nomes de variáveis, mensagens de erro, logs e documentação voltada ao usuário, deve estar em **Português Brasileiro**.

### 2.2. Convenções de Nomenclatura

-   **Exceções:** Nomes de classes de exceção devem seguir o padrão `ErroXxxx`, por exemplo, `ErroEntidadeNaoEncontrada`.
-   **Repositórios JPA:** Nomes de interfaces de repositório JPA devem seguir o padrão `XxxxRepo`, por exemplo, `SubprocessoRepo`.

## 3. Backend (Java/Kotlin com Spring Boot)

*(Adicionar diretrizes específicas para o backend aqui, se houver. Ex: uso de Lombok, padrões de serviço, etc.)*

## 4. Frontend (Vue.js com TypeScript)

-   **Arquitetura:** Siga o padrão `Views -> Stores -> Services` conforme detalhado no `frontend/README.md`.
-   **Componentes:** Ao criar ou modificar componentes, adira aos "Princípios dos Componentes" descritos em `frontend/src/components/README.md` (reutilizáveis, controlados por props/eventos, sem lógica de negócio complexa).
-   **Gerenciamento de Estado:** Utilize Pinia para gerenciamento de estado, organizando as stores de forma modular.
-   **Comunicação com API:** Utilize a instância configurada do Axios (`axios-setup.ts`) para todas as chamadas de API.

## 5. Uso de Ferramentas

### 5.1. Gradle

-   Evite usar a flag `--no-daemon` ao executar comandos Gradle, a menos que seja estritamente necessário, para otimizar o tempo de build.

### 5.2. Playwright (Testes E2E)

Os testes E2E são cruciais para a qualidade do sistema. Siga estas diretrizes para executá-los e mantê-los:

-   **Execução Otimizada:**
    -   Sempre execute o mínimo de testes possível para o contexto da sua alteração.
    -   Para rodar apenas os testes que falharam na última execução:
        ```bash
        npx playwright test --last-failed
        ```
    -   Para rodar um subconjunto de testes (ex: os primeiros 5 CDUs):
        ```bash
        npx playwright test e2e/cdu/cdu-0[1-5].spec.ts
        ```
-   **Evitar Timeouts Explícitos:**
    -   Não utilize `page.waitForTimeout()` ou outros timeouts explícitos. Timeouts geralmente indicam que os elementos não estão visíveis ou interativos como esperado, seja por um problema no teste (ex: seletor incorreto) ou um defeito no sistema. O Playwright possui mecanismos de auto-espera que devem ser suficientes.
-   **Seletores Robustos:**
    -   Prefira o uso de atributos `data-testid` para identificar elementos na interface do usuário. Isso torna os testes mais resilientes a mudanças na estrutura HTML ou CSS.
-   **Reutilização de Código:**
    -   Utilize as funções auxiliares (`helpers/acoes`, `helpers/dados`, `helpers/verificacoes`) para padronizar interações, dados e verificações, evitando duplicação e melhorando a legibilidade.
-   **Modo UI para Depuração:**
    -   Para depurar testes ou entender o fluxo, utilize o modo UI do Playwright:
        ```bash
        npx playwright test --ui
        ```
        Isso permite inspecionar elementos, executar testes passo a passo e visualizar o estado da aplicação durante a execução do teste.
