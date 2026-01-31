# ADR-004: Padrão de DTOs Obrigatórios

---

## Contexto

Aplicações REST precisam transferir dados entre cliente (frontend) e servidor (backend).
A decisão mais simples seria expor entidades JPA diretamente nas APIs:

```java
// ❌ ANTI-PATTERN: Expor entidade JPA diretamente
@GetMapping("/{id}")
public Subprocesso buscar(@PathVariable Long id) {
    return subprocessoRepo.findById(id).orElseThrow();
}
```

Esta abordagem, embora simples, traz **problemas graves**:

### Problemas de Expor Entidades JPA Diretamente

1. **Lazy Loading Issues**
    - Jackson tenta serializar relações lazy (`@OneToMany`, `@ManyToOne`)
    - Lança `LazyInitializationException` fora da transação
    - Solução comum: `@JsonIgnore` polui entidades com concerns de API

2. **Vazamento de Dados Internos**
    - Campos técnicos expostos (versão, timestamps internos)
    - Relacionamentos bidirecionais causam referências cíclicas
    - Difícil controlar o que é exposto (tudo ou nada)

3. **Acoplamento Cliente-Servidor**
    - Mudança no modelo de dados quebra clientes
    - Impossível ter diferentes views dos mesmos dados
    - Evolução do schema de BD impacta APIs

4. **Segurança**
    - Risco de mass assignment (aceitar campos não desejados)
    - Difícil ocultar dados sensíveis seletivamente
    - Dificulta auditoria de quais dados foram acessados

5. **Performance**
    - Impossível otimizar queries (sempre carrega tudo)
    - Difícil fazer projeções customizadas
    - N+1 queries inevitáveis

6. **Validação**
    - Validações de entrada misturadas com regras de persistência
    - Bean Validation em entidades afeta ambos leitura e escrita
    - Difícil ter regras diferentes para criação vs. atualização

## Decisão

**Proibir exposição de entidades JPA nas APIs REST.**

**Obrigar uso de DTOs (Data Transfer Objects) em todos os controllers.**

### Regra Arquitetural

```
┌─────────────────────────────────────────────────────────────┐
│                         REGRA                               │
│                                                             │
│  Controllers SEMPRE recebem e retornam DTOs.                │
│  Entidades JPA NUNCA são expostas em APIs REST.             │
│                                                             │
│  - Request: DTO → Mapper → Entidade → Service              │
│  - Response: Service → Entidade → Mapper → DTO             │
└─────────────────────────────────────────────────────────────┘
```

### Enforcement

1. **Testes Arquiteturais (ArchUnit)**
   ```java
   @Test
   void controllersNaoDevemExporEntidadesJPA() {
       noMethods()
           .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
           .should().haveRawReturnType(assignableTo(classesWithAnnotation("jakarta.persistence.Entity")))
           .check(classes);
   }
   ```

2. **Code Review**
    - Checklist: "Controller retorna DTOs?"
    - Aprovação automática bloqueada se entidades expostas

3. **Convenções de Nomenclatura**
    - DTOs devem terminar com `Dto`, `Req`, `Request`, `Resp`, `Response`
    - Facilita identificação visual

## Tipos de DTOs

### 1. DTOs de Request (Entrada)

Usados em `@RequestBody` de POST/PUT/PATCH.

**Características:**

- Mutáveis (setters para deserialização JSON)
- Validados com Bean Validation
- Específicos para cada operação (evitar DTOs genéricos)

**Exemplo:**

```java
public class AceitarCadastroReq {
    @NotBlank(message = "Parecer é obrigatório")
    @Size(max = 1000)
    private String parecer;
    
    private LocalDate dataAnalise;
    
    // Getters e Setters
}
```

**Nomenclatura:**

- `Req` (curto): Para operações simples
- `Request` (longo): Para operações complexas
- Sufixo descritivo: `AceitarCadastroReq`, `AlterarDataLimiteRequest`

### 2. DTOs de Response (Saída)

Usados como retorno de `@GetMapping` ou `ResponseEntity<Dto>`.

**Características:**

- Imutáveis preferencialmente (usar Java Records quando possível)
- SEM validações (dados já validados)
- Podem ter campos calculados (permissões, flags)

**Exemplo com Record:**

```java
public record SubprocessoDto(
    Long codigo,
    String descricao,
    SituacaoSubprocesso situacao,
    UnidadeDto unidade,
    LocalDate dataLimite
) {
    // Record gera construtor, getters, equals, hashCode, toString automaticamente
}
```

**Exemplo com Classe (para DTOs complexos):**

```java
public class SubprocessoDetalheDto {
    private final Long codigo;
    private final String descricao;
    private final SubprocessoPermissoesDto permissoes;  // Calculado
    private final List<AtividadeVisualizacaoDto> atividades;
    
    // Construtor all-args
    // Apenas getters (imutável)
}
```

**Nomenclatura:**

- `Dto` (genérico): Dados básicos
- `DetalheDto`: Dados completos com relacionamentos
- `VisualizacaoDto`: Dados formatados para exibição
- `Resp` / `Response`: Resposta de operação

### 3. DTOs Bidirecionais (Evitar)

DTOs que servem tanto para entrada quanto saída.

**Quando usar:**

- ⚠️ RARAMENTE
- Apenas para operações CRUD muito simples
- Quando estrutura de entrada = estrutura de saída

**Problema:**

- Mistura validações de entrada com dados de saída
- Dificulta evolução independente
- Confunde responsabilidades

**Preferir:** DTOs separados mesmo com duplicação

## Mapeamento: Entidades ↔ DTOs

### MapStruct (Recomendado)

Geração de código de mapeamento em tempo de compilação.

**Vantagens:**

- Performance (sem reflection)
- Segurança de tipos (erros em compile-time)
- Geração automática de código boilerplate

**Exemplo:**

```java
@Mapper(componentModel = "spring")
public interface SubprocessoMapper {
    
    // Entidade → DTO (Response)
    SubprocessoDto toDto(Subprocesso entity);
    
    // DTO → Entidade (Request - raramente usado)
    @Mapping(target = "codigo", ignore = true)  // Gerado pelo BD
    @Mapping(target = "versao", ignore = true)  // Gerenciado por JPA
    Subprocesso toEntity(SubprocessoDto dto);
    
    // Lista de entidades → Lista de DTOs
    List<SubprocessoDto> toDtoList(List<Subprocesso> entities);
}
```

**Mapeamentos Customizados:**

```java
@Mapper(componentModel = "spring", uses = {AccessControlService.class})
public interface SubprocessoDetalheMapper {
    
    @Mapping(target = "permissoes", source = ".", qualifiedByName = "calcularPermissoes")
    @Mapping(target = "atividades", source = "atividades", qualifiedByName = "mapearAtividades")
    SubprocessoDetalheDto toDto(Subprocesso sp, @Context Usuario usuario);
    
    @Named("calcularPermissoes")
    default SubprocessoPermissoesDto calcularPermissoes(
        Subprocesso sp, 
        @Context Usuario usuario,
        @Context AccessControlService accessControl
    ) {
        return new SubprocessoPermissoesDto(
            accessControl.podeExecutar(usuario, EDITAR_CADASTRO, sp),
            accessControl.podeExecutar(usuario, VALIDAR_MAPA, sp)
            // ... outras permissões
        );
    }
}
```

### Mapeamento Manual (Casos Específicos)

Quando MapStruct não é suficiente:

- Lógica complexa de transformação
- Agregação de múltiplas entidades
- Cálculos pesados

**Exemplo:**

```java
@Service
public class SubprocessoDetalheService {
    
    public SubprocessoDetalheDto montarDetalhes(Subprocesso sp, Usuario usuario) {
        // Lógica complexa manual
        SubprocessoPermissoesDto permissoes = calcularPermissoes(sp, usuario);
        List<AtividadeVisualizacaoDto> atividades = montarAtividades(sp, usuario);
        ContextoEdicaoDto contexto = montarContexto(sp);
        
        return new SubprocessoDetalheDto(
            sp.getCodigo(),
            sp.getDescricao(),
            permissoes,
            atividades,
            contexto
        );
    }
}
```

## Padrões de Uso

### Em Controllers

```java
@RestController
@RequestMapping("/api/subprocessos")
public class SubprocessoCadastroController {
    
    private final SubprocessoFacade facade;
    
    // ✅ BOM: Recebe DTO, retorna DTO
    @PostMapping("/{id}/cadastro/aceitar")
    public ResponseEntity<RespostaDto> aceitarCadastro(
        @PathVariable Long id,
        @RequestBody @Valid AceitarCadastroReq request,  // DTO de entrada
        Authentication auth
    ) {
        RespostaDto resposta = facade.aceitarCadastro(id, request, auth);
        return ResponseEntity.ok(resposta);  // DTO de saída
    }
    
    // ✅ BOM: Retorna DTO detalhado
    @GetMapping("/{id}")
    public SubprocessoDetalheDto buscarDetalhes(
        @PathVariable Long id,
        Authentication auth
    ) {
        return facade.buscarDetalhes(id, auth);  // DTO de saída
    }
    
    // ❌ RUIM: Retorna entidade JPA
    @GetMapping("/{id}/entidade")
    public Subprocesso buscarEntidade(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();  // ❌ PROIBIDO
    }
}
```

### Em Facades/Services

```java
@Service
public class SubprocessoFacade {
    
    private final SubprocessoRepo repo;
    private final SubprocessoMapper mapper;
    private final SubprocessoDetalheMapper detalheMapper;
    private final AccessControlService accessControl;
    
    // Trabalha com entidades internamente, expõe DTOs
    public SubprocessoDetalheDto buscarDetalhes(Long codigo, Authentication auth) {
        Usuario usuario = usuarioService.buscarPorTituloEleitoral(auth.getName());
        
        // 1. Busca entidade
        Subprocesso entity = repo.findById(codigo)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codigo));
        
        // 2. Verifica permissão (usa entidade)
        accessControl.verificarPermissao(usuario, VISUALIZAR_SUBPROCESSO, entity);
        
        // 3. Converte para DTO (expõe DTO)
        return detalheMapper.toDto(entity, usuario);
    }
    
    public RespostaDto aceitarCadastro(Long codigo, AceitarCadastroReq request, Authentication auth) {
        Usuario usuario = usuarioService.buscarPorTituloEleitoral(auth.getName());
        Subprocesso sp = repo.findById(codigo).orElseThrow();
        
        // Verifica permissão
        accessControl.verificarPermissao(usuario, ACEITAR_CADASTRO, sp);
        
        // Delega para service de negócio (trabalha com entidade)
        cadastroService.aceitarCadastro(sp, request.getParecer(), usuario);
        
        // Retorna DTO simples
        return new RespostaDto("Cadastro aceito com sucesso");
    }
}
```

## Validação

### Validação de Entrada (DTOs de Request)

Usar Bean Validation (Jakarta Validation):

```java
public class CriarProcessoReq {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 5, max = 200, message = "Nome deve ter entre 5 e 200 caracteres")
    private String nome;
    
    @NotNull(message = "Data de início é obrigatória")
    @Future(message = "Data de início deve ser futura")
    private LocalDate dataInicio;
    
    @NotNull(message = "Unidade é obrigatória")
    @Min(value = 1, message = "Código de unidade inválido")
    private Long codigoUnidade;
    
    @Valid  // Valida objeto nested
    private ConfiguracaoProcessoReq configuracao;
    
    // Getters e Setters
}
```

**Ativação no Controller:**

```java
@PostMapping
public ResponseEntity<ProcessoDto> criar(@RequestBody @Valid CriarProcessoReq request) {
    // Spring valida automaticamente
    // Se inválido, retorna 400 Bad Request com erros detalhados
}
```

**Validações Customizadas:**

```java
@Constraint(validatedBy = DataLimiteValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataLimiteValida {
    String message() default "Data limite deve ser futura e dia útil";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class DataLimiteValidator implements ConstraintValidator<DataLimiteValida, LocalDate> {
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true;  // @NotNull cuida disso
        
        return value.isAfter(LocalDate.now()) && !isFimDeSemana(value);
    }
}
```

### Validação de Negócio (Services)

**NÃO** usar Bean Validation para regras de negócio complexas.

```java
// ❌ RUIM: Bean Validation para regra de negócio
public class AceitarCadastroReq {
    @SubprocessoDeveEstarDisponibilizado  // ❌ Muito complexo para anotação
    private Long codigoSubprocesso;
}

// ✅ BOM: Validação no Service
public void aceitarCadastro(Subprocesso sp, String parecer) {
    if (sp.getSituacao() != CADASTRO_DISPONIBILIZADO) {
        throw new ErroProcessoEmSituacaoInvalida(
            "Subprocesso deve estar com cadastro disponibilizado. Situação atual: " + sp.getSituacao()
        );
    }
    
    // ... lógica de negócio
}
```

## Segurança

### 1. Mass Assignment Protection

**Problema:**

```java
// ❌ Vulnerável: Cliente pode enviar campos não desejados
@PutMapping("/{id}")
public Subprocesso atualizar(@PathVariable Long id, @RequestBody Subprocesso sp) {
    // Cliente poderia enviar: { "codigo": 999, "situacao": "HOMOLOGADO", ... }
    // Bypass de regras de negócio!
    return repo.save(sp);
}
```

**Solução com DTOs:**

```java
// ✅ Seguro: DTO define exatamente o que é aceito
public class AtualizarSubprocessoReq {
    @NotBlank
    private String descricao;  // APENAS descrição pode ser alterada
    
    // codigo, situacao, etc. NÃO podem ser alterados via API
}

@PutMapping("/{id}")
public SubprocessoDto atualizar(@PathVariable Long id, @RequestBody @Valid AtualizarSubprocessoReq req) {
    // Impossível alterar campos não presentes no DTO
}
```

### 2. Dados Sensíveis

**Problema:**

```java
@Entity
public class Usuario {
    private String senhaHash;  // ❌ Exposto se retornar entidade
    private String tituloEleitoral;  // ⚠️ Sensível
}
```

**Solução com DTOs:**

```java
// DTO omite campos sensíveis
public record UsuarioDto(
    Long codigo,
    String nome,
    String email
    // senhaHash NÃO incluído
    // tituloEleitoral NÃO incluído
) {}
```

### 3. Dados Contextuais (Permissões)

DTOs podem incluir permissões calculadas baseadas no usuário:

```java
public record SubprocessoDetalheDto(
    Long codigo,
    String descricao,
    SituacaoSubprocesso situacao,
    SubprocessoPermissoesDto permissoes  // Calculado para o usuário autenticado
) {}

public record SubprocessoPermissoesDto(
    boolean podeEditar,
    boolean podeDisponibilizar,
    boolean podeValidar,
    boolean podeAceitar,
    boolean podeHomologar
) {}
```

Frontend usa permissões para mostrar/esconder botões:

```typescript
if (subprocesso.permissoes.podeEditar) {
    // Mostrar botão "Editar"
}
```

## Performance

### Projeções JPA

Para listagens grandes, considerar projeções em vez de DTOs completos:

```java
public interface SubprocessoProjection {
    Long getCodigo();
    String getDescricao();
    SituacaoSubprocesso getSituacao();
}

@Repository
public interface SubprocessoRepo extends JpaRepository<Subprocesso, Long> {
    
    List<SubprocessoProjection> findAllProjectedBy();  // Mais rápido que DTOs completos
}
```

### DTOs Otimizados

Para operações de alto volume, criar DTOs "enxutos":

```java
// DTO completo (para detalhes)
public record SubprocessoDetalheDto(...) {}

// DTO enxuto (para listagens)
public record SubprocessoListagemDto(
    Long codigo,
    String descricao,
    SituacaoSubprocesso situacao
) {}
```

## Estrutura de Pacotes

```
sgc/
├── processo/
│   ├── dto/           ✅ DTOs de processo
│   │   ├── ProcessoDto.java
│   │   ├── CriarProcessoReq.java
│   │   └── ProcessoDetalheDto.java
│   ├── mapper/        ✅ Mappers (MapStruct)
│   │   ├── ProcessoMapper.java
│   │   └── ProcessoDetalheMapper.java
│   ├── model/         ⚠️ Entidades JPA (NÃO expor)
│   │   └── Processo.java
│   └── service/
│       └── ProcessoFacade.java  ✅ Usa DTOs
│
├── subprocesso/
│   ├── dto/
│   ├── mapper/
│   ├── model/
│   └── service/
...
```

## Consequências

### Vantagens ✅

1. **Desacoplamento**
    - Evolução independente de API e modelo de dados
    - Frontend não quebra com mudanças no BD
    - Versioning de API facilitado

2. **Segurança**
    - Mass assignment protection automático
    - Controle fino de dados expostos
    - Ocultação de campos sensíveis

3. **Performance**
    - Queries otimizadas (apenas dados necessários)
    - Evita lazy loading issues
    - Projeções customizadas

4. **Clareza**
    - API autodocumentada (DTOs descrevem contratos)
    - Separação clara entrada/saída
    - Facilita geração de documentação (Swagger)

5. **Testabilidade**
    - Fácil mockar DTOs
    - Testes de contrato de API simplificados
    - Validações testáveis isoladamente

### Desvantagens ⚠️

1. **Duplicação de Código**
    - DTO + Entidade + Mapper para cada recurso
    - Mais arquivos para manter
    - **Mitigação**: MapStruct reduz boilerplate

2. **Overhead de Mapeamento**
    - Conversão Entidade ↔ DTO em toda requisição
    - **Mitigação**: MapStruct gera código eficiente (< 1ms)

3. **Curva de Aprendizado**
    - Novos desenvolvedores precisam entender padrão
    - **Mitigação**: Documentação + exemplos + code review

## Alternativas Consideradas

### Alternativa 1: Expor Entidades JPA (❌ Rejeitada)

- **Prós**: Simples, menos código
- **Contras**: Todos os problemas listados no Contexto
- **Motivo da Rejeição**: Inseguro, não escalável

### Alternativa 2: JsonView (❌ Rejeitada)

- **Prós**: Controle de serialização sem DTOs
- **Contras**:
    - Polui entidades com anotações de API
    - Difícil manter múltiplas views
    - Não resolve mass assignment
- **Motivo da Rejeição**: Acoplamento alto

### Alternativa 3: GraphQL (❌ Não Aplicável)

- **Prós**: Cliente escolhe campos
- **Contras**:
    - Mudança radical de arquitetura
    - Complexidade adicional
    - Time não familiarizado
- **Motivo da Rejeição**: Over-engineering para o contexto

### Alternativa 4: DTOs Obrigatórios (✅ ESCOLHIDA)

- **Prós**: Seguro, flexível, testável, performático
- **Contras**: Mais código (mitigado por MapStruct)
- **Motivo da Escolha**: Melhor práticas da indústria


## Referências

### Documentos Relacionados

- [ADR-001: Facade Pattern](ADR-001-facade-pattern.md) - Facades usam DTOs
- [ADR-003: Security Architecture](ADR-003-security-architecture.md) - Segurança com DTOs
- `/regras/backend-padroes.md` - Padrões de DTOs
- `/AGENTS.md` - Convenções de DTOs

### Código de Referência

- `sgc.processo.dto` - Pacote completo de DTOs de processo
- `sgc.subprocesso.mapper.SubprocessoDetalheMapper` - Mapper complexo
- `sgc.mapa.dto.ImpactoMapaDto` - DTO de análise complexa

### Padrões Externos

- Martin Fowler - DTO Pattern: https://martinfowler.com/eaaCatalog/dataTransferObject.html
- MapStruct Documentation: https://mapstruct.org
- Jakarta Bean Validation: https://beanvalidation.org
- OWASP - Mass Assignment: https://owasp.org/API-Security/editions/2023/en/0xa6-unrestricted-access-to-sensitive-business-flows/
