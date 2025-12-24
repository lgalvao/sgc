# Plano de Mutation-Based Testing (MBT) - SGC

## ✅ Status Atual da Implementação

**Data**: 2025-12-24  
**Status**: Pronto para uso

### Compatibilidade com Gradle 9.x Confirmada

O plugin Gradle do PITest (`info.solidsoft.pitest`) versão **1.19.0-rc.2** agora suporta Gradle 9.x com compatibilidade de configuration cache.

- **Versão do Plugin**: 1.19.0-rc.2 (lançada em 01 de outubro de 2025)
- **Fonte**: <https://plugins.gradle.org/plugin/info.solidsoft.pitest>
- **Release Notes**: <https://github.com/szpak/gradle-pitest-plugin/releases>

Para usar, adicione ao `backend/build.gradle.kts`:

```kotlin
plugins {
    id("info.solidsoft.pitest") version "1.19.0-rc.2"
}
```

E execute:

```bash
./gradlew :backend:pitest
```

---

## 📋 Visão Geral

Este documento detalha a estratégia de **Mutation-Based Testing (MBT)** para o projeto SGC (Sistema de Gestão de Competências). O MBT é uma técnica avançada de qualidade de testes que avalia a eficácia da suíte de testes ao introduzir pequenas modificações (mutações) no código-fonte e verificar se os testes conseguem detectar essas mudanças.

### O que é Mutation Testing?

Mutation Testing (Teste de Mutação) é uma técnica que:

1. **Cria "mutantes"** do código original aplicando pequenas alterações sintáticas
2. **Executa a suíte de testes** contra cada mutante
3. **Verifica se os testes falham** (mutante "morto") ou passam (mutante "sobrevivente")
4. **Calcula métricas** de qualidade baseadas na taxa de mutantes mortos

**Exemplo:**

```java
// Código Original
if (saldo > 100) {
    return true;
}

// Mutante 1: Operador de comparação (> para >=)
if (saldo >= 100) {  // Se testes não pegam isso, mutante sobrevive
    return true;
}

// Mutante 2: Negação de condição
if (!(saldo > 100)) {  // Se testes não pegam isso, mutante sobrevive
    return true;
}
```

### Por que Mutation Testing é importante?

- **Cobertura de código não garante qualidade**: 100% de cobertura pode ter testes fracos
- **Valida a qualidade dos testes**: Testes que matam mutantes são eficazes
- **Encontra casos extremos não testados**: Revela boundary conditions e edge cases
- **Força testes mais rigorosos**: Incentiva assertions mais específicas
- **Identifica código morto**: Mutantes equivalentes revelam código sem efeito

---

## 🎯 Objetivos do MBT no SGC

### Objetivos Primários

1. **Avaliar qualidade dos testes unitários** nos módulos core (processo, subprocesso, mapa)
2. **Identificar gaps de teste** em lógica de negócio crítica
3. **Melhorar cobertura de branches** (atualmente 62.1%) através de testes mais rigorosos
4. **Estabelecer baseline de mutation score** para acompanhamento contínuo
5. **Criar cultura de testes de alta qualidade** entre desenvolvedores e agentes de IA

### Métricas de Sucesso

| Métrica | Baseline Atual | Meta Inicial (3 meses) | Meta Final (6 meses) |
|---------|----------------|------------------------|----------------------|
| **Mutation Score (Geral)** | - | 70% | 80% |
| **Mutation Score (Core Modules)** | - | 75% | 85% |
| **Test Strength** | - | 0.70 | 0.80 |
| **Branch Coverage** | 62.1% | 70% | 75% |

---

## 🔍 Ferramenta: PITest (PIT Mutation Testing)

### Por que PITest?

- **Padrão de mercado** para Java (mais usado e maduro)
- **Integração nativa** com Gradle e JUnit 5
- **Suporte a Java 21** e features modernas
- **Execução paralela** (performance)
- **Análise incremental** (roda apenas código modificado)
- **Relatórios detalhados** HTML com drill-down por classe/método

### Mutadores Configurados

O projeto utiliza os seguintes grupos de mutadores:

#### 1. DEFAULTS (Mutadores Padrão)

- **Conditionals Boundary**: `<` → `<=`, `>` → `>=`
- **Increments**: `++` → `--`, `x++` → `x--`
- **Invert Negatives**: `-x` → `x`
- **Math**: `+` → `-`, `*` → `/`, `%` → `*`
- **Negate Conditionals**: `==` → `!=`, `>` → `<=`
- **Return Values**: `return true` → `return false`, `return x` → `return null`
- **Void Method Calls**: Remove chamadas a métodos void

#### 2. STRONGER (Mutadores Mais Fortes)

- **Remove Conditionals**: Remove completamente `if`, `while`, `for` conditions
- **Experimental Switch**: Mutações em `switch` statements

#### 3. REMOVE_CONDITIONALS (Foco Específico)

- Remove condicionais para verificar se branches são realmente testados

### Mutantes Excluídos

Para evitar falsos positivos e focar em lógica de negócio:

- **Entidades JPA**: Apenas getters/setters (sem lógica)
- **DTOs**: Objetos de transferência de dados
- **Mappers MapStruct**: Código gerado automaticamente
- **Configurações Spring**: Beans e configurações
- **Exceções customizadas**: Apenas estrutura, sem lógica
- **Classe Main**: Ponto de entrada da aplicação

---

## 🎯 Módulos Priorizados para MBT

### Classificação de Prioridade

Os módulos foram classificados em **3 níveis de prioridade** baseados em:

1. **Complexidade ciclomática** (número de branches e decisões)
2. **Criticidade de negócio** (impacto de bugs)
3. **Cobertura de testes atual**
4. **Tamanho do código** (LOC - Lines of Code)

---

### 🔴 **Prioridade ALTA** (Críticos - Executar Primeiro)

#### 1. `processo.internal.service.ProcessoService`

- **LOC**: 443
- **Complexidade**: MUITO ALTA
- **Criticidade**: CRÍTICA (orquestrador central)
- **Cobertura atual**: ~85%
- **Razão**: Gerencia ciclo de vida de processos, publica eventos, controla permissões
- **Mutation Score esperado inicial**: 65-75%
- **Foco de melhoria**:
  - Validações de estado (situações válidas/inválidas)
  - Lógica de permissões (checarAcesso)
  - Publicação de eventos (verificar side effects)

#### 2. `subprocesso.internal.service.SubprocessoMapaWorkflowService`

- **LOC**: 414
- **Complexidade**: MUITO ALTA
- **Criticidade**: CRÍTICA (máquina de estados)
- **Cobertura atual**: ~80%
- **Razão**: Transições de estado complexas, workflow de aprovação
- **Mutation Score esperado inicial**: 60-70%
- **Foco de melhoria**:
  - Transições de situação (validar todas as combinações)
  - Condições de salvamento (mapa vazio → mapa com dados)
  - Validações de workflow (rejeitar, aprovar, submeter)

#### 3. `mapa.MapaService`

- **LOC**: 228
- **Complexidade**: ALTA
- **Criticidade**: CRÍTICA (domínio principal)
- **Cobertura atual**: ~90%
- **Razão**: Salva competências, validações complexas, sanitização HTML
- **Mutation Score esperado inicial**: 70-80%
- **Foco de melhoria**:
  - Validações de competências (atividades duplicadas, IDs inválidos)
  - Lógica de merge (adicionar, atualizar, remover competências)
  - Sanitização de inputs (segurança)

#### 4. `mapa.internal.service.ImpactoMapaService`

- **LOC**: 417
- **Complexidade**: ALTA
- **Criticidade**: ALTA (análise de impacto)
- **Cobertura atual**: ~75%
- **Razão**: Cálculos complexos de impacto, comparações entre mapas
- **Mutation Score esperado inicial**: 65-75%
- **Foco de melhoria**:
  - Lógica de diff (competências adicionadas, removidas, alteradas)
  - Cálculos de impacto (contadores, percentuais)
  - Casos extremos (mapas vazios, mapas idênticos)

---

### 🟡 **Prioridade MÉDIA** (Importantes - Executar em Segunda Fase)

#### 5. `subprocesso.internal.service.SubprocessoCadastroWorkflowService`

- **LOC**: 347
- **Complexidade**: MÉDIA-ALTA
- **Razão**: Workflow de cadastro, validações de etapas

#### 6. `atividade.AtividadeService`

- **LOC**: 281
- **Complexidade**: MÉDIA
- **Razão**: CRUD com validações, gestão de conhecimentos

#### 7. `unidade.service.UnidadeService`

- **LOC**: 293
- **Complexidade**: MÉDIA
- **Razão**: Hierarquia organizacional, consultas recursivas

#### 8. `sgrh.SgrhService`

- **LOC**: 431
- **Complexidade**: MÉDIA
- **Razão**: Integração externa, cache, autenticação

---

### 🟢 **Prioridade BAIXA** (Executar em Terceira Fase)

#### 9. `comum.erros.*` (Baseline Test)

- **LOC**: ~50
- **Complexidade**: BAIXA
- **Razão**: **Módulo de teste inicial** para validar configuração PIT
- **Mutation Score esperado**: 90%+ (código simples)

#### 10. `painel.PainelService`

- **LOC**: 255
- **Complexidade**: BAIXA-MÉDIA
- **Razão**: Agregações e estatísticas (menos crítico)

#### 11. `alerta.AlertaService`, `analise.AnaliseService`, `notificacao.*`

- **LOC**: 100-200 cada
- **Complexidade**: BAIXA
- **Razão**: Serviços de suporte, menos lógica complexa

---

## 🚀 Guia de Execução para Agentes de IA

### Pré-requisitos

Antes de executar mutation tests:

```bash
# 1. Garantir que testes unitários passam
./gradlew :backend:test

# 2. Verificar cobertura atual
./gradlew :backend:jacocoTestReport
# Ver relatório em: backend/build/reports/jacoco/test/html/index.html
```

### Comandos de Execução

#### 1. Executar MBT em Módulo Específico (Recomendado)

```bash
# Executar MBT no módulo 'comum.erros' (baseline test)
./gradlew :backend:mutationTestModule -Pmodule=comum.erros

# Executar MBT no módulo 'processo'
./gradlew :backend:mutationTestModule -Pmodule=processo

# Executar MBT no módulo 'mapa'
./gradlew :backend:mutationTestModule -Pmodule=mapa
```

#### 2. Executar MBT Completo (Todos os Módulos Configurados)

```bash
# Execução completa (pode demorar 10-30 minutos)
./gradlew :backend:pitest

# Ver relatório em: backend/build/reports/pitest/index.html
```

#### 3. Execução Incremental (Apenas Código Modificado)

```bash
# PITest automaticamente detecta mudanças e executa apenas no código alterado
# se historyInputLocation estiver configurado
./gradlew :backend:pitest
```

### Interpretando Relatórios

O relatório HTML do PIT é gerado em: `backend/build/reports/pitest/index.html`

#### Métricas Principais

1. **Mutation Coverage** (Cobertura de Mutação)
   - **Fórmula**: `Mutantes Mortos / Total de Mutantes`
   - **Meta**: ≥ 70% (inicial), ≥ 80% (final)
   - **Interpretação**: Percentual de mutantes que os testes conseguiram detectar

2. **Test Strength** (Força dos Testes)
   - **Fórmula**: `Mutantes Mortos / Mutantes Cobertos`
   - **Meta**: ≥ 0.70 (inicial), ≥ 0.80 (final)
   - **Interpretação**: Eficácia dos testes existentes (exclui código não coberto)

3. **Line Coverage** (Cobertura de Linhas)
   - **Fórmula**: `Linhas Cobertas / Total de Linhas`
   - **Meta**: ≥ 80% (já alcançado: 85.9%)
   - **Interpretação**: Percentual de linhas executadas pelos testes

#### Estados de Mutantes

- ✅ **KILLED** (Morto): Teste detectou a mutação → **BOM**
- ❌ **SURVIVED** (Sobrevivente): Teste NÃO detectou a mutação → **RUIM** (adicionar/melhorar teste)
- ⚠️ **NO_COVERAGE** (Sem Cobertura): Linha não executada por nenhum teste → **MUITO RUIM** (adicionar teste)
- ⚪ **TIMED_OUT**: Teste entrou em loop infinito → Revisar teste ou código
- 🟰 **MEMORY_ERROR**: Teste consumiu muita memória → Revisar teste
- 🔄 **EQUIVALENT**: Mutante equivalente ao original → **OK** (ignorar)

#### Exemplo de Relatório

```
Package: sgc.processo.internal.service
Class: ProcessoService

Mutation Coverage: 75% (45/60)
Test Strength: 0.83 (45/54)
Line Coverage: 92% (120/130)

Sobreviventes (15):
1. Line 85: Negated conditional (if (x > 0) → if (x <= 0))
2. Line 120: Changed return value (return true → return false)
...
```

---

## 📊 Estratégia de Melhoria Iterativa

### Fase 1: Baseline (Semana 1)

**Objetivo**: Estabelecer baseline e validar configuração

1. Executar MBT no módulo `comum.erros` (simples)
2. Analisar relatório e familiarizar com output
3. Executar MBT no módulo `processo` (complexo)
4. Documentar mutation score inicial por módulo
5. Identificar top 10 mutantes sobreviventes críticos

**Entregável**: Relatório de baseline com scores por módulo

---

### Fase 2: Matar Mutantes de Alta Prioridade (Semanas 2-4)

**Objetivo**: Focar em mutantes críticos de módulos core

Para cada mutante sobrevivente:

#### Passo 1: Analisar o Mutante

```java
// Exemplo de mutante sobrevivente
// Original (linha 85 de ProcessoService):
if (processo.getSituacao() == SituacaoProcesso.CRIADO) {
    processo.iniciar();
}

// Mutante: Negated conditional
if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
    processo.iniciar();
}
```

#### Passo 2: Identificar Gap de Teste

```java
// Teste atual (insuficiente):
@Test
void deveIniciarProcesso() {
    Processo p = ProcessoFixture.processoPadrao();
    processoService.iniciar(p.getCodigo());
    // ❌ Não valida que iniciar() foi chamado!
}
```

#### Passo 3: Criar Teste que Mata o Mutante

```java
// Teste melhorado (mata o mutante):
@Test
@DisplayName("Deve iniciar processo quando situação for CRIADO")
void deveIniciarProcessoQuandoSituacaoCriado() {
    // Arrange
    Processo p = ProcessoFixture.comSituacao(SituacaoProcesso.CRIADO);
    when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
    
    // Act
    processoService.iniciar(1L);
    
    // Assert
    verify(processoRepo).save(argThat(proc -> 
        proc.getSituacao() == SituacaoProcesso.EM_ANDAMENTO
    ));
}

@Test
@DisplayName("Deve lançar exceção quando situação não for CRIADO")
void deveLancarExcecaoQuandoSituacaoInvalida() {
    // Arrange
    Processo p = ProcessoFixture.comSituacao(SituacaoProcesso.FINALIZADO);
    when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
    
    // Act & Assert
    assertThatThrownBy(() -> processoService.iniciar(1L))
        .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
    
    // Garantir que iniciar() NÃO foi chamado
    verify(processoRepo, never()).save(any());
}
```

#### Passo 4: Re-executar MBT

```bash
./gradlew :backend:mutationTestModule -Pmodule=processo
# Verificar se mutation score aumentou
```

**Repetir** para os mutantes prioritários.

---

### Fase 3: Automação e CI/CD (Semanas 5-6)

**Objetivo**: Integrar MBT no pipeline de qualidade

1. Criar script `scripts/run-mutation-tests.sh`
2. Adicionar task Gradle para quality gate com MBT
3. Configurar threshold de mutation score mínimo (70%)
4. Documentar processo no README.md

**Entregável**: MBT integrado ao quality gate

---

## 🧪 Tipos de Mutantes Comuns e Como Matá-los

### 1. Conditionals Boundary (Fronteiras de Condicionais)

**Mutação**: `>` → `>=`, `<` → `<=`

```java
// Original
if (idade > 18) {
    permitirAcesso();
}

// Mutante
if (idade >= 18) {  // Mutante sobrevive se não temos teste com idade = 18
    permitirAcesso();
}
```

**Como matar**:

```java
@Test
void devePermitirAcessoQuandoIdadeMaiorQue18() {
    assertTrue(verificarAcesso(19));  // Mata ambos
}

@Test
void deveNegarAcessoQuandoIdadeIgualA18() {
    assertFalse(verificarAcesso(18));  // Mata o mutante >=
}

@Test
void deveNegarAcessoQuandoIdadeMenorQue18() {
    assertFalse(verificarAcesso(17));
}
```

---

### 2. Negate Conditionals (Negação de Condicionais)

**Mutação**: `==` → `!=`, `>` → `<=`, `&&` → `||`

```java
// Original
if (usuario.isAtivo() && usuario.temPermissao()) {
    return true;
}

// Mutantes
if (usuario.isAtivo() || usuario.temPermissao()) { }  // Mutante 1
if (!usuario.isAtivo() && usuario.temPermissao()) { }  // Mutante 2
```

**Como matar**:

```java
@Test
void deveRetornarTrueQuandoAtivoEComPermissao() {
    Usuario u = new Usuario(true, true);
    assertTrue(verificar(u));  // Mata lógica OR
}

@Test
void deveRetornarFalseQuandoAtivoMasSemPermissao() {
    Usuario u = new Usuario(true, false);
    assertFalse(verificar(u));  // Mata mutante 1 (OR)
}

@Test
void deveRetornarFalseQuandoInativoComPermissao() {
    Usuario u = new Usuario(false, true);
    assertFalse(verificar(u));  // Mata mutante 2 (negação)
}
```

---

### 3. Return Values (Valores de Retorno)

**Mutação**: `return true` → `return false`, `return x` → `return null`, `return 0` → `return 1`

```java
// Original
public boolean isValid() {
    return this.status == Status.ACTIVE;
}

// Mutante
public boolean isValid() {
    return false;  // Sempre retorna false
}
```

**Como matar**:

```java
@Test
void deveRetornarTrueQuandoStatusAtivo() {
    Entidade e = new Entidade(Status.ACTIVE);
    assertTrue(e.isValid());  // Mata o mutante
}

@Test
void deveRetornarFalseQuandoStatusInativo() {
    Entidade e = new Entidade(Status.INACTIVE);
    assertFalse(e.isValid());
}
```

---

### 4. Math Operators (Operadores Matemáticos)

**Mutação**: `+` → `-`, `*` → `/`, `%` → `*`

```java
// Original
public int calcularTotal(int quantidade, int preco) {
    return quantidade * preco;
}

// Mutantes
return quantidade + preco;  // Mutante 1
return quantidade / preco;  // Mutante 2
return quantidade - preco;  // Mutante 3
```

**Como matar**:

```java
@Test
void deveCalcularTotalCorretamente() {
    assertEquals(200, calcularTotal(10, 20));  // 10 * 20 = 200
    // Mata todos: 10+20=30, 10/20=0, 10-20=-10
}

@Test
void deveCalcularTotalComQuantidadeUm() {
    assertEquals(50, calcularTotal(1, 50));  // 1 * 50 = 50
    // Mata: 1+50=51, 1/50=0, 1-50=-49
}
```

---

### 5. Void Method Calls (Remoção de Chamadas Void)

**Mutação**: Remove chamadas a métodos void

```java
// Original
public void processar(Pedido pedido) {
    validar(pedido);
    salvar(pedido);
    notificar(pedido);
}

// Mutantes
public void processar(Pedido pedido) {
    // validar(pedido);  // Removido
    salvar(pedido);
    notificar(pedido);
}
```

**Como matar**:

```java
@Test
void deveValidarPedidoAntesDeProcessar() {
    Pedido p = PedidoFixture.invalido();
    
    assertThatThrownBy(() -> processar(p))
        .isInstanceOf(ErroValidacao.class);
    
    // Garante que salvar() não foi chamado
    verify(repo, never()).save(any());
}

@Test
void deveNotificarAposSalvar() {
    Pedido p = PedidoFixture.valido();
    processar(p);
    
    // Verifica que notificar foi chamado
    verify(notificador).enviar(eq(p.getId()));
}
```

---

### 6. Remove Conditionals (Remoção Completa de Condicionais)

**Mutação**: Remove completamente `if`, `while`, `for`

```java
// Original
public void aplicarDesconto(Pedido pedido) {
    if (pedido.getTotal() > 1000) {
        pedido.setDesconto(10);
    }
}

// Mutante
public void aplicarDesconto(Pedido pedido) {
    // if removido - sempre executa
    pedido.setDesconto(10);
}
```

**Como matar**:

```java
@Test
void deveAplicarDescontoQuandoTotalMaiorQue1000() {
    Pedido p = new Pedido(1500);
    aplicarDesconto(p);
    assertEquals(10, p.getDesconto());
}

@Test
void naoDeveAplicarDescontoQuandoTotalMenorQue1000() {
    Pedido p = new Pedido(500);
    aplicarDesconto(p);
    assertEquals(0, p.getDesconto());  // Mata o mutante
}
```

---

## 🎓 Boas Práticas para Agentes de IA

### 1. Sempre Testar Casos Extremos (Boundary Values)

```java
// ❌ Teste fraco
@Test
void deveValidarIdade() {
    assertTrue(validar(25));
}

// ✅ Testes fortes
@Test
void deveAceitarIdade18() { assertTrue(validar(18)); }

@Test
void deveRejeitarIdade17() { assertFalse(validar(17)); }

@Test
void deveAceitarIdade100() { assertTrue(validar(100)); }
```

---

### 2. Testar Ambos os Branches de Condicionais

```java
// ❌ Teste fraco (só testa branch true)
@Test
void deveProcessarQuandoValido() {
    processar(entidadeValida);
    verify(repo).save(any());
}

// ✅ Testes fortes (ambos os branches)
@Test
void deveProcessarQuandoValido() {
    processar(entidadeValida);
    verify(repo).save(any());
}

@Test
void naoDeveProcessarQuandoInvalido() {
    processar(entidadeInvalida);
    verify(repo, never()).save(any());
}
```

---

### 3. Usar Assertions Específicas

```java
// ❌ Assertion fraca
@Test
void deveRetornarAlgo() {
    assertNotNull(service.buscar(1L));
}

// ✅ Assertion forte
@Test
void deveRetornarProcessoComDadosCorretos() {
    ProcessoDto result = service.buscar(1L);
    
    assertThat(result)
        .isNotNull()
        .extracting("codigo", "descricao", "situacao")
        .containsExactly(1L, "Teste", SituacaoProcesso.CRIADO);
}
```

---

### 4. Verificar Side Effects

```java
// ❌ Teste sem verificar side effects
@Test
void deveIniciarProcesso() {
    service.iniciar(1L);
    // Não verifica se evento foi publicado!
}

// ✅ Teste completo
@Test
void deveIniciarProcessoEPublicarEvento() {
    service.iniciar(1L);
    
    verify(processoRepo).save(argThat(p -> 
        p.getSituacao() == SituacaoProcesso.EM_ANDAMENTO
    ));
    
    verify(eventPublisher).publishEvent(
        argThat(e -> e instanceof EventoProcessoIniciado)
    );
}
```

---

### 5. Usar Fixtures para Facilitar Casos de Teste

```java
// Criar fixtures para diferentes estados
public class ProcessoFixture {
    public static Processo criado() {
        return comSituacao(SituacaoProcesso.CRIADO);
    }
    
    public static Processo emAndamento() {
        return comSituacao(SituacaoProcesso.EM_ANDAMENTO);
    }
    
    public static Processo finalizado() {
        return comSituacao(SituacaoProcesso.FINALIZADO);
    }
    
    private static Processo comSituacao(SituacaoProcesso situacao) {
        Processo p = new Processo();
        p.setSituacao(situacao);
        return p;
    }
}

// Usar nos testes
@Test
void deveFinalizarQuandoEmAndamento() {
    Processo p = ProcessoFixture.emAndamento();
    // ...
}
```

---

## 📈 Monitoramento e Evolução

### Métricas a Acompanhar

1. **Mutation Score por Módulo** (semanal)
2. **Número de Mutantes Sobreviventes** (diminuir)
3. **Test Strength** (aumentar)
4. **Tempo de Execução MBT** (otimizar)

### Relatório Mensal

Criar relatório em `/planejamento/mutation-testing-YYYY-MM.md` com:

- Mutation scores por módulo
- Top 10 mutantes sobreviventes persistentes
- Melhorias implementadas
- Próximos passos

---

## 📚 Recursos Adicionais

### Documentação PITest

- **Site oficial**: <https://pitest.org/>
- **Guia de Quick Start**: <https://pitest.org/quickstart/>
- **Mutadores**: <https://pitest.org/quickstart/mutators/>

### Artigos e Papers

- "Are Mutants a Valid Substitute for Real Faults in Software Testing?" (SIGSOFT 2014)
- "An Analysis and Survey of the Development of Mutation Testing" (IEEE TSE 2011)

### Exemplos no Projeto

- Exemplo de teste que mata mutantes: `/backend/src/test/java/sgc/processo/ProcessoServiceTest.java`
- Fixtures para facilitar testes: `/backend/src/test/java/sgc/fixture/ProcessoFixture.java`

---

## 🔚 Conclusão

Mutation Testing é uma ferramenta poderosa para elevar a qualidade dos testes no SGC. Ao seguir este plano e focar nos módulos prioritários, conseguiremos:

✅ Identificar e corrigir gaps de teste em lógica crítica  
✅ Criar testes mais rigorosos e confiáveis  
✅ Estabelecer cultura de qualidade de testes  
✅ Reduzir bugs em produção  

**Próximo Passo**: Executar baseline test no módulo `comum.erros` e analisar primeiro relatório.

---

**Documento criado em**: 2025-12-24  
**Versão**: 1.0  
**Responsável**: Equipe de Qualidade SGC  
**Última atualização**: 2025-12-24
