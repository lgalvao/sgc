# Plano de Refatoração de DTOs

**Data:** 2025-12-13  
**Objetivo:** Padronizar e melhorar a consistência dos DTOs, eliminando construtores longos, padronizando a instanciação e identificando onde mappers devem ser adicionados ou simplificados.

---

## 1. Análise Geral

### 1.1. Números Gerais
- **Total de DTOs analisados:** 42 arquivos
- **Total de Mappers existentes:** 7 (AlertaMapper, AnaliseMapper, AtividadeMapper, ConhecimentoMapper, MapaMapper, ProcessoMapper/ProcessoDetalheMapper, SubprocessoMapper, MovimentacaoMapper)
- **Módulos com DTOs:** alerta, analise, atividade, diagnostico, mapa, notificacao, processo, sgrh, subprocesso, unidade

### 1.2. Padrões Identificados

#### Padrões de Instanciação Encontrados:
1. **Lombok @Builder** (padrão mais usado) - 35+ ocorrências
2. **Records Java** (para DTOs imutáveis simples) - 10 ocorrências
3. **Construtores explícitos longos** (anti-padrão) - 3-4 casos
4. **Factory methods estáticos** (`of()`, `semImpacto()`, `comImpactos()`) - 3 casos
5. **Instanciação via construtor direto** (chamadas `new XxxDto(...)`) - múltiplas ocorrências nos services

#### Anotações Lombok Utilizadas:
- **@Builder**: Padrão recomendado para DTOs mutáveis
- **@Data**: Usado em alguns DTOs (gera getters, setters, equals, hashCode, toString)
- **@Value**: Usado em alguns DTOs imutáveis (com @Builder)
- **@Getter + @Setter**: Usado em alguns DTOs
- **@AllArgsConstructor**: Presente em 91 casos (muitas vezes desnecessário quando há @Builder)
- **@NoArgsConstructor**: Comum para DTOs que precisam de serialização/desserialização

---

## 2. Problemas Identificados

### 2.1. Construtores Longos (Anti-padrão)

#### **Problema Crítico: UnidadeDto (sgc.sgrh.dto)**
```java
public UnidadeDto(
    Long codigo,
    String nome,
    String sigla,
    Long codigoPai,
    String tipo,
    boolean isElegivel) {
    // 6 parâmetros - ainda aceitável, mas instanciação manual é problemática
}
```

**Instanciações encontradas em:**
- `UnidadeService.java`: Múltiplas chamadas `new UnidadeDto(...)` com 6-7 argumentos

**Impacto:**
- Dificulta manutenção: adicionar campo requer alterar todas as chamadas
- Propensa a erros: ordem dos parâmetros pode ser confundida
- Não utiliza o builder disponível

**Recomendação:**
- ✅ **UnidadeDto já possui @Builder**
- ❌ Serviços não estão usando o builder
- 🔧 **Ação:** Refatorar `UnidadeService` para usar `UnidadeDto.builder()` em vez de construtores

#### **Problema: EmailDto (sgc.notificacao.dto)**
```java
// Construtor customizado para texto simples
public EmailDto(String destinatario, String assunto, String corpo) {
    this(destinatario, assunto, corpo, false);
}
```

**Status:**
- ✅ Tem @Builder
- ⚠️ Construtor customizado usado em `NotificacaoEmailService.java`

**Recomendação:**
- 🔧 **Ação:** Manter construtor customizado (apenas 3 parâmetros, caso de uso específico válido)
- ✅ Ou criar factory method: `EmailDto.textoSimples(destinatario, assunto, corpo)`

#### **Problema: CompetenciaMapaDto (sgc.mapa.dto)**
```java
public CompetenciaMapaDto(Long codigo, String descricao, List<Long> atividadesCodigos) {
    this.codigo = codigo;
    this.descricao = descricao;
    this.atividadesCodigos = (atividadesCodigos == null) ? null : new ArrayList<>(atividadesCodigos);
}
```

**Instanciações encontradas em:**
- `MapaService.java`: Múltiplas chamadas `new CompetenciaMapaDto(...)`

**Recomendação:**
- 🔧 **Ação:** Refatorar para usar builder
- ⚠️ Construtor tem lógica de cópia defensiva - preservar no builder ou setter

#### **Problema: MapaCompletoDto (sgc.mapa.dto)**
```java
// Instanciado manualmente em MapaService.java
return new MapaCompletoDto(
    mapa.getCodigo(),
    codSubprocesso,
    mapa.getObservacoesDisponibilizacao(),
    competenciasDto);
```

**Recomendação:**
- 🔧 **Ação:** Usar builder em vez de construtor

### 2.2. Inconsistência no Uso de @AllArgsConstructor e @NoArgsConstructor

Muitos DTOs têm ambas as anotações, o que pode ser redundante quando há @Builder:

#### Padrão Recomendado para DTOs com @Builder:
```java
@Data
@Builder
@NoArgsConstructor  // Para deserialização JSON/Jackson
@AllArgsConstructor // Para o builder funcionar
public class MeuDto {
    // campos
}
```

#### Exceção - DTOs Imutáveis (usando @Value):
```java
@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)  // force=true permite inicialização de campos final
public class MeuDtoImutavel {
    // campos final
}
```

**Inconsistências encontradas:**

1. **AlertaDto**: ✅ Padrão correto (@Value + @Builder)
2. **AnaliseHistoricoDto**: ⚠️ Usa `@NoArgsConstructor(force = true)` mas poderia simplificar
3. **AtividadeDto**: ✅ Padrão correto (@Data + @Builder + @NoArgsConstructor + @AllArgsConstructor)
4. **ProcessoDto**: ⚠️ Usa `@AllArgsConstructor(access = AccessLevel.PRIVATE)` - desnecessário com builder
5. **UnidadeDto**: ❌ Tem builder mas também tem construtor customizado explícito sendo usado

### 2.3. Mappers Ausentes ou Insuficientes

#### Módulos **SEM** Mappers (mas que possivelmente precisariam):

1. **diagnostico** (8 DTOs, 0 mappers)
   - Usa **records com @Builder** - padrão moderno, boa escolha
   - ✅ **Recomendação:** Manter records, considerar criar `DiagnosticoMapper` se conversões entidade→DTO ficarem complexas

2. **mapa** (7 DTOs, 1 mapper simples)
   - `MapaMapper` existe mas é **interface simples** (apenas mapeamento direto)
   - ⚠️ Conversões complexas estão em **`MapaService`** (lógica manual)
   - **DTOs sem mapper:** MapaCompletoDto, CompetenciaMapaDto, ImpactoMapaDto, CompetenciaImpactadaDto, AtividadeImpactadaDto
   - 🔧 **Ação:** Considerar criar `MapaCompletoMapper` e `ImpactoMapaMapper` para extrair lógica de conversão do service

3. **notificacao** (1 DTO, 0 mappers)
   - EmailDto é simples, não precisa de mapper
   - ✅ **OK**

4. **processo** (7 DTOs, 2 mappers)
   - Tem `ProcessoMapper` e `ProcessoDetalheMapper`
   - ⚠️ **ProcessoDetalheMapperCustom** é extremamente complexo (138 linhas)
   - Possui lógica de negócio misturada (verificação de permissões, queries adicionais)
   - 🔧 **Ação:** Considerar extrair lógica de negócio para service dedicado

5. **sgrh** (9 DTOs, 0 mappers)
   - DTOs principalmente de request/response de API
   - Conversões manuais nos services (UnidadeService, etc.)
   - 🔧 **Ação:** Considerar criar `SgrhMapper` para UnidadeDto e ServidorDto

6. **subprocesso** (28 DTOs, 2 mappers)
   - Tem `SubprocessoMapper` e `MovimentacaoMapper`
   - Muitos DTOs complexos com factory methods estáticos (`SubprocessoDetalheDto.of()`, `SugestoesDto.of()`, `MapaAjusteDto.of()`)
   - ⚠️ Factory methods têm lógica complexa (50-80 linhas)
   - 🔧 **Ação:** Considerar extrair lógica dos factory methods para mappers dedicados ou services

7. **unidade** (1 DTO, 0 mappers)
   - CriarAtribuicaoTemporariaRequest é um record simples
   - ✅ **OK**

### 2.4. Mappers Excessivamente Complexos

#### **Problema Crítico: ProcessoDetalheMapperCustom**
- **138 linhas** de código
- Contém:
  - Lógica de autorização (verificação de admin, chefe, coordenador)
  - Queries adicionais ao repositório (`subprocessoRepo.findByProcessoCodigoWithUnidade`)
  - Construção de hierarquia de unidades (lógica de negócio)
  - Formatação de campos

**Problemas:**
- Mapper deveria ser **apenas mapeamento**, não lógica de negócio
- Dificulta testes unitários
- Viola Single Responsibility Principle

**Recomendação:**
- 🔧 **Ação:** Extrair lógica para `ProcessoDetalheService` ou `ProcessoDetalheBuilder`
- Mapper deve apenas mapear campos diretos
- Lógica de permissões → service dedicado
- Construção de hierarquia → builder ou service

#### **Problema: AlertaMapper**
```java
@Named("extractProcessoName")
protected String extractProcessoName(String descricao) {
    Pattern pattern = Pattern.compile(".*processo '(.*?)'.*");
    Matcher matcher = pattern.matcher(descricao);
    if (matcher.find()) {
        return matcher.group(1);
    }
    return "";
}
```

**Problemas:**
- Lógica de parsing com regex no mapper
- Deveria estar em um utilitário ou no service

**Recomendação:**
- 🔧 **Ação:** Extrair para `AlertaUtil.extrairNomeProcesso(String descricao)` ou manter se for puramente transformação de apresentação

#### **Problema: AnaliseMapper**
```java
protected String getUnidadeSigla(Long codigo) {
    if (codigo == null) return null;
    return unidadeRepo.findById(codigo)
            .map(sgc.unidade.model.Unidade::getSigla)
            .orElse(null);
}
```

**Problemas:**
- Mapper fazendo query ao banco de dados
- Pode causar N+1 queries

**Recomendação:**
- 🔧 **Ação:** Alterar para receber `Unidade` completa na entidade em vez de apenas código, ou usar fetch join na query original

### 2.5. Uso Inconsistente de Records vs Classes

**Records encontrados (padrão moderno Java 17+):**
- diagnostico: 8 DTOs (todos records)
- processo: IniciarProcessoReq (record)
- unidade: CriarAtribuicaoTemporariaRequest (record)

**Classes Lombok encontradas:**
- Todos os outros módulos

**Análise:**
- ✅ Records são **recomendados** para DTOs imutáveis simples (especialmente requests)
- ⚠️ Records com @Builder funcionam mas requerem cuidado (desde Java 16+)
- ✅ Classes com @Builder são mais flexíveis para DTOs complexos ou mutáveis

**Recomendação:**
- 🔧 **Ação:** Padronizar uso:
  - **Records**: Para request/response simples (< 5 campos, sem lógica)
  - **Classes @Builder**: Para DTOs complexos (nested, listas, lógica de apresentação)
  - **Classes @Value + @Builder**: Para DTOs imutáveis complexos

### 2.6. Factory Methods vs Builders

**Factory methods encontrados:**
1. `SubprocessoDetalheDto.of(...)` - 85 linhas de lógica
2. `SugestoesDto.of(...)` - 13 linhas
3. `MapaAjusteDto.of(...)` - 53 linhas
4. `ImpactoMapaDto.semImpacto()` - Factory para estado vazio
5. `ImpactoMapaDto.comImpactos(...)` - Factory para estado populado

**Análise:**
- ✅ Factory methods **semImpacto()** e **comImpactos()** são **bom padrão** - estados nomeados claros
- ⚠️ Factory methods `of()` com 50+ linhas são **anti-padrão** - deveria ser mapper ou service
- ✅ Factory methods simples (< 10 linhas) são aceitáveis para conveniência

**Recomendação:**
- 🔧 **Ação:**
  - Manter factory methods para estados nomeados (ex: `semImpacto()`)
  - Converter `of()` complexos para mappers ou services
  - Usar builders para construção geral de DTOs

---

## 3. Recomendações de Refatoração

### 3.1. Prioridade ALTA

#### 1. Eliminar Construtores Longos em Services
**Arquivos afetados:**
- `UnidadeService.java` → Usar `UnidadeDto.builder()`
- `MapaService.java` → Usar builders para `MapaCompletoDto` e `CompetenciaMapaDto`

**Benefícios:**
- Reduz acoplamento
- Facilita adição de novos campos
- Código mais legível

#### 2. Simplificar ProcessoDetalheMapperCustom
**Ação:**
- Extrair lógica de autorização para `ProcessoPermissoesService`
- Extrair construção de hierarquia para `ProcessoDetalheBuilder` ou service
- Mapper deve apenas mapear campos

**Benefícios:**
- Separação de responsabilidades
- Facilita testes
- Reduz complexidade

#### 3. Criar Mappers Faltando para Módulo Mapa
**Ação:**
- Criar `MapaCompletoMapper` para converter Mapa + Competencias em MapaCompletoDto
- Criar `ImpactoMapaMapper` se análise de impacto ficar mais complexa

**Benefícios:**
- Centraliza lógica de conversão
- Reduz lógica em services

### 3.2. Prioridade MÉDIA

#### 4. Padronizar Uso de Annotations Lombok
**Ação:**
- Revisar todos os DTOs e aplicar padrão:
  ```java
  // Para DTOs mutáveis simples:
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public class MeuDto { }
  
  // Para DTOs imutáveis:
  @Value
  @Builder
  public class MeuDtoImutavel { }
  
  // Para DTOs imutáveis muito simples:
  public record MeuRecord(String campo1, Long campo2) { }
  ```

#### 5. Converter Factory Methods Complexos para Mappers
**Ação:**
- `SubprocessoDetalheDto.of()` → `SubprocessoDetalheMapper`
- `MapaAjusteDto.of()` → `MapaAjusteMapper`

**Benefícios:**
- Segue convenção do projeto (usar MapStruct)
- Facilita manutenção

#### 6. Criar SgrhMapper
**Ação:**
- Centralizar conversões de `Usuario` → `ServidorDto`, `Unidade` → `UnidadeDto`

### 3.3. Prioridade BAIXA

#### 7. Revisar Query no AnaliseMapper
**Ação:**
- Avaliar se é possível usar eager fetching na consulta original em vez de lazy loading no mapper

#### 8. Padronizar Nomenclatura
**Observação atual:**
- Alguns usam sufixo `Req` (CriarProcessoReq)
- Alguns usam sufixo `Request` (CriarAnaliseRequest)
- Alguns usam sufixo `Resp` (LoginResp)

**Recomendação:**
- ✅ Manter flexibilidade: `Req`/`Request` e `Resp`/`Response` são aceitáveis
- 🔧 Documentar no AGENTS.md qual é o padrão preferido (ex: sufixos curtos `Req`/`Resp`)

---

## 4. Padrão Recomendado (Guia de Estilo)

### 4.1. Para Novos DTOs

#### Request/Response Simples (< 5 campos, sem lógica):
```java
public record MeuRequest(
    @NotNull String campo1,
    @NotBlank String campo2,
    Long campo3
) {
}
```

#### DTOs de Resposta Imutáveis:
```java
@Value
@Builder
public class MeuDto {
    Long codigo;
    String descricao;
    LocalDateTime dataHora;
}
```

#### DTOs Mutáveis/Complexos:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeuDtoComplexo {
    private Long codigo;
    private String descricao;
    private List<SubDto> filhos;
}
```

#### DTOs com Factory Methods (estados nomeados):
```java
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResultadoDto {
    private boolean sucesso;
    private String mensagem;
    private List<String> erros;
    
    public static ResultadoDto sucesso(String mensagem) {
        return ResultadoDto.builder()
            .sucesso(true)
            .mensagem(mensagem)
            .erros(List.of())
            .build();
    }
    
    public static ResultadoDto erro(List<String> erros) {
        return ResultadoDto.builder()
            .sucesso(false)
            .mensagem(null)
            .erros(List.copyOf(erros))
            .build();
    }
}
```

### 4.2. Mappers

#### Usar MapStruct para Conversões Simples:
```java
@Mapper(componentModel = "spring")
public interface MeuMapper {
    MeuDto toDto(MinhaEntidade entidade);
    MinhaEntidade toEntity(MeuDto dto);
}
```

#### Evitar Lógica de Negócio em Mappers:
- ❌ Queries ao banco
- ❌ Verificações de autorização
- ❌ Cálculos complexos
- ✅ Formatação de campos (datas, strings)
- ✅ Mapeamento direto de campos
- ✅ Conversões de tipos simples

---

## 5. Plano de Implementação

### Fase 1: Correções Críticas (Sprint 1)
1. Refatorar `UnidadeService` para usar builders
2. Refatorar `MapaService` para usar builders
3. Simplificar `ProcessoDetalheMapperCustom`

### Fase 2: Padronização (Sprint 2)
4. Criar mappers faltando (MapaCompletoMapper, SgrhMapper)
5. Converter factory methods complexos para mappers
6. Padronizar annotations Lombok em todos os DTOs

### Fase 3: Refinamento (Sprint 3)
7. Revisar e otimizar queries em mappers
8. Atualizar documentação (AGENTS.md, README.md)
9. Adicionar testes para novos mappers

---

## 6. Inventário Completo de DTOs

### 6.1. Por Módulo

| Módulo | DTOs | Mappers | Padrão Dominante | Observações |
|--------|------|---------|------------------|-------------|
| alerta | 1 | 1 | @Value + @Builder | ✅ Padrão OK, mapper OK |
| analise | 4 | 1 | @Value + @Builder | ⚠️ Mapper com query ao banco |
| atividade | 2 | 2 | @Data + @Builder | ✅ Padrão OK |
| diagnostico | 8 | 0 | Records + @Builder | ✅ Padrão moderno OK |
| mapa | 7 | 1 | @Data/@Getter + @Builder | ⚠️ Conversões manuais no service |
| notificacao | 1 | 0 | @Data + @Builder | ✅ Simples, OK |
| processo | 7 | 2 | @Getter + @Builder | ⚠️ Mapper customizado muito complexo |
| sgrh | 9 | 0 | @Data + @Builder | ⚠️ Falta mapper |
| subprocesso | 28 | 2 | Misto | ⚠️ Factory methods complexos |
| unidade | 1 | 0 | Record | ✅ OK |

### 6.2. DTOs que Precisam de Atenção

| DTO | Problema | Prioridade |
|-----|----------|-----------|
| UnidadeDto | Construtor longo usado em service | ALTA |
| CompetenciaMapaDto | Construtor manual usado | ALTA |
| MapaCompletoDto | Construtor manual usado | ALTA |
| ProcessoDetalheMapperCustom | Complexidade excessiva | ALTA |
| SubprocessoDetalheDto | Factory method de 85 linhas | MÉDIA |
| MapaAjusteDto | Factory method de 53 linhas | MÉDIA |
| AnaliseMapper | Query no mapper | MÉDIA |
| EmailDto | Construtor customizado (aceitável) | BAIXA |

---

## 7. Conclusão

O sistema possui uma base sólida de DTOs, com uso consistente de **Lombok @Builder** na maioria dos casos. Os principais problemas identificados são:

1. **Construtores longos sendo usados** em vez de builders disponíveis
2. **Mappers com lógica de negócio** (especialmente ProcessoDetalheMapperCustom)
3. **Factory methods muito complexos** que deveriam ser mappers
4. **Falta de mappers** em alguns módulos (mapa, sgrh)

A refatoração proposta é **incremental** e **não-breaking**, focando em melhorar a manutenibilidade sem alterar a API pública dos DTOs. A adoção de **records** para DTOs simples é encorajada, mas a migração é **opcional** e deve ser feita caso a caso.

---

**Próximos Passos:**
1. Revisar este plano com a equipe
2. Priorizar itens de acordo com o roadmap do projeto
3. Criar issues/tasks para cada item de refatoração
4. Implementar em sprints incrementais
