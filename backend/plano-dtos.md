# Plano de Padronização de DTOs

> Documento de acompanhamento da refatoração de DTOs do projeto SGC.
> Última atualização: 2026-01-14T19:30

## Status Geral

| Fase | Descrição | Status |
|------|-----------|--------|
| 0 | Análise Profunda e Documentação | ✅ Completo |
| 1 | Eliminar duplicatas Req/Request | 🔄 Em Progresso |
| 2 | Padronizar anotações Lombok | 🔄 Em Progresso |
| 3 | Separar DTOs bidirecionais | ⏳ Pendente |
| 4 | Remover validação de Response DTOs | ⏳ Pendente |
| 5 | Converter para records | ⏳ Pendente |

---

## 📊 Análise Profunda - Estado Atual

### Problemas Identificados

#### 1. **DUPLICAÇÃO DE ARQUIVOS** (Prioridade Alta 🔴)

Existem múltiplos arquivos para o mesmo propósito, causando confusão e inconsistência:

**Módulo `processo`:**
- `CriarProcessoReq.java` + `CriarProcessoRequest.java` (DUPLICADOS - quase idênticos)
- `AtualizarProcessoReq.java` + `AtualizarProcessoRequest.java` (DUPLICADOS)
- `IniciarProcessoReq.java` + `IniciarProcessoRequest.java` (DUPLICADOS)
- `EnviarLembreteReq.java` + `EnviarLembreteRequest.java` (DUPLICADOS)

**Módulo `analise`:**
- `CriarAnaliseReq.java` + `CriarAnaliseApiReq.java` + `CriarAnaliseRequest.java` (3 ARQUIVOS!)
- `CriarAnaliseCommand.java` (correto - uso interno)

**Ação:** Manter apenas `*Request.java` e deletar `*Req.java`

#### 2. **INCONSISTÊNCIA DE LOMBOK** (Prioridade Alta 🔴)

Três padrões diferentes encontrados:

**Padrão A - Correto (@Data):**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarProcessoRequest { }
```

**Padrão B - Incorreto (@Getter/@Setter):**
```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessoDetalheDto { }
```

**Padrão C - Moderno (Record):**
```java
@Builder
public record CriarAnaliseRequest(...) {}
```

**DTOs usando @Getter/@Setter (devem migrar para @Data):**
- `SubprocessoDto`
- `ProcessoDetalheDto`
- Aproximadamente 15-20 DTOs no total

#### 3. **DTOS BIDIRECIONAIS** (Prioridade Média 🟠)

DTOs usados tanto para entrada quanto saída, violando separação de concerns:

**Exemplo crítico - `SubprocessoDto`:**
```java
@Getter
@Setter
public class SubprocessoDto {
    private Long codigo;
    
    @NotNull(message = "O código do processo é obrigatório")  // ❌ Validação em DTO de resposta!
    private Long codProcesso;
    
    // ... outros campos
}
```

**Problema:** 
- Tem validação `@NotNull` (indica Request)
- Mas é retornado em endpoints GET (Response)
- Não é claro quando validação se aplica

**Solução:**
- Criar `SubprocessoRequest` (com validação)
- Criar `SubprocessoResponse` (sem validação)
- Depreciar `SubprocessoDto` ou convertê-lo em base comum

**Outros DTOs bidirecionais:**
- `ProcessoDto`
- `UsuarioDto` (parcialmente)
- `UnidadeDto` (parcialmente)

#### 4. **VALIDAÇÃO EM RESPONSE DTOS** (Prioridade Média 🟠)

DTOs de resposta não devem ter validação (Bean Validation é para entrada):

**Exemplos encontrados:**
- `SubprocessoDto` tem `@NotNull`
- Alguns `*Dto` genéricos têm validações

**Regra:** Apenas `*Request` deve ter validação (`@NotNull`, `@NotBlank`, `@Size`, etc.)

#### 5. **SUBUSO DE *Response** (Prioridade Baixa 🟡)

Apenas ~5 arquivos usam sufixo `*Response`:
- `EntrarResp.java` (deveria ser `EntrarResponse`)
- `AtividadeOperacaoResp.java` (deveria ser `AtividadeOperacaoResponse`)

**Problema:** A maioria das respostas usa `*Dto` genérico, não deixando claro o contrato de saída.

**Ideal:**
- Endpoints GET retornam `*Response`
- Endpoints POST (create) retornam `*Response`
- `*Dto` é reservado para mapeamento interno

---

## Fase 0: Análise Profunda e Documentação ✅

- [x] Catalogar todos os DTOs (250+ arquivos)
- [x] Identificar padrões de Lombok
- [x] Encontrar duplicatas Req/Request
- [x] Analisar validação em Response DTOs
- [x] Identificar DTOs bidirecionais
- [x] Documentar problemas e soluções

---

## Fase 1: Eliminar Duplicatas Req/Request

### Estratégia:
1. Manter `*Request.java` (nome completo, mais explícito)
2. Deletar `*Req.java`
3. Atualizar imports em Controllers/Services

### Módulo `processo`

#### Duplicatas a Remover:
- [x] ✅ Existem duplicatas: `CriarProcessoReq` + `CriarProcessoRequest`
- [x] ✅ Existem duplicatas: `AtualizarProcessoReq` + `AtualizarProcessoRequest`
- [x] ✅ Existem duplicatas: `IniciarProcessoReq` + `IniciarProcessoRequest`
- [x] ✅ Existem duplicatas: `EnviarLembreteReq` + `EnviarLembreteRequest`

#### Ações:
- [ ] Verificar qual versão é usada nos controllers
- [ ] Garantir `*Request` tem todas as features da versão `*Req`
- [ ] Atualizar imports
- [ ] Deletar `*Req.java`

### Módulo `analise`

- [x] ✅ Existem TRÊS arquivos para criação de análise!
  - `CriarAnaliseReq.java`
  - `CriarAnaliseApiReq.java`
  - `CriarAnaliseRequest.java` (RECORD - versão moderna)

#### Ações:
- [ ] Verificar qual é usada
- [ ] Manter apenas `CriarAnaliseRequest` (record)
- [ ] Deletar `CriarAnaliseReq` e `CriarAnaliseApiReq`

### Módulo `subprocesso`

#### Arquivos com apenas `*Req` (precisam renomear):
- [ ] `AceitarCadastroReq` → `AceitarCadastroRequest`
- [ ] `ApresentarSugestoesReq` → `ApresentarSugestoesRequest`
- [ ] `CompetenciaReq` → `CompetenciaRequest`
- [ ] `DevolverCadastroReq` → `DevolverCadastroRequest`
- [ ] `DevolverValidacaoReq` → `DevolverValidacaoRequest`
- [ ] `HomologarCadastroReq` → `HomologarCadastroRequest`
- [ ] `ImportarAtividadesReq` → `ImportarAtividadesRequest`
- [ ] `ReabrirProcessoReq` → `ReabrirProcessoRequest`
- [ ] `SalvarAjustesReq` → `SalvarAjustesRequest`
- [ ] `SubmeterMapaAjustadoReq` → `SubmeterMapaAjustadoRequest`

#### Duplicatas:
- [x] ✅ `DisponibilizarMapaReq` + `DisponibilizarMapaRequest`
  - [ ] Manter `DisponibilizarMapaRequest`
  - [ ] Deletar `DisponibilizarMapaReq`

### Módulo `seguranca`

- [ ] `AutenticarReq` → `AutenticarRequest`
- [ ] `EntrarReq` → `EntrarRequest`

### Módulo `organizacao`

- [ ] `CriarAtribuicaoTemporariaReq` → `CriarAtribuicaoTemporariaRequest`

### Módulo `mapa`

- [x] ✅ `SalvarMapaRequest` - Já está correto!

---

## Fase 2: Padronizar Anotações Lombok

### Padrão Alvo para Request DTOs (Classes):
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

### Padrão Alvo para Request DTOs (Records):
```java
@Builder
public record ExemploRequest(
    @NotNull(message = "Campo obrigatório")
    String campo
) {}
```

### Padrão Alvo para Response DTOs:
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

### DTOs a Corrigir (@Getter/@Setter → @Data):

- [ ] `SubprocessoDto`
- [ ] `ProcessoDetalheDto`
- [ ] `SubprocessoDetalheDto`
- [ ] Outros ~10-15 DTOs

---

## Fase 3: Separar DTOs Bidirecionais

### DTOs Problemáticos:

#### `SubprocessoDto` (CRÍTICO)
```java
// ATUAL - Usado para ambos input/output
@Getter
@Setter
public class SubprocessoDto {
    @NotNull  // ❌ Validação não faz sentido em response
    private Long codProcesso;
}
```

**Solução:**
- [ ] Criar `SubprocessoRequest` (com validação)
- [ ] Criar `SubprocessoResponse` (sem validação)
- [ ] Atualizar controllers para usar correto
- [ ] Depreciar ou remover `SubprocessoDto`

#### Outros candidatos:
- [ ] `ProcessoDto`
- [ ] `UsuarioDto`
- [ ] `UnidadeDto`

---

## Fase 4: Remover Validação de Response DTOs

**Regra:** Apenas `*Request` deve ter anotações de validação.

### Checklist:
- [ ] Auditar todos `*Dto` que são retornados em GET
- [ ] Remover `@NotNull`, `@NotBlank`, `@Size`, etc.
- [ ] Garantir validação existe apenas em `*Request`

---

## Fase 5: Converter para Records

### Benefícios dos Records:
- Imutáveis por padrão
- Menos boilerplate
- Thread-safe
- Performance otimizada

### Candidatos (DTOs sem lógica customizada):

#### Request DTOs:
- [ ] `AceitarCadastroRequest`
- [ ] `ApresentarSugestoesRequest`
- [ ] `DevolverCadastroRequest`
- [ ] Outros após análise individual

#### Response DTOs:
- [ ] `ProcessoResponse` (após separação)
- [ ] `SubprocessoResponse` (após separação)
- [ ] Outros após análise

### Não candidatos (têm lógica customizada):
- ❌ `CriarProcessoRequest` (tem getters/setters customizados para `unidades`)
- ❌ DTOs com métodos de negócio

---

## Convenções Finais (Adotar no AGENTS.md)

| Tipo | Sufixo | Estrutura | Lombok | Validação | Uso |
|------|--------|-----------|--------|-----------|-----|
| **Request API** | `*Request` | class ou record | `@Data @Builder` ou `@Builder` (record) | ✅ Sim | Entrada de Controllers |
| **Response API** | `*Response` | class ou record | `@Data @Builder` ou `@Builder` (record) | ❌ Não | Saída de Controllers |
| **Comando Interno** | `*Command` | record | `@Builder` | ❌ Não | Chamadas entre Services |
| **DTO Genérico** | `*Dto` | class | `@Data @Builder` | ❌ Não | Mapeamento de Entidades (uso interno) |

### Regras de Ouro:

1. **Nunca** exponha Entidades JPA diretamente
2. **Sempre** use `*Request` para entrada de dados
3. **Sempre** use `*Response` para saída de dados
4. **Validação** apenas em `*Request`
5. **@Data** é preferido sobre `@Getter/@Setter`
6. **Records** para DTOs sem lógica customizada
7. **Classes** para DTOs com métodos auxiliares

---

## Métricas de Progresso

### Estado Inicial:
- Total de DTOs: ~250 arquivos
- Duplicatas Req/Request: ~25 pares
- DTOs com @Getter/@Setter: ~20
- DTOs bidirecionais: ~10
- Validação em Response: ~15

### Estado Alvo:
- DTOs únicos: ~225 arquivos
- Padrão Lombok consistente: 100%
- Separação Request/Response clara: 100%
- Validação apenas em Request: 100%
- Records para DTOs simples: ~50%

---

## Riscos e Mitigações

### Risco 1: Quebra de Compatibilidade com Frontend
- **Mitigação:** Mapear uso de cada DTO antes de deletar
- **Mitigação:** Manter compatibilidade JSON (nomes de campos)

### Risco 2: Testes Quebrados
- **Mitigação:** Rodar suite completa após cada fase
- **Mitigação:** Atualizar mocks e fixtures

### Risco 3: Performance de Conversão
- **Mitigação:** MapStruct já otimiza conversões
- **Mitigação:** Records são mais performáticos

---

## Próximos Passos

1. ✅ Concluir análise profunda
2. 🔄 Executar Fase 1 (eliminar duplicatas)
3. ⏳ Executar Fase 2 (padronizar Lombok)
4. ⏳ Executar Fase 3 (separar bidirecionais)
5. ⏳ Executar Fase 4 (remover validação de responses)
6. ⏳ Executar Fase 5 (converter para records)
7. ⏳ Atualizar AGENTS.md com convenções finais
8. ⏳ Rodar suite completa de testes
