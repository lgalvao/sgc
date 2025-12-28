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
O plugin do PiTest foi adicionado ao arquivo `build.gradle.kts` do módulo backend.

**Configuração Atual:**
```kotlin
plugins {
    id("info.solidsoft.pitest") version "1.19.0-rc.1"
}

pitest {
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(setOf("sgc.subprocesso.*")) // Escopo atual (Fase 2)
    excludedClasses.set(setOf(
        "sgc.Sgc",
        "sgc.**.*Config",
        "sgc.**.*Dto",
        "sgc.**.*Exception",
        "sgc.**.*Repo",
        "sgc.**.*MapperImpl"
    ))
    threads.set(4)
    outputFormats.set(setOf("XML", "HTML"))
}
```

### 3.2. Escopo de Execução (Fases)

#### Fase 1: Piloto (Pacote `processo`) - **CONCLUÍDO**
- **Execução Inicial:** Mutation Score de 47% (86 mortos / 183 gerados).
- **Ações Realizadas:**
    - Reforço de asserções em `ProcessoServiceTest` (validação de mensagens de erro).
    - Criação de `ProcessoInicializadorTest` para cobrir lógica de inicialização anteriormente não testada.
    - Melhoria em `ProcessoDetalheBuilderTest` para validar lógica de segurança e ordenação.
- **Resultado Final:** Mutation Score subiu para **63%** (116 mortos).

#### Fase 2: Domínios Core (`subprocesso`) - **CONCLUÍDO**
- **Execução Inicial:** Mutation Score de 44% (181 mortos / 410 gerados).
- **Ações Realizadas:**
    - Refatoração completa de `SubprocessoPermissoesServiceTest` para cobrir lógica booleana complexa em `calcularPermissoes` e `validar`, matando mutantes de negação condicional e retorno booleano.
    - Melhoria em `SubprocessoMapaWorkflowServiceTest` para verificar chamadas de limpeza de análise (`analiseService.removerPorSubprocesso`), matando mutantes de remoção de chamada de método.
    - Melhoria em `SubprocessoCadastroWorkflowServiceTest` para verificar chamadas de limpeza de análise, garantindo integridade do workflow.
- **Resultado:** Aumento significativo na confiabilidade dos testes de workflow e permissões.

#### Fase 3: Outros Domínios (`mapa`, `atividade`)
Expandir para outros domínios principais.
- **Ação:** Atualizar `targetClasses` no gradle e rodar análise para `sgc.mapa.*` e `sgc.atividade.*`.

#### Fase 4: Camada de Segurança e Serviços Auxiliares (`sgrh`, `seguranca`)
Validar se as regras de autorização estão robustas contra alterações acidentais.

## 4. Fluxo de Trabalho de Refatoração

Para cada execução do PiTest:

1.  **Executar:** Rodar o comando Gradle (ex: `./gradlew pitest`).
2.  **Analisar:** Abrir o relatório HTML gerado em `backend/build/reports/pitest/index.html`.
3.  **Identificar Mutantes Sobreviventes:** Focar nos mutantes marcados como **SURVIVED**.
4.  **Corrigir:** Melhorar testes ou corrigir código.
5.  **Revalidar:** Rodar os testes unitários padrão e depois o PiTest novamente.

## 5. Métricas de Sucesso

*   **Mutation Score:** Porcentagem de mutantes mortos pelos testes.
    *   *Mínimo Aceitável:* 60%
    *   *Ideal:* > 80%
*   **Força do Teste (Test Strength):** Mede quão bons são os testes apenas nas linhas que eles cobrem.

## 6. Próximos Passos

1.  Iniciar Fase 3 focando no pacote `sgc.mapa`, que contém regras de negócio críticas sobre competências e atividades.
