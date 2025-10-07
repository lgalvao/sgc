# Contexto do Projeto GEMINI: SGC - Sistema de Gestão de Conhecimentos

## Visão Geral do Projeto

Este é um projeto full-stack e multi-módulo gerenciado pelo Gradle.

* **Backend:** Uma API RESTful construída com **Java** e o framework **Spring Boot**. Utiliza Spring Data JPA para persistência de dados com um banco de dados **PostgreSQL**.
* **Frontend:** Uma aplicação de página única (SPA) construída com **Vue.js 3**. Utiliza **Vite** como ferramenta de build, **Pinia** para gerenciamento de estado e **Vue Router** para navegação.
* **Sistema de Build:** O **Gradle** orquestra o processo de build para os módulos de backend e frontend, incluindo a construção do frontend e seu empacotamento no JAR final do Spring Boot para produção.

O pacote base para a aplicação Java do backend é `sgc`.

## Construindo e Executando

### Backend

Para executar o servidor de desenvolvimento do backend (disponível em `http://localhost:8080`):

```bash
# A partir da raiz do projeto (C:\sgc)
.\gradlew :backend:bootRun
```

*(Nota: O wrapper do Gradle (`gradlew`) ainda não foi adicionado. Por enquanto, use o comando `gradle` globalmente se o tiver instalado.)*

### Frontend

Para executar o servidor de desenvolvimento do frontend (disponível em `http://localhost:5173`):

```bash
# A partir da raiz do projeto (C:\sgc)
cd frontend
npm run dev
```

*Nota: O servidor de desenvolvimento do frontend está configurado para redirecionar (proxy) as requisições de API de `/api` para o backend em `http://localhost:8080`.*

### Build de Produção

Para criar um build de produção auto-contido (um único arquivo JAR):

```bash
# A partir da raiz do projeto (C:\sgc)
.\gradlew build
```

Este comando irá instalar as dependências do frontend, construir os assets estáticos do frontend e empacotá-los dentro do JAR do backend. O artefato final estará localizado em `C:\sgc\backend\build\libs\sgc-0.0.1-SNAPSHOT.jar`.

Para executar a aplicação em produção:

```bash
java -jar C:\sgc\backend\build\libs\sgc-0.0.1-SNAPSHOT.jar
```

## Convenções de Desenvolvimento

### Testes do Backend

O backend utiliza **JUnit 5** para testes. O arquivo de build do Gradle (`C:\sgc\backend\build.gradle.kts`) contém várias configurações otimizadas para o agente e tarefas auxiliares.

* **Rodar todos os testes (otimizado para agente):**

    ```bash
    gradle :backend:agentTest
    ```

* **Rodar uma única classe de teste:**

    ```bash
    gradle testClass -PtestClass=SeuNomeDeClasse
    ```

### Desenvolvimento do Frontend

Os comandos principais estão definidos em `C:\sgc\frontend\package.json`:

* **Rodar testes unitários (Vitest):**

    ```bash
    npm run test:unit
    ```

* **Rodar testes end-to-end (Playwright):**

    ```bash
    npx run plawright test
    ```

* **Lint e formatação de arquivos (ESLint):**

    ```bash
    npm run lint
    ```

* **Verificação estática de tipos (TypeScript):**

    ```bash
    npm run typecheck
    ```
