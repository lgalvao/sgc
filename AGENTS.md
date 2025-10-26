# Orientações para Agentes

Este documento fornece instruções e dicas para agentes de IA que trabalham neste repositório.

## Para executar o Backend

Para executar o backend em modo de desenvolvimento, use o seguinte comando a partir do diretório raiz:

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
```

Este comando utiliza o perfil `local` do Spring, que configura a aplicação para usar um banco de dados H2 em memória,
não exigindo um PostgreSQL externo.

Para manter o backend em execução sem bloquear o terminal, adicione `&` ao final do comando.

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local' &
```

## Executando o Frontend

Para executar o frontend em modo de desenvolvimento, navegue até o diretório `frontend` e use o seguinte comando:

```bash
npm run dev
```

Perceba que isso irá 'travar' o console, adicione '&' ao final para rodar em segundo plano.

## Testes de Frontend

### Testes de Unidade

Para executar os testes de unidade do frontend, navegue até o diretório `frontend` e use o seguinte comando:

```bash
npm run test:unit
```

### Testes E2E

A suíte de testes E2E depende de duas configurações críticas: o backend precisa servir os arquivos estáticos do frontend, e o Playwright precisa de um endpoint estável para verificar se o servidor está pronto.

**Preparação (Obrigatória):**

Antes de executar os testes, o frontend precisa ser compilado e seus arquivos copiados para o diretório de recursos do backend. Execute os seguintes comandos a partir do diretório raiz do projeto:

1.  **Instale as dependências e compile o frontend:**
    ```bash
    cd frontend && npm install && npm run build
    ```

2.  **Copie os arquivos para o backend (execute a partir do diretório raiz):**
    ```bash
    mkdir -p backend/src/main/resources/static && cp -r frontend/dist/* backend/src/main/resources/static/
    ```

**Execução:**

Após a preparação, você pode executar a suíte de testes. O Playwright gerenciará automaticamente o ciclo de vida do servidor backend.

Para executar todos os testes E2E, navegue até o diretório `frontend` e use o seguinte comando:

```bash
npx playwright test
```

**Importante:** A configuração do Playwright (`playwright.config.ts`) se encarrega de iniciar e parar o backend. Se você encontrar problemas de porta em uso, certifique-se de que nenhuma outra instância do backend esteja rodando antes de iniciar os testes.

## Informações Gerais do Backend

O backend utiliza **Java 21**.

## Testes de Backend

Para executar todos os testes de backend, use o seguinte comando a partir do diretório raiz. (Isso usará como default o task `agentTest` que filtra os stack traces)  

```bash
./gradlew :backend:test
```
