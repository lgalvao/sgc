# Backend do SGC

## Visão geral

Este módulo contém a API REST do SGC, com foco em regras de negócio, segurança e persistência.

- Linguagem: **Java 25**
- Framework: **Spring Boot 4**
- Build: **Gradle**
- Persistência: **JPA/Hibernate**

## Organização por domínios

Código principal em `src/main/java/sgc/`:

- `alerta`
- `comum`
- `e2e`
- `feedback`
- `mapa`
- `organizacao`
- `parametros`
- `processo`
- `relatorio`
- `seguranca`
- `subprocesso`

## Execução

Da raiz do projeto:

```bash
./gradlew :backend:bootRun -PENV=e2e
```

Perfis típicos:

- `local`
- `e2e`
- `hom`
- `prod`

Sem `-PENV`, o `bootRun` usa `e2e` como padrão no build atual.

## Build

```bash
./gradlew :backend:build
```

## Testes

```bash
./gradlew :backend:test
./gradlew :backend:unitTest
./gradlew :backend:integrationTest
```

## Qualidade

```bash
./gradlew :backend:qualityCheck
./gradlew :backend:qualityCheckFast
```

## Segurança e acesso

- Controle centralizado por `SgcPermissionEvaluator`.
- Leitura baseada em hierarquia organizacional.
- Escrita baseada na localização atual do subprocesso.
- Aplicação de regra de acesso via `@PreAuthorize` nos controllers.

## Observabilidade

- Endpoints Actuator em `/actuator`.
- Métricas para Prometheus habilitáveis por perfil.
- Monitoramento de lentidão via `MonitoramentoAspect` e `FiltroMonitoramentoHttp`.

## Referências internas

- [README raiz](../README.md)
- [Módulo comum](src/main/java/sgc/comum/README.md)
- [Módulo de subprocesso](src/main/java/sgc/subprocesso/README.md)
- [Suporte E2E no backend](src/main/java/sgc/e2e/README.md)
