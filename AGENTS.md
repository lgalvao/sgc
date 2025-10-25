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

Para executar os testes end-to-end (E2E) do frontend, primeiro garanta que o backend está em execução.
Depois navegue até o diretório `frontend` e use o seguinte comando:

```bash
npm run test:e2e
```

## Informações Gerais do Backend

O backend utiliza **Java 21**.

## Testes de Backend

Para executar todos os testes de backend, use o seguinte comando a partir do diretório raiz. (Isso usará como default o task `agentTest` que filtra os stack traces)

```bash
./gradlew :backend:test
```
