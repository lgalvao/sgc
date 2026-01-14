# Plano de Padronização de DTOs

> Documento de acompanhamento da refatoração de DTOs do projeto SGC.
> Última atualização: 2026-01-14T21:30

## Status Geral

| Fase | Descrição | Status |
|------|-----------|--------|
| 0 | Análise Profunda e Documentação | ✅ Completo |
| 1 | Eliminar duplicatas Req/Request | ✅ Completo |
| 2 | Padronizar anotações Lombok | ⏳ Pendente |
| 3 | Separar DTOs bidirecionais | ⏳ Pendente |
| 4 | Remover validação de Response DTOs | ⏳ Pendente |
| 5 | Converter para records | ⏳ Pendente |

---

## ✅ Fase 1 COMPLETA - Eliminar Duplicatas Req/Request

### Resumo da Execução

**Impacto Total:**
- ✅ 7 arquivos deletados (duplicatas e obsoletos)
- ✅ 14 DTOs renomeados (Req → Request)
- ✅ 60+ arquivos atualizados (controllers, services, tests)
- ✅ 174 testes afetados - todos passando (100%)
- ✅ Build compilando com sucesso

### Por Módulo

| Módulo | DTOs Refatorados | Status |
|--------|------------------|--------|
| **processo** | 4 DTOs deletados | ✅ |
| **analise** | 2 deletados, 2 mantidos | ✅ |
| **subprocesso** | 11 renomeados, 1 deletado | ✅ |
| **seguranca** | 2 renomeados | ✅ |
| **organizacao** | 1 renomeado | ✅ |

### Arquivos Mantidos (Padrão Final)

**Request DTOs (API Boundary):**
- `CriarProcessoRequest`, `AtualizarProcessoRequest`, `IniciarProcessoRequest`, `EnviarLembreteRequest`
- `CriarAnaliseRequest`
- `AceitarCadastroRequest`, `ApresentarSugestoesRequest`, `CompetenciaRequest`, `DevolverCadastroRequest`, `DevolverValidacaoRequest`, `DisponibilizarMapaRequest`, `HomologarCadastroRequest`, `ImportarAtividadesRequest`, `ReabrirProcessoRequest`, `SalvarAjustesRequest`, `SubmeterMapaAjustadoRequest`
- `AutenticarRequest`, `EntrarRequest`
- `CriarAtribuicaoTemporariaRequest`

**Command DTOs (Internal):**
- `CriarAnaliseCommand`

---

## Problemas Remanescentes

### 1. **INCONSISTÊNCIA DE LOMBOK** (Prioridade Alta 🔴)

**Problema:** DTOs usam 3 padrões diferentes de Lombok.

**Padrões Encontrados:**
```java
// Padrão A - Correto (@Data)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CriarProcessoRequest { }

// Padrão B - Incorreto (@Getter/@Setter)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProcessoDetalheDto { }

// Padrão C - Moderno (Record)
@Builder
public record CriarAnaliseRequest(...) {}
```

**DTOs a corrigir:** ~15-20 DTOs usando `@Getter/@Setter` devem migrar para `@Data`

### 2. **DTOS BIDIRECIONAIS** (Prioridade Média 🟠)

**Problema:** DTOs usados para input E output violam separação de concerns.

**Exemplo Crítico - SubprocessoDto:**
```java
@Getter @Setter
public class SubprocessoDto {
    @NotNull(message = "...") // ❌ Validação em DTO de resposta!
    private Long codProcesso;
}
```

**Solução Necessária:**
- Separar em `SubprocessoRequest` (com validação) + `SubprocessoResponse` (sem validação)
- Outros: `ProcessoDto`, `UsuarioDto`, `UnidadeDto`

### 3. **VALIDAÇÃO EM RESPONSE DTOS** (Prioridade Média 🟠)

**Problema:** DTOs de resposta têm anotações de validação Bean Validation.

**Regra:** Apenas `*Request` deve ter `@NotNull`, `@NotBlank`, `@Size`, etc.

### 4. **SUBUSO DE *Response** (Prioridade Baixa 🟡)

**Problema:** Maioria das respostas usa `*Dto` genérico, não `*Response`.

**Ideal:**
- GET retorna `*Response`
- POST (create) retorna `*Response`
- `*Dto` apenas para mapeamento interno

---

## Fase 2: Padronizar Anotações Lombok

### Padrões Alvo

**Request DTOs (Classes):**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExemploRequest {
    @NotNull(message = "Campo obrigatório")
    private String campo;
}
```

**Request DTOs (Records):**
```java
@Builder
public record ExemploRequest(
    @NotNull(message = "Campo obrigatório")
    String campo
) {}
```

**Response DTOs:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExemploResponse {
    private Long codigo;
    private String descricao;
    // SEM validação!
}
```

### Checklist

- [ ] Identificar todos DTOs com `@Getter/@Setter`
- [ ] Migrar para `@Data` (mantendo `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- [ ] Verificar compilação
- [ ] Rodar testes

---

## Fase 3: Separar DTOs Bidirecionais

### Estratégia

Para cada DTO bidirecionail:
1. Criar `{Entidade}Request` (input, com validação)
2. Criar `{Entidade}Response` (output, sem validação)
3. Atualizar controllers
4. Depreciar DTO original ou transformar em base

### Checklist

- [ ] `SubprocessoDto` → `SubprocessoRequest` + `SubprocessoResponse`
- [ ] `ProcessoDto` (avaliar necessidade)
- [ ] `UsuarioDto` (avaliar necessidade)
- [ ] `UnidadeDto` (avaliar necessidade)

---

## Fase 4: Remover Validação de Response DTOs

### Checklist

- [ ] Auditar todos `*Dto` retornados em GET
- [ ] Remover `@NotNull`, `@NotBlank`, `@Size`, etc. de response DTOs
- [ ] Garantir validação só existe em `*Request`

---

## Fase 5: Converter para Records

### Benefícios

- Imutáveis por padrão
- Menos boilerplate
- Thread-safe
- Performance otimizada

### Candidatos

**Request DTOs sem lógica customizada:**
- Avaliar após Fases 2-4

**Response DTOs simples:**
- Avaliar após Fases 2-4

**Não candidatos:**
- DTOs com métodos de negócio
- DTOs com getters/setters customizados (ex: `CriarProcessoRequest.getUnidades()`)

---

## Convenções Finais

| Tipo | Sufixo | Estrutura | Lombok | Validação | Uso |
|------|--------|-----------|--------|-----------|-----|
| **Request API** | `*Request` | class ou record | `@Data @Builder` ou `@Builder` | ✅ Sim | Entrada de Controllers |
| **Response API** | `*Response` | class ou record | `@Data @Builder` ou `@Builder` | ❌ Não | Saída de Controllers |
| **Comando Interno** | `*Command` | record | `@Builder` | ❌ Não | Service-to-Service |
| **DTO Genérico** | `*Dto` | class | `@Data @Builder` | ❌ Não | Mapeamento interno |

### Regras de Ouro

1. **Nunca** exponha Entidades JPA diretamente
2. **Sempre** use `*Request` para entrada
3. **Sempre** use `*Response` para saída (ideal)
4. **Validação** apenas em `*Request`
5. **@Data** preferido sobre `@Getter/@Setter`
6. **Records** para DTOs simples sem lógica
7. **Classes** para DTOs com métodos auxiliares

---

## Métricas de Progresso

### Estado Inicial
- DTOs totais: ~250
- Duplicatas Req/Request: ~25 pares
- DTOs com `@Getter/@Setter`: ~20
- DTOs bidirecionais: ~10
- Validação em Response: ~15

### Estado Atual (Pós Fase 1)
- ✅ DTOs únicos: ~225 (-25)
- ✅ Padrão Req/Request: 100% consistente
- ⏳ `@Getter/@Setter`: ~20 pendentes
- ⏳ Bidirecionais: ~10 pendentes
- ⏳ Validação em Response: ~15 pendentes

### Estado Alvo Final
- DTOs otimizados: ~220
- Lombok consistente: 100%
- Request/Response separados: 100%
- Validação correta: 100%
- Records (onde aplicável): ~50%

---

## Próximos Passos

1. ✅ ~~Fase 1: Eliminar duplicatas~~
2. 🔄 **Fase 2: Padronizar Lombok** (PRÓXIMO)
3. ⏳ Fase 3: Separar bidirecionais
4. ⏳ Fase 4: Remover validação de responses
5. ⏳ Fase 5: Converter para records
6. ⏳ Atualizar AGENTS.md com convenções finais

---

## Riscos e Mitigações

### Risco 1: Quebra de compatibilidade com frontend
- ✅ Mitigado: Mantida compatibilidade JSON (nomes de campos inalterados)

### Risco 2: Testes quebrados
- ✅ Mitigado: Todos os 174 testes afetados passando

### Risco 3: Performance de conversão
- ✅ Mitigado: MapStruct otimiza, Records são mais performáticos

---

## Log de Alterações

### 2026-01-14T21:30 - Fase 1 Completa
- ✅ Todos os módulos refatorados (processo, analise, subprocesso, seguranca, organizacao)
- ✅ 7 arquivos deletados, 14 renomeados
- ✅ 60+ arquivos atualizados
- ✅ 174 testes passando (100%)
- ✅ Build limpo
