# Backlog de Melhorias de Testabilidade

Este documento lista oportunidades de refatoração identificadas durante a análise de cobertura de testes.
A implementação deve ser feita de forma consistente em todo o codebase, não pontualmente.

## Data de Criação

2026-01-06

## Métricas Atuais

- **Cobertura de Linhas**: 98.75%
- **Cobertura de Branches**: 87.03%
- **Total de Testes**: 1007

---

## 1. Extrair Validações para Métodos Dedicados

### Problema

Validações inline dificultam testes unitários e aumentam a complexidade ciclomática.

### Classes Afetadas

- `AtividadeFacade` (linhas 37-40)
- `GerenciadorJwt` (linhas 49-50)
- `SubprocessoFactory` (validações de criação)
- Diversos controllers com guards de autenticação

### Padrão Proposto

```java
// Antes
public Result metodo(Input input) {
    if (input == null || input.getCampo().isBlank()) {
        throw new ErroValidacao("...");
    }
    // lógica
}

// Depois
public Result metodo(Input input) {
    validarInput(input);
    // lógica
}

void validarInput(Input input) {  // package-private para testes
    if (input == null || input.getCampo().isBlank()) {
        throw new ErroValidacao("...");
    }
}
```

### Esforço Estimado

Médio - Refatoração mecânica, baixo risco.

---

## 2. Strategy Pattern para Lógica Condicional por Tipo

### Problema

Chains de if/else por tipo de entidade são difíceis de testar e violam Open/Closed.

### Classes Afetadas

- `ProcessoController.iniciar()` (linhas 159-167) - 3 branches por TipoProcesso
- `EventoProcessoListener` - lógica diferente por TipoUnidade
- `TipoTransicao` - múltiplos branches

### Padrão Proposto

```java
// Antes
if (tipo == TIPO_A) {
    return metodoA();
} else if (tipo == TIPO_B) {
    return metodoB();
}

// Depois
private final Map<Tipo, Supplier<Result>> handlers = Map.of(
    TIPO_A, this::metodoA,
    TIPO_B, this::metodoB
);

public Result processar(Tipo tipo) {
    return Optional.ofNullable(handlers.get(tipo))
        .map(Supplier::get)
        .orElseThrow(() -> new ErroTipoDesconhecido(tipo));
}
```

### Esforço Estimado

Alto - Requer análise de impacto e testes de regressão.

---

## 3. Injeção de Dependências para Ambiente/Tempo

### Problema

Código que verifica ambiente ou usa tempo atual é difícil de testar.

### Classes Afetadas

- `GerenciadorJwt.verificarSegurancaChave()` - verifica perfil de ambiente
- `FiltroAutenticacaoMock` - comportamento diferente por ambiente
- Classes que usam `LocalDateTime.now()` ou `Instant.now()`

### Padrão Proposto

```java
// Interface para abstração de ambiente
public interface AmbienteInfo {
    boolean isProducao();
    boolean isDesenvolvimento();
}

// Interface para abstração de tempo (já comum no projeto)
public interface RelogioService {
    Instant agora();
    LocalDateTime agoraLocal();
}
```

### Esforço Estimado

Médio - Criar interfaces e atualizar injeções.

---

## 4. Separar Guards de Segurança via AOP

### Problema

Guards de autenticação/autorização repetidos em métodos poluem a lógica de negócio.

### Classes Afetadas

- `AtividadeFacade` - verificação de usuário autenticado
- `UsuarioController` - verificação de IP
- Controllers com `@PreAuthorize`

### Padrão Proposto

```java
// Annotation customizada
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuthentication {
    String message() default "Usuário não autenticado";
}

// Aspect para interceptar
@Aspect
@Component
public class AuthenticationAspect {
    @Before("@annotation(RequireAuthentication)")
    public void verificarAutenticacao(JoinPoint jp) {
        // lógica de verificação
    }
}
```

### Esforço Estimado

Alto - Requer conhecimento de AOP e testes de regressão.

---

## 5. Factory Methods para Responses

### Problema

Builders inline dificultam testes e reutilização.

### Classes Afetadas

- `AtividadeFacade.criarRespostaOperacao()`
- Controllers que montam ResponseEntity complexos

### Padrão Proposto

Criar classes `*ResponseFactory` com métodos estáticos para construção de responses.

### Esforço Estimado

Baixo - Refatoração mecânica.

---

## Priorização Sugerida

| # | Melhoria | Impacto em Cobertura | Esforço | Prioridade |
|---|----------|---------------------|---------|------------|
| 1 | Extrair Validações | +2-3% branches | Médio | Alta |
| 2 | Strategy Pattern | +1-2% branches | Alto | Média |
| 5 | Factory Methods | +0.5% branches | Baixo | Média |
| 3 | Injeção Ambiente/Tempo | +1% branches | Médio | Baixa |
| 4 | Guards via AOP | +0.5% branches | Alto | Baixa |

---

## Notas

- A meta de 90% de branch coverage pode ser atingida com as melhorias 1 e 2.
- Todas as refatorações devem ser acompanhadas de testes de regressão.
- Considerar implementar em uma sprint dedicada a débito técnico.
