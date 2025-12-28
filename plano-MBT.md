# Plano de Adoção de Mutation-Based Testing (MBT) no Backend

Este documento descreve a estratégia para introduzir e utilizar Testes de Mutação (Mutation-Based Testing) no backend do projeto SGC. O objetivo é elevar a qualidade dos testes unitários, garantindo que eles sejam capazes de detectar falhas na lógica de negócio que a cobertura de código tradicional (code coverage) pode deixar passar.

## 1. Objetivo
Identificar lacunas na suíte de testes unitários existente (`backend/src/test/java`), onde o código é executado (coberto), mas os resultados ou efeitos colaterais não são devidamente assertados. O MBT introduzirá "mutantes" (alterações artificiais no bytecode) para verificar se os testes falham quando a lógica muda.

## 2. Ferramenta Escolhida: PiTest (PIT)
Utilizaremos o **PiTest (PIT)**, que é o padrão de mercado para o ecossistema Java/JVM. Ele se integra bem com JUnit 5 e Gradle.

Principais benefícios:
- Gera relatórios detalhados em HTML.
- Permite execução incremental.
- Integração via plugin do Gradle.

## 3. Estratégia de Implementação

### 3.1. Configuração do Build (Gradle)
O primeiro passo é adicionar o plugin do PiTest ao arquivo `build.gradle.kts` do módulo backend.

**Dependência:**
Plugin: `info.solidsoft.pitest`

**Configuração Inicial Sugerida:**
```kotlin
plugins {
    id("info.solidsoft.pitest") version "1.15.0" // Verificar versão mais recente compatível
}

pitest {
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(listOf("sgc.*")) // Define o escopo das classes a serem mutadas
    excludedClasses.set(listOf(
        "*.dto.*",
        "*.model.*",
        "*Config",
        "*Application",
        "*MapperImpl" // Ignorar implementações geradas pelo MapStruct
    ))
    threads.set(4) // Otimização de performance
    outputFormats.set(listOf("HTML"))
    timestampedReports.set(false)
}
```

### 3.2. Escopo de Execução (Fases)
Como o teste de mutação é computacionalmente custoso, não devemos rodá-lo em todo o projeto de uma vez inicialmente. A execução será dividida por pacotes de domínio críticos.

#### Fase 1: Piloto (Pacote `processo`)
O pacote `sgc.processo` contém a lógica de orquestração central. É o candidato ideal para validar a configuração.
- **Ação:** Rodar PiTest apenas para `sgc.processo.*`.
- **Meta:** Atingir > 70% de Mutation Score.

#### Fase 2: Domínios Core (`atividade`, `mapa`, `subprocesso`)
Após o sucesso do piloto, expandir para os outros domínios principais que contêm regras de negócio complexas.
- **Ação:** Rodar e analisar `sgc.atividade.*`, `sgc.mapa.*` e `sgc.subprocesso.*`.

#### Fase 3: Camada de Segurança e Serviços Auxiliares (`sgrh`, `seguranca`)
Validar se as regras de autorização estão robustas contra alterações acidentais.

## 4. Fluxo de Trabalho de Refatoração

Para cada execução do PiTest:

1.  **Executar:** Rodar o comando Gradle (ex: `./gradlew pitest`).
2.  **Analisar:** Abrir o relatório HTML gerado em `backend/build/reports/pitest/index.html`.
3.  **Identificar Mutantes Sobreviventes:** Focar nos mutantes marcados como **SURVIVED**. Isso significa que o PiTest alterou o código (ex: mudou `if (x > 0)` para `if (x >= 0)`), mas nenhum teste falhou.
4.  **Corrigir:**
    *   Se o mutante revelar um bug no código: Corrigir o código.
    *   Se o código estiver correto, mas o teste for fraco: Adicionar asserções ou novos casos de teste para cobrir o cenário.
    *   Se for um falso positivo (código equivalente): Marcar para exclusão ou ignorar.
5.  **Revalidar:** Rodar os testes unitários padrão e depois o PiTest novamente para confirmar a morte do mutante.

## 5. Métricas de Sucesso

*   **Mutation Score:** Porcentagem de mutantes mortos pelos testes.
    *   *Mínimo Aceitável:* 60%
    *   *Ideal:* > 80%
*   **Força do Teste (Test Strength):** Mede quão bons são os testes apenas nas linhas que eles cobrem. Devemos buscar 100% aqui.

## 6. Próximos Passos Imediatos

1.  Atualizar o `backend/build.gradle.kts` com o plugin do PiTest.
2.  Realizar a execução piloto no pacote `sgc.processo`.
3.  Documentar os primeiros achados e ajustar a configuração de exclusão de classes (para ignorar DTOs e código boilerplate que geram ruído).
