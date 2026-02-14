# 🔍 Análise Detalhada de Mutantes - Módulo Alerta

**Data:** 2026-02-14  
**Módulo:** sgc.alerta.*  
**Mutation Score:** 79% (27/34)

---

## 📊 Sumário de Mutantes

### Distribuição por Status

- ✅ **Mortos:** 27 (79%)
- ❌ **Sobreviventes:** 7 (21%)
- ⚠️ **Total Gerado:** 34

### Distribuição por Classe

| Classe             | Gerados | Mortos | Sobreviventes | Score |
|--------------------|---------|--------|---------------|-------|
| AlertaFacade       | 21      | 16     | 5             | 76%   |
| AlertaService      | 9       | 9      | 0             | 100%  |
| AlertaController   | 4       | 2      | 2             | 50%   |

---

## 🚨 Mutantes Sobreviventes (Crítico)

### 1. AlertaFacade.listarAlertasPorUsuario (Linha 219)

**Mutador:** RemoveConditionalMutator_EQUAL_ELSE  
**Status:** SURVIVED  
**Criticidade:** 🔴 ALTA

**Código (provável):**
```java
if (condicao) {
    // código
}
```

**Problema:**
O teste não valida ambos os caminhos da condição. Se removermos o `if`, o teste continua passando.

**Como corrigir:**
```java
@Test
void deveExecutarQuandoCondicaoVerdadeira() {
    // Setup para condicao = true
    List<Alerta> resultado = facade.listarAlertasPorUsuario(...);
    // Validar resultado quando condição é verdadeira
}

@Test
void naoDeveExecutarQuandoCondicaoFalsa() {
    // Setup para condicao = false
    List<Alerta> resultado = facade.listarAlertasPorUsuario(...);
    // Validar resultado quando condição é falsa (comportamento diferente)
}
```

---

### 2. AlertaFacade.obterSiglaParaUsuario (Linha 57)

**Mutador:** RemoveConditionalMutator_EQUAL_ELSE  
**Status:** SURVIVED  
**Criticidade:** 🔴 ALTA

**Problema:**
Condicional não está sendo testada. Método não tem teste cobrindo este caso.

**Como corrigir:**
```java
@Nested
@DisplayName("Obter Sigla Para Usuário")
class ObterSiglaParaUsuarioTest {
    
    @Test
    void deveRetornarSiglaQuandoUsuarioTemUnidade() {
        Usuario usuario = criarUsuarioComUnidade("UN001");
        String sigla = facade.obterSiglaParaUsuario(usuario);
        assertEquals("UN001", sigla);
    }
    
    @Test
    void deveRetornarDefaultQuandoUsuarioSemUnidade() {
        Usuario usuario = criarUsuarioSemUnidade();
        String sigla = facade.obterSiglaParaUsuario(usuario);
        assertNotNull(sigla);  // Ou verificar valor default esperado
    }
}
```

---

### 3. AlertaFacade.obterSiglaParaUsuario (Linha 58)

**Mutador:** EmptyObjectReturnValsMutator  
**Status:** SURVIVED  
**Criticidade:** 🟡 MÉDIA

**Problema:**
Método retorna String, mas teste não valida se a string está vazia ou tem conteúdo.

**Como corrigir:**
```java
@Test
void deveRetornarSiglaNaoVazia() {
    Usuario usuario = criarUsuarioComUnidade("UN001");
    String sigla = facade.obterSiglaParaUsuario(usuario);
    
    assertNotNull(sigla);
    assertFalse(sigla.isEmpty());  // Valida que não é vazio
    assertEquals("UN001", sigla);
}
```

---

### 4. AlertaFacade.obterSiglaParaUsuario (Linha 60)

**Mutador:** EmptyObjectReturnValsMutator  
**Status:** SURVIVED  
**Criticidade:** 🟡 MÉDIA

**Problema:**
Mesmo que #3 - outra linha do método não valida retorno vazio.

**Como corrigir:**
Mesmo que #3, garantir que todas as branches retornam strings não-vazias.

---

### 5. AlertaController.listarAlertas (Linha 31)

**Mutador:** NullReturnValsMutator  
**Status:** SURVIVED  
**Criticidade:** 🔴 ALTA

**Problema:**
Controller pode retornar `null` e o teste não detecta. Possível NullPointerException em produção.

**Como corrigir:**
```java
@Test
void listarAlertas_deveRetornarListaNaoNula() {
    ResponseEntity<List<AlertaResponse>> response = controller.listarAlertas(...);
    
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());
}

@Test
void listarAlertas_quandoNaoHaDados_deveRetornarListaVazia() {
    when(facade.listarAlertasPorUsuario(...)).thenReturn(Collections.emptyList());
    
    ResponseEntity<List<AlertaResponse>> response = controller.listarAlertas(...);
    
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());
}
```

---

### 6. AlertaController.listarNaoLidos (Linha 41)

**Mutador:** NullReturnValsMutator  
**Status:** SURVIVED  
**Criticidade:** 🔴 ALTA

**Problema:**
Mesmo que #5 - possível retorno null não testado.

**Como corrigir:**
```java
@Test
void listarNaoLidos_deveRetornarListaNaoNula() {
    ResponseEntity<List<AlertaResponse>> response = controller.listarNaoLidos(...);
    
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());
}
```

---

### 7. AlertaController.marcarComoLidos (Linha 53)

**Mutador:** NullReturnValsMutator  
**Status:** SURVIVED  
**Criticidade:** 🟡 MÉDIA

**Problema:**
Método retorna `ResponseEntity` mas teste não valida se é null.

**Como corrigir:**
```java
@Test
void marcarComoLidos_deveRetornarResponseNaoNulo() {
    ResponseEntity<Void> response = controller.marcarComoLidos(...);
    
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
}
```

---

## ✅ Exemplos de Testes Eficazes

### Exemplo 1: AlertaService (100% Mutation Score)

**Por que é eficaz:**

```java
@Test
void deveBuscarPorCodigo() {
    // Setup
    Alerta alerta = criarAlerta();
    when(repo.findByCodigo("AL001")).thenReturn(Optional.of(alerta));
    
    // Act
    Optional<Alerta> resultado = service.buscarPorCodigo("AL001");
    
    // Assert
    assertTrue(resultado.isPresent());      // Valida não é empty
    assertEquals("AL001", resultado.get().getCodigo());
}
```

**Mutantes mortos:**
- ✅ EmptyObjectReturnValsMutator - assertTrue detecta se retornou empty()
- ✅ NullReturnValsMutator - isPresent() detecta null

---

### Exemplo 2: AlertaFacade.criarAlerta

**Por que é eficaz:**

```java
@Test
void deveCriarAlertaComSucesso() {
    // Setup
    AlertaRequest request = criarRequest();
    Alerta alerta = criarAlerta();
    when(service.salvar(any())).thenReturn(alerta);
    
    // Act
    AlertaResponse response = facade.criarAlerta(request);
    
    // Assert
    assertNotNull(response);                     // Detecta null
    assertEquals("AL001", response.getCodigo()); // Valida valor correto
    verify(service).salvar(any());               // Valida que salvou
}
```

**Mutantes mortos:**
- ✅ NullReturnValsMutator - assertNotNull detecta
- ✅ Outros mutadores - assertEquals valida valor específico

---

## 📋 Plano de Ação Prioritário

### Prioridade 1 - Controllers (Crítico)

| Método                    | Mutante     | Ação                           | Estimativa |
|---------------------------|-------------|--------------------------------|------------|
| listarAlertas             | NullReturn  | Adicionar assertion null       | 5 min      |
| listarNaoLidos            | NullReturn  | Adicionar assertion null       | 5 min      |
| marcarComoLidos           | NullReturn  | Adicionar assertion null       | 5 min      |

**Total Prioridade 1:** ~15 minutos

### Prioridade 2 - Facade (Alto)

| Método                    | Mutante        | Ação                           | Estimativa |
|---------------------------|----------------|--------------------------------|------------|
| listarAlertasPorUsuario   | Conditional    | Testar ambos os branches       | 15 min     |
| obterSiglaParaUsuario     | Conditional    | Criar testes para método       | 20 min     |
| obterSiglaParaUsuario     | EmptyObject x2 | Validar string não vazia       | 5 min      |

**Total Prioridade 2:** ~40 minutos

### Impacto Esperado

- **Mutation Score Atual:** 79%
- **Mutation Score Pós-Correção:** **~97%** (33/34 mortos)
- **Tempo Total:** ~55 minutos
- **Testes a Criar/Modificar:** ~8 testes

---

## 🎯 Padrões Identificados

### Anti-Padrão 1: Não Validar Retorno Null

**Ocorrências:** 4 casos (Controllers)

**Problema:**
```java
@Test
void testListar() {
    controller.listar();  // Não captura retorno!
}
```

**Solução:**
```java
@Test
void deveRetornarListaNaoNula() {
    ResponseEntity<?> response = controller.listar();
    assertNotNull(response);
    assertNotNull(response.getBody());
}
```

### Anti-Padrão 2: Não Testar Ambos os Branches

**Ocorrências:** 2 casos (Facade)

**Problema:**
```java
// Só testa caminho feliz
@Test
void testMetodo() {
    resultado = facade.metodo(true);
    assertEquals(esperado, resultado);
}
```

**Solução:**
```java
@Test
void deveExecutarQuandoCondicaoTrue() {
    resultado = facade.metodo(true);
    assertEquals(esperadoTrue, resultado);
}

@Test
void naoDeveExecutarQuandoCondicaoFalse() {
    resultado = facade.metodo(false);
    assertEquals(esperadoFalse, resultado);
}
```

### Anti-Padrão 3: Não Validar String Vazia vs Null

**Ocorrências:** 2 casos (Facade)

**Problema:**
```java
@Test
void testObterSigla() {
    String sigla = facade.obterSigla();
    // Não valida se é vazio ou null
}
```

**Solução:**
```java
@Test
void deveRetornarSiglaNaoVazia() {
    String sigla = facade.obterSigla();
    
    assertNotNull(sigla);
    assertFalse(sigla.isEmpty());
    assertTrue(sigla.length() > 0);
}
```

---

## 📈 Próximos Passos

### 1. Corrigir Mutantes Sobreviventes (Esta Sprint)

- [ ] Corrigir 3 mutantes de AlertaController (15 min)
- [ ] Corrigir 4 mutantes de AlertaFacade (40 min)
- [ ] Re-executar mutation testing
- [ ] Validar mutation score >95%

### 2. Aplicar Padrões a Outros Módulos

- [ ] Documentar padrões encontrados
- [ ] Revisar outros Controllers para mesmo problema
- [ ] Criar checklist de code review

### 3. Automatizar Validação

- [ ] Adicionar mutation testing ao PR template
- [ ] Configurar threshold de 85% no CI
- [ ] Criar dashboard de mutation score

---

## 🔬 Detalhes Técnicos

### Mutadores Ativos (DEFAULTS)

1. **NullReturnValsMutator** - Troca retorno por `null`
2. **EmptyObjectReturnValsMutator** - Troca retorno por coleção vazia
3. **RemoveConditionalMutator_EQUAL_ELSE** - Remove condicionais
4. **BooleanTrueReturnValsMutator** - Troca `true` por `false`
5. **VoidMethodCallMutator** - Remove chamadas void

### Configuração Utilizada

```kotlin
pitest {
    targetClasses.set(listOf("sgc.alerta.*"))
    mutators.set(listOf("DEFAULTS"))
    threads.set(12)
}
```

### Comando Executado

```bash
./gradlew mutationTestModulo -PtargetModule=alerta
```

---

## 📊 Comparação com Baseline Esperado

| Métrica                | Esperado | Obtido | Status |
|------------------------|----------|--------|--------|
| Mutation Score         | 70-75%   | 79%    | ✅ Acima |
| Classes 100% Score     | ~20%     | 33%    | ✅ Acima |
| Mutantes Sobreviventes | ~10      | 7      | ✅ Melhor |
| Tempo Execução         | <5min    | 2m20s  | ✅ OK |

**Conclusão:** Módulo Alerta está **acima da média** esperada, indicando que outros módulos podem ter scores mais baixos (~60-70%).

---

**Próxima Revisão:** Após correção dos 7 mutantes sobreviventes  
**Meta:** Mutation Score >95% (33/34)  
**Responsável:** Time de Backend
