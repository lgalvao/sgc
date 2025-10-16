# SGC - Sistema de Gestão de Competências

O SGC é uma aplicação full-stack projetada para gerenciar o mapeamento de competências e seus fluxos de trabalho associados dentro de uma organização.

-   **Backend:** Java 21 com Spring Boot e Gradle.
-   **Frontend:** Vue 3 com Vite, Pinia e Playwright.

## Ambiente de Desenvolvimento

Esta seção descreve como configurar e executar o ambiente de desenvolvimento local.

### Executando o Backend

O backend pode ser executado localmente sem a necessidade de um container Docker, utilizando o perfil `local` do Spring Boot, que ativa um banco de dados H2 em memória.

Para iniciar o servidor backend, execute o seguinte comando na raiz do projeto:

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
```

O servidor estará disponível em `http://localhost:10000`.

### Executando o Frontend

O servidor de desenvolvimento do frontend pode ser iniciado com o seguinte comando:

```bash
cd frontend
npm install
npm run dev
```

A aplicação estará disponível em `http://localhost:5173`.

## Testes

### Testes do Backend

Para executar a suíte de testes de unidade e integração do backend, use o seguinte comando:

```bash
./gradlew :backend:test
```

### Testes do Frontend

Para executar os testes unitários (Vitest) e end-to-end (Playwright) do frontend, use os seguintes comandos a partir do diretório `frontend/`:

```bash
# Testes Unitários
npm run test:unit

# Testes End-to-End (requer que o backend esteja rodando)
npx playwright test
```

## Notas sobre a Integração Frontend-Backend

A integração completa da aplicação envolveu a substituição de todos os dados mockados do frontend por chamadas reais à API do backend. Durante este processo, alguns desafios de configuração foram encontrados e resolvidos:

1.  **Prefixo de API Centralizado**: Para garantir a manutenibilidade, o prefixo `/api` foi centralizado em ambos os ambientes:
    -   **Backend**: A propriedade `server.servlet.context-path=/api` foi adicionada ao `application.yml` para aplicar um prefixo global a todos os controllers.
    -   **Frontend**: O composable `useApi.ts` foi tornado a única fonte de verdade para a construção dos URLs da API, adicionando o prefixo `/api` a todas as chamadas.

2.  **Configuração de Segurança para Desenvolvimento**: O perfil `local` do Spring Boot ativa uma configuração de segurança que, por padrão, bloqueia o acesso não autenticado. Para permitir o fluxo de login nos testes e no desenvolvimento local, a classe `SecurityConfig.java` foi ajustada para permitir acesso anônimo (`permitAll`) aos endpoints de autenticação em `/api/usuarios/**`.

3.  **Execução de Testes E2E**: A execução dos testes end-to-end com Playwright requer que tanto o servidor do backend quanto o servidor de desenvolvimento do frontend estejam em execução simultaneamente. A falha em iniciar um dos servidores resultará em erros de conexão (`ECONNREFUSED`).

Essas configurações garantem um ambiente de desenvolvimento robusto e um fluxo de integração claro entre o frontend e o backend.