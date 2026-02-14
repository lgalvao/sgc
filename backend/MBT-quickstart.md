# 🧬 Guia Rápido - Mutation-Based Testing (MBT)

**Versão:** 1.0  
**Data:** 2026-02-14

---

## 🎯 O que é MBT?

**Mutation-Based Testing (MBT)** verifica se seus testes realmente detectam mudanças no código, introduzindo pequenas modificações (mutações) e rodando os testes:

- ✅ **Mutante Morto:** Teste falhou (detectou a mudança) = Bom!
- ❌ **Mutante Sobrevivente:** Teste passou (não detectou) = Problema!

### Exemplo

```java
// Código original
if (idade >= 18) {
    permitirAcesso();
}

// Mutação (PIT troca >= por >)
if (idade > 18) {  // 18 anos não permitiria mais!
    permitirAcesso();
}
```

**Se seu teste não falhar** com essa mudança, ele é **ineficaz**.

---

## 🚀 Comandos

### 1. Mutation Testing Completo

```bash
cd backend
./gradlew mutationTest
```

⏱️ **Tempo:** ~2-4 horas (projeto completo)  
📊 **Relatório:** `backend/build/reports/pitest/index.html`

### 2. Mutation Testing por Módulo (Recomendado)

```bash
cd backend
./gradlew mutationTestModulo -PtargetModule=processo
```

⏱️ **Tempo:** ~2-5 minutos por módulo  
🎯 **Use para:** Análise rápida de módulos específicos

**Módulos disponíveis:**
- `processo`
- `subprocesso`
- `mapa`
- `atividade`
- `alerta`
- `organizacao`
- `notificacao`
- `analise`
- `seguranca`

### 3. Mutation Testing Incremental (Mais Rápido)

```bash
cd backend
./gradlew mutationTestIncremental
```

⏱️ **Tempo:** ~1-3 minutos  
🎯 **Use para:** Apenas classes modificadas recentemente (git diff)

---

## 📊 Interpretando Resultados

### Mutation Score

```
Mutation Score: 79%
- 27 mutantes mortos
- 7 mutantes sobreviventes
```

**Interpretação:**
- 🟢 **≥85%:** Excelente - testes robustos
- 🟡 **70-84%:** Bom - melhorias necessárias
- 🔴 **<70%:** Fraco - muitos testes ineficazes

### Relatório HTML

Abra `backend/build/reports/pitest/index.html`:

1. **Verde** = Mutante morto (teste eficaz) ✅
2. **Vermelho** = Mutante sobrevivente (teste ineficaz) ❌
3. **Clique na classe** para ver linha por linha

---

## 🔧 Como Corrigir Mutantes Sobreviventes

### Padrão 1: Falta Assertion

❌ **Antes (ineficaz):**
```java
@Test
void testCriarProcesso() {
    service.criar(request);  // Só executa, não valida!
}
```

✅ **Depois (eficaz):**
```java
@Test
void deveCriarProcessoComStatusPendente() {
    ProcessoResponse response = service.criar(request);
    
    assertNotNull(response);
    assertEquals(StatusProcesso.PENDENTE, response.getStatus());
}
```

### Padrão 2: Não Testa Ambos os Caminhos

❌ **Antes (ineficaz):**
```java
@Test
void testValidar() {
    validator.validar(processoInvalido);
    // Só testa caso inválido!
}
```

✅ **Depois (eficaz):**
```java
@Test
void deveLancarErroQuandoInvalido() {
    assertThrows(ErroValidacao.class, 
        () -> validator.validar(processoInvalido));
}

@Test
void naoDeveLancarErroQuandoValido() {
    assertDoesNotThrow(() -> validator.validar(processoValido));
}
```

### Padrão 3: Não Valida Null

❌ **Antes (ineficaz):**
```java
@Test
void testBuscar() {
    Processo p = service.buscar(codigo);
    assertEquals("PROC001", p.getCodigo());  // NPE se null!
}
```

✅ **Depois (eficaz):**
```java
@Test
void deveRetornarProcessoQuandoExiste() {
    Processo p = service.buscar("PROC001");
    
    assertNotNull(p);  // Valida que não é null
    assertEquals("PROC001", p.getCodigo());
}

@Test
void deveRetornarNullQuandoNaoExiste() {
    Processo p = service.buscar("INEXISTENTE");
    
    assertNull(p);  // Testa caso null
}
```

### Padrão 4: Não Valida Coleções Vazias

❌ **Antes (ineficaz):**
```java
@Test
void testListar() {
    List<Processo> lista = service.listar();
    // Não valida se vazio ou null!
}
```

✅ **Depois (eficaz):**
```java
@Test
void deveRetornarListaVaziaQuandoNaoHaDados() {
    when(repo.findAll()).thenReturn(Collections.emptyList());
    
    List<Processo> lista = service.listar();
    
    assertNotNull(lista);
    assertTrue(lista.isEmpty());
}

@Test
void deveRetornarListaPreenchida() {
    when(repo.findAll()).thenReturn(List.of(processo1, processo2));
    
    List<Processo> lista = service.listar();
    
    assertNotNull(lista);
    assertEquals(2, lista.size());
}
```

---

## 🎯 Workflow Recomendado

### 1. Antes de Commitar

```bash
# Rodar mutation testing nas classes modificadas
./gradlew mutationTestIncremental
```

### 2. Durante Code Review

```bash
# Rodar mutation testing no módulo afetado
./gradlew mutationTestModulo -PtargetModule=processo
```

### 3. CI/CD (Semanal)

```bash
# Análise completa (automática)
./gradlew mutationTest
```

---

## 📋 Checklist - Teste de Qualidade

Antes de considerar um teste "pronto":

- [ ] **Executa o código?** (cobertura de linha)
- [ ] **Valida o resultado?** (assertions)
- [ ] **Testa casos de erro?** (null, vazio, exceções)
- [ ] **Testa ambos os caminhos?** (if/else, loops)
- [ ] **Mutantes mortos?** (mutation score >85%)

---

## 🐛 Tipos de Mutações Comuns

| Mutador                    | O que faz                        | Como testar                       |
|----------------------------|----------------------------------|-----------------------------------|
| **NullReturnValsMutator**  | Retorna `null` ao invés do valor | Testar casos null e não-null      |
| **RemoveConditionalMutator** | Remove `if` statements         | Testar ambos os ramos             |
| **EmptyObjectReturnValsMutator** | Retorna coleção vazia      | Testar `isEmpty()` e `size()`     |
| **BooleanTrueReturnValsMutator** | Troca `true` por `false`   | Testar ambos os casos             |
| **Math**                   | Troca `+` por `-`, etc           | Testar valores específicos        |
| **Increments**             | Troca `++` por `--`              | Testar valores antes/depois       |

---

## ⚡ Dicas de Performance

### Otimizar Execução

1. **Use análise incremental** para mudanças pequenas
2. **Analise por módulo** durante desenvolvimento
3. **Análise completa** apenas em CI/CD
4. **Exclua classes irrelevantes** (configs, DTOs)

### Configuração de Exclusões

Já configurado em `build.gradle.kts`:
```kotlin
excludedClasses.set(listOf(
    "sgc.config.*",      // Configurações
    "sgc.*Exception",    // Exceções
    "sgc.*Mapper*",      // Mappers gerados
    "sgc.*.dto.*"        // DTOs
))
```

---

## 🆘 Troubleshooting

### Problema: Timeout

**Causa:** Teste muito lento  
**Solução:** Otimizar teste ou adicionar ao excludedClasses

### Problema: Mutation Score muito baixo (<60%)

**Causa:** Testes só executam código, sem validar  
**Solução:** Adicionar assertions em todos os testes

### Problema: Mutante Equivalente

**Causa:** Mutação não muda comportamento  
**Solução:** Normal, ~3-5% são equivalentes, ignorar

### Problema: Muito lento (>1h)

**Causa:** Muitas classes/testes  
**Solução:**
1. Usar `mutationTestModulo`
2. Aumentar exclusões
3. Habilitar análise incremental

---

## 📚 Documentação Completa

- **[MBT-plan.md](MBT-plan.md)** - Plano completo de implementação
- **[MBT-baseline.md](MBT-baseline.md)** - Resultados iniciais
- **[GUIA-MELHORIAS-TESTES.md](etc/docs/GUIA-MELHORIAS-TESTES.md)** - Padrões de teste

---

## 💡 Exemplo Completo

### Código a Testar

```java
public class ProcessoService {
    public ProcessoResponse criar(ProcessoRequest request) {
        if (request.getTitulo() == null) {
            throw new ErroValidacao("Título obrigatório");
        }
        
        Processo processo = new Processo();
        processo.setTitulo(request.getTitulo());
        processo.setStatus(StatusProcesso.PENDENTE);
        
        processo = repo.save(processo);
        
        return mapper.toResponse(processo);
    }
}
```

### Testes Eficazes (Matam Mutantes)

```java
@Nested
@DisplayName("Criar Processo")
class CriarProcessoTest {
    
    @Test
    @DisplayName("deve criar processo com título válido")
    void deveCriarProcessoComTituloValido() {
        ProcessoRequest request = criarRequest("Processo Teste");
        
        ProcessoResponse response = service.criar(request);
        
        assertNotNull(response);
        assertEquals("Processo Teste", response.getTitulo());
        assertEquals(StatusProcesso.PENDENTE, response.getStatus());
    }
    
    @Test
    @DisplayName("deve lançar erro quando título é null")
    void deveLancarErroQuandoTituloNull() {
        ProcessoRequest request = criarRequest(null);
        
        ErroValidacao erro = assertThrows(ErroValidacao.class,
            () -> service.criar(request));
        
        assertThat(erro.getMessage()).contains("Título obrigatório");
    }
    
    @Test
    @DisplayName("deve iniciar com status PENDENTE")
    void deveIniciarComStatusPendente() {
        ProcessoRequest request = criarRequest("Teste");
        
        ProcessoResponse response = service.criar(request);
        
        assertEquals(StatusProcesso.PENDENTE, response.getStatus());
    }
}
```

**Mutation Score esperado:** >90%

---

**Última Atualização:** 2026-02-14  
**Dúvidas?** Consulte [MBT-plan.md](MBT-plan.md) ou o time de Backend
