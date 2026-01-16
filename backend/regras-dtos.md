# Regras e Convenções de DTOs

> Documento de referência para criação e manutenção de DTOs no projeto SGC.
> Para o plano de execução da refatoração, consulte [plano-dtos.md](./plano-dtos.md).

---

## Taxonomia de DTOs

### Rationale

Os DTOs (Data Transfer Objects) no SGC servem diferentes propósitos dependendo do contexto. 
Sem uma taxonomia clara, surgem problemas como:

1. **DTOs bidirecionais**: Mesma classe usada para entrada E saída, causando validação em responses
2. **Sufixos inconsistentes**: `*Req`, `*Resp`, `*Request`, `*Dto` usados sem critério
3. **Responsabilidades misturadas**: DTOs de API misturados com DTOs internos
4. **Dificuldade de manutenção**: Não fica claro o que pode ser alterado sem quebrar contratos

### Decisões de Design

1. **Sufixos em inglês**: Alinhamento com padrões técnicos (DDD, Clean Architecture, Spring)
2. **Exceção**: `Evento*` permanece em português (14 classes consolidadas, baixo ROI para mudar)
3. **Separação clara de responsabilidades**: Cada categoria tem regras específicas

---

## Categorias

| Categoria | Sufixo | Contexto | Validação? | Imutável? | Estrutura Preferida |
|-----------|--------|----------|------------|-----------|---------------------|
| **Entrada API** | `*Request` | Controller recebe do cliente | ✅ Sim | ✅ Sim | record ou class |
| **Saída API** | `*Response` | Controller retorna ao cliente | ❌ Não | ✅ Sim | record ou class |
| **Comando** | `*Command` | Service→Service (ação interna) | ❌ Não | ✅ Sim | record |
| **Consulta** | `*Query` | Filtros/parâmetros de busca | ❌ Não | ✅ Sim | record |
| **Projeção** | `*View` | Read-model otimizado/reutilizável | ❌ Não | ✅ Sim | record |
| **DTO Interno** | `*Dto` | Mapeamento entre camadas | ❌ Não | Depende | class |
| **Evento** | `Evento*` | Spring ApplicationEvent | — | ✅ Sim | class extends ApplicationEvent |

---

## Diagrama de Fluxo

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENTE (Frontend)                       │
└─────────────────────────────────────────────────────────────────┘
                    │                           ▲
                    │ *Request                  │ *Response
                    │ (com validação)           │ (sem validação)
                    ▼                           │
┌─────────────────────────────────────────────────────────────────┐
│                         CONTROLLER                              │
│   - Recebe: *Request (validado via @Valid)                      │
│   - Retorna: *Response ou *Dto                                  │
└─────────────────────────────────────────────────────────────────┘
                    │                           ▲
                    │ *Command                  │ *Dto / *View
                    │ (ação já validada)        │ (dados internos)
                    ▼                           │
┌─────────────────────────────────────────────────────────────────┐
│                          SERVICE                                │
│   - Recebe: *Command (de outro service) ou *Request (direto)    │
│   - Publica: Evento* (via ApplicationEventPublisher)            │
│   - Consulta: *Query (parâmetros de busca)                      │
│   - Retorna: *Dto, *View, ou Entity                             │
└─────────────────────────────────────────────────────────────────┘
                    │                           ▲
                    │ Entity                    │ Entity / *Query
                    ▼                           │
┌─────────────────────────────────────────────────────────────────┐
│                        REPOSITORY                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## Regras por Categoria

### 1. `*Request` - Entrada de API

**Quando usar:**
- Parâmetro de endpoint REST que recebe dados do cliente
- Sempre com `@Valid` no controller

**Regras:**
- ✅ DEVE ter anotações de validação (`@NotNull`, `@NotBlank`, `@Size`, etc.)
- ✅ DEVE ser imutável quando possível (record ou campos final)
- ✅ DEVE ter nomes de campos compatíveis com o contrato JSON da API
- ❌ NÃO deve conter lógica de negócio
- ❌ NÃO deve ser retornado como resposta

**Exemplo:**
```java
@Builder
public record CriarProcessoRequest(
    @NotBlank(message = "Descrição é obrigatória")
    String descricao,
    
    @NotNull(message = "Tipo é obrigatório")
    TipoProcesso tipo,
    
    @NotEmpty(message = "Pelo menos uma unidade deve ser informada")
    List<Long> unidades
) {}
```

### 2. `*Response` - Saída de API

**Quando usar:**
- Retorno de endpoint REST para o cliente
- Contratos de API bem definidos

**Regras:**
- ✅ DEVE ser imutável (record ou campos final com @Getter apenas)
- ✅ DEVE representar exatamente o que o cliente precisa (nem mais, nem menos)
- ❌ NÃO deve ter anotações de validação
- ❌ NÃO deve expor detalhes internos (IDs de entidades relacionadas sem contexto)

**Exemplo:**
```java
@Builder
public record ProcessoResponse(
    Long codigo,
    String descricao,
    SituacaoProcesso situacao,
    LocalDateTime dataCriacao,
    List<UnidadeResumoResponse> unidades
) {}
```

### 3. `*Command` - Comando Interno

**Quando usar:**
- Chamadas entre services que representam uma AÇÃO
- Quando o service A precisa pedir ao service B para FAZER algo

**Regras:**
- ✅ DEVE ser um record (imutável)
- ✅ DEVE representar uma intenção clara (verbo no nome: Criar, Atualizar, Iniciar)
- ✅ DEVE conter todos os dados necessários para a ação
- ❌ NÃO deve ter validação Bean Validation (já foi validado no Request)
- ❌ NÃO deve ser exposto na API

**Exemplo:**
```java
@Builder
public record CriarAnaliseCommand(
    Long codSubprocesso,
    TipoAnalise tipo,
    TipoAcaoAnalise acao,
    String siglaUnidade,
    String tituloUsuario,
    String motivo,
    String observacoes
) {}
```

### 4. `*Query` - Consulta/Filtros

**Quando usar:**
- Agrupar parâmetros de busca/filtro
- Evitar métodos com muitos parâmetros

**Regras:**
- ✅ DEVE ser um record (imutável)
- ✅ DEVE ter valores default sensatos via builder
- ✅ PODE incluir Pageable ou parâmetros de ordenação
- ❌ NÃO deve ter validação (valores inválidos retornam resultados vazios)

**Exemplo:**
```java
@Builder
public record BuscarProcessosQuery(
    @Nullable String descricao,
    @Nullable SituacaoProcesso situacao,
    @Nullable LocalDate dataInicio,
    @Nullable LocalDate dataFim,
    @Builder.Default Pageable pageable = Pageable.unpaged()
) {}
```

### 5. `*View` - Projeção Reutilizável

**Quando usar:**
- Read-model otimizado para leitura
- Projeções usadas em múltiplos endpoints
- Dados agregados/calculados

**Regras:**
- ✅ DEVE ser um record (imutável)
- ✅ DEVE ser otimizado para o caso de uso de leitura
- ✅ PODE conter campos calculados/derivados
- ❌ NÃO deve ter validação

**Exemplo:**
```java
@Builder  
public record ProcessoResumoView(
    Long codigo,
    String descricao,
    String situacaoLabel,
    int totalSubprocessos,
    int subprocessosConcluidos,
    double percentualConclusao
) {}
```

### 6. `*Dto` - DTO Interno

**Quando usar:**
- Mapeamento entre camadas (Entity ↔ representação)
- Casos onde `*Response` seria overhead
- DTOs legados em processo de migração

**Regras:**
- ✅ PODE ser class ou record
- ✅ PODE ter getters/setters se necessário para frameworks (MapStruct)
- ❌ NÃO deve ter validação Bean Validation
- ⚠️ Preferir `*Response` para novos endpoints de API

**Exemplo:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapaDto {
    private Long codigo;
    private String nome;
    private List<AtividadeDto> atividades;
}
```

### 7. `Evento*` - Evento de Domínio

**Quando usar:**
- Notificações assíncronas entre componentes
- Desacoplamento de side-effects (enviar email, auditoria, etc.)

**Regras:**
- ✅ DEVE estender `ApplicationEvent` (Spring)
- ✅ DEVE ser imutável
- ✅ DEVE conter apenas IDs ou dados mínimos necessários
- ✅ DEVE usar prefixo `Evento` (padrão consolidado em português)
- ❌ NÃO deve conter entidades completas (apenas IDs)

**Exemplo:**
```java
@Getter
public class EventoProcessoCriado extends ApplicationEvent {
    private final Long codProcesso;

    public EventoProcessoCriado(Object fonte, Long codProcesso) {
        super(fonte);
        this.codProcesso = codProcesso;
    }
}
```

---

## Decisões Pragmáticas

1. **`*Dto` continua válido** - Para mapeamento interno e respostas simples onde criar `*Response` seria overhead desnecessário.

2. **`*Response` é preferido para novos endpoints** - Contratos de API mais claros e evita validação acidental.

3. **`*Query` é opcional** - Para métodos com até 3 parâmetros, parâmetros soltos são aceitáveis. Use `*Query` quando passar de 3-4 parâmetros.

4. **`*View` para projeções reutilizáveis** - Se uma projeção é usada em apenas um endpoint, `*Response` é suficiente.

5. **`Evento*` permanece em português** - 14 classes consolidadas, baixo ROI para renomear. Novos eventos podem seguir o padrão existente.

---

## Convenções Lombok

### 🚫 Proibição de `@Data`

A anotação `@Data` está **PROIBIDA** em todo o projeto. Motivos:

1. **Gera setters desnecessários** → Quebra imutabilidade
2. **Gera equals/hashCode automáticos** → Problemático com JPA (lazy loading)
3. **Gera toString com todos os campos** → Expõe dados sensíveis em logs
4. **Redundante com @Builder** → Se tem builder, não precisa de setters

### Tabela de Anotações

| Tipo | Estrutura | Anotações Lombok | Notas |
|------|-----------|------------------|-------|
| **Request** (class) | class | `@Getter @Builder @AllArgsConstructor` | Imutável, sem setters |
| **Request** (record) | record | `@Builder` | ✅ Preferido |
| **Response** (class) | class | `@Getter @Builder @AllArgsConstructor` | Imutável, sem setters |
| **Response** (record) | record | `@Builder` | ✅ Preferido |
| **Command** | record | `@Builder` | Sempre record |
| **Query** | record | `@Builder` | Sempre record |
| **View** | record | `@Builder` | Sempre record |
| **Dto** | class | `@Getter @Builder @AllArgsConstructor` | Imutável, sem setters |
| **Dto** | record | `@Builder` | ✅ Preferido para novos DTOs |
| **Evento** | class | `@Getter` | Imutável, extends ApplicationEvent |
| **Configuração** | record | Nenhuma | ✅ Record com constructor binding |

### Exemplos

```java
// ✅ CORRETO - Request imutável
@Getter
@Builder
@AllArgsConstructor
public class CriarProcessoRequest {
    @NotBlank private final String descricao;
    @NotNull private final TipoProcesso tipo;
}

// ✅ CORRETO - Record (preferido)
@Builder
public record CriarProcessoRequest(
    @NotBlank String descricao,
    @NotNull TipoProcesso tipo
) {}

// ✅ CORRETO - Configuração Spring com Record (constructor binding)
@ConfigurationProperties(prefix = "aplicacao.cors")
public record ConfigCors(
    List<String> allowedOrigins,
    List<String> allowedMethods,
    boolean allowCredentials
) {
    public ConfigCors {
        // Valores default no compact constructor
        allowedOrigins = allowedOrigins != null ? allowedOrigins : List.of("http://localhost:5173");
        allowedMethods = allowedMethods != null ? allowedMethods : List.of("GET", "POST", "PUT", "DELETE");
    }
}

// ❌ PROIBIDO - @Data em qualquer contexto
@Data
public class QualquerClasse { ... }
```

---

## Regras de Ouro

1. **Nunca** exponha Entidades JPA diretamente na API
2. **Sempre** use `*Request` para entrada de dados do cliente
3. **Prefira** `*Response` para saída de dados para o cliente
4. **Validação** Bean Validation apenas em `*Request`
5. **🚫 `@Data` está PROIBIDO** em todo o projeto
6. **`@Getter` + `@Builder`** para classes imutáveis
7. **Records** são a estrutura preferida para novos DTOs e configurações
8. **`*Query`** quando método tem mais de 3-4 parâmetros de filtro
9. **`*View`** para projeções reutilizadas em múltiplos endpoints
10. **`Evento*`** permanece em português (padrão consolidado)

