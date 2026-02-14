# 🤖 MBT Implementation Plan - Adapted for AI Agents

**Data de Criação:** 2026-02-14  
**Status:** Phase 2+ In Progress  
**Adaptação:** Otimizado para execução por agentes de IA (não equipes humanas)

---

## 🎯 Visão Geral

Este documento adapta o plano MBT original para execução autônoma por agentes de IA. Diferente do plano original (que pressupõe equipe humana), este foca em:

- **Automação completa** de análise e correções
- **Iterações incrementais** com validação contínua
- **Priorização baseada em dados** (não opiniões humanas)
- **Documentação auto-gerada** de progresso e decisões
- **Paralelização segura** de tarefas independentes

---

## 🚀 Phase 2: Baseline Analysis (CURRENT)

### Objetivos

1. Estabelecer baseline completo de mutation score
2. Categorizar mutantes automaticamente
3. Priorizar correções baseado em impacto e criticidade
4. Otimizar configurações para análise incremental

### Estratégia Adaptada para IA

**Problema Original:** Análise completa do projeto (~300 classes) leva ~4 horas e pode causar timeouts.

**Solução AI-Optimized:**

1. **Análise Modular Incremental**
   - Rodar mutation testing módulo por módulo
   - Cada módulo = análise de 2-5 minutos
   - Paralelizar análises quando possível
   - Agregar resultados em baseline global

2. **Priorização Automática**
   - Módulos críticos primeiro (processo, subprocesso, mapa)
   - Módulos com maior número de classes depois
   - Módulos utilitários por último

3. **Categorização Automática de Mutantes**
   - Categoria A (Crítico): Services, Facades, Validators
   - Categoria B (Alto): Controllers, Repositories
   - Categoria C (Médio): Helpers, Utils
   - Categoria D (Baixo): DTOs, Mappers (já excluídos)

### Módulos Identificados (Ordem de Prioridade)

```
1. processo      (~40 classes) - CRÍTICO - Lógica de negócio principal
2. subprocesso   (~30 classes) - CRÍTICO - Dependente de processo
3. mapa          (~25 classes) - ALTO - Workflow visual
4. atividade     (~20 classes) - ALTO - Tarefas do processo
5. organizacao   (~35 classes) - MÉDIO - Estrutura organizacional
6. alerta        (~3 classes)  - BAIXO - Já analisado (79% score)
7. notificacao   (~15 classes) - MÉDIO - Comunicações
8. analise       (~10 classes) - BAIXO - Relatórios
9. seguranca     (~45 classes) - ALTO - Autenticação/Autorização
10. integracao   (~20 classes) - MÉDIO - APIs externas
```

### Execução Phase 2 (Passo a Passo AI)

#### Step 2.1: Análise Modular
```bash
# Para cada módulo prioritário, executar:
for module in processo subprocesso mapa atividade seguranca; do
    echo "=== Analyzing module: $module ==="
    ./gradlew mutationTestModulo -PtargetModule=$module --console=plain
    
    # Extrair métricas do relatório
    # Salvar em MBT-baseline-$module.md
    # Agregar em baseline global
done
```

#### Step 2.2: Extração de Métricas
Para cada módulo, extrair automaticamente:
- Total de mutações geradas
- Mutation score
- Mutantes sobreviventes por tipo
- Top 10 classes com mais mutantes sobreviventes
- Tempo de execução

#### Step 2.3: Categorização Automática
Regras de categorização:

```python
def categorize_mutant(class_name, method_name, mutator_type):
    # Categoria A - CRÍTICO (Business Logic)
    if 'Service' in class_name and method_name in CRITICAL_METHODS:
        return 'A'
    if 'Validator' in class_name:
        return 'A'
    if 'Facade' in class_name and mutator_type in ['RemoveConditional', 'NullReturn']:
        return 'A'
    
    # Categoria B - ALTO (Control Flow)
    if 'Controller' in class_name:
        return 'B'
    if mutator_type == 'RemoveConditional':
        return 'B'
    
    # Categoria C - MÉDIO (Data Flow)
    if mutator_type in ['EmptyObject', 'Math']:
        return 'C'
    
    # Categoria D - BAIXO (Utilities)
    if 'Util' in class_name or 'Helper' in class_name:
        return 'D'
    
    # Default
    return 'C'

CRITICAL_METHODS = [
    'criar', 'atualizar', 'excluir', 'iniciar', 'concluir',
    'validar', 'autorizar', 'processar', 'executar'
]
```

#### Step 2.4: Priorização de Correções
Top 20 mutantes para correção imediata:

Critérios de pontuação:
```
score = (category_weight * 100) + 
        (method_criticality * 50) + 
        (class_usage_frequency * 30) - 
        (fix_complexity * 20)

category_weight:
  A = 4, B = 3, C = 2, D = 1

method_criticality:
  criar/excluir/validar = 5
  atualizar/processar = 4
  buscar/listar = 3
  outros = 2

fix_complexity (estimado):
  NullReturn/EmptyObject = baixa = 1
  RemoveConditional = média = 2
  Math/Increments = alta = 3
```

---

## 🛠️ Phase 3: Critical Fixes - Processo Module

### Objetivos

- Corrigir top 15-20 mutantes categoria A no módulo processo
- Elevar mutation score de ~70% para >80%
- Documentar padrões de correção aplicados
- Validar que correções não quebram testes existentes

### Estratégia AI

1. **Identificar Mutantes**
   - Extrair lista de mutantes sobreviventes do relatório PIT
   - Categorizar e ordenar por prioridade
   - Selecionar top 15-20 mutantes categoria A

2. **Análise de Contexto**
   ```bash
   # Para cada mutante:
   # 1. Ler código-fonte da classe
   # 2. Ler testes existentes
   # 3. Identificar gap de cobertura
   # 4. Determinar tipo de correção necessária
   ```

3. **Padrões de Correção**

   **Padrão 1: Adicionar Assertion de Null**
   ```java
   // Antes (teste ineficaz)
   @Test
   void testCriar() {
       service.criar(request);
   }
   
   // Depois (teste eficaz)
   @Test
   void deveCriarERetornarNaoNulo() {
       ProcessoResponse response = service.criar(request);
       assertNotNull(response);
       assertEquals("esperado", response.getTitulo());
   }
   ```

   **Padrão 2: Testar Ambos os Branches**
   ```java
   // Antes (só caminho feliz)
   @Test
   void testValidar() {
       validator.validar(processoValido);
   }
   
   // Depois (ambos os caminhos)
   @Test
   void devePassarQuandoValido() {
       assertDoesNotThrow(() -> validator.validar(processoValido));
   }
   
   @Test
   void deveFalharQuandoInvalido() {
       assertThrows(ErroValidacao.class, 
           () -> validator.validar(processoInvalido));
   }
   ```

   **Padrão 3: Validar Coleções Vazias**
   ```java
   // Antes
   @Test
   void testListar() {
       List<Processo> result = service.listar();
   }
   
   // Depois
   @Test
   void deveRetornarListaVaziaQuandoSemDados() {
       when(repo.findAll()).thenReturn(Collections.emptyList());
       List<Processo> result = service.listar();
       assertNotNull(result);
       assertTrue(result.isEmpty());
   }
   ```

4. **Aplicação de Correções**
   - Aplicar uma correção por vez
   - Rodar testes unitários após cada correção
   - Rodar mutation testing no módulo para validar
   - Documentar mutante corrigido e padrão aplicado

5. **Validação**
   ```bash
   # Após cada lote de 5 correções:
   ./gradlew :backend:test --tests "*Processo*"
   ./gradlew mutationTestModulo -PtargetModule=processo
   
   # Verificar:
   # - Testes continuam passando
   # - Mutation score aumentou
   # - Nenhum mutante novo criado
   ```

---

## 🔄 Phase 4: Secondary Modules

### Módulos Alvo

- subprocesso
- mapa
- atividade
- seguranca

### Estratégia

Aplicar mesma estratégia da Phase 3, mas em paralelo quando possível:

```
┌─────────────┐     ┌─────────────┐
│ subprocesso │     │    mapa     │
│   Agent 1   │     │   Agent 2   │
└─────────────┘     └─────────────┘
        │                   │
        └───────┬───────────┘
                ▼
        Consolidar Resultados
```

### Meta Phase 4

- Mutation score global: >80%
- Módulos críticos: >85%
- Documentação de todos os padrões aplicados

---

## ⚡ Phase 5: Refinement

### Objetivos

1. **Habilitar STRONGER mutators**
   ```kotlin
   mutators.set(listOf("STRONGER"))
   ```

2. **Otimizar Performance**
   - Target: <20 minutos para análise completa
   - Estratégias:
     - Análise incremental refinada
     - Exclusões mais agressivas
     - Paralelização aumentada

3. **Documentar Equivalentes**
   - Identificar mutantes equivalentes (~3-5%)
   - Marcar no código com comentários
   - Ajustar thresholds considerando taxa

---

## 🔗 Phase 6: CI/CD Integration

### Objetivos

1. **Integração ao Pipeline**
   ```yaml
   # .github/workflows/quality.yml
   - name: Mutation Testing
     run: ./gradlew mutationTestIncremental
     if: github.event_name == 'pull_request'
   
   - name: Full Mutation Analysis
     run: ./gradlew mutationTest
     if: github.event_name == 'schedule'  # Weekly
   ```

2. **Thresholds Automatizados**
   ```kotlin
   pitest {
       mutationThreshold.set(85)
       coverageThreshold.set(99)
   }
   ```

3. **Dashboard de Métricas**
   - Mutation score por módulo
   - Tendências ao longo do tempo
   - Top classes com problemas

---

## 📊 Métricas de Progresso (Auto-Tracked)

### Template de Atualização Automática

Após cada fase, gerar automaticamente:

```markdown
## Phase X Completed - YYYY-MM-DD

### Métricas
- Mutation Score: X% → Y% (+Z%)
- Mutantes Mortos: +N
- Testes Criados: N
- Testes Modificados: N
- Tempo de Análise: Xmin

### Mutantes Corrigidos
1. Classe.método() - Mutador - Categoria - Padrão aplicado
2. ...

### Lições Aprendidas (Auto-Detectadas)
- Padrão X encontrado em N lugares
- Mutador Y mais comum em módulo Z
- ...
```

---

## 🤖 AI Agent Workflow

### Workflow Principal

```
1. SELECT_PHASE()
   ↓
2. ANALYZE_MODULE()
   - Run mutation testing
   - Extract metrics
   - Categorize mutants
   ↓
3. PRIORITIZE_FIXES()
   - Score mutants
   - Select top N
   ↓
4. FOR_EACH_MUTANT:
     - Read code
     - Read tests
     - Identify gap
     - Apply pattern
     - Validate
     ↓
5. VERIFY_MODULE()
   - Run tests
   - Run mutation testing
   - Check score improvement
   ↓
6. DOCUMENT_PROGRESS()
   - Update baseline
   - Update progress tracking
   - Generate report
   ↓
7. NEXT_MODULE() or NEXT_PHASE()
```

### Decision Rules

```python
def should_continue_to_next_phase(current_phase):
    if current_phase == 2:
        return all_modules_analyzed() and baseline_documented()
    elif current_phase == 3:
        return processo_score > 80
    elif current_phase == 4:
        return global_score > 80 and critical_modules_score > 85
    elif current_phase == 5:
        return global_score > 85 and execution_time < 20_min
    elif current_phase == 6:
        return ci_integrated and dashboard_created
    return False

def select_next_module():
    incomplete = [m for m in modules if m.score < 85]
    return max(incomplete, key=lambda m: m.priority_score)

def select_next_mutant(mutants):
    return max(mutants, key=calculate_priority_score)
```

---

## 📈 Expected Outcomes

### Phase 2 (Baseline)
- **Duração:** 2-3 horas (análise modular)
- **Entregável:** Baseline completo de todos os módulos
- **Mutation Score Esperado:** 70-75%

### Phase 3 (Processo)
- **Duração:** 4-6 horas (análise + correções)
- **Entregável:** 15-20 testes melhorados
- **Mutation Score:** 70% → 80%+

### Phase 4 (Secundários)
- **Duração:** 6-8 horas
- **Entregável:** 20-25 testes melhorados
- **Mutation Score:** 80% → 85%+

### Phase 5 (Refinamento)
- **Duração:** 2-3 horas
- **Entregável:** Performance otimizada, STRONGER mutators
- **Mutation Score:** Manter >85%

### Phase 6 (CI/CD)
- **Duração:** 2-3 horas
- **Entregável:** Pipeline configurado, dashboard criado
- **Mutation Score:** >85% mantido automaticamente

**Total Estimado:** 16-23 horas de trabalho de AI agent

---

## 🎯 Success Criteria

### Phase 2
- [x] Configuração PIT otimizada com timeouts
- [ ] Todos os módulos analisados
- [ ] Baseline documentado por módulo
- [ ] Categorização automática implementada
- [ ] Top 20 mutantes globais identificados

### Phase 3
- [ ] 15+ mutantes categoria A corrigidos em processo
- [ ] Mutation score processo >80%
- [ ] Todos os testes continuam passando
- [ ] Padrões documentados

### Phase 4
- [ ] Mutation score global >80%
- [ ] 3+ módulos com score >85%
- [ ] Padrões consolidados

### Phase 5
- [ ] STRONGER mutators habilitados
- [ ] Tempo de execução <20min
- [ ] Equivalentes documentados

### Phase 6
- [ ] PIT no CI/CD
- [ ] Thresholds configurados
- [ ] Dashboard funcional

---

## 📝 Next Steps (IMMEDIATE)

1. **Complete Phase 2 Baseline**
   - [x] Optimize PIT configuration (timeouts, memory, exclusions)
   - [ ] Run mutation testing on: processo, subprocesso, mapa, atividade, seguranca
   - [ ] Extract and aggregate metrics
   - [ ] Categorize all surviving mutants
   - [ ] Generate consolidated baseline report

2. **Begin Phase 3 (Processo Module)**
   - [ ] Select top 15-20 Category A mutants
   - [ ] Apply correction patterns
   - [ ] Validate improvements
   - [ ] Document patterns used

3. **Continuous Updates**
   - Use `report_progress` after each module analysis
   - Update MBT-progress.md with actual data
   - Track time spent per phase

---

**Status:** Phase 2 In Progress - Configuration Optimized  
**Next Action:** Run modular analysis starting with processo module  
**Estimated Completion:** Phase 2 by end of today, Phase 3-6 over next 2-3 days
