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

### Testes E2E (End-to-End)

**IMPORTANTE: A execução dos testes E2E requer uma configuração específica.**

A suíte de testes E2E usa o Playwright para interagir com a aplicação. A configuração do Playwright (`playwright.config.ts`) é responsável por iniciar o servidor de desenvolvimento do **frontend** (`npm run dev`), mas **NÃO** gerencia o ciclo de vida do backend.

**Procedimento para Execução Correta:**

1.  **Inicie o Backend Manualmente:** Antes de rodar os testes, você **deve** iniciar o servidor backend em um terminal separado. Use o perfil `jules`, que é configurado para o ambiente de teste.

    ```bash
    cd /app
    ./gradlew :backend:bootRun --args='--spring.profiles.active=jules' > backend.log 2>&1 &
    ```

2.  **Verifique se o Backend Iniciou:** Aguarde alguns segundos e verifique o `backend.log` para confirmar que o servidor iniciou com sucesso (procure pela mensagem "Started Sgc").

3.  **Execute os Testes E2E:** Com o backend rodando, navegue até o diretório `frontend` e execute o comando do Playwright.

    ```bash
    cd /app/frontend
    npm run test:e2e
    ```

4.  **Pare o Backend Manualmente:** Após a conclusão dos testes, lembre-se de parar o processo do backend que você iniciou.

    ```bash
    kill %1
    ```

**Resumo:** O Playwright cuida do frontend, mas o backend precisa ser gerenciado manualmente. Tentar configurar o Playwright para gerenciar ambos os servidores provou ser instável e leva a timeouts.

## Informações Gerais do Backend

O backend utiliza **Java 21**.

## Testes de Backend

Para executar todos os testes de backend, use o seguinte comando a partir do diretório raiz. (Isso usará como default o task `agentTest` que filtra os stack traces)

```bash
./gradlew :backend:test
```
