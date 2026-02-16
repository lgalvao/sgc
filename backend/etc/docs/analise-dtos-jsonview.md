# 📊 Análise de DTOs para Conversão @JsonView

**Data:** 16 de Fevereiro de 2026  
**Objetivo:** Identificar DTOs Response simples candidatos à substituição por @JsonView

---

## 🎯 Critérios de Seleção

### ✅ Candidatos IDEAIS para @JsonView

1. **Response DTO** (não Request)
2. **Estrutura 1:1** com uma única entidade
3. **Sem agregações** de múltiplas entidades
4. **Sem campos calculados** ou derivados
5. **Sem transformações** complexas

### ❌ MANTER como DTO

1. **Request DTOs** (com Bean Validation)
2. **Agregações** de múltiplas entidades
3. **Campos calculados** ou derivados
4. **Transformações** complexas
5. **Dados voláteis** que mudam estrutura frequentemente

---

## 📋 Análise Detalhada dos DTOs

### ✅ CANDIDATOS APROVADOS (3 DTOs)

#### 1. AtividadeResponse

**Localização:** `sgc/mapa/dto/AtividadeResponse.java`

**Estrutura Atual:**
```java
@Builder
public record AtividadeResponse(
    Long codigo,
    Long mapaCodigo,
    String descricao
) {}
```

**Entidade Correspondente:** `sgc.mapa.model.Atividade`

**Análise:**
- ✅ Response simples (usado em GET)
- ✅ Estrutura 1:1 com entidade Atividade
- ✅ Apenas 3 campos básicos
- ✅ Sem agregações (não inclui conhecimentos ou competências)
- ✅ Sem campos calculados
- ✅ Sem transformações

**Decisão:** ✅ **CONVERTER para @JsonView**

**Plano de Migração:**
1. Adicionar classe `Views` em `Atividade.java`
2. Anotar campos relevantes com `@JsonView(Views.Publica.class)`
3. Atualizar controller para retornar `Atividade` com `@JsonView`
4. Criar testes de serialização JSON
5. Remover `AtividadeResponse.java`
6. Remover mapper correspondente (se existir)

---

#### 2. ConhecimentoResponse

**Localização:** `sgc/mapa/dto/ConhecimentoResponse.java`

**Estrutura Atual:**
```java
@Builder
public record ConhecimentoResponse(
    Long codigo,
    Long atividadeCodigo,
    String descricao
) {}
```

**Entidade Correspondente:** `sgc.mapa.model.Conhecimento`

**Análise:**
- ✅ Response simples (usado em GET)
- ✅ Estrutura 1:1 com entidade Conhecimento
- ✅ Apenas 3 campos básicos
- ✅ Sem agregações
- ✅ Sem campos calculados (atividadeCodigo é direto de relacionamento)
- ✅ Sem transformações

**Decisão:** ✅ **CONVERTER para @JsonView**

**Plano de Migração:**
1. Adicionar classe `Views` em `Conhecimento.java`
2. Anotar campos relevantes com `@JsonView(Views.Publica.class)`
3. Adicionar método anotado `getAtividadeCodigo()` com `@JsonView` (já existe na entidade)
4. Atualizar controller para retornar `Conhecimento` com `@JsonView`
5. Criar testes de serialização JSON
6. Remover `ConhecimentoResponse.java`
7. Remover mapper correspondente (se existir)

---

#### 3. ConhecimentoDto (visualização)

**Localização:** `sgc/mapa/dto/visualizacao/ConhecimentoDto.java`

**Estrutura Atual:**
```java
@Builder
public record ConhecimentoDto(
    Long codigo,
    String descricao
) {}
```

**Entidade Correspondente:** `sgc.mapa.model.Conhecimento`

**Análise:**
- ✅ DTO ultra-simples (apenas 2 campos)
- ✅ Estrutura 1:1 com entidade Conhecimento
- ✅ Sem agregações
- ✅ Sem campos calculados
- ✅ Usado para visualização minimalista

**Decisão:** ✅ **CONVERTER para @JsonView**

**Observação:** Este DTO pode usar a mesma view `Conhecimento.Views.Publica` mas omitindo `atividadeCodigo`. Podemos criar uma view hierárquica:
- `Views.Minimal` - apenas codigo e descricao
- `Views.Publica extends Minimal` - adiciona atividadeCodigo

**Plano de Migração:**
1. Adicionar view `Views.Minimal` em `Conhecimento.java`
2. Anotar `codigo` e `descricao` com `@JsonView(Views.Minimal.class)`
3. Anotar `atividadeCodigo` com `@JsonView(Views.Publica.class)`
4. Atualizar controller para usar view apropriada
5. Criar testes de serialização JSON
6. Remover `ConhecimentoDto.java`

---

### ❌ MANTER COMO DTO (4 DTOs)

#### 1. AtividadeOperacaoResponse

**Localização:** `sgc/subprocesso/dto/AtividadeOperacaoResponse.java`

**Motivo:** ❌ **Agregação de múltiplas entidades**

**Análise:**
- Retorna `atividade` + `subprocesso` + lista de atividades atualizadas
- Combina dados de múltiplas fontes para atualização de UI
- Concerns misturados: CRUD operation response + status update
- Transformação específica para frontend

**Decisão:** ❌ **MANTER DTO**

---

#### 2. SubprocessoSituacaoDto

**Localização:** `sgc/subprocesso/dto/SubprocessoSituacaoDto.java`

**Motivo:** ❌ **Campos derivados/calculados**

**Análise:**
- Wrapper de status/enum com `situacaoLabel` derivado
- Campo calculado baseado em situação
- Lógica de transformação específica

**Decisão:** ❌ **MANTER DTO**

---

#### 3. AtividadeDto

**Localização:** `sgc/mapa/dto/AtividadeDto.java` (se existir com nested)

**Motivo:** ❌ **Agregação nested**

**Análise:**
- Combina `Atividade` + `List<ConhecimentoDto>`
- Estrutura nested para display em árvore
- Agregação controlada

**Decisão:** ❌ **MANTER DTO** (se usado para estruturas nested complexas)

---

#### 4. MensagemResponse

**Localização:** `sgc/subprocesso/dto/MensagemResponse.java`

**Motivo:** ❌ **Padrão arquitetural**

**Análise:**
- Utility DTO genérico (single message field)
- Wrapper de resposta desacoplado
- Não corresponde a nenhuma entidade específica
- Mantém consistência com padrão de resposta

**Decisão:** ❌ **MANTER DTO**

---

## 📊 Resumo Quantitativo

| Categoria | Quantidade | Ação |
|-----------|------------|------|
| **Candidatos @JsonView** | 3 | Converter |
| **Manter como DTO** | 4 | Sem mudança |
| **Redução Estimada** | ~70 LOC | DTOs + Mappers removidos |

---

## 🚀 Plano de Implementação

### Fase 1: Preparação (1 dia)

1. **Criar Views em Entities**
   - [ ] Adicionar `MapaViews.java` em `sgc.mapa.model`
   - [ ] Adicionar views em `Atividade.java`
   - [ ] Adicionar views em `Conhecimento.java`

2. **Anotar Campos**
   - [ ] Anotar campos em `Atividade` com `@JsonView`
   - [ ] Anotar campos em `Conhecimento` com `@JsonView`
   - [ ] Adicionar `@JsonIgnore` em relacionamentos não serializados

### Fase 2: Testes de Serialização (1 dia)

1. **Criar Testes JSON**
   - [ ] `AtividadeJsonViewTest.java` - validar serialização
   - [ ] `ConhecimentoJsonViewTest.java` - validar serialização
   - [ ] Testar que campos corretos são serializados
   - [ ] Testar que relacionamentos não vazam

### Fase 3: Migração de Controllers (0.5 dia)

1. **Atualizar Controllers**
   - [ ] Identificar controllers que usam `AtividadeResponse`
   - [ ] Identificar controllers que usam `ConhecimentoResponse`
   - [ ] Atualizar para retornar entities com `@JsonView`

### Fase 4: Limpeza (0.5 dia)

1. **Remover DTOs Obsoletos**
   - [ ] Remover `AtividadeResponse.java`
   - [ ] Remover `ConhecimentoResponse.java`
   - [ ] Remover `ConhecimentoDto.java` (visualização)
   - [ ] Remover mappers associados (se existirem)
   - [ ] Remover testes de DTOs obsoletos

### Fase 5: Validação Final (0.5 dia)

1. **Validar Testes**
   - [ ] Rodar suite completa backend
   - [ ] Rodar testes de serialização
   - [ ] Rodar testes ArchUnit
   - [ ] Validar cobertura mantida >70%

**Duração Total:** ~3.5 dias

---

## ⚠️ Riscos e Mitigações

### Risco 1: Vazamento de Dados Sensíveis

**Descrição:** @JsonView pode expor acidentalmente relacionamentos ou campos não desejados.

**Mitigação:**
- ✅ Testes de serialização JSON obrigatórios
- ✅ Adicionar `@JsonIgnore` explicitamente em relacionamentos
- ✅ Code review focado em segurança

### Risco 2: Quebra de Contrato de API

**Descrição:** Mudança de estrutura JSON pode quebrar clientes.

**Mitigação:**
- ✅ Comparar JSON antes/depois da mudança
- ✅ Testes E2E para validar contratos
- ✅ Versionamento de API se necessário

### Risco 3: Performance

**Descrição:** Serialização de entities pode ser mais lenta que DTOs.

**Mitigação:**
- ✅ Medir performance antes/depois
- ✅ Jackson é otimizado para @JsonView
- ✅ Rollback se degradação >5%

---

## 📚 Referências

- **ADR-004:** [DTO Pattern](../adr/ADR-004-dto-pattern.md) - Seção @JsonView
- **ADR-008:** [Simplification Decisions](../adr/ADR-008-simplification-decisions.md)
- **Spring Docs:** [Jackson @JsonView](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/jackson.html)

---

**Próximo Passo:** Iniciar Fase 1 - Preparação (criar Views em entities)
