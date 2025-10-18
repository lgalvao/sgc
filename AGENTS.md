# Orientações para Agentes

Este documento fornece instruções e dicas para agentes de IA que trabalham neste repositório.

## Executando o Backend

Para executar o backend em modo de desenvolvimento, use o seguinte comando a partir do diretório raiz:

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
```

Este comando utiliza o perfil `local` do Spring, que configura a aplicação para usar um banco de dados H2 em memória, não exigindo um PostgreSQL externo.

### Executando em Segundo Plano

Para manter o backend em execução sem bloquear o terminal, adicione `&` ao final do comando. Isso é útil para executar outras tarefas, como os testes de frontend.

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local' &
```