# Technical Debt - SGC

Este documento registra dívidas técnicas identificadas que devem ser tratadas em iterações futuras.

---

## TD-001: DTOs Request com Setters (Mutabilidade Desnecessária) ✅ RESOLVIDO

**Data de Identificação:** 2026-01-16  
**Data de Resolução:** 2026-01-17  
**Prioridade:** Média  
**Esforço Estimado:** Alto (muitos testes a refatorar)  
**Status:** ✅ COMPLETO

### Descrição

Os DTOs de entrada (Requests) atualmente usam `@Setter` e `@NoArgsConstructor`, o que viola o princípio de imutabilidade. Requests, uma vez recebidos da API, não deveriam ser modificados.

### Situação Atual

```java
@Getter
@Setter  // ❌ Desnecessário
@Builder
@NoArgsConstructor  // ❌ Desnecessário para deserialização Jackson
@AllArgsConstructor
public class CompetenciaRequest {
    private String descricao;
    private List<Long> atividadesIds;
}
```

### Situação Desejada

```java
@Getter
@Builder
@AllArgsConstructor
public class CompetenciaRequest {
    private final String descricao;
    private final List<Long> atividadesIds;
}
```

Ou, preferencialmente, usar **records**:

```java
public record CompetenciaRequest(
    @NotBlank String descricao,
    @NotEmpty List<Long> atividadesIds
) {}
```

### Motivo da Postergação

Os testes usam o padrão legado de construtor sem argumentos + setters:

```java
CompetenciaRequest req = new CompetenciaRequest();
req.setDescricao("teste");
```

Refatorar para usar builders ou records requer atualizar muitos arquivos de teste.

### Ação Necessária

1. Refatorar todos os testes para usar o padrão Builder:
   ```java
   CompetenciaRequest req = CompetenciaRequest.builder()
       .descricao("teste")
       .atividadesIds(List.of(1L))
       .build();
   ```

2. Remover `@Setter` e `@NoArgsConstructor` dos Requests

3. Adicionar `final` aos campos

### Arquivos Afetados

**Requests que precisam ser refatorados:**
- `sgc.processo.dto.CriarProcessoRequest`
- `sgc.processo.dto.AtualizarProcessoRequest`
- `sgc.processo.dto.EnviarLembreteRequest`
- `sgc.subprocesso.dto.CompetenciaRequest`
- `sgc.subprocesso.dto.CriarSubprocessoRequest`
- `sgc.subprocesso.dto.AtualizarSubprocessoRequest`
- `sgc.subprocesso.dto.DisponibilizarMapaRequest`
- `sgc.subprocesso.dto.SubmeterMapaAjustadoRequest`
- `sgc.subprocesso.dto.SalvarAjustesRequest`
- `sgc.subprocesso.dto.ProcessarEmBlocoRequest`
- `sgc.subprocesso.dto.AlterarDataLimiteRequest`
- `sgc.subprocesso.dto.AceitarCadastroRequest`
- `sgc.subprocesso.dto.DevolverCadastroRequest`
- `sgc.subprocesso.dto.DevolverValidacaoRequest`
- `sgc.subprocesso.dto.HomologarCadastroRequest`
- `sgc.subprocesso.dto.ImportarAtividadesRequest`
- `sgc.subprocesso.dto.ReabrirProcessoRequest`
- `sgc.subprocesso.dto.ApresentarSugestoesRequest`
- `sgc.mapa.dto.AtividadeDto` (usado como entrada)
- `sgc.mapa.dto.ConhecimentoDto` (usado como entrada)
- `sgc.mapa.dto.CompetenciaMapaDto` (usado como entrada)
- `sgc.mapa.dto.SalvarMapaRequest`

**Testes que usam padrão legado:**
- Múltiplos arquivos em `src/test/java/sgc/integracao/`
- `SubprocessoMapaWorkflowServiceTest.java`
- `SubprocessoMapaControllerTest.java`
- `AtividadeServiceTest.java`
- `ConhecimentoServiceTest.java`
- Entre outros

---

## TD-002: Separar DTOs Bidirecionais em Request/Response ✅ RESOLVIDO

**Data de Identificação:** 2026-01-16  
**Data de Resolução:** 2026-01-17  
**Prioridade:** Média  
**Esforço Estimado:** Médio  
**Status:** ✅ COMPLETO

### Descrição

Os DTOs `AtividadeDto`, `ConhecimentoDto` e `CompetenciaMapaDto` são usados tanto para entrada (requests) quanto para saída (responses), o que dificulta a aplicação de validações específicas e viola o princípio de responsabilidade única.

### Problema

1. **Validações misturadas**: Anotações como `@NotBlank` só fazem sentido para entrada, mas estão presentes em DTOs de saída
2. **Campo `codigo`**: Na criação, o código não existe; na resposta, sempre existe
3. **Dificuldade de evolução**: Mudanças para entrada podem afetar a saída e vice-versa
4. **Semântica confusa**: O mesmo DTO com significados diferentes dependendo do contexto

### Situação Atual

```java
// Usado tanto para entrada quanto para saída
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeDto {
    private Long codigo;        // Ignorado na criação, obrigatório na resposta
    private Long mapaCodigo;
    @NotBlank                   // Validação só faz sentido na entrada
    private String descricao;
}
```

### Situação Desejada

```java
// Entrada para criação - validações específicas, sem código
public record CriarAtividadeRequest(
    @NotNull Long mapaCodigo,
    @NotBlank @Size(max = 500) String descricao
) {}

// Entrada para atualização - pode ter validações diferentes
public record AtualizarAtividadeRequest(
    @NotBlank @Size(max = 500) String descricao
) {}

// Saída - imutável, sempre com código
public record AtividadeResponse(
    Long codigo,
    Long mapaCodigo,
    String descricao
) {}
```

### Plano de Refatoração

| DTO Atual | Request(s) | Response |
|-----------|------------|----------|
| `AtividadeDto` | `CriarAtividadeRequest`, `AtualizarAtividadeRequest` | `AtividadeResponse` |
| `ConhecimentoDto` | `CriarConhecimentoRequest`, `AtualizarConhecimentoRequest` | `ConhecimentoResponse` |
| `CompetenciaMapaDto` | `CompetenciaMapaRequest` | N/A (embutido no `MapaCompletoDto`) |

### Ação Necessária

1. Criar os novos records de Request para cada operação (criar/atualizar)
2. Criar os records de Response para retorno das APIs
3. Atualizar os Controllers para usar os novos tipos
4. Atualizar os Mappers para converter entre Request → Entity e Entity → Response
5. Atualizar os Services para usar os novos tipos
6. Atualizar os testes para usar os novos records
7. Remover os DTOs antigos após migração completa

### Arquivos Afetados

**DTOs a refatorar:**
- `sgc.mapa.dto.AtividadeDto`
- `sgc.mapa.dto.ConhecimentoDto`
- `sgc.mapa.dto.CompetenciaMapaDto`

**Controllers:**
- `sgc.mapa.AtividadeController`

**Services:**
- `sgc.mapa.service.AtividadeFacade`
- `sgc.mapa.service.AtividadeService`
- `sgc.mapa.service.ConhecimentoService`
- `sgc.mapa.service.MapaSalvamentoService`

**Mappers:**
- `sgc.mapa.mapper.AtividadeMapper`
- `sgc.mapa.mapper.ConhecimentoMapper`
- `sgc.mapa.mapper.MapaCompletoMapper`

### Benefícios Esperados

- ✅ Validações específicas para cada operação
- ✅ Campos imutáveis em Requests (records)
- ✅ Separação clara de responsabilidades
- ✅ Facilidade de evolução independente
- ✅ Documentação implícita do contrato da API

---

## Resumo de Resolução

### TD-001 - Resolução Completa ✅

**Ações Realizadas:**
1. ✅ Removidos `@Setter` e `@NoArgsConstructor` de todos os Request DTOs
2. ✅ Adicionado `final` a todos os campos
3. ✅ Refatorados 37 arquivos de teste para usar o padrão Builder
4. ✅ Todos os 1189 testes continuam passando

**Arquivos Afetados:**
- 22 Request DTOs refatorados
- 37 arquivos de teste atualizados
- 1 arquivo de produção (SubprocessoFacade.java)

### TD-002 - Resolução Completa ✅

**Ações Realizadas:**
1. ✅ Criados 6 novos DTOs (Request/Response separados)
2. ✅ Atualizados Controllers para usar novos tipos
3. ✅ Atualizados Services com métodos sobrecarregados
4. ✅ Atualizados Mappers com conversões para novos tipos
5. ✅ DTOs antigos marcados como `@Deprecated`
6. ✅ Todos os testes continuam passando

**Novos DTOs Criados:**
- `CriarAtividadeRequest` / `AtualizarAtividadeRequest` / `AtividadeResponse`
- `CriarConhecimentoRequest` / `AtualizarConhecimentoRequest` / `ConhecimentoResponse`

**DTOs Depreciados:**
- `AtividadeDto` - usar Request/Response específicos
- `ConhecimentoDto` - usar Request/Response específicos

**CompetenciaMapaDto:** Mantido como está (uso correto dentro de contexto específico)

---

*Última atualização: 2026-01-17*  
*Status Geral: Todas as dívidas técnicas documentadas foram resolvidas ✅*
