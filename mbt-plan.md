# Plano de Implementação de Testes de Mutação (MBT) no Backend SGC

Este documento define a estratégia para a introdução de *Mutation-Based Testing* (MBT) no backend do Sistema de Gestão de Competências (SGC). O objetivo é elevar a qualidade dos testes unitários, identificando lacunas de cobertura que as métricas tradicionais (como cobertura de linha) não detectam.

## 1. Contexto e Ferramentas

Utilizaremos o **PITest (PIT)**, a ferramenta padrão da indústria para testes de mutação em Java. Ela se integra nativamente ao JUnit 5 e ao Gradle, permitindo a geração de mutantes (alterações artificiais no bytecode) e verificação se os testes existentes falham ("matam" o mutante) ou passam ("mutante sobrevive").

*   **Ferramenta:** PITest (http://pitest.org/)
*   **Plugin Gradle:** `info.solidsoft.pitest`
*   **Plugin JUnit 5:** `pitest-junit5-plugin`

## 2. Configuração do Projeto

A primeira etapa consiste em configurar o `backend/build.gradle.kts` para suportar a execução do PITest.

### 2.1. Adicionar o Plugin

No bloco `plugins` do `backend/build.gradle.kts`:

```kotlin
plugins {
    // ... outros plugins
    id("info.solidsoft.pitest") version "1.15.0" // Verificar versão mais recente estável
}
```

### 2.2. Configurar o Bloco `pitest`

Adicionar a configuração básica no final do arquivo `backend/build.gradle.kts`. É crucial limitar o escopo inicial e usar threads para performance.

```kotlin
configure<info.solidsoft.gradle.pitest.PitestPluginExtension> {
    // Integração com JUnit 5
    junit5PluginVersion.set("1.2.1")

    // Evitar rodar em todo o projeto inicialmente (muito custoso)
    // Definiremos classes alvo via linha de comando ou perfis, mas aqui fica o padrão seguro
    targetClasses.set(setOf("sgc.*"))

    // Excluir classes que não devem ser mutadas (Configurações, DTOs, Exceções, etc.)
    excludedClasses.set(setOf(
        "sgc.Sgc",
        "sgc.**.*Config",
        "sgc.**.*Dto",
        "sgc.**.*Exception",
        "sgc.**.*Repo", // Repositórios são interfaces/Spring Data, mutação aqui é pouco útil
        "sgc.**.*MapperImpl" // Classes geradas pelo MapStruct
    ))

    // Threads para paralelismo (ajustar conforme a máquina)
    threads.set(4)

    // Formatos de saída
    outputFormats.set(setOf("XML", "HTML"))

    // Fail-fast: para no primeiro mutante sobrevivente (útil para CI, opcional localmente)
    // mutationThreshold.set(80) // Meta de 80% de mutantes mortos
}
```

## 3. Estratégia de Execução: Fase Piloto

Devido ao alto custo computacional do MBT, não é recomendável rodar em todo o projeto de uma vez. A estratégia será incremental.

**Pacote Piloto:** `sgc.unidade`
*   *Justificativa:* Domínio central, lógica de negócio clara, hierarquia e regras de validação.

### Comando para execução do Piloto:

```bash
cd /app && ./gradlew :backend:pitest --targetClasses="sgc.unidade.*" --targetTests="sgc.unidade.*"
```

*Nota: O parâmetro `--targetClasses` e `--targetTests` filtra a execução apenas para o pacote desejado.*

## 4. Fluxo de Trabalho do Agente de IA (Analysis Loop)

Ao receber a tarefa de "melhorar testes via MBT", o agente deve seguir este ciclo:

1.  **Executar MBT no Escopo:** Rodar o comando Gradle focado na classe ou pacote que está sendo trabalhado.
2.  **Ler o Relatório HTML:** O relatório é gerado em `backend/build/reports/pitest/index.html`. O agente deve procurar por **Mutantes Sobreviventes** (cor vermelha).
3.  **Analisar Mutantes Sobreviventes:**
    *   *Exemplo:* O código original era `if (a > b)`. O mutante alterou para `if (a >= b)`. Os testes passaram.
    *   *Diagnóstico:* Falta um caso de teste para a condição de borda onde `a == b`.
4.  **Criar/Ajustar Teste:** Escrever um novo teste unitário que cubra explicitamente o cenário que permitiu a sobrevivência do mutante.
5.  **Verificar:** Rodar o PITest novamente para confirmar que o mutante foi "morto" (killed).

## 5. Integração Contínua (CI) e Performance

Para evitar lentidão no pipeline principal:

1.  **Não adicionar ao `build` padrão:** O MBT não deve rodar no `./gradlew build` ou `test` padrão. Ele deve ser uma tarefa separada.
2.  **Execução Noturna ou Sob Demanda:** Configurar pipelines específicos para rodar MBT em pacotes críticos.
3.  **Cache:** O PITest possui suporte a histórico (`historyInputLocation` e `historyOutputLocation`) para rodar apenas em classes alteradas. Isso deve ser habilitado em fases futuras.

## 6. Próximos Passos (Action Items)

1.  Aplicar as alterações no `backend/build.gradle.kts`.
2.  Validar a instalação rodando `./gradlew tasks | grep pitest`.
3.  Realizar a primeira rodada no pacote `sgc.unidade`.
4.  Documentar os primeiros mutantes encontrados e corrigidos como exemplo para a equipe.
