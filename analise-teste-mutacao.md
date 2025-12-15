# Análise de Viabilidade: Testes de Mutação no SGC

Este relatório detalha a viabilidade, benefícios e estratégia para adoção de testes de mutação no projeto SGC (Sistema de Gestão de Competências), cobrindo tanto o backend (Spring Boot/Java 21) quanto o frontend (Vue.js/TypeScript).

## 1. Visão Geral

Testes de mutação avaliam a qualidade da suíte de testes introduzindo pequenos defeitos ("mutantes") no código fonte e verificando se os testes existentes conseguem detectá-los (ou seja, "matar" os mutantes). É uma métrica superior à cobertura de código tradicional, pois mede a eficácia das asserções, não apenas a execução das linhas.

## 2. Análise Backend (Java / Spring Boot)

### Ferramenta Recomendada: Pitest

O **Pitest** é o padrão da indústria para Java. Ele opera em nível de bytecode, tornando-o rápido e capaz de se integrar bem com JUnit 5.

### Alvos de Alto Valor (Backend)

Quando viável, a aplicação deve focar nos pacotes que contêm lógica de estado e regras de negócio críticas:

1. `sgc.processo.service`: Orquestração de workflows.
2. `sgc.subprocesso.service`: Máquina de estados das unidades (transições complexas).
3. `sgc.alerta.service`: Regras de disparo de notificações.

## 3. Análise Frontend (Vue.js / TypeScript)

### Ferramenta Recomendada: StrykerJS

O **StrykerJS** é a ferramenta líder para JavaScript/TypeScript, com suporte robusto para Vitest e Vue SFCs (Single File Components).

### Prova de Conceito (Executada com Sucesso)

Realizamos uma execução piloto no arquivo `src/stores/usuarios.ts`.

- **Resultado:** Score de mutação de **61.11%**.
- **Mutantes Sobreviventes:** 6 mutantes não foram detectados (ex: alterações em booleanos de loading e tratamento de erro).
- **Performance:** Execução em ~3 minutos para um único arquivo (com overhead de setup).

### Benefícios Identificados

- Detecção de lógica de UI não testada (ex: flags de `isLoading` que nunca são verificadas nos testes).
- Validação de tratamento de erros (blocos `catch` vazios ou ignorados).

### Estratégia de Adoção

Recomendamos uma abordagem gradual ("Shift Left") focada primeiro no Frontend, onde a ferramenta já é compatível.

### Fase 1: Piloto Frontend (Imediato)

- **Instalação:** Adicionar `@stryker-mutator/core` e `@stryker-mutator/vitest-runner` como `devDependencies`.
- **Configuração:** Utilizar `stryker.config.json` focado apenas nas *Stores* (Pinia) e *Services*, que contêm a maior parte da lógica de negócio do frontend.
- **Execução:** Rodar localmente ou em CI apenas para arquivos modificados (incremental).

### Fase 2: Monitoramento Backend (Futuro)

- Monitorar a issue [#378](https://github.com/szpak/gradle-pitest-plugin/issues/378) do plugin Pitest.
- Assim que resolvido, aplicar configuração para os pacotes críticos listados na seção 2.

## 5. Como Adicionar (Guia Técnico)

### Frontend (Vue/Stryker)

1. **Instalar Dependências:**

    ```bash
    cd frontend
    npm install --save-dev @stryker-mutator/core @stryker-mutator/vitest-runner
    ```

2. **Criar Configuração (`frontend/stryker.config.json`):**

    ```json
    {
      "$schema": "./node_modules/@stryker-mutator/core/schema/stryker-schema.json",
      "packageManager": "npm",
      "reporters": ["html", "clear-text", "progress"],
      "testRunner": "vitest",
      "coverageAnalysis": "perTest",
      "mutate": [
        "src/stores/**/*.ts",
        "src/services/**/*.ts"
      ]
    }
    ```

3. **Executar:**

    ```bash
    npx stryker run
    ```

### Backend (Java/Pitest - Bloqueado/Rascunho)

*Configuração para `build.gradle.kts` (após fix de compatibilidade):*

```kotlin
plugins {
    id("info.solidsoft.pitest") version "1.15.0+" // Aguardar versão compatível com Gradle 9
}

pitest {
    targetClasses.set(listOf("sgc.processo.*", "sgc.subprocesso.*"))
    targetTests.set(listOf("sgc.processo.*Test", "sgc.subprocesso.*Test"))
    threads.set(4)
    outputFormats.set(listOf("XML", "HTML"))
    timestampedReports.set(false)
    junit5PluginVersion.set("1.2.1")
}
```

## 6. Conclusão

O projeto SGC se beneficiaria significativamente de testes de mutação para garantir a robustez de seus workflows complexos.

- **Frontend:** Adoção imediata recomendada para Stores e Services.
- **Backend:** Aguardar atualização do ecossistema Gradle.
