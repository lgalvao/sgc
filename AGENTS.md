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

A suíte de testes E2E foi configurada para gerenciar automaticamente o ciclo de vida do servidor backend. O Playwright irá iniciar uma instância do backend com um perfil de teste (`jules`) antes de executar os testes e irá desligá-lo ao final.

Para executar todos os testes E2E, navegue até o diretório `frontend` e use o seguinte comando:

```bash
npx playwright test
```

**Importante:** Não é mais necessário iniciar o backend manualmente. A configuração do Playwright (`playwright.config.ts`) se encarrega disso. Se você encontrar problemas de porta em uso, certifique-se de que nenhuma outra instância do backend esteja rodando.

## Informações Gerais do Backend

O backend utiliza **Java 21**.

## Testes de Backend

Para executar todos os testes de backend, use o seguinte comando a partir do diretório raiz. (Isso usará como default o task `agentTest` que filtra os stack traces)  

```bash
./gradlew :backend:test
```
