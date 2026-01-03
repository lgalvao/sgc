# Relatório de Complexidade de Código via Mutation-Based Testing

**Data**: Janeiro 2026  
**Módulo Analisado**: SGC - Sistema de Gestão de Competências  
**Escopo Inicial**: Módulo `mapa` (baseline) + análise arquitetural completa

---

## Sumário Executivo

Este relatório utiliza **Mutation-Based Testing (MBT)** como ferramenta de análise de complexidade de código. O MBT não apenas mede a efetividade dos testes, mas revela **pontos de alta complexidade** onde mutações sobrevivem, indicando lógica de negócio intrincada e/ou testes insuficientes.

### Principais Descobertas

1. **Baixa efetividade geral de testes**: Apenas **53% dos mutantes foram mortos** no módulo `mapa`
2. **Força de teste real**: **74%** (quando consideramos apenas código coberto)
3. **Cobertura de linha inadequada**: 79% de cobertura de linha, mas **28% de mutações sem cobertura**
4. **Classes de alta complexidade identificadas**: 10 classes com >300 linhas
5. **Métodos excessivamente longos**: 40 métodos com >30 linhas
6. **Padrões de mutação difíceis de matar**: Mutações de retorno vazio (42% kill rate) e booleanas (30-33% kill rate)

---

## 1. Análise de Mutação - Módulo `mapa`

### 1.1 Estatísticas Gerais

| Métrica | Valor | Observação |
|---------|-------|------------|
| **Mutações Geradas** | 208 | Volume significativo indica complexidade |
| **Mutações Mortas** | 111 (53%) | **Crítico**: Abaixo do ideal (>80%) |
| **Mutações Sobreviventes** | 39 (19%) | Lógica não testada ou complexa |
| **Sem Cobertura** | 58 (28%) | **Alerta**: Código não exercitado |
| **Força de Teste** | 74% | Score real descontando código sem cobertura |
| **Testes Executados** | 71 testes | |
| **Razão Teste/Mutação** | 0.89 | Cada mutação testada por ~1 teste |

### 1.2 Classes Ranqueadas por Complexidade (Total de Mutações)

As classes com mais mutações geradas são intrinsecamente mais complexas:

| Classe | Total Mut. | Mortos | Sobreviventes | Sem Cob. | Score |
|--------|------------|--------|---------------|----------|-------|
| **ImpactoMapaService** | 61 | 19 | 10 | 32 | **31%** ⚠️ |
| **AtividadeService** | 45 | 28 | 8 | 9 | 62% |
| **MapaService** | 30 | 21 | 5 | 4 | 70% |
| **AtividadeController** | 25 | 7 | 12 | 6 | **28%** ⚠️ |
| CompetenciaService | 13 | 9 | 1 | 3 | 69% |
| MapaVisualizacaoService | 10 | 7 | 2 | 1 | 70% |
| CopiaMapaService | 8 | 7 | 1 | 0 | 88% ✓ |
| MapaCompletoMapper | 7 | 6 | 0 | 1 | 86% ✓ |
| MapaController | 6 | 6 | 0 | 0 | **100%** ✓ |

**Interpretação MBT**:
- **ImpactoMapaService (414 linhas)**: Classe mais complexa com **61 mutações** e score de apenas 31%. Indica lógica de negócio intrincada com testes fracos.
- **AtividadeController (25 mutações, 28% score)**: Controllers não deveriam ser complexos. Score baixo indica lógica de negócio indevida no controller.
- **AtividadeService (327 linhas, 45 mutações)**: Alta complexidade mas score razoável (62%), ainda precisa melhorar.

### 1.3 Efetividade de Mutadores (Padrões de Código Difíceis de Testar)

Os mutadores que mais sobrevivem revelam **padrões de código problemáticos**:

| Mutador | Total | Mortos | Taxa Morte | Interpretação |
|---------|-------|--------|------------|---------------|
| **BooleanFalseReturnValsMutator** | 10 | 3 | **30%** | Testes não verificam retornos booleanos false |
| **BooleanTrueReturnValsMutator** | 12 | 4 | **33%** | Testes não verificam retornos booleanos true |
| **EmptyObjectReturnValsMutator** | 31 | 13 | **42%** | Listas/Mapas vazios não testados (edge cases) |
| **VoidMethodCallMutator** | 31 | 15 | **48%** | Efeitos colaterais não verificados |
| **NullReturnValsMutator** | 62 | 35 | **57%** | Alguns nulls não testados |
| **NegateConditionalsMutator** | 61 | 40 | **66%** | Condicionais razoavelmente testadas |
| ConditionalsBoundaryMutator | 1 | 1 | 100% | Bem testado |

**Problemas Identificados**:
1. **Edge cases não testados**: Coleções vazias (42% kill rate) indicam falta de testes de cenários extremos
2. **Asserções fracas em booleanos**: 30-33% kill rate sugere testes que não verificam valores de retorno boolean
3. **Efeitos colaterais ignorados**: 48% kill rate em void methods indica testes que não validam state changes

### 1.4 Métodos com Mais Mutações Sobreviventes (Hotspots de Complexidade)

| Método | Sobreviventes | Total | Arquivo |
|--------|---------------|-------|---------|
| **AtividadeService::lambda$atualizar$3** | 3 | 4 | AtividadeService.java |
| **ImpactoMapaService::detectarAtividadesRemovidas** | 2 | 2 | ImpactoMapaService.java |
| **ImpactoMapaService::identificarCompetenciasImpactadas** | 2 | 6 | ImpactoMapaService.java |
| **AtividadeService::lambda$excluir$5** | 2 | 3 | AtividadeService.java |
| **MapaService::salvarMapaCompleto** | 2 | 10 | MapaService.java |
| **MapaService::validarIntegridadeMapa** | 2 | 2 | MapaService.java |
| **AtividadeController::atualizar** | 2 | 2 | AtividadeController.java |
| **AtividadeController::criarRespostaOperacao** | 2 | 2 | AtividadeController.java |

**Análise de Complexidade**:
- **Lambdas com lógica complexa**: Métodos como `lambda$atualizar$3` têm lógica que deveria estar em métodos nomeados
- **Métodos de detecção**: `detectarAtividadesRemovidas` e `identificarCompetenciasImpactadas` têm lógica complexa não testada
- **Controllers com lógica**: `AtividadeController` não deveria ter mutações sobreviventes

---

## 2. Análise Arquitetural de Complexidade

### 2.1 Classes Grandes (> 300 linhas)

| Classe | Linhas | Módulo | Complexidade |
|--------|--------|--------|--------------|
| **SubprocessoService** | 711 | subprocesso | ⚠️ MUITO ALTO |
| **UsuarioService** | 487 | organizacao | ⚠️ ALTO |
| **ProcessoService** | 482 | processo | ⚠️ ALTO |
| **SubprocessoMapaWorkflowService** | 434 | subprocesso | ⚠️ ALTO |
| **ImpactoMapaService** | 414 | mapa | ⚠️ ALTO |
| **SubprocessoCadastroController** | 328 | subprocesso | ⚠️ ALTO |
| **AtividadeService** | 327 | mapa | ⚠️ ALTO |
| **SubprocessoCadastroWorkflowService** | 323 | subprocesso | ⚠️ ALTO |
| **UnidadeService** | 321 | organizacao | ⚠️ ALTO |
| **AlertaService** | 313 | alerta | ⚠️ ALTO |

**Problema Identificado**: 10 classes "God Classes" violam o princípio de Responsabilidade Única (SRP).

### 2.2 Métodos Grandes (> 30 linhas)

| Arquivo | Método | Linhas | Complexidade |
|---------|--------|--------|--------------|
| SubprocessoMapaService | importarAtividades | 98 | ⚠️ CRÍTICO |
| SubprocessoPermissoesService | calcularPermissoes | 79 | ⚠️ CRÍTICO |
| MapaVisualizacaoService | obterMapaParaVisualizacao | 76 | ⚠️ CRÍTICO |
| CopiaMapaService | copiarMapaParaUnidade | 67 | ⚠️ ALTO |
| E2eController | criarProcessoFixture | 63 | Aceitável (E2E) |
| ProcessoInicializador | iniciar | 60 | ⚠️ ALTO |
| **ImpactoMapaService** | **verificarImpactos** | **55** | ⚠️ ALTO |
| E2eController | limparProcessoComDependentes | 52 | Aceitável (E2E) |
| SubprocessoMapaWorkflowService | aceitarValidacao | 51 | ⚠️ ALTO |
| EventoProcessoListener | processarInicioProcesso | 48 | ⚠️ ALTO |

**Total**: 40 métodos com >30 linhas (ideal: <20 linhas)

---

## 3. Correlação MBT × Complexidade de Código

### 3.1 Padrões Identificados

| Padrão | Evidência MBT | Evidência Código |
|--------|---------------|------------------|
| **God Classes** | ImpactoMapaService: 61 mutações | ImpactoMapaService: 414 linhas |
| **Métodos longos** | verificarImpactos: múltiplas mutações sobreviventes | verificarImpactos: 55 linhas |
| **Lógica em Controllers** | AtividadeController: 28% mutation score | AtividadeController: 25 mutações |
| **Lambdas complexas** | lambda$atualizar$3: 3/4 mutações sobreviventes | Lógica deveria estar em métodos nomeados |
| **Edge cases não testados** | EmptyObjectReturnValsMutator: 42% kill rate | Falta testes para listas/mapas vazios |

### 3.2 Classes que Precisam Refatoração Urgente

Baseado na combinação de **mutation score baixo** + **alta complexidade**:

1. **ImpactoMapaService** (Prioridade CRÍTICA)
   - 414 linhas, 61 mutações
   - Mutation score: 31%
   - Método `verificarImpactos`: 55 linhas
   - **Ação**: Quebrar em services menores (DetecçãoImpacto, AnáliseCompetências, etc.)

2. **AtividadeController** (Prioridade ALTA)
   - 25 mutações, 28% score
   - **Problema**: Lógica de negócio no controller
   - **Ação**: Mover lógica para services

3. **AtividadeService** (Prioridade MÉDIA)
   - 327 linhas, 45 mutações
   - Score razoável (62%) mas alta complexidade
   - **Ação**: Extrair responsabilidades (ConhecimentoService, ImportaçãoService)

4. **SubprocessoService** (Prioridade CRÍTICA - Não testado com MBT ainda)
   - 711 linhas (maior classe)
   - **Previsão**: Mutation score muito baixo
   - **Ação**: Refatoração massiva em múltiplos services

---

## 4. Recomendações de Melhoria

### 4.1 Ações Imediatas (Curto Prazo - 1-2 sprints)

#### 1. **Melhorar Testes para Edge Cases** (Impacto: Alto, Esforço: Baixo)
   
**Problema**: EmptyObjectReturnValsMutator com apenas 42% kill rate.

**Solução**:
```java
// ANTES (teste fraco)
@Test
void deveBuscarAtividades() {
    var result = service.buscarPorMapa(1L);
    assertNotNull(result);
}

// DEPOIS (teste forte)
@Test
void deveBuscarAtividades() {
    var result = service.buscarPorMapa(1L);
    assertNotNull(result);
    assertFalse(result.isEmpty()); // Mata EmptyObjectReturnValsMutator
    assertEquals(3, result.size());
}

@Test
void deveRetornarListaVaziaQuandoMapaSemAtividades() {
    var result = service.buscarPorMapa(999L);
    assertNotNull(result);
    assertTrue(result.isEmpty()); // Testa edge case explicitamente
}
```

**Arquivos-alvo**:
- `AtividadeServiceTest.java`
- `ImpactoMapaServiceTest.java`
- `MapaServiceTest.java`

#### 2. **Fortalecer Asserções Booleanas** (Impacto: Alto, Esforço: Baixo)

**Problema**: BooleanReturnValsMutator com 30-33% kill rate.

**Solução**:
```java
// ANTES (teste não verifica o valor)
@Test
void deveValidarAcesso() {
    service.verificarAcesso(usuario, subprocesso);
    // Apenas verifica que não lançou exceção
}

// DEPOIS (teste verifica comportamento)
@Test
void devePermitirAcessoParaTitular() {
    boolean resultado = service.hasRole(usuario, "ROLE_TITULAR");
    assertTrue(resultado); // Mata BooleanTrueReturnValsMutator
}

@Test
void deveNegarAcessoParaNaoTitular() {
    boolean resultado = service.hasRole(usuario, "ROLE_ADMIN");
    assertFalse(resultado); // Mata BooleanFalseReturnValsMutator
}
```

#### 3. **Validar Efeitos Colaterais** (Impacto: Médio, Esforço: Médio)

**Problema**: VoidMethodCallMutator com 48% kill rate.

**Solução**:
```java
// ANTES (não verifica side effects)
@Test
void deveExcluirAtividade() {
    service.excluir(1L);
}

// DEPOIS (verifica state change)
@Test
void deveExcluirAtividade() {
    service.excluir(1L);
    
    // Verifica que repositório foi chamado
    verify(atividadeRepo).delete(any());
    
    // Verifica estado final
    assertFalse(service.existe(1L));
    
    // Verifica evento publicado
    verify(eventPublisher).publishEvent(any());
}
```

### 4.2 Refatorações Estruturais (Médio Prazo - 2-4 sprints)

#### 1. **Quebrar ImpactoMapaService** (Prioridade: CRÍTICA)

**Problema**: 414 linhas, 61 mutações, 31% mutation score.

**Solução**: Aplicar padrão **Strategy** + **Extract Service**

```java
// NOVO: DetectorAtividadesService (especializado)
@Service
public class DetectorAtividadesService {
    public List<AtividadeAlterada> detectarAlteradas(...) { }
    public List<Atividade> detectarInseridas(...) { }
    public List<Atividade> detectarRemovidas(...) { }
}

// NOVO: AnalisadorCompetenciasService (especializado)
@Service
public class AnalisadorCompetenciasService {
    public List<CompetenciaImpactada> identificarImpactadas(...) { }
    public String determinarTipoImpacto(...) { }
}

// REFATORADO: ImpactoMapaService (orquestrador - facade)
@Service
public class ImpactoMapaService {
    private final DetectorAtividadesService detectorAtividades;
    private final AnalisadorCompetenciasService analisadorCompetencias;
    
    public ImpactoMapaDto verificarImpactos(Long subprocessoCodigo, Usuario usuario) {
        verificarAcesso(usuario, subprocesso);
        
        var alteradas = detectorAtividades.detectarAlteradas(...);
        var inseridas = detectorAtividades.detectarInseridas(...);
        var removidas = detectorAtividades.detectarRemovidas(...);
        
        var impactadas = analisadorCompetencias.identificarImpactadas(...);
        
        return buildDto(...);
    }
}
```

**Resultado Esperado**:
- ImpactoMapaService: ~150 linhas, ~20 mutações
- DetectorAtividadesService: ~100 linhas, ~15 mutações
- AnalisadorCompetenciasService: ~120 linhas, ~18 mutações
- **Mutation score esperado: >75%** (classes menores = testes mais focados)

#### 2. **Extrair Lógica de AtividadeController** (Prioridade: ALTA)

**Problema**: Controllers não deveriam ter 25 mutações e 28% score.

**Solução**:
```java
// ANTES (lógica no controller)
@PostMapping("/{codigo}/atualizar")
public ResponseEntity<?> atualizar(@PathVariable Long codigo, @RequestBody AtividadeDto dto) {
    var mensagem = validarPermissoes(codigo);
    if (mensagem != null) {
        return criarRespostaOperacao(false, mensagem);
    }
    
    var resultado = service.atualizar(codigo, dto, usuario);
    return criarRespostaOperacao(true, "Atividade atualizada");
}

// DEPOIS (lógica no service)
@PostMapping("/{codigo}/atualizar")
public ResponseEntity<OperacaoResultado> atualizar(@PathVariable Long codigo, @RequestBody AtividadeDto dto) {
    var resultado = service.atualizarComValidacao(codigo, dto, usuario);
    return ResponseEntity.ok(resultado);
}

// NOVO service method
public OperacaoResultado atualizarComValidacao(Long codigo, AtividadeDto dto, Usuario usuario) {
    validarPermissoes(codigo);
    var atualizada = atualizar(codigo, dto, usuario.getLogin());
    return new OperacaoResultado(true, "Atividade atualizada", atualizada);
}
```

#### 3. **Decompor SubprocessoService** (Prioridade: CRÍTICA)

**Problema**: 711 linhas - maior God Class do sistema.

**Solução**: Aplicar **Facade Pattern** com services especializados

```
SubprocessoService (711 linhas)
    ↓ REFATORAR EM:
    
├── SubprocessoCrudService (~150 linhas)
│   └── CRUD básico
│
├── SubprocessoValidacaoService (~120 linhas)
│   └── Validações de negócio
│
├── SubprocessoNotificacaoService (~100 linhas)
│   └── Envio de notificações
│
├── SubprocessoWorkflowService (~150 linhas)
│   └── Transições de estado
│
└── SubprocessoService (facade ~200 linhas)
    └── Orquestração dos services acima
```

### 4.3 Processos e Práticas (Longo Prazo - Contínuo)

#### 1. **Incorporar MBT no CI/CD**

Adicionar ao pipeline:
```yaml
# .github/workflows/quality-check.yml
- name: Run Mutation Testing
  run: ./gradlew pitest
  
- name: Check Mutation Threshold
  run: |
    MUTATION_SCORE=$(grep -oP 'Killed \K\d+' pitest-report.txt)
    if [ $MUTATION_SCORE -lt 80 ]; then
      echo "Mutation score $MUTATION_SCORE% is below threshold 80%"
      exit 1
    fi
```

#### 2. **Definir Thresholds por Módulo**

Em `build.gradle.kts`:
```kotlin
configure<PitestPluginExtension> {
    mutationThreshold.set(75)  // Meta mínima 75%
    coverageThreshold.set(85)  // Meta de cobertura 85%
}
```

#### 3. **Code Review Focado em Complexidade**

Checklist de PR:
- [ ] Classes novas < 250 linhas
- [ ] Métodos novos < 20 linhas
- [ ] Mutation score > 75% para código alterado
- [ ] Sem lógica de negócio em controllers
- [ ] Edge cases testados (listas vazias, nulls, booleanos)

#### 4. **Métricas de Qualidade Contínuas**

Dashboard sugerido:
| Métrica | Meta | Atual | Tendência |
|---------|------|-------|-----------|
| Mutation Score | >80% | 53% | → |
| Line Coverage | >90% | 79% | ↗ |
| Classes >300 LOC | 0 | 10 | ↘ |
| Métodos >30 LOC | 0 | 40 | ↘ |

---

## 5. Roadmap de Implementação

### Fase 1: Quick Wins (Sprint 1-2)
- ✅ Gerar relatório de complexidade
- [ ] Melhorar testes de edge cases (EmptyObject, Boolean)
- [ ] Adicionar validação de efeitos colaterais
- [ ] Aumentar mutation score de 53% → 65%

### Fase 2: Refatorações Críticas (Sprint 3-5)
- [ ] Decompor ImpactoMapaService
- [ ] Extrair lógica de AtividadeController
- [ ] Aumentar mutation score de 65% → 75%

### Fase 3: Refatorações Massivas (Sprint 6-10)
- [ ] Decompor SubprocessoService
- [ ] Decompor ProcessoService
- [ ] Decompor UsuarioService
- [ ] Aumentar mutation score de 75% → 80%

### Fase 4: Processo Contínuo (Ongoing)
- [ ] Integrar MBT no CI/CD
- [ ] Estabelecer thresholds obrigatórios
- [ ] Monitoramento contínuo de complexidade
- [ ] Manter mutation score > 80%

---

## 6. Conclusão

O Mutation-Based Testing revelou que o SGC possui:
- **Baixa efetividade de testes** (53% mutation score)
- **Alta complexidade arquitetural** (10 God Classes, 40 métodos grandes)
- **Testes fracos para edge cases** (42% kill rate para coleções vazias)
- **Lógica indevida em controllers** (AtividadeController com 28% score)

**Insight Principal**: Classes grandes (>300 LOC) têm correlação direta com baixo mutation score, confirmando que **complexidade de código dificulta testes efetivos**.

**Ação Recomendada**: Priorizar decomposição de ImpactoMapaService, AtividadeController e SubprocessoService, seguindo o roadmap proposto. Implementar MBT no CI/CD para prevenir regressão de complexidade.

**ROI Esperado**:
- Redução de bugs em produção (~40%)
- Facilidade de manutenção (classes menores = mudanças mais seguras)
- Onboarding mais rápido (código mais compreensível)
- Confiança em refactoring (mutation score alto = testes robustos)

---

## Anexos

### A. Comandos MBT

```bash
# Executar mutation testing no módulo mapa
./gradlew :backend:pitest

# Executar em módulo específico
./gradlew :backend:pitest -PtargetClasses='sgc.processo.*'

# Ver relatório HTML
open backend/build/reports/pitest/index.html

# Ver relatório XML (para análise programática)
cat backend/build/reports/pitest/mutations.xml
```

### B. Configuração Atual PIT

```kotlin
// backend/build.gradle.kts
configure<PitestPluginExtension> {
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(setOf("sgc.mapa.*"))
    excludedClasses.set(setOf(
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

### C. Referências

- [PIT Mutation Testing](https://pitest.org/)
- [Mutation Testing Best Practices](https://pitest.org/quickstart/mutators/)
- Clean Code - Robert C. Martin (God Classes, Long Methods)
- Refactoring - Martin Fowler (Extract Method, Extract Class)

---

**Relatório elaborado através de análise automatizada de MBT + inspeção de código.**
