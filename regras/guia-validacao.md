# Guia de Validação - SGC

Este documento define o padrão de validação a ser seguido no sistema SGC.

## Arquitetura de Validação

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Controller │────▶│  Bean Validation │────▶│     Service     │
│   @Valid    │     │   (DTOs/Reqs)    │     │ Regras Negócio  │
└─────────────┘     └──────────────────┘     └─────────────────┘
       │                    │                        │
       ▼                    ▼                        ▼
   HTTP 400            HTTP 400/422              HTTP 422
 (Sintaxe JSON)    (Campos Inválidos)     (Regras de Negócio)
```

## Camadas de Validação

### 1. Controller (Entrada)

- Use `@Valid` no parâmetro `@RequestBody`
- **NÃO** faça validação manual de campos obrigatórios

```java
@PostMapping
public ResponseEntity<ProcessoDto> criar(@Valid @RequestBody CriarProcessoReq req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
}
```

### 2. DTOs/Requests (Bean Validation)

**OBRIGATÓRIO** em todo DTO de entrada:

| Anotação | Uso | Mensagem Obrigatória |
|----------|-----|---------------------|
| `@NotNull` | Campos que não podem ser nulos | Sim |
| `@NotBlank` | Strings obrigatórias | Sim |
| `@NotEmpty` | Listas obrigatórias | Sim |
| `@Size` | Limites de tamanho | Sim |
| `@Future` | Datas futuras | Opcional |
| `@Valid` | Validação em cascata | N/A |

**Exemplo correto:**

```java
public class CriarProcessoReq {
    @NotBlank(message = "Preencha a descrição")
    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @NotNull(message = "A data limite é obrigatória")
    @Future(message = "A data limite deve ser futura")
    private LocalDateTime dataLimite;

    @NotEmpty(message = "Pelo menos uma unidade deve ser selecionada")
    private List<Long> unidades;
}
```

### 3. Service (Regras de Negócio)

Valide apenas regras que **não podem** ser expressas via anotações:

| Tipo | Exceção | HTTP |
|------|---------|------|
| Estado inválido | `ErroProcessoEmSituacaoInvalida` | 422 |
| Entidade não encontrada | `ErroEntidadeNaoEncontrada` | 404 |
| Regra de negócio violada | `ErroValidacao` | 422 |
| Acesso negado | `ErroAccessoNegado` | 403 |

```java
public void iniciar(Long codigo) {
    Processo p = buscarPorCodigo(codigo); // Pode lançar 404
    
    if (p.getSituacao() != CRIADO) {
        throw new ErroProcessoEmSituacaoInvalida(
            "Apenas processos em estado CRIADO podem ser iniciados");
    }
}
```

## Regras de Mensagens

1. **Idioma:** Todas em **português brasileiro**
2. **Tom:** Direto e orientado à ação ("Preencha...", "Selecione...")
3. **Contexto:** Incluir nome do campo quando aplicável

**Correto:**
```java
@NotBlank(message = "A justificativa é obrigatória")
```

**Incorreto:**
```java
@NotBlank  // Sem mensagem - usará inglês padrão
@NotBlank(message = "must not be blank")  // Inglês
```

## Limites de Tamanho (Referência SQL)

| Campo | Max | Uso |
|-------|-----|-----|
| `descricao` | 255 | Descrições gerais |
| `observacoes` | 500 | Campos de observação |
| `justificativa` | 500 | Campos de justificativa |
| `motivo` | 200 | Campo motivo |
| `sugestoes` | 1000 | Sugestões de validação |

## O Que NÃO Fazer

```java
// ❌ ERRADO: Validação manual duplicando Bean Validation
public ProcessoDto criar(CriarProcessoReq req) {
    if (req.getDescricao() == null || req.getDescricao().isBlank()) {
        throw new ConstraintViolationException("Descrição obrigatória", null);
    }
    // ...
}

// ❌ ERRADO: @NotBlank sem mensagem
@NotBlank
private String nome;

// ❌ ERRADO: Mensagem em inglês
@NotNull(message = "Field is required")
private Long codigo;
```

## Testes

- **Controller Tests:** Validam que `@Valid` está presente e retorna 400
- **Service Tests:** Validam apenas regras de negócio (não testar campos obrigatórios)

```java
// ✓ Correto: Teste de regra de negócio no Service
@Test
void deveFalharSeProcessoNaoEstiverCriado() {
    assertThatThrownBy(() -> facade.iniciar(id))
        .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
}
```
