# ADR-003: Arquitetura de Controle de Acesso Centralizada

---

## Contexto

O sistema SGC precisa controlar acesso a operações baseado em múltiplos fatores:

1. **Perfil do Usuário**: ADMIN, GESTOR, CHEFE, SERVIDOR
2. **Hierarquia de Unidades**: Unidade do usuário vs. unidade do recurso
3. **Estado do Recurso**: Situação do subprocesso/processo
4. **Ownership**: Usuário pertence à mesma unidade do recurso

Historicamente, o SGC implementava controle de acesso de forma **dispersa**:

- Controllers com `@PreAuthorize`
- Services com verificações programáticas ad-hoc
- Lógica de permissões espalhada em múltiplos arquivos
- Padrões inconsistentes entre módulos

Isso resultava em:

- ❌ **Difícil auditoria**: Impossível rastrear todas as decisões de acesso
- ❌ **Manutenção complexa**: Mudanças em regras requeriam alterações em múltiplos locais
- ❌ **Risco de bypass**: Lógica duplicada aumentava chance de inconsistências
- ❌ **Testabilidade baixa**: Testes de segurança dispersos e incompletos

## Decisão

Implementamos uma **arquitetura de controle de acesso centralizada em 3 camadas**:

```
┌─────────────────────────────────────────────────────────┐
│           CAMADA 1: HTTP (Authentication)              │
│  - ConfigSeguranca (SecurityFilterChain)               │
│  - @PreAuthorize nos Controllers (verificação de roles)│
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│         CAMADA 2: AUTHORIZATION (Centralizada)         │
│  ┌───────────────────────────────────────────────────┐ │
│  │        AccessControlService (Orquestrador)        │ │
│  │  - verificarPermissao(usuario, acao, recurso)    │ │
│  │  - podeExecutar(usuario, acao, recurso)          │ │
│  └───────────────────────────────────────────────────┘ │
│         ↓                    ↓                    ↓     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  Processo    │  │ Subprocesso  │  │  Atividade   │ │
│  │AccessPolicy  │  │AccessPolicy  │  │AccessPolicy  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │     HierarchyService (Hierarquia de Unidades)    │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │   AccessAuditService (Auditoria de Decisões)     │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│         CAMADA 3: BUSINESS LOGIC (Services)            │
│  - Executam regras de negócio                          │
│  - SEM verificações de acesso diretas                  │
│  - Confiam que Camada 2 já validou permissões          │
└─────────────────────────────────────────────────────────┘
```

### Componentes Principais

#### 1. `AccessControlService` (Orquestrador)

Service centralizado que:

- Recebe requisições de verificação de permissão
- Delega para a `AccessPolicy` apropriada
- Loga todas as decisões via `AccessAuditService`
- Lança `ErroAccessoNegado` quando acesso negado

```java
@Service
public class AccessControlService {
    
    public void verificarPermissao(Usuario usuario, Acao acao, Object recurso) {
        AccessPolicy policy = getPolicyForResource(recurso);
        boolean permitido = policy.canExecute(usuario, acao, recurso);
        
        if (permitido) {
            auditService.logAccessGranted(usuario, acao, recurso);
        } else {
            auditService.logAccessDenied(usuario, acao, recurso, policy.getMotivoNegacao());
            throw new ErroAccessoNegado(policy.getMotivoNegacao());
        }
    }
}
```

#### 2. `AccessPolicy<T>` (Interface)

Políticas especializadas por tipo de recurso:

```java
public interface AccessPolicy<T> {
    boolean canExecute(Usuario usuario, Acao acao, T recurso);
    String getMotivoNegacao();
}
```

**Implementações:**

- `ProcessoAccessPolicy` - Regras para processos
- `SubprocessoAccessPolicy` - Regras para subprocessos
- `AtividadeAccessPolicy` - Regras para atividades
- `MapaAccessPolicy` - Regras para mapas

#### 3. `Acao` (Enum)

Enumeração de todas as ações do sistema:

```java
public enum Acao {
    // Processo
    CRIAR_PROCESSO, EDITAR_PROCESSO, EXCLUIR_PROCESSO,
    INICIAR_PROCESSO, FINALIZAR_PROCESSO,
    
    // Subprocesso - Cadastro
    VISUALIZAR_SUBPROCESSO, EDITAR_CADASTRO,
    DISPONIBILIZAR_CADASTRO, DEVOLVER_CADASTRO,
    ACEITAR_CADASTRO, HOMOLOGAR_CADASTRO,
    
    // Subprocesso - Mapa
    VERIFICAR_IMPACTOS, APRESENTAR_SUGESTOES,
    VALIDAR_MAPA, DEVOLVER_MAPA, ACEITAR_MAPA,
    HOMOLOGAR_MAPA, AJUSTAR_MAPA,
    
    // ... outras ações
}
```

#### 4. `HierarchyService`

Centraliza verificações de hierarquia de unidades:

```java
@Service
public class HierarchyService {
    boolean isSubordinada(Unidade alvo, Unidade superior);
    List<Unidade> buscarSubordinadas(Unidade raiz);
    List<Long> buscarCodigosHierarquia(Long codUnidade);
}
```

#### 5. `AccessAuditService`

Loga todas as decisões de acesso para auditoria:

```java
@Service
public class AccessAuditService {
    void logAccessGranted(Usuario usuario, Acao acao, Object recurso);
    void logAccessDenied(Usuario usuario, Acao acao, Object recurso, String motivo);
}
```

Logs no formato:

```
ACCESS_GRANTED: user=333333333333, action=VALIDAR_MAPA, resource=Subprocesso:42, timestamp=...
ACCESS_DENIED: user=444444444444, action=HOMOLOGAR_CADASTRO, resource=Subprocesso:42, 
               reason="Perfil CHEFE não autorizado. Requer: [ADMIN]", timestamp=...
```

### Exemplo de Implementação: SubprocessoAccessPolicy

```java
@Component
public class SubprocessoAccessPolicy implements AccessPolicy<Subprocesso> {
    
    // Mapeamento: Ação → (Perfis, Situações, Requisito Hierarquia)
    private static final Map<Acao, RegrasAcao> REGRAS = Map.ofEntries(
        entry(DISPONIBILIZAR_CADASTRO, new RegrasAcao(
            Set.of(CHEFE),
            Set.of(CADASTRO_EM_ANDAMENTO),
            RequisitoHierarquia.MESMA_UNIDADE
        )),
        entry(ACEITAR_CADASTRO, new RegrasAcao(
            Set.of(ADMIN, GESTOR),
            Set.of(CADASTRO_DISPONIBILIZADO),
            RequisitoHierarquia.SUPERIOR_IMEDIATA
        ))
        // ... 24 outras ações
    );
    
    @Override
    public boolean canExecute(Usuario usuario, Acao acao, Subprocesso subprocesso) {
        RegrasAcao regras = REGRAS.get(acao);
        
        return temPerfilPermitido(usuario, regras.perfisPermitidos)
            && temSituacaoPermitida(subprocesso, regras.situacoesPermitidas)
            && verificaHierarquia(usuario, subprocesso.getUnidade(), regras.requisitoHierarquia);
    }
}
```

## Fluxo de Uso

### Antes (Disperso)

```java
// Controller
@PostMapping("/{id}/disponibilizar")
@PreAuthorize("hasRole('CHEFE')")  // Verificação 1
public RespostaDto disponibilizar(@PathVariable Long id, Auth auth) {
    return service.disponibilizar(id, auth);
}

// Service
public RespostaDto disponibilizar(Long id, Authentication auth) {
    Subprocesso sp = repo.findById(id).orElseThrow();
    
    // Verificação 2 (duplicada)
    if (!auth.hasRole("CHEFE")) {
        throw new ErroAccessoNegado("Apenas CHEFE");
    }
    
    // Verificação 3
    if (sp.getSituacao() != CADASTRO_EM_ANDAMENTO) {
        throw new ErroProcessoEmSituacaoInvalida("...");
    }
    
    // Verificação 4
    if (!sp.getUnidade().equals(usuarioLogado.getUnidade())) {
        throw new ErroAccessoNegado("Unidade diferente");
    }
    
    // ... lógica de negócio
}
```

### Depois (Centralizado)

```java
// Controller
@PostMapping("/{id}/disponibilizar")
@PreAuthorize("hasRole('CHEFE')")  // Apenas autenticação básica
public RespostaDto disponibilizar(@PathVariable Long id, Auth auth) {
    return facade.disponibilizar(id, auth);
}

// Facade
public RespostaDto disponibilizar(Long id, Authentication auth) {
    Usuario usuario = usuarioService.buscarPorTituloEleitoral(auth.getName());
    Subprocesso sp = repo.findById(id).orElseThrow();
    
    // UMA ÚNICA verificação centralizada
    accessControlService.verificarPermissao(usuario, DISPONIBILIZAR_CADASTRO, sp);
    
    // ... delega para service de negócio (SEM verificações de acesso)
    return cadastroService.disponibilizar(sp);
}

// Service (PURO - apenas lógica de negócio)
public RespostaDto disponibilizar(Subprocesso sp) {
    sp.setSituacao(CADASTRO_DISPONIBILIZADO);
    sp.setDataDisponibilizacao(LocalDate.now());
    repo.save(sp);
    
    eventPublisher.publishEvent(new EventoCadastroDisponibilizado(sp.getCodigo()));
    
    return new RespostaDto("Cadastro disponibilizado com sucesso");
}
```

## Validação

### Testes ArchUnit

Criado `CyclicDependencyTest.java` com:
```java
@ArchTest
static final ArchRule no_cycles_within_service_packages = slices()
        .matching("sgc.(*).service.(**)")
        .should()
        .beFreeOfCycles();
```

## Consequências

### Vantagens ✅

1. **Auditabilidade Total**
    - Todas as decisões de acesso logadas em um único ponto
    - Fácil rastrear quem tentou acessar o quê
    - Compliance com LGPD e requisitos de auditoria

2. **Manutenibilidade**
    - Mudança de regras em um único local (AccessPolicy)
    - Fácil adicionar novas ações (enum + regras)
    - Código de negócio limpo (sem lógica de segurança)

3. **Testabilidade**
    - Testes de segurança centralizados em `AccessControlServiceTest`
    - Testes de policies isolados

4. **Consistência**
    - Padrão único para todas as verificações
    - Impossível esquecer verificações (compilador força)
    - Mensagens de erro padronizadas

5. **Performance**
    - Verificações otimizadas (cache de hierarquias)
    - Sem duplicação de queries
    - Decisões rápidas (mapa de regras em memória)

6. **Segurança**
    - Fail-safe defaults (padrão é negar)
    - Impossível bypass (camada obrigatória)
    - Auditoria automática de todas as tentativas

### Desvantagens ⚠️

1. **Complexidade Inicial**
    - Curva de aprendizado para novos desenvolvedores
    - Mais arquivos/classes
    - **Mitigação**: Documentação completa + exemplos

2. **Overhead de Abstração**
    - Chamada adicional (AccessControlService)
    - Lookup de policy
    - **Mitigação**: Overhead < 1ms (imperceptível)

3. **Manutenção de Enum Acao**
    - Precisa adicionar nova ação para cada operação
    - **Mitigação**: Processo claro + checklist

### Riscos Mitigados ✅

| Risco Anterior          | Mitigação                                            |
|-------------------------|------------------------------------------------------|
| Verificações esquecidas | Enum `Acao` força mapeamento completo                |
| Lógica inconsistente    | Políticas centralizadas garantem consistência        |
| Bypass de segurança     | Camada obrigatória + testes arquiteturais (ArchUnit) |
| Falta de auditoria      | `AccessAuditService` loga tudo automaticamente       |
| Difícil rastreamento    | Logs estruturados + um único ponto de decisão        |

## Alternativas Consideradas

### Alternativa 1: Manter Status Quo (Rejeitada)

- **Prós**: Sem mudanças, sem riscos
- **Contras**: Problemas de auditoria, manutenção e segurança permanecem
- **Motivo da Rejeição**: Insustentável a longo prazo

### Alternativa 2: Spring Security Method Security Pura (Rejeitada)

- **Prós**: Padrão do Spring, bem documentado
- **Contras**:
    - Difícil centralizar lógica complexa (hierarquia + estado)
    - Auditoria requer aspect customizado
    - SpEL complexo e difícil de testar
- **Motivo da Rejeição**: Não atende requisitos de auditoria e complexidade

### Alternativa 3: AOP com Aspects (Rejeitada)

- **Prós**: Separação de concerns via AOP
- **Contras**:
    - Mágica implícita (difícil debugar)
    - Ordem de execução de aspects pode ser problemática
    - Mais complexo que solução explícita
- **Motivo da Rejeição**: Muito mágico, prefere-se explícito

### Alternativa 4: AccessControlService + Policies (✅ ESCOLHIDA)

- **Prós**:
    - Centralização explícita
    - Fácil testar e auditar
    - Extensível (novas policies)
    - Performance adequada
- **Contras**: Overhead mínimo de abstração
- **Motivo da Escolha**: Melhor trade-off entre clareza, testabilidade e auditoria

## Lições Aprendidas

1. **Centralização Compensa**: Overhead inicial vale a pena para manutenibilidade a longo prazo
2. **Auditoria é Crucial**: Logs estruturados facilitam compliance e debugging
3. **Políticas Declarativas**: Map de regras é mais claro que if/else aninhados
4. **Testes Arquiteturais**: ArchUnit garante que padrão seja seguido

## Referências

- ADR-001: Facade Pattern
- ADR-002: Unified Events Pattern
- [Spring @Lazy Documentation](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-lazy-init.html)
- [Circular Dependencies in Spring](https://www.baeldung.com/circular-dependencies-in-spring)
