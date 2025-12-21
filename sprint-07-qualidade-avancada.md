# Sprint 7: Qualidade Avançada

**Baseado em:** `analise-junit-nova.md` - Onda 7

## Contexto do Projeto SGC

### Estado Atual após Sprints 0-6
Neste ponto, assumindo que os sprints anteriores foram executados:
- ✅ Testes boilerplate removidos (Sprint 1)
- ✅ `LENIENT` eliminado (Sprint 2)
- ✅ Fixtures/builders criados (Sprint 3)
- ✅ Padronização aplicada (Sprint 4)
- ✅ Testes de integração isolados (Sprint 5)
- ✅ Cobertura visível e monitorada (Sprint 6)

### Foco deste Sprint: Robustez Real
Agora elevamos a qualidade de **como** testamos:
- **Parametrização**: Evitar duplicação de cenários similares
- **Asserções completas**: Verificar mais que apenas "não deu erro"
- **Testes de eventos**: Validar comportamento assíncrono/reativo

### Tecnologias e Padrões

#### 1. Testes Parametrizados (JUnit 5)
```java
@ParameterizedTest
@CsvSource({
    "CADASTRO, true",
    "VALIDACAO, true",
    "FINALIZADO, false"
})
@DisplayName("Deve permitir edição apenas para situações específicas")
void devePermitirEdicaoApenasSituacoesEspecificas(Situacao situacao, boolean esperado) {
    // Arrange
    subprocesso.setSituacao(situacao);
    
    // Act
    boolean resultado = service.podeEditar(subprocesso);
    
    // Assert
    assertThat(resultado).isEqualTo(esperado);
}
```

#### 2. Asserções de Exceção Completas
```java
// ❌ ANTES (incompleto)
@Test
void deveLancarExcecao() {
    assertThatThrownBy(() -> service.criar(null))
        .isInstanceOf(ErroNegocio.class);
}

// ✅ DEPOIS (completo)
@Test
@DisplayName("Deve lançar ErroNegocio com mensagem específica quando dados nulos")
void deveLancarErroNegocioQuandoDadosNulos() {
    assertThatThrownBy(() -> service.criar(null))
        .isInstanceOf(ErroNegocio.class)
        .hasMessageContaining("Dados não podem ser nulos")
        .hasNoCause();
}
```

#### 3. Testes de Eventos (Spring Events)
O SGC usa eventos de domínio (`ApplicationEventPublisher`):
- `ProcessoIniciadoEvento`
- Módulos `notificacao` e `alerta` escutam eventos

```java
@SpringBootTest
class ProcessoEventoIntegrationTest {
    
    @Autowired ProcessoService processoService;
    @Autowired ApplicationEventPublisher eventPublisher;
    
    @MockBean NotificacaoListener notificacaoListener;
    
    @Test
    @DisplayName("Deve publicar evento quando processo iniciado")
    void devePublicarEventoQuandoProcessoIniciado() {
        // Arrange
        var processo = ProcessoFixture.novo();
        
        // Act
        processoService.iniciar(processo.getCodigo());
        
        // Assert - Verificar que evento foi capturado
        verify(notificacaoListener, times(1))
            .onProcessoIniciado(argThat(evento -> 
                evento.getProcessoCodigo().equals(processo.getCodigo())
            ));
    }
}
```

### Módulos com Eventos no SGC
- **processo**: Publica eventos de ciclo de vida
- **notificacao**: Escuta eventos e envia e-mails
- **alerta**: Escuta eventos e cria alertas na UI

## Objetivo
Elevar a robustez real dos testes, verificando comportamentos complexos e reduzindo repetição.

## Tarefas

### 1. Parametrizar Testes Repetitivos
- Identificar testes com cenários similares (muitos if/else ou testes quase idênticos).
- Refatorar usando `@ParameterizedTest` com `@ValueSource`, `@CsvSource`, `@MethodSource`, etc.
- Aplicar em testes de validação, regras de negócio com múltiplas entradas.

### 2. Completar Asserções de Exceção
- Localizar testes que apenas verificam `isInstanceOf`.
- Adicionar verificação de mensagem (`hasMessageContaining`).
- Verificar causa quando relevante (`hasCause`, `hasNoCause`).
- Para exceções de negócio, verificar campos customizados.

### 3. Testar Efeitos Colaterais de Eventos
- Identificar services que publicam eventos (`ApplicationEventPublisher`).
- Criar testes de integração que verificam listeners.
- Usar `@MockBean` para listeners ou verificar efeitos (banco, filas).
- Considerar usar `@EventListener` de teste para capturar eventos.

### 4. Melhorar Asserções de Estado
- Não apenas verificar que método foi chamado.
- Verificar estado final consistente (invariantes).
- Usar `assertAll` para múltiplas verificações:

```java
assertAll(
    () -> assertThat(resultado.getNome()).isEqualTo("Esperado"),
    () -> assertThat(resultado.getSituacao()).isEqualTo(ATIVO),
    () -> assertThat(resultado.getDataCriacao()).isNotNull()
);
```

## Comandos de Verificação

### Identificar testes candidatos a parametrização
```bash
# Procurar testes com nomes similares que indicam parametrização
grep -R "@Test" backend/src/test --include="*.java" -A 2 | grep -E "void deve.*Quando(Invalido|Valido|Nulo)"
```

### Localizar asserções de exceção incompletas
```bash
# Procurar apenas isInstanceOf sem hasMessage
grep -R "isInstanceOf" backend/src/test --include="*.java" -A 1 | grep -v "hasMessage"
```

### Identificar publicadores de eventos
```bash
# Procurar uso de ApplicationEventPublisher
grep -R "eventPublisher.publishEvent" backend/src/main --include="*.java"
```

### Verificar listeners de eventos
```bash
# Procurar @EventListener
grep -R "@EventListener\|@TransactionalEventListener" backend/src/main --include="*.java"
```

### Contar uso de @ParameterizedTest
```bash
grep -R "@ParameterizedTest" backend/src/test --include="*.java" | wc -l
```

### Executar testes
```bash
./gradlew :backend:test
```

## Exemplos de Refatoração

### Parametrização: Antes e Depois

```java
// ❌ ANTES - Testes duplicados
@Test
void deveRejeitarNomeVazio() {
    assertThatThrownBy(() -> service.criar(""))
        .isInstanceOf(ErroValidacao.class);
}

@Test
void deveRejeitarNomeNulo() {
    assertThatThrownBy(() -> service.criar(null))
        .isInstanceOf(ErroValidacao.class);
}

@Test
void deveRejeitarNomeMuitoGrande() {
    assertThatThrownBy(() -> service.criar("a".repeat(300)))
        .isInstanceOf(ErroValidacao.class);
}

// ✅ DEPOIS - Parametrizado
@ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {"   ", "a".repeat(300)})
@DisplayName("Deve rejeitar nomes inválidos")
void deveRejeitarNomesInvalidos(String nomeInvalido) {
    assertThatThrownBy(() -> service.criar(nomeInvalido))
        .isInstanceOf(ErroValidacao.class)
        .hasMessageContaining("Nome inválido");
}
```

### Asserções de Estado: Antes e Depois

```java
// ❌ ANTES - Asserção incompleta
@Test
void deveCriarProcesso() {
    var resultado = service.criar(dados);
    assertThat(resultado).isNotNull();
    verify(repo).save(any());
}

// ✅ DEPOIS - Asserção completa
@Test
@DisplayName("Deve criar processo com dados corretos e situação inicial")
void deveCriarProcessoComDadosCorretos() {
    // Arrange
    var dados = ProcessoFixture.dadosValidos();
    
    // Act
    var resultado = service.criar(dados);
    
    // Assert
    assertAll(
        () -> assertThat(resultado.getCodigo()).isNotNull(),
        () -> assertThat(resultado.getNome()).isEqualTo(dados.getNome()),
        () -> assertThat(resultado.getSituacao()).isEqualTo(PLANEJAMENTO),
        () -> assertThat(resultado.getDataCriacao()).isNotNull(),
        () -> assertThat(resultado.getCriador()).isEqualTo(usuarioLogado)
    );
    verify(repo).save(argThat(p -> 
        p.getNome().equals(dados.getNome()) && 
        p.getSituacao() == PLANEJAMENTO
    ));
}
```

## Ferramentas Úteis

### JUnit 5 - Testes Parametrizados
- `@ValueSource` - valores simples
- `@EnumSource` - valores de enum
- `@CsvSource` - múltiplos parâmetros CSV inline
- `@CsvFileSource` - múltiplos parâmetros de arquivo
- `@MethodSource` - método que retorna Stream/Collection
- `@ArgumentsSource` - fonte customizada

### AssertJ - Asserções Fluentes
- `assertThat().isInstanceOf().hasMessageContaining()`
- `assertThatThrownBy().hasNoCause()`
- `assertAll()` - múltiplas asserções
- `extracting()` - verificar listas/coleções

### Awaitility - Comportamento Assíncrono
Já disponível no projeto (`testImplementation("org.awaitility:awaitility")`):
```java
await().atMost(2, SECONDS)
    .untilAsserted(() -> {
        verify(listener).onEvento(any());
    });
```

## Critérios de Aceite
- `./gradlew :backend:test` passa sem erros.
- Menos código duplicado em testes de cenários similares (uso de `@ParameterizedTest`).
- Asserções de exceção mais rigorosas (mensagem, causa).
- Maior verificação de comportamento real do sistema.
- Testes de efeitos colaterais de eventos implementados para módulos reativos.
- Redução de pelo menos 30% em testes duplicados através de parametrização.

---

## Diretrizes para agentes de IA (Regras de Ouro)

1. **PRs Pequenos:** Um tema por PR.
2. **Critérios Universais de Aceite:**
   - `./gradlew test` (ou `mvn test`) passa.
   - Não aumentar flakiness (nenhum teste novo com `Thread.sleep`).
   - Não reintroduzir `Strictness.LENIENT`.
   - Sem hardcode em integração sem criação explícita.
3. **Não refatorar produção** a menos que estritamente necessário para o teste.

## Guia de Estilo (Obrigatório)

### Estrutura AAA
```java
@Test
@DisplayName("Deve criar processo quando dados válidos")
void deveCriarProcessoQuandoDadosValidos() {
    // Arrange
    // Act
    // Assert
}
```

### Nomenclatura
- **Método:** `deve{Acao}Quando{Condicao}`
- **Variáveis:** Português, descritivas.
- **Agrupamento:** `@Nested` por feature/fluxo.

### Mockito
- **Proibido:** `Strictness.LENIENT` (padrão).
- **Preferência:** Stubs locais.

## Checklist de Revisão

- [ ] Testes passam local/CI.
- [ ] `LENIENT` não aparece no diff.
- [ ] Não houve adição de `Thread.sleep`.
- [ ] Integração não depende de seed global sem setup explícito.
- [ ] PR descreve comandos executados e métricas simples (grep/contagem de arquivos).
