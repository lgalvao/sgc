# 🧪 Test Utilities - SGC Backend

Este pacote contém utilitários para facilitar a criação de testes, reduzindo código duplicado e mocks desnecessários.

## 📋 Builders Disponíveis

### UnidadeTestBuilder

Cria objetos `Unidade` para testes sem necessidade de mocks.

**Padrões pré-configurados:**

```java
// Assessoria (ASSESSORIA_11)
Unidade assessoria = UnidadeTestBuilder.assessoria().build();

// Seção (SECAO_111)
Unidade secao = UnidadeTestBuilder.secao().build();

// Coordenadoria (COORD_11)
Unidade coord = UnidadeTestBuilder.coordenadoria().build();

// Secretaria (SECRETARIA_1)
Unidade sec = UnidadeTestBuilder.secretaria().build();
```

**Customização:**

```java
Unidade unidadeCustom = UnidadeTestBuilder.umaDe()
    .comCodigo("CUSTOM_01")
    .comSigla("CUSTOM_01")
    .comNome("Unidade Customizada")
    .comTipo(TipoUnidade.ASSESSORIA)
    .comSuperior("SECRETARIA_2")
    .build();
```

---

### UsuarioTestBuilder

Cria objetos `Usuario` para testes sem necessidade de mocks.

**Padrões pré-configurados:**

```java
// Admin (191919)
Usuario admin = UsuarioTestBuilder.admin().build();

// Gestor (222222)
Usuario gestor = UsuarioTestBuilder.gestor().build();

// Chefe de Unidade (555555)
Usuario chefe = UsuarioTestBuilder.chefeUnidade().build();

// Servidor (666666)
Usuario servidor = UsuarioTestBuilder.servidor().build();
```

**Customização:**

```java
Usuario usuarioCustom = UsuarioTestBuilder.umDe()
    .comTitulo("123456")
    .comNome("João Silva")
    .comUnidade("ASSESSORIA_22")
    .comPerfil(Perfil.GESTOR)
    .comPerfil(Perfil.CHEFE_UNIDADE) // Múltiplos perfis
    .build();

// Ou com múltiplos perfis de uma vez:
Usuario multiPerfil = UsuarioTestBuilder.umDe()
    .comPerfis(Perfil.ADMIN, Perfil.GESTOR)
    .build();
```

---

## 🎯 Benefícios

### ❌ ANTES (Com Mocks)

```java
@Test
void deveValidarHierarquia() {
    // Setup complexo com mocks
    Unidade unidade = mock(Unidade.class);
    when(unidade.getCodigo()).thenReturn("ASSESSORIA_11");
    when(unidade.getSigla()).thenReturn("ASSESSORIA_11");
    when(unidade.getTipo()).thenReturn(TipoUnidade.ASSESSORIA);
    when(unidade.getCodigoUnidadeSuperior()).thenReturn("SECRETARIA_1");
    
    Usuario usuario = mock(Usuario.class);
    when(usuario.getTitulo()).thenReturn("191919");
    when(usuario.getNome()).thenReturn("Admin Teste");
    when(usuario.getCodigoUnidade()).thenReturn("SEDOC");
    Set<Perfil> perfis = new HashSet<>();
    perfis.add(Perfil.ADMIN);
    when(usuario.getPerfis()).thenReturn(perfis);
    
    // Teste real...
    boolean resultado = service.validar(usuario, unidade);
    
    assertTrue(resultado);
}
```

**Problemas:**

- 15 linhas de setup
- Frágil (se adicionar campo em Usuario/Unidade, quebra)
- Difícil de ler e manter

### ✅ DEPOIS (Com Builders)

```java
@Test
void deveValidarHierarquia() {
    // Setup conciso e legível
    Unidade unidade = UnidadeTestBuilder.assessoria().build();
    Usuario usuario = UsuarioTestBuilder.admin().build();
    
    // Teste real...
    boolean resultado = service.validar(usuario, unidade);
    
    assertTrue(resultado);
}
```

**Benefícios:**

- 2 linhas de setup (87% menos código)
- Robusto (builders adaptam automaticamente a mudanças)
- Legível e autodocumentado

---

## 📊 Quando Usar

### ✅ USE Builders Quando:

1. **Teste de Lógica de Negócio**: Validações, cálculos, regras
2. **Objetos Simples**: DTOs, Entities, Value Objects
3. **Dados Imutáveis**: Records, configurações
4. **Setup Repetitivo**: Mesmo objeto usado em múltiplos testes

### ⚠️ USE Mocks Quando:

1. **Dependências Externas**: Repositórios, APIs REST, bancos de dados
2. **Comportamento Complexo**: Workflows, services com múltiplas dependências
3. **Verificação de Interações**: Precisa verificar se método foi chamado

---

## 🔄 Guia de Migração

### Padrão 1: Mock de Value Objects → Builder

**❌ ANTES:**

```java
Usuario usuario = mock(Usuario.class);
when(usuario.getTitulo()).thenReturn("191919");
when(usuario.getNome()).thenReturn("Admin");
when(usuario.getCodigoUnidade()).thenReturn("SEDOC");
when(usuario.getPerfis()).thenReturn(Set.of(Perfil.ADMIN));
```

**✅ DEPOIS:**

```java
Usuario usuario = UsuarioTestBuilder.admin().build();
```

**Redução:** 5 linhas → 1 linha (80% menos código)

---

### Padrão 2: Setup Complexo → Builders Encadeados

**❌ ANTES:**

```java
Unidade unidade1 = mock(Unidade.class);
when(unidade1.getCodigo()).thenReturn("ASSESSORIA_11");
// ... 4 linhas mais

Unidade unidade2 = mock(Unidade.class);
when(unidade2.getCodigo()).thenReturn("SECAO_111");
// ... 4 linhas mais

Usuario usuario = mock(Usuario.class);
when(usuario.getTitulo()).thenReturn("191919");
// ... 4 linhas mais
```

**✅ DEPOIS:**

```java
Unidade unidade1 = UnidadeTestBuilder.assessoria().build();
Unidade unidade2 = UnidadeTestBuilder.secao().build();
Usuario usuario = UsuarioTestBuilder.admin().build();
```

**Redução:** 18 linhas → 3 linhas (83% menos código)

---

## 📝 Checklist de Migração

### Por Teste

- [ ] Identificar mocks de Value Objects (Usuario, Unidade)
- [ ] Substituir por builder apropriado
- [ ] Remover imports de Mockito não utilizados
- [ ] Verificar se teste ainda passa
- [ ] Remover setup methods se ficaram vazios

### Por Arquivo de Teste

1. Buscar padrões `mock(Usuario.class)` e `mock(Unidade.class)`
2. Substituir por `UsuarioTestBuilder` e `UnidadeTestBuilder`
3. Simplificar `@Mock` fields se não forem mais necessários
4. Executar teste: `./gradlew :backend:test --tests NomeDoTeste`

---

## 🎯 Priorização

Migrar nesta ordem:

1. **Alta prioridade**: Testes com 5+ mocks de value objects
    - SubprocessoFacadeComplementaryTest (22 mocks)
    - SubprocessoCadastroWorkflowServiceTest (13 mocks)
    - SubprocessoMapaWorkflowServiceTest (12 mocks)

2. **Média prioridade**: Testes com 3-4 mocks de value objects
    - SubprocessoFacadeTest (12 mocks)
    - AtividadeFacadeTest (6 mocks)

3. **Baixa prioridade**: Testes com 1-2 mocks de value objects

---

## 📚 Referências

- **Padrão Builder**: [Effective Java - Item 2](https://www.oreilly.com/library/view/effective-java/9780134686097/)
- **Test Data Builders**: [Growing Object-Oriented Software](http://www.growing-object-oriented-software.com/)
- **Por que evitar over-mocking
  **: [Don't Mock What You Don't Own](https://github.com/testdouble/contributing-tests/wiki/Don't-mock-what-you-don't-own)
