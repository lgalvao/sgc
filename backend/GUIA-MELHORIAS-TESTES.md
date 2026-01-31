# 📋 Guia de Melhorias de Testes - Backend

**Data:** 2026-01-31  
**Status:** Diretrizes para Melhorias Futuras

---

## 🎯 Objetivo

Este documento fornece diretrizes para melhorar a qualidade dos testes no backend do SGC, focando em duas áreas principais:

1. **Redução de Múltiplos Asserts por Teste**
2. **Refatoração de Testes que Verificam Implementação**

---

## 1. Múltiplos Asserts por Teste

### Problema

Testes com 5-10+ assertions dificultam a identificação rápida do que falhou e violam o princípio "one concept per test".

### Exemplo Atual (Anti-Padrão)

```java
@Test
void testCriarProcesso() {
    ProcessoResponse response = service.criar(request);
    
    assertNotNull(response);
    assertEquals("Teste", response.getTitulo());
    assertEquals("Descrição", response.getDescricao());
    assertEquals(StatusProcesso.PENDENTE, response.getStatus());
    assertNotNull(response.getDataCriacao());
    assertEquals("UN001", response.getCodigoUnidade());
    assertTrue(response.getSubprocessos().isEmpty());
    // Se qualquer assertion falhar, as demais não são executadas
    // Dificulta diagnosticar o problema real
}
```

### Solução Recomendada

Dividir em múltiplos testes focados:

```java
@Nested
@DisplayName("Criar Processo")
class CriarProcessoTest {
    
    private ProcessoResponse response;
    
    @BeforeEach
    void setup() {
        response = service.criar(request);
    }
    
    @Test
    @DisplayName("deve retornar processo não-nulo")
    void deveRetornarProcessoNaoNulo() {
        assertNotNull(response);
    }
    
    @Test
    @DisplayName("deve definir título corretamente")
    void deveDefinirTituloCorretamente() {
        assertEquals("Teste", response.getTitulo());
    }
    
    @Test
    @DisplayName("deve definir descrição corretamente")
    void deveDefinirDescricaoCorretamente() {
        assertEquals("Descrição", response.getDescricao());
    }
    
    @Test
    @DisplayName("deve iniciar com status PENDENTE")
    void deveIniciarComStatusPendente() {
        assertEquals(StatusProcesso.PENDENTE, response.getStatus());
    }
    
    @Test
    @DisplayName("deve definir data de criação")
    void deveDefinirDataCriacao() {
        assertNotNull(response.getDataCriacao());
    }
    
    @Test
    @DisplayName("deve iniciar sem subprocessos")
    void deveIniciarSemSubprocessos() {
        assertTrue(response.getSubprocessos().isEmpty());
    }
}
```

### Benefícios

- ✅ Cada teste verifica um único conceito
- ✅ Falhas são facilmente identificáveis pelo nome do teste
- ✅ Todos os testes rodam mesmo se um falhar
- ✅ Melhor documentação do comportamento esperado

### Quando Múltiplos Asserts São Aceitáveis

```java
@Test
void deveRetornarUsuarioCompleto() {
    Usuario usuario = service.buscar(codigo);
    
    // ✅ ACEITÁVEL: Assertions verificam o mesmo conceito (completude do objeto)
    assertAll(
        () -> assertNotNull(usuario.getNome()),
        () -> assertNotNull(usuario.getCpf()),
        () -> assertNotNull(usuario.getUnidade())
    );
}

@Test
void deveValidarCamposObrigatorios() {
    ErroNegocio erro = assertThrows(ErroNegocio.class, 
        () -> service.criar(requestInvalido));
    
    // ✅ ACEITÁVEL: Verifica estrutura da mensagem de erro
    assertThat(erro.getMessage())
        .contains("nome")
        .contains("obrigatório");
}
```

---

## 2. Testes que Verificam Implementação

### Problema

Testes que verificam **como** o código funciona (detalhes de implementação) ao invés de **o que** ele faz (comportamento observável).

### Exemplo Atual (Anti-Padrão)

```java
@Test
void testIniciarProcesso() {
    service.iniciar(codigo);
    
    // ❌ ERRADO: Verifica detalhes internos de implementação
    verify(repo, times(1)).findByCodigo(codigo);
    verify(repo, times(1)).save(any());
    verify(eventPublisher, times(1)).publishEvent(any());
    
    // Se refatorarmos para chamar findByCodigo duas vezes,
    // o teste quebra mesmo que o comportamento esteja correto!
}
```

### Solução Recomendada

Testar comportamento observável:

```java
@Test
void deveIniciarProcessoComSucesso() {
    // Arrange
    Processo processo = criarProcessoPendente();
    when(repo.findByCodigo(codigo)).thenReturn(Optional.of(processo));
    
    // Act
    service.iniciar(codigo);
    
    // Assert - COMPORTAMENTO OBSERVÁVEL
    assertEquals(StatusProcesso.INICIADO, processo.getStatus());
    assertNotNull(processo.getDataInicio());
}

@Test
void devePublicarEventoAoIniciarProcesso() {
    // Arrange
    Processo processo = criarProcessoPendente();
    when(repo.findByCodigo(codigo)).thenReturn(Optional.of(processo));
    
    ArgumentCaptor<EventoProcessoIniciado> captor = 
        ArgumentCaptor.forClass(EventoProcessoIniciado.class);
    
    // Act
    service.iniciar(codigo);
    
    // Assert - VERIFICA TIPO E DADOS DO EVENTO (comportamento público)
    verify(eventPublisher).publishEvent(captor.capture());
    EventoProcessoIniciado evento = captor.getValue();
    assertEquals(codigo, evento.getCodigoProcesso());
}

@Test
void deveLancarErroQuandoProcessoNaoExiste() {
    // Arrange
    when(repo.findByCodigo(codigo)).thenReturn(Optional.empty());
    
    // Act & Assert - COMPORTAMENTO OBSERVÁVEL (exceção lançada)
    ErroNegocio erro = assertThrows(ErroNegocio.class, 
        () -> service.iniciar(codigo));
    
    assertEquals("Processo não encontrado", erro.getMessage());
}
```

### Quando Verificar Interações É Aceitável

```java
@Test
void deveEnviarEmailDeNotificacao() {
    // ✅ ACEITÁVEL: Email é um efeito colateral importante
    // que não pode ser facilmente observado de outra forma
    service.finalizarProcesso(codigo);
    
    verify(emailService).enviar(
        eq("admin@example.com"),
        contains("Processo finalizado")
    );
}

@Test
void deveLogarAcaoDeSeguranca() {
    // ✅ ACEITÁVEL: Log de auditoria é comportamento crítico
    service.excluirUsuario(codigo);
    
    verify(auditService).registrar(
        eq(AcaoAuditoria.EXCLUSAO_USUARIO),
        eq(codigo)
    );
}
```

### Diferença Fundamental

| ❌ Testa Implementação | ✅ Testa Comportamento |
|----------------------|----------------------|
| `verify(repo, times(1)).save(any())` | `assertEquals(StatusProcesso.ATIVO, processo.getStatus())` |
| `verify(mapper).toDto(any())` | `assertNotNull(response.getTitulo())` |
| `verify(validator).validar(any())` | `assertThrows(ErroValidacao.class, ...)` |
| Quebra com refatoração | Quebra apenas com mudança de comportamento |

---

## 3. Checklist para Refatoração

Ao refatorar testes existentes:

- [ ] Identifique testes com 5+ assertions
- [ ] Agrupe assertions por conceito testado
- [ ] Crie `@Nested` classes para organizar testes relacionados
- [ ] Use `@DisplayName` descritivo em português
- [ ] Remova `verify()` de detalhes de implementação
- [ ] Mantenha `verify()` apenas para efeitos colaterais importantes
- [ ] Teste estado final ao invés de passos intermediários
- [ ] Use `assertThrows()` para verificar exceções
- [ ] Valide que o teste falha quando deveria (run with failing condition)

---

## 4. Exemplos Práticos

### Antes (Anti-Padrão)

```java
@Test
void testProcessarSubprocesso() {
    service.processar(codigo);
    
    verify(repo).findByCodigo(codigo);
    verify(validador).validar(any());
    verify(mapper).toDto(any());
    verify(publisher).publishEvent(any());
    verify(repo).save(any());
    
    Subprocesso sub = repo.findByCodigo(codigo).get();
    assertEquals(StatusSubprocesso.PROCESSADO, sub.getStatus());
    assertNotNull(sub.getDataProcessamento());
    assertEquals("admin", sub.getProcessadoPor());
}
```

### Depois (Padrão Correto)

```java
@Nested
@DisplayName("Processar Subprocesso")
class ProcessarSubprocessoTest {
    
    @Test
    @DisplayName("deve alterar status para PROCESSADO")
    void deveAlterarStatusParaProcessado() {
        Subprocesso sub = service.processar(codigo);
        assertEquals(StatusSubprocesso.PROCESSADO, sub.getStatus());
    }
    
    @Test
    @DisplayName("deve registrar data de processamento")
    void deveRegistrarDataProcessamento() {
        Subprocesso sub = service.processar(codigo);
        assertNotNull(sub.getDataProcessamento());
        assertTrue(sub.getDataProcessamento().isBefore(LocalDateTime.now()));
    }
    
    @Test
    @DisplayName("deve registrar usuário processador")
    void deveRegistrarUsuarioProcessador() {
        Subprocesso sub = service.processar(codigo);
        assertEquals("admin", sub.getProcessadoPor());
    }
    
    @Test
    @DisplayName("deve publicar evento de processamento")
    void devePublicarEventoProcessamento() {
        ArgumentCaptor<EventoSubprocessoProcessado> captor = 
            ArgumentCaptor.forClass(EventoSubprocessoProcessado.class);
        
        service.processar(codigo);
        
        verify(publisher).publishEvent(captor.capture());
        assertEquals(codigo, captor.getValue().getCodigoSubprocesso());
    }
}
```

---

## 5. Ferramentas e Recursos

### AssertJ (Recomendado)

```java
// Assertions mais expressivas e encadeáveis
assertThat(processo.getStatus())
    .isEqualTo(StatusProcesso.ATIVO)
    .isNotNull();

assertThat(processo.getSubprocessos())
    .hasSize(3)
    .extracting(Subprocesso::getStatus)
    .containsOnly(StatusSubprocesso.PENDENTE);
```

### JUnit 5 @Nested e @DisplayName

```java
@DisplayName("ProcessoService")
class ProcessoServiceTest {
    
    @Nested
    @DisplayName("Criar Processo")
    class CriarTest {
        // testes agrupados
    }
    
    @Nested
    @DisplayName("Iniciar Processo")
    class IniciarTest {
        // testes agrupados
    }
}
```

---

## 6. Impacto Esperado

Após aplicar estas diretrizes:

- ✅ Testes mais fáceis de debugar (nome do teste indica falha)
- ✅ Refatoração segura (testes não quebram com mudanças internas)
- ✅ Melhor documentação (testes descrevem comportamento)
- ✅ Maior confiança (testes verificam o que realmente importa)
- ✅ Manutenção reduzida (menos testes quebram desnecessariamente)

---

## 7. Testes Candidatos à Refatoração

Baseado na análise do código, aproximadamente **75 testes** podem se beneficiar destas melhorias:

- **35 testes** com múltiplos asserts (ação #26)
- **40 testes** que testam implementação (ação #27)

### Priorização

1. **Alta:** Testes de Facades e Services (comportamento de negócio)
2. **Média:** Testes de Mappers e Validators
3. **Baixa:** Testes de DTOs e Entities (getters/setters já removidos)

---

**Última Atualização:** 2026-01-31  
**Referências:**
- [JUnit 5 Best Practices](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- Martin Fowler - [Unit Testing](https://martinfowler.com/bliki/UnitTest.html)
