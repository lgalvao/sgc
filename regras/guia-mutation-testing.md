# Guia de Mutation Testing - SGC

Este guia orienta a criação e melhoria de testes unitários usando **Mutation Testing** com PITest.

---

## 📋 O que é Mutation Testing?

Mutation Testing avalia a **qualidade dos testes** (não apenas cobertura):

1. **Cria mutantes**: pequenas alterações no código (ex: `>` → `>=`)
2. **Executa testes**: contra cada mutante
3. **Verifica resultado**: teste falha = mutante morto (bom) | teste passa = mutante sobrevive (ruim)

```java
// Código Original
if (saldo > 100) { return true; }

// Mutante: Boundary
if (saldo >= 100) { return true; }  // Se testes passam, mutante sobrevive!
```

---

## 🚀 Comandos de Execução

```bash
# Pré-requisito: garantir que testes passam
./gradlew :backend:test

# Executar mutation testing completo
./gradlew :backend:pitest

# Executar em módulo específico (recomendado)
./gradlew :backend:mutationTestModule -Pmodule=processo
./gradlew :backend:mutationTestModule -Pmodule=subprocesso
./gradlew :backend:mutationTestModule -Pmodule=mapa

# Script auxiliar
./scripts/run-mutation-tests.sh --module processo
```

**Relatório**: `backend/build/reports/pitest/index.html`

---

## 🎯 Módulos Prioritários

| Módulo | Criticidade | Foco |
|--------|-------------|------|
| **ProcessoService** | CRÍTICA | Validações de estado, permissões, eventos |
| **SubprocessoMapaWorkflowService** | CRÍTICA | Transições de estado, workflow |
| **SubprocessoPermissoesService** | ALTA | Lógica de permissões |
| **MapaService** | CRÍTICA | Validações, merge de competências |
| **ImpactoMapaService** | ALTA | Cálculos de diff e impacto |

---

## � Baseline Atual (2025-12-24)

| Métrica | Valor | Meta |
|---------|-------|------|
| **KILLED** | 535 (51.7%) | - |
| **SURVIVED** | 242 (23.4%) | 0% |
| **NO_COVERAGE** | 257 (24.9%) | 0% |
| **Mutation Score** | ~52% | ≥ 70% |

### Classes com mais mutantes sobreviventes

| Classe | SURVIVED | NO_COVERAGE |
|--------|----------|-------------|
| ProcessoService | 38 | 25 |
| ImpactoMapaService | 37 | 10 |
| SubprocessoPermissoesService | 29 | 2 |
| SubprocessoMapaWorkflowService | 26 | 3 |
| SubprocessoDtoService | 18 | 26 |

---

## ⚠️ Padrões Problemáticos Encontrados

### 1. Cadeias de OR em validações de situação

```java
// Código com mutantes sobreviventes (linhas 103-107 de SubprocessoPermissoesService)
boolean situacaoImpactoValida =
    (isRevisao && sp.getSituacao() == NAO_INICIADO)
        || sp.getSituacao() == MAPEAMENTO_CADASTRO_HOMOLOGADO  // SURVIVED
        || sp.getSituacao() == REVISAO_CADASTRO_EM_ANDAMENTO   // SURVIVED
        || sp.getSituacao() == REVISAO_CADASTRO_DISPONIBILIZADA // SURVIVED
        || sp.getSituacao() == REVISAO_CADASTRO_HOMOLOGADA
        || sp.getSituacao() == REVISAO_MAPA_AJUSTADO;

// Solução: testar CADA situação individualmente
@ParameterizedTest
@EnumSource(value = SituacaoSubprocesso.class, names = {
    "MAPEAMENTO_CADASTRO_HOMOLOGADO",
    "REVISAO_CADASTRO_EM_ANDAMENTO",
    // ... todas as situações válidas
})
void devePermitirImpactoEmSituacoesValidas(SituacaoSubprocesso situacao) {
    // Arrange - criar subprocesso com a situação
    // Assert - verificar que podeVisualizarImpacto é true
}

@ParameterizedTest
@EnumSource(value = SituacaoSubprocesso.class, names = {
    "MAPEAMENTO_MAPA_CRIADO",
    "MAPEAMENTO_MAPA_COM_SUGESTOES",
    // ... todas as situações inválidas
})
void naoDevePermitirImpactoEmSituacoesInvalidas(SituacaoSubprocesso situacao) {
    // Assert - verificar que podeVisualizarImpacto é false
}
```

### 2. Lambdas com múltiplas condições (isSubordinada)

```java
// Código com mutantes sobreviventes (linhas 147-154)
private boolean isSubordinada(Unidade alvo, Unidade superior) {
    if (alvo == null || superior == null || alvo.getUnidadeSuperior() == null) return false;
    
    Unidade atual = alvo;
    while (atual != null) {
        if (Objects.equals(superior.getCodigo(), atual.getCodigo())) return true;
        atual = atual.getUnidadeSuperior();
    }
    return false;
}

// Solução: testar CADA caso de guarda separadamente
@Test void retornaFalseQuandoAlvoNull() { assertFalse(isSubordinada(null, unidade)); }
@Test void retornaFalseQuandoSuperiorNull() { assertFalse(isSubordinada(unidade, null)); }
@Test void retornaFalseQuandoSemUnidadeSuperior() { 
    unidade.setUnidadeSuperior(null);
    assertFalse(isSubordinada(unidade, outraUnidade)); 
}
@Test void retornaTrueQuandoEhSubordinadaDireta() { /* hierarquia de 1 nível */ }
@Test void retornaTrueQuandoEhSubordinadaIndireta() { /* hierarquia de 2+ níveis */ }
```

### 3. Condições em stream lambdas

```java
// Mutante sobrevive em lambdas complexas (linha 81)
usuario.getTodasAtribuicoes().stream()
    .anyMatch(a -> a.getPerfil() == Perfil.GESTOR
            && a.getUnidade() != null           // SURVIVED
            && a.getUnidade().getCodigo() != null  // SURVIVED
            && (a.getUnidade().getCodigo().equals(spUnidadeCodigo)
            || isSubordinada(sp.getUnidade(), a.getUnidade())));

// Solução: testar casos onde unidade ou código são null
@Test void naoPermiteQuandoGestorComUnidadeNull() {
    Usuario gestor = criarUsuarioComPerfilUnidadeNull(Perfil.GESTOR);
    var result = service.calcularPermissoes(subprocesso, gestor);
    assertFalse(result.isPodeEditarMapa());
}
```

---

## �📊 Interpretando Relatórios

### Estados de Mutantes

| Estado | Significado | Ação |
|--------|-------------|------|
| ✅ **KILLED** | Teste detectou mutação | Nenhuma (bom!) |
| ❌ **SURVIVED** | Teste NÃO detectou | Adicionar/melhorar teste |
| ⚠️ **NO_COVERAGE** | Código não testado | Criar teste |

### Métricas

- **Mutation Score**: `Mutantes Mortos / Total` → Meta: ≥ 70%
- **Test Strength**: `Mortos / Cobertos` → Meta: ≥ 0.70

---

## 🧪 Tipos de Mutantes e Como Matá-los

### 1. Conditionals Boundary (`>` → `>=`)

```java
// Mutante sobrevive se não testamos o limite exato
if (idade > 18) { permitir(); }

// Testes que matam:
@Test void limite_19() { assertTrue(verificar(19)); }   // passa em ambos
@Test void limite_18() { assertFalse(verificar(18)); }  // MATA o mutante >=
@Test void limite_17() { assertFalse(verificar(17)); }
```

### 2. Negate Conditionals (`&&` → `||`, `==` → `!=`)

```java
// Original
if (ativo && temPermissao) { return true; }

// Testes que matam:
@Test void ativoComPermissao() { assertTrue(verificar(true, true)); }
@Test void ativoSemPermissao() { assertFalse(verificar(true, false)); }   // MATA ||
@Test void inativoComPermissao() { assertFalse(verificar(false, true)); } // MATA negação
```

### 3. Return Values (`return true` → `return false`)

```java
// Original
public boolean isValido() { return status == ACTIVE; }

// Testes que matam:
@Test void validoQuandoAtivo() { assertTrue(criar(ACTIVE).isValido()); }     // MATA false
@Test void invalidoQuandoInativo() { assertFalse(criar(INACTIVE).isValido()); }
```

### 4. Void Method Calls (remove chamadas)

```java
// Original
public void processar(Pedido p) {
    validar(p);
    salvar(p);
    notificar(p);
}

// Testes que matam:
@Test void deveValidar() {
    assertThrows(ErroValidacao.class, () -> processar(pedidoInvalido));
    verify(repo, never()).save(any());  // MATA remoção de validar()
}

@Test void deveNotificar() {
    processar(pedidoValido);
    verify(notificador).enviar(any());  // MATA remoção de notificar()
}
```

### 5. Math Operators (`+` → `-`, `*` → `/`)

```java
// Original
return quantidade * preco;

// Teste que mata todos os mutantes:
@Test void calculo() {
    assertEquals(200, calcular(10, 20));  // 10*20=200, 10+20=30, 10/20=0, 10-20=-10
}
```

---

## ✅ Boas Práticas

### 1. Testar Limites (Boundary Values)

```java
// ❌ Fraco
@Test void validar() { assertTrue(validar(25)); }

// ✅ Forte
@Test void limite_exato() { assertTrue(validar(18)); }
@Test void abaixo_limite() { assertFalse(validar(17)); }
@Test void acima_limite() { assertTrue(validar(19)); }
```

### 2. Testar Ambos os Branches

```java
// ❌ Apenas um branch
@Test void quandoValido() {
    processar(valido);
    verify(repo).save(any());
}

// ✅ Ambos os branches
@Test void quandoValido() { /* ... */ }
@Test void quandoInvalido() {
    processar(invalido);
    verify(repo, never()).save(any());
}
```

### 3. Assertions Específicas

```java
// ❌ Fraca
assertNotNull(result);

// ✅ Forte
assertThat(result)
    .extracting("codigo", "situacao")
    .containsExactly(1L, SituacaoProcesso.CRIADO);
```

### 4. Verificar Side Effects

```java
// ❌ Não verifica efeitos
service.iniciar(1L);

// ✅ Verifica todos os efeitos
service.iniciar(1L);
verify(repo).save(argThat(p -> p.getSituacao() == EM_ANDAMENTO));
verify(eventPublisher).publishEvent(any(ProcessoIniciadoEvent.class));
```

---

## 🔄 Workflow para Matar Mutantes

1. **Executar MBT** no módulo alvo
2. **Abrir relatório** (`build/reports/pitest/index.html`)
3. **Identificar mutantes SURVIVED** na classe
4. **Analisar** qual condição não está sendo testada
5. **Criar teste** que cubra o caso específico
6. **Re-executar MBT** e verificar que mutante foi morto

---

## 📈 Métricas de Sucesso

| Métrica | Meta |
|---------|------|
| Mutation Score (geral) | ≥ 70% |
| Mutation Score (core) | ≥ 75% |
| Test Strength | ≥ 0.70 |

---

## 📚 Recursos

- [PITest Official](https://pitest.org/)
- [Mutators Documentation](https://pitest.org/quickstart/mutators/)
- Relatório local: `backend/build/reports/pitest/index.html`
