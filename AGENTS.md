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

A suíte de testes E2E é totalmente automatizada. A configuração do Playwright (`playwright.config.ts`) é responsável por:

1.  **Construir o frontend:** Instala as dependências (`npm install`), compila os artefatos (`npm run build`).
2.  **Copiar os artefatos:** Copia os arquivos estáticos do frontend para o diretório de recursos do backend.
3.  **Iniciar o backend:** Inicia o servidor Spring Boot com um perfil de teste otimizado (`jules`).
4.  **Executar os testes:** Executa a suíte de testes do Playwright.
5.  **Parar o backend:** Desliga o servidor Spring Boot após a conclusão dos testes.

**Execução:**

Para executar todos os testes E2E, navegue até o diretório `frontend` e use o seguinte comando:

```bash
npm run test:e2e
```

**Importante:** A configuração do Playwright (`playwright.config.ts`) se encarrega de iniciar e parar o backend. Se você encontrar problemas de porta em uso, certifique-se de que nenhuma outra instância do backend esteja rodando antes de iniciar os testes.

## Informações Gerais do Backend

O backend utiliza **Java 21**.

## Testes de Backend

Para executar todos os testes de backend, use o seguinte comando a partir do diretório raiz. (Isso usará como default o task `agentTest` que filtra os stack traces)  

```bash
./gradlew :backend:test
```
