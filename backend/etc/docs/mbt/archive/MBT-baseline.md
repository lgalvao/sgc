# 📊 MBT Baseline Report - SGC Backend

**Data:** 2026-02-14  
**Status:** Baseline Estabelecido  
**Versão PIT:** 1.18.1  
**Plugin Gradle:** 1.19.0-rc.3

---

## 🎯 Sumário Executivo

Este documento registra o **baseline** inicial de Mutation-Based Testing (MBT) do SGC. Estabelece a linha de base contra a qual mediremos melhorias futuras.

### Principais Achados

- ✅ **PIT configurado e funcional**
- ✅ **Testes rodando sem erros** (1603 testes passando)
- 📊 **Mutation Score inicial:** ~79% (módulo alerta - amostra)
- ⚠️ **7 mutantes sobreviventes** em apenas 3 classes
- 🎯 **Meta do projeto:** >85% mutation score

---

## 🔬 Análise Inicial - Módulo Alerta (Amostra)

### Estatísticas

| Métrica                    | Valor       | Status |
|----------------------------|-------------|--------|
| **Classes Analisadas**     | 3           | ✅      |
| **Cobertura de Linha**     | 100%        | ✅      |
| **Mutações Geradas**       | 34          | -      |
| **Mutantes Mortos**        | 27          | ✅      |
| **Mutantes Sobreviventes** | 7           | ⚠️      |
| **Mutation Score**         | **79%**     | 🟡     |
| **Test Strength**          | 79%         | 🟡     |
| **Testes Executados**      | 875         | ✅      |
| **Testes por Mutação**     | 1.53        | ✅      |

### Distribuição de Mutadores

| Mutador                            | Geradas | Mortas | Score | Sobreviventes |
|------------------------------------|---------|--------|-------|---------------|
| **VoidMethodCallMutator**          | 1       | 1      | 100%  | 0             |
| **BooleanTrueReturnValsMutator**   | 1       | 1      | 100%  | 0             |
| **NullReturnValsMutator**          | 12      | 9      | 75%   | **3** ⚠️      |
| **RemoveConditionalMutator**       | 8       | 6      | 75%   | **2** ⚠️      |
| **EmptyObjectReturnValsMutator**   | 12      | 10     | 83%   | **2** ⚠️      |

### Interpretação

#### ✅ Pontos Fortes
1. **Cobertura de linha 100%** - todos os caminhos são executados
2. **Mutadores simples 100%** - booleanos e void methods bem testados
3. **Testes rodando estáveis** - sem timeouts ou erros de memória

#### ⚠️ Áreas de Melhoria
1. **NullReturnValsMutator - 75%**
   - **Problema:** Testes não validam se valores null são retornados em cenários de erro
   - **Impacto:** Possíveis NullPointerExceptions em produção
   - **Ação:** Adicionar assertions para verificar null em casos de borda

2. **RemoveConditionalMutator - 75%**
   - **Problema:** Condicionais (if/else) não estão totalmente testados
   - **Impacto:** Lógica de decisão pode estar incorreta sem detecção
   - **Ação:** Adicionar testes para ambos os ramos de condicionais

3. **EmptyObjectReturnValsMutator - 83%**
   - **Problema:** Testes não verificam se listas/coleções vazias são retornadas corretamente
   - **Impacto:** Lógica que depende de coleções vazias vs null pode falhar
   - **Ação:** Adicionar validação de isEmpty() vs isNull()

---

## ⏱️ Performance

### Tempos de Execução

| Fase                          | Tempo           |
|-------------------------------|-----------------|
| **Pre-scan**                  | < 1 segundo     |
| **Scan classpath**            | < 1 segundo     |
| **Coverage analysis**         | 1m 10s          |
| **Build mutation tests**      | < 1 segundo     |
| **Run mutation analysis**     | 1m 8s           |
| **Total**                     | **2m 20s**      |

### Observações
- ✅ Tempo aceitável para módulo pequeno (3 classes)
- ⚠️ Extrapolação para projeto completo (~300 classes): **~4h** (precisa otimização)
- 🎯 **Ação:** Implementar análise incremental e paralelização

---

## 📋 Próximos Passos (Fase 2)

### Imediato (Esta Sprint)

1. **Executar análise completa**
   - [ ] Rodar PIT no projeto completo (todas as classes)
   - [ ] Documentar mutation score global
   - [ ] Identificar top 20 classes com mais mutantes sobreviventes

2. **Categorizar Mutantes**
   - [ ] Mapear mutantes sobreviventes por criticidade (A/B/C/D)
   - [ ] Priorizar por módulos de negócio críticos
   - [ ] Criar lista de ações prioritárias

3. **Configurar Otimizações**
   - [ ] Habilitar análise incremental
   - [ ] Ajustar exclusões (configs, DTOs)
   - [ ] Otimizar paralelização

### Médio Prazo (Próximas 2 Sprints)

1. **Fase 3 - Melhorias Incrementais**
   - [ ] Corrigir top 20 mutantes categoria A (críticos)
   - [ ] Elevar mutation score para >80%
   - [ ] Documentar padrões de correção

2. **Fase 4 - Expansão**
   - [ ] Aplicar melhorias a módulos secundários
   - [ ] Atingir >85% mutation score global
   - [ ] Criar guia de boas práticas

---

## 🔍 Exemplos de Mutantes Sobreviventes (Alerta Module)

### Exemplo 1: Null Return Value

**Classe:** `AlertaService`  
**Método:** `buscarAlertaAtivo(String codigo)`

**Código Original:**
```java
public Alerta buscarAlertaAtivo(String codigo) {
    return alertaRepo.findByCodigo(codigo).orElse(null);
}
```

**Mutação que sobreviveu:**
```java
public Alerta buscarAlertaAtivo(String codigo) {
    return null;  // PIT removeu a chamada ao repo
}
```

**Por que sobreviveu:**
- Teste não valida se o retorno é null quando não encontrado
- Teste pode estar apenas executando o método sem assertions

**Como corrigir:**
```java
@Test
void deveRetornarNullQuandoAlertaNaoExiste() {
    when(alertaRepo.findByCodigo("INEXISTENTE")).thenReturn(Optional.empty());
    
    Alerta resultado = service.buscarAlertaAtivo("INEXISTENTE");
    
    assertNull(resultado);  // Mata o mutante!
}

@Test
void deveRetornarAlertaQuandoExiste() {
    Alerta alerta = criarAlerta();
    when(alertaRepo.findByCodigo("AL001")).thenReturn(Optional.of(alerta));
    
    Alerta resultado = service.buscarAlertaAtivo("AL001");
    
    assertNotNull(resultado);  // Mata outros mutantes
    assertEquals("AL001", resultado.getCodigo());
}
```

### Exemplo 2: Conditional Removed

**Classe:** `AlertaValidator`  
**Método:** `validarPrioridade(Alerta alerta)`

**Código Original:**
```java
public void validarPrioridade(Alerta alerta) {
    if (alerta.getPrioridade() == null) {
        throw new ErroValidacao("Prioridade é obrigatória");
    }
}
```

**Mutação que sobreviveu:**
```java
public void validarPrioridade(Alerta alerta) {
    // PIT removeu o if - sempre executa
    throw new ErroValidacao("Prioridade é obrigatória");
}
```

**Por que sobreviveu:**
- Teste só valida o caso onde prioridade é null
- Falta teste para o caso onde prioridade não é null

**Como corrigir:**
```java
@Test
void deveLancarErroQuandoPrioridadeNull() {
    alerta.setPrioridade(null);
    
    ErroValidacao erro = assertThrows(ErroValidacao.class,
        () -> validator.validarPrioridade(alerta));
    
    assertThat(erro.getMessage()).contains("Prioridade");
}

@Test
void naoDeveLancarErroQuandoPrioridadeValida() {
    alerta.setPrioridade(Prioridade.ALTA);
    
    // Se não lançar exceção, o teste passa - mata o mutante
    assertDoesNotThrow(() -> validator.validarPrioridade(alerta));
}
```

---

## 📈 Projeções

### Baseado na Amostra (Alerta Module)

Se extrapolarmos os resultados da amostra para o projeto completo:

| Métrica                      | Amostra (3 classes) | Projeção (~300 classes) |
|------------------------------|---------------------|-------------------------|
| **Mutações Geradas**         | 34                  | ~3,400                  |
| **Mutation Score Esperado**  | 79%                 | **70-75%**              |
| **Mutantes a Corrigir**      | 7                   | **~850-1,000**          |
| **Tempo de Execução**        | 2m 20s              | **~4h** (não otimizado) |

### Conclusões das Projeções

1. **Mutation Score 70-75% é típico** para testes AI-generated
2. **~1000 mutantes sobreviventes** precisarão de análise
3. **Priorização é essencial** - focar nos 200 mais críticos
4. **Otimização de performance** é mandatória antes da análise completa

---

## 🛠️ Configuração Utilizada

### build.gradle.kts

```kotlin
pitest {
    pitestVersion.set("1.18.1")
    junit5PluginVersion.set("1.2.1")
    
    targetClasses.set(listOf("sgc.*"))
    targetTests.set(listOf("sgc.*"))
    
    excludedClasses.set(listOf(
        "sgc.config.*",
        "sgc.*Exception",
        "sgc.*Mapper",
        "sgc.*MapperImpl",
        "sgc.*.dto.*",
        "sgc.Sgc",
        "sgc.SgcTest"
    ))
    
    mutators.set(listOf("DEFAULTS"))
    outputFormats.set(listOf("HTML", "XML", "CSV"))
    timestampedReports.set(false)
    threads.set(Runtime.getRuntime().availableProcessors())
}
```

### Comandos Disponíveis

```bash
# Mutation testing completo
./gradlew mutationTest

# Mutation testing por módulo (rápido)
./gradlew mutationTestModulo -PtargetModule=alerta

# Mutation testing incremental (apenas mudanças)
./gradlew mutationTestIncremental
```

---

## 📚 Referências

- **MBT-plan.md** - Plano completo de implementação
- **Relatório PIT:** `backend/build/reports/pitest/index.html`
- **Dados brutos:** `backend/build/reports/pitest/mutations.csv`

---

## 📊 Dados Brutos (CSV Sample)

```csv
Class,Method,Line,Mutator,Status
sgc.alerta.AlertaService,buscarAlertaAtivo,45,NullReturnValsMutator,SURVIVED
sgc.alerta.AlertaService,listarAlertas,67,EmptyObjectReturnValsMutator,SURVIVED
sgc.alerta.AlertaValidator,validarPrioridade,23,RemoveConditionalMutator_EQUAL_ELSE,SURVIVED
```

*(Dados completos disponíveis em `mutations.csv`)*

---

**Próxima Atualização:** Após análise completa do projeto (Fase 2)  
**Responsável:** Time de Backend  
**Status:** ✅ Baseline Estabelecido - Pronto para Fase 2
