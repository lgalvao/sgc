# Orientações para Agentes

Este documento fornece instruções e dicas para agentes de IA que trabalham neste repositório.

## Para executar o Backend

Para executar o backend em modo de desenvolvimento, use o seguinte comando a partir do diretório raiz:

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
```
Este comando utiliza o perfil `local` do Spring, que configura a aplicação para usar um banco de dados H2 em memória, não exigindo um PostgreSQL externo.

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

### Testes Padrão

Para executar todos os testes de backend, use o seguinte comando a partir do diretório raiz:

```bash
./gradlew :backend:test
```

### Testes Otimizados para Agentes

Para executar os testes de backend com uma saída otimizada para agentes (resumo conciso), use o seguinte comando a partir do diretório raiz:

```bash
./gradlew :backend:agentTest
```

### Testes com stack trace completo

A configuração atual do build do backend filtra as exceções. Para ver todas as exceções com o stack-trace completo, use o comando a seguir:

```bash
gradle test --tests sgc.integracao.<Classe do Teste> --full-stacktrace
```

### Testes com Saída Completa (Verbose)

Para executar os testes de backend com uma saída completa (mais detalhes sobre cada teste), use o seguinte comando a partir do diretório raiz:

```bash
./gradlew :backend:verboseTest
```

## Análise Estática de Código (PMD)

Para executar a análise estática de código com PMD no backend, use o seguinte comando a partir do diretório raiz:

```bash
./gradlew :backend:pmd
```