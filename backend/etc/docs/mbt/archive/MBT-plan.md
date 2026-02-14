# 🧬 Plano de Mutation-Based Testing (MBT) para SGC

**Data de Criação:** 2026-02-14  
**Status:** Plano Aprovado - Em Implementação  
**Versão:** 1.0

---

## 📋 Sumário Executivo

Este documento apresenta um plano completo e faseado para implementar **Mutation-Based Testing (MBT)** no backend do SGC. Embora o projeto tenha 100% de cobertura de código (JaCoCo), os testes foram gerados por IA e podem não ser efetivos na detecção de defeitos. MBT é uma técnica que introduz mutações (pequenas modificações) no código-fonte e verifica se os testes detectam essas mudanças, revelando fraquezas nos testes.

### Objetivos

1. **Avaliar qualidade real dos testes** através de mutation score
2. **Identificar testes ineficazes** que não detectam mudanças no código
3. **Melhorar assertion quality** - testes que passam mas não validam comportamento
4. **Reduzir falso senso de segurança** da cobertura de 100%
5. **Criar cultura de qualidade** sustentável para testes futuros

### Resultados Esperados

- **Mutation Score inicial:** 60-70% (baseline típico para testes AI-generated)
- **Mutation Score meta:** >85% (padrão industrial para projetos críticos)
- **Identificação de 200-300 mutantes sobreviventes** para análise
- **Plano de melhorias priorizadas** baseado em criticidade de negócio

---

## 🎯 O que é Mutation-Based Testing?

### Conceito

MBT introduz pequenas mudanças (mutações) no código e executa os testes:

- **Mutante Morto (Killed):** Teste detectou a mutação (falhou) ✅ Bom
- **Mutante Sobrevivente (Survived):** Teste passou apesar da mutação ❌ Problema
- **Mutante Equivalente:** Mudança não afeta comportamento (ignorado)

### Exemplo Prático

**Código Original:**
```java
public boolean isAtivo() {
    return status == Status.ATIVO;
}
```

**Mutação:** Trocar `==` por `!=`
```java
public boolean isAtivo() {
    return status != Status.ATIVO;  // Mutação
}
```

**Teste Efetivo:**
```java
@Test
void deveRetornarTrueQuandoAtivo() {
    processo.setStatus(Status.ATIVO);
    assertTrue(processo.isAtivo());  // Mata o mutante
}
```

**Teste Ineficaz:**
```java
@Test
void testeIsAtivo() {
    processo.isAtivo();  // Não valida resultado - mutante sobrevive!
}
```

### Por que Cobertura 100% Não É Suficiente?

```java
// Este teste dá 100% de cobertura, mas não valida nada!
@Test
void testCriarProcesso() {
    service.criar(request);  // Linha executada ✓
    // Sem assertions - mutantes sobrevivem!
}

// Este teste é efetivo
@Test
void deveCriarProcessoComStatusPendente() {
    ProcessoResponse response = service.criar(request);
    assertEquals(StatusProcesso.PENDENTE, response.getStatus());  // Mata mutantes
}
```

---

## 🛠️ Ferramenta Escolhida: PIT (Pitest)

### Características

- **Mais popular** para JVM (Java/Kotlin)
- **Integração nativa** com Gradle e JUnit 5
- **Mutadores configuráveis** (defaults, stronger, all)
- **Relatórios detalhados** (HTML, XML, CSV)
- **Performance otimizada** (análise incremental, paralelização)
- **Suporte Spring Boot** através de extensões

### Alternativas Consideradas

| Ferramenta | Prós                         | Contras                          | Decisão     |
|------------|------------------------------|----------------------------------|-------------|
| **PIT**    | Maduro, rápido, Spring Boot  | -                                | ✅ Escolhido |
| Stryker4s  | Relatórios bonitos           | Focado em Scala                  | ❌ Rejeitado |
| µTest      | Acadêmico                    | Pouco suporte, não mantido       | ❌ Rejeitado |
| Manual     | Controle total               | Muito trabalhoso, não escalável  | ❌ Rejeitado |

---

## 📊 Fases de Implementação

### Fase 1: Configuração e Baseline (Semana 1)

**Objetivo:** Configurar PIT e estabelecer baseline de qualidade

#### Ações

1. **Adicionar PIT ao build.gradle.kts**
   ```kotlin
   plugins {
       id("info.solidsoft.pitest") version "1.15.0"
   }
   
   dependencies {
       testImplementation("org.pitest:pitest-junit5-plugin:1.2.1")
   }
   
   pitest {
       targetClasses.set(listOf("sgc.*"))
       targetTests.set(listOf("sgc.*"))
       mutators.set(listOf("DEFAULTS"))
       outputFormats.set(listOf("HTML", "XML", "CSV"))
       timestampedReports.set(false)
       threads.set(Runtime.getRuntime().availableProcessors())
   }
   ```

2. **Executar primeira análise completa**
   ```bash
   ./gradlew pitest
   # Relatório gerado em: backend/build/reports/pitest/index.html
   ```

3. **Documentar baseline**
   - Mutation Score total
   - Top 10 classes com mais mutantes sobreviventes
   - Tipos de mutantes mais comuns
   - Tempo de execução

4. **Criar estrutura de rastreamento**
   - `MBT-baseline.md` - Snapshot inicial
   - `MBT-progress.md` - Progresso por sprint
   - `MBT-mutantes-prioritarios.md` - Lista de mutantes críticos

**Critério de Sucesso:** Baseline documentado, relatórios gerados, tempo de execução <30min

**Entregáveis:**
- ✅ PIT configurado no Gradle
- ✅ Primeiro relatório de mutação gerado
- ✅ Documentação de baseline criada

---

### Fase 2: Análise Exploratória (Semana 2)

**Objetivo:** Entender padrões de mutantes sobreviventes e priorizar ações

#### Ações

1. **Análise por Módulo**
   ```bash
   # Gerar relatório por módulo
   ./gradlew pitest -PtargetClasses=sgc.processo.*
   ./gradlew pitest -PtargetClasses=sgc.subprocesso.*
   ./gradlew pitest -PtargetClasses=sgc.mapa.*
   ```

2. **Categorizar Mutantes Sobreviventes**
   - **Categoria A - Crítico:** Lógica de negócio (validações, transições de estado)
   - **Categoria B - Alto:** Controle de fluxo (if/else, loops)
   - **Categoria C - Médio:** Operadores matemáticos/lógicos
   - **Categoria D - Baixo:** Constantes, getters/setters

3. **Identificar Padrões Comuns**
   - Testes sem assertions (apenas executam código)
   - Assertions genéricas (`assertNotNull` sem validar valor)
   - Falta de casos de borda (null, vazio, limites)
   - Mocks excessivos (não validam interações importantes)

4. **Priorizar Módulos para Fase 3**
   - Criticidade de negócio (Processo > Mapa > Subprocesso)
   - Número de mutantes sobreviventes
   - Complexidade ciclomática
   - Histórico de bugs em produção

**Critério de Sucesso:** >200 mutantes categorizados, top 20 prioritários identificados

**Entregáveis:**
- ✅ Relatório de análise por módulo
- ✅ Categorização de mutantes
- ✅ Lista de 20 mutantes prioritários para correção

---

### Fase 3: Melhorias Incrementais - Sprint 1 (Semanas 3-4)

**Objetivo:** Corrigir mutantes categoria A (críticos) e elevar mutation score 10-15%

#### Escopo

- **Foco:** Módulo `sgc.processo.*` (mais crítico)
- **Meta:** Mutation score 70% → 80%+

#### Ações

1. **Analisar testes de ProcessoService**
   - Identificar métodos com mutantes sobreviventes
   - Revisar assertions existentes
   - Adicionar casos de teste faltantes

2. **Padrões de Melhoria**

   **Antes (Ineficaz):**
   ```java
   @Test
   void testIniciarProcesso() {
       service.iniciar(codigo);
       verify(repo).save(any());  // Não valida comportamento
   }
   ```

   **Depois (Eficaz):**
   ```java
   @Test
   void deveAlterarStatusParaIniciadoAoIniciar() {
       Processo processo = criarProcessoPendente();
       when(repo.findByCodigo(codigo)).thenReturn(Optional.of(processo));
       
       service.iniciar(codigo);
       
       assertEquals(StatusProcesso.INICIADO, processo.getStatus());
       assertNotNull(processo.getDataInicio());
   }
   
   @Test
   void deveLancarErroAoIniciarProcessoInexistente() {
       when(repo.findByCodigo(codigo)).thenReturn(Optional.empty());
       
       ErroNegocio erro = assertThrows(ErroNegocio.class, 
           () -> service.iniciar(codigo));
       assertThat(erro.getMessage()).contains("não encontrado");
   }
   ```

3. **Executar PIT incrementalmente**
   ```bash
   # Apenas ProcessoService
   ./gradlew pitest -PtargetClasses=sgc.processo.ProcessoService
   ```

4. **Validar melhorias**
   - Mutation score aumentou?
   - Novos mutantes mortos?
   - Tempo de execução aceitável?

**Critério de Sucesso:** 15+ mutantes mortos, mutation score >75% no módulo processo

**Entregáveis:**
- ✅ 15-20 testes melhorados/criados
- ✅ Relatório de progresso comparativo
- ✅ Documentação de padrões aplicados

---

### Fase 4: Melhorias Incrementais - Sprint 2 (Semanas 5-6)

**Objetivo:** Corrigir mutantes categoria B (alto) em módulos secundários

#### Escopo

- **Foco:** Módulos `sgc.subprocesso.*` e `sgc.mapa.*`
- **Meta:** Mutation score 70% → 78%+

#### Ações

1. **Aplicar padrões da Fase 3** aos novos módulos
2. **Focar em controle de fluxo**
   - Condicionais (if/else)
   - Loops (for/while)
   - Switch/case
   - Short-circuit operators (&&, ||)

3. **Melhorar validação de estados**
   ```java
   // Teste ineficaz
   @Test
   void testTransicao() {
       service.transicionar(codigo, novoStatus);
   }
   
   // Teste eficaz - valida todas as transições
   @ParameterizedTest
   @CsvSource({
       "PENDENTE, INICIADO, true",
       "INICIADO, CONCLUIDO, true",
       "CONCLUIDO, PENDENTE, false",  // Transição inválida
   })
   void deveValidarTransicoesDeStatus(Status origem, Status destino, boolean valida) {
       processo.setStatus(origem);
       
       if (valida) {
           assertDoesNotThrow(() -> service.transicionar(codigo, destino));
           assertEquals(destino, processo.getStatus());
       } else {
           assertThrows(ErroNegocio.class, 
               () -> service.transicionar(codigo, destino));
       }
   }
   ```

4. **Adicionar testes de limites**
   ```java
   @Test
   void deveLancarErroQuandoListaExcedeLimite() {
       List<SubprocessoRequest> muitos = gerarLista(101);  // Limite é 100
       
       ErroValidacao erro = assertThrows(ErroValidacao.class,
           () -> service.criarEmLote(muitos));
       assertThat(erro.getMessage()).contains("máximo de 100");
   }
   ```

**Critério de Sucesso:** 20+ mutantes mortos, mutation score global >75%

**Entregáveis:**
- ✅ 20-25 testes melhorados/criados
- ✅ Cobertura de casos de borda ampliada
- ✅ Documentação de transições de estado testadas

---

### Fase 5: Refinamento e Otimização (Semana 7)

**Objetivo:** Otimizar configuração PIT e refinar testes restantes

#### Ações

1. **Habilitar mutadores mais fortes**
   ```kotlin
   pitest {
       mutators.set(listOf("STRONGER"))  // Mutações mais agressivas
   }
   ```

2. **Revisar mutantes equivalentes**
   - Marcar mutantes que não afetam comportamento
   - Documentar no código (comentários)

3. **Otimizar performance**
   ```kotlin
   pitest {
       threads.set(8)  // Paralelização
       timeoutFactor.set(1.5)  // Evitar timeouts falsos
       excludedClasses.set(listOf(
           "sgc.config.*",    // Configurações
           "sgc.*Exception",  // Exceptions (baixo valor)
           "sgc.*Mapper*"     // Mappers (já validados indiretamente)
       ))
   }
   ```

4. **Criar tarefa Gradle dedicada**
   ```kotlin
   tasks.register("mutationTest") {
       group = "quality"
       description = "Executa mutation testing com PIT"
       dependsOn("pitest")
   }
   
   tasks.register("mutationTestFast") {
       group = "quality"
       description = "Mutation testing apenas em mudanças recentes"
       dependsOn("pitest")
       doFirst {
           // Configurar para rodar apenas em classes modificadas
       }
   }
   ```

**Critério de Sucesso:** Tempo de execução <20min, 95% mutadores relevantes configurados

**Entregáveis:**
- ✅ Configuração PIT otimizada
- ✅ Tarefas Gradle customizadas
- ✅ Documentação de mutantes equivalentes

---

### Fase 6: Integração CI/CD (Semana 8)

**Objetivo:** Integrar MBT ao pipeline de qualidade e criar governança

#### Ações

1. **Adicionar PIT ao qualityCheck**
   ```kotlin
   tasks.named("qualityCheck") {
       dependsOn("pitest")
   }
   ```

2. **Configurar thresholds**
   ```kotlin
   pitest {
       mutationThreshold.set(80)  // Falha se <80%
       coverageThreshold.set(99)  // Mantém cobertura alta
   }
   ```

3. **Criar relatório consolidado**
   - Mutation score por módulo
   - Tendência ao longo do tempo
   - Top 10 classes com mais mutantes

4. **Documentar processo para equipe**
   - Como rodar MBT localmente
   - Como interpretar relatórios
   - Como corrigir mutantes sobreviventes
   - Quando rodar (pre-commit, CI, semanal)

5. **Estabelecer cadência**
   - **Diária (CI):** Mutation testing incremental (apenas mudanças)
   - **Semanal:** Mutation testing completo
   - **Mensal:** Análise de tendências e ajuste de metas

**Critério de Sucesso:** PIT rodando em CI, documentação completa, equipe treinada

**Entregáveis:**
- ✅ PIT integrado ao CI/CD
- ✅ Dashboard de mutation score
- ✅ Guia de MBT para desenvolvedores
- ✅ Política de qualidade atualizada

---

## 📈 Métricas e KPIs

### Métricas Principais

| Métrica                    | Baseline | Meta Fase 3 | Meta Fase 4 | Meta Final |
|----------------------------|----------|-------------|-------------|------------|
| **Mutation Score Global**  | ~65%     | 75%         | 80%         | >85%       |
| **Mutantes Mortos**        | ~1200    | ~1600       | ~1800       | >2000      |
| **Classes com >90% Score** | ~20%     | ~40%        | ~60%        | >75%       |
| **Tempo Execução (min)**   | ~30      | ~25         | ~22         | <20        |

### Métricas Secundárias

- **Mutantes por Categoria** (A/B/C/D)
- **Mutation Score por Módulo** (Processo, Subprocesso, Mapa, etc)
- **Taxa de Mutantes Equivalentes** (<5% é ideal)
- **Cobertura de Assertions** (ratio assertions/testes)

### Rastreamento de Progresso

```markdown
## Sprint N - Progresso MBT

**Data:** YYYY-MM-DD
**Módulo Foco:** sgc.processo.*

### Métricas
- Mutation Score: 72% → 78% (+6%)
- Mutantes Mortos: +18
- Testes Melhorados: 12
- Testes Criados: 6

### Mutantes Prioritários Corrigidos
1. ✅ ProcessoService.iniciar() - Boundary condition
2. ✅ ProcessoValidator.validarTransicao() - Conditional negation
3. ✅ ProcessoService.excluir() - Return value mutation
...

### Lições Aprendidas
- Testes de transição de estado eram muito genéricos
- Faltavam validações de exceções
- Mocks não validavam argumentos
```

---

## 🔍 Tipos de Mutações do PIT

### Mutadores DEFAULTS (Fase 1-4)

| Mutador                    | Descrição                           | Exemplo                               |
|----------------------------|-------------------------------------|---------------------------------------|
| **Conditionals Boundary**  | Troca `<` por `<=`, `>` por `>=`    | `if (x < 10)` → `if (x <= 10)`        |
| **Negate Conditionals**    | Inverte condicionais                | `if (x == y)` → `if (x != y)`         |
| **Math**                   | Troca operadores matemáticos        | `a + b` → `a - b`                     |
| **Increments**             | Troca `++` por `--`                 | `i++` → `i--`                         |
| **Invert Negatives**       | Remove `-` de números               | `-x` → `x`                            |
| **Void Method Calls**      | Remove chamadas void                | `log.info(...)` → (removido)          |
| **Return Values**          | Troca valores de retorno            | `return true` → `return false`        |

### Mutadores STRONGER (Fase 5)

- **Remove Conditionals:** Remove `if` completamente
- **Switch Statements:** Muda ordem de cases
- **Constructor Calls:** Altera argumentos de construtores

---

## 🎓 Padrões de Correção de Mutantes

### Padrão 1: Assertions Ausentes

**Problema:**
```java
@Test
void testCriar() {
    service.criar(request);  // Executa mas não valida!
}
```

**Solução:**
```java
@Test
void deveCriarComDadosCorretos() {
    ProcessoResponse response = service.criar(request);
    
    assertNotNull(response);
    assertEquals(request.getTitulo(), response.getTitulo());
    assertEquals(StatusProcesso.PENDENTE, response.getStatus());
}
```

### Padrão 2: Assertions Genéricas

**Problema:**
```java
@Test
void testBuscar() {
    Processo p = service.buscar(codigo);
    assertNotNull(p);  // Muito genérico, não valida estado
}
```

**Solução:**
```java
@Test
void deveBuscarComDadosCompletos() {
    Processo p = service.buscar(codigo);
    
    assertNotNull(p.getCodigo());
    assertNotNull(p.getTitulo());
    assertEquals(StatusProcesso.PENDENTE, p.getStatus());
    assertFalse(p.getSubprocessos().isEmpty());
}
```

### Padrão 3: Condicionais Não Testadas

**Problema:**
```java
// Código
if (quantidade > 0) {
    processar();
}

// Teste - só testa caminho feliz
@Test
void testProcessar() {
    service.processar(10);
    verify(repo).save(any());
}
```

**Solução:**
```java
@Test
void deveProcessarQuandoQuantidadePositiva() {
    service.processar(10);
    verify(repo).save(any());
}

@Test
void naoDeveProcessarQuandoQuantidadeZero() {
    service.processar(0);
    verify(repo, never()).save(any());
}

@Test
void naoDeveProcessarQuandoQuantidadeNegativa() {
    assertThrows(ErroValidacao.class, () -> service.processar(-1));
}
```

### Padrão 4: Valores de Retorno Não Validados

**Problema:**
```java
@Test
void testEhValido() {
    validator.ehValido(processo);  // Não captura retorno!
}
```

**Solução:**
```java
@Test
void deveRetornarTrueQuandoValido() {
    assertTrue(validator.ehValido(processoValido));
}

@Test
void deveRetornarFalseQuandoInvalido() {
    assertFalse(validator.ehValido(processoInvalido));
}
```

### Padrão 5: Exceções Não Testadas

**Problema:**
```java
// Código
if (processo == null) {
    throw new ErroNegocio("Processo não pode ser nulo");
}

// Teste - não testa exceção
@Test
void testIniciar() {
    service.iniciar(codigo);
}
```

**Solução:**
```java
@Test
void deveLancarErroQuandoProcessoNulo() {
    when(repo.findByCodigo(codigo)).thenReturn(Optional.empty());
    
    ErroNegocio erro = assertThrows(ErroNegocio.class,
        () -> service.iniciar(codigo));
    
    assertThat(erro.getMessage())
        .contains("Processo")
        .contains("não encontrado");
}
```

---

## 🚀 Quick Start Guide

### Para Desenvolvedores

```bash
# 1. Rodar mutation testing completo
./gradlew pitest

# 2. Ver relatório
open backend/build/reports/pitest/index.html

# 3. Rodar apenas para suas mudanças (rápido)
./gradlew pitest -PtargetClasses=sgc.processo.ProcessoService

# 4. Verificar qualidade completa
./gradlew qualityCheck  # Inclui PIT após Fase 6
```

### Interpretando o Relatório

**Mutation Coverage:** Percentual de mutantes mortos
- 🟢 **>85%:** Excelente
- 🟡 **70-85%:** Bom (meta intermediária)
- 🔴 **<70%:** Necessita melhorias

**Classes com Problemas:**
- Ordenadas por número de mutantes sobreviventes
- Clique para ver linha por linha
- Verde = mutante morto, vermelho = sobrevivente

**Tipos de Mutantes:**
- Foco em "Conditionals" e "Return Values" primeiro
- "Void Method Calls" são menos críticos

---

## 📚 Recursos e Referências

### Documentação

- [PIT Official Documentation](https://pitest.org/)
- [PIT Gradle Plugin](https://gradle-pitest-plugin.solidsoft.info/)
- [JUnit 5 + PIT Integration](https://pitest.org/quickstart/junit5/)
- [Mutation Testing Best Practices](https://pitest.org/quickstart/best_practices/)

### Artigos Acadêmicos

- *"Are Mutants a Valid Substitute for Real Faults?"* - Andrews et al.
- *"An Analysis of Mutation Operators"* - Offutt & Untch
- *"Mutation Testing: A Comprehensive Survey"* - Jia & Harman

### Ferramentas Complementares

- **Stryker Mutator Dashboard** - Visualização de tendências
- **Mutation Testing Elements** - Web components para relatórios
- **Gradle Build Scans** - Análise de performance do PIT

---

## ⚠️ Riscos e Mitigações

### Risco 1: Tempo de Execução Longo

**Impacto:** MBT pode levar 10-30x mais tempo que testes normais

**Mitigação:**
- Executar apenas em CI (não pre-commit)
- Usar análise incremental
- Paralelizar com `threads.set()`
- Rodar full scan apenas semanalmente
- Excluir classes de baixo risco (configs, DTOs)

### Risco 2: Mutantes Equivalentes

**Impacto:** 3-5% mutantes não afetam comportamento (falsos positivos)

**Mitigação:**
- Documentar e marcar equivalentes
- Ajustar threshold considerando taxa esperada
- Revisar manualmente casos suspeitos

### Risco 3: Falso Senso de Segurança

**Impacto:** 85% mutation score não significa código perfeito

**Mitigação:**
- Combinar com outras técnicas (property-based, E2E)
- Manter code review rigoroso
- Usar SpotBugs, SonarQube para análise estática
- Testes de integração e E2E continuam essenciais

### Risco 4: Resistência da Equipe

**Impacto:** Desenvolvedores podem ver como trabalho extra

**Mitigação:**
- Demonstrar valor com exemplos concretos de bugs
- Começar com módulos pequenos
- Celebrar melhorias
- Automatizar no CI (não manual)
- Documentação clara e acessível

---

## 🎯 Próximos Passos Imediatos

### Semana 1 (Esta semana)

- [ ] Revisar e aprovar este plano
- [ ] Configurar PIT no build.gradle.kts
- [ ] Executar primeira análise (baseline)
- [ ] Documentar resultados em MBT-baseline.md

### Semana 2

- [ ] Analisar relatório de baseline
- [ ] Categorizar top 50 mutantes sobreviventes
- [ ] Priorizar 20 mutantes para Fase 3
- [ ] Definir metas específicas por módulo

### Semanas 3-4 (Fase 3)

- [ ] Implementar melhorias em sgc.processo.*
- [ ] Documentar padrões aplicados
- [ ] Medir progresso vs baseline
- [ ] Ajustar plano se necessário

---

## 📞 Suporte e Contato

### Responsáveis

- **Implementação Técnica:** Time de Backend
- **Revisão de Qualidade:** Tech Lead
- **Aprovação de Metas:** Engineering Manager

### Comunicação

- **Status Semanal:** Sprint review
- **Dúvidas Técnicas:** Slack #backend-quality
- **Documentação:** Este plano + relatórios semanais

---

## 📝 Histórico de Revisões

| Versão | Data       | Autor     | Mudanças                          |
|--------|------------|-----------|-----------------------------------|
| 1.0    | 2026-02-14 | Jules AI  | Criação inicial do plano completo |

---

**Última Atualização:** 2026-02-14  
**Status:** ✅ Aprovado para Implementação
