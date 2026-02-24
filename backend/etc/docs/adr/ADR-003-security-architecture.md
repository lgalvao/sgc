# ADR-003: Arquitetura de Controle de Acesso — SgcPermissionEvaluator

**Status:** ✅ Ativo (Reescrito 2026-02-24)  
**Supersede:** Versão anterior baseada em `AccessControlService` + `AccessPolicy` (removidos em 2026-02)

---

## Contexto

O SGC é um sistema interno para 5-10 usuários simultâneos. O controle de acesso precisa considerar:

1. **Perfil do Usuário**: `ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`
2. **Hierarquia de Unidades**: Relação entre a unidade do usuário e a unidade do recurso
3. **Localização do Subprocesso**: Para ações de escrita, o subprocesso deve estar na mesma unidade do usuário

### Histórico

Anteriormente o SGC utilizava um framework custom (`AccessControlService` → `AccessPolicy` → `AccessAuditService`).
Essa abordagem foi removida por ser **sobre-engenheirada para a escala do sistema**: 4 classes de policy, 1 orquestrador, 1 auditor e 1 enum,
quando toda a lógica de permissão cabia em uma única classe de ~230 linhas usando a interface padrão do Spring Security.

---

## Decisão

Implementar o controle de acesso através de um **único `PermissionEvaluator`** do Spring Security,
eliminando toda a camada custom de policies e auditoria.

### Arquitetura Atual

```
┌──────────────────────────────────────────────────────┐
│           CAMADA 1: HTTP (Authentication)            │
│  - ConfigSeguranca (SecurityFilterChain)             │
│  - Autenticação via ActiveDirectory (prod) ou       │
│    bypass em ambiente de testes                      │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│       CAMADA 2: AUTHORIZATION (PermissionEvaluator)  │
│  ┌────────────────────────────────────────────────┐  │
│  │         SgcPermissionEvaluator                 │  │
│  │  - implements PermissionEvaluator (Spring)     │  │
│  │  - "Regra de Ouro" centralizada               │  │
│  │  - Perfil + Hierarquia + Localização           │  │
│  └────────────────────────────────────────────────┘  │
│                        ↓                              │
│  ┌────────────────────────────────────────────────┐  │
│  │   HierarquiaService (hierarquia de unidades)   │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│         CAMADA 3: BUSINESS LOGIC (Services)          │
│  - Executam regras de negócio                        │
│  - SEM verificações de acesso diretas                │
└──────────────────────────────────────────────────────┘
```

### Componente Principal: `SgcPermissionEvaluator`

Localização: `sgc.seguranca.SgcPermissionEvaluator`

```java
@Component
public class SgcPermissionEvaluator implements PermissionEvaluator {

    // Ações de escrita exigem que o subprocesso esteja na mesma unidade do usuário
    private static final Set<String> ACOES_ESCRITA = Set.of(
        "EDITAR_CADASTRO", "DISPONIBILIZAR_CADASTRO", ...
    );

    @Override
    public boolean hasPermission(Authentication auth, Object target, Object permission) {
        // Dispatch por tipo: Subprocesso ou Processo
    }

    private boolean checkSubprocesso(Usuario usuario, Subprocesso sp, String acao) {
        // 1. Leitura → Hierarquia (Admin vê tudo, Gestor vê subordinadas, Chefe/Servidor só sua)
        // 2. Escrita → RBAC (checkPerfil) + Localização (Regra de Ouro)
    }
}
```

### A "Regra de Ouro"

Regra central que governa todo o controle de acesso:

| Tipo de Ação | Regra |
|:--|:--|
| **Leitura** | Hierarquia da Unidade Responsável do subprocesso |
| **Escrita** | Localização Atual do Subprocesso (unidade do usuário == localização) |

### Uso nos Controllers

```java
// Via @PreAuthorize com SpEL
@PostMapping("/{codigo}/disponibilizar")
@PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'DISPONIBILIZAR_CADASTRO')")
public RespostaDto disponibilizar(@PathVariable Long codigo) {
    return facade.disponibilizar(codigo);
}
```

---

## Consequências

### Vantagens ✅

1. **Simplicidade** — Toda a lógica de permissão em uma única classe (~230 linhas)
2. **Padrão Spring** — Usa `PermissionEvaluator` nativo, sem framework custom
3. **Testabilidade** — Fácil testar com `@WithMockUser` e testes de integração
4. **Manutenibilidade** — Mudanças de regras em um único local
5. **Auditoria** — Logging via SLF4J integrado nos pontos de negação

### O que foi removido

| Componente Antigo | Motivo da Remoção |
|:--|:--|
| `AccessControlService` | Orquestrador desnecessário para 1 evaluator |
| `AccessPolicy<T>` (interface) | Sobre-abstração — `if/else` por tipo basta |
| `ProcessoAccessPolicy` | Absorvido em `checkProcesso()` |
| `SubprocessoAccessPolicy` | Absorvido em `checkSubprocesso()` |
| `AtividadeAccessPolicy` | Absorvido em `checkPerfil()` |
| `MapaAccessPolicy` | Absorvido em `checkSubprocesso()` |
| `AccessAuditService` | Substituído por `log.info()` nos pontos de negação |
| `Acao` (enum) | Substituído por `String` — enum não justificava overhead |

---

## Referências

- [acesso.md](/acesso.md) — Documento autoritativo com todas as regras de negócio, perfis, CDUs e a "Regra de Ouro"
- Spring Security `PermissionEvaluator`: [docs](https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html)
- ADR-001: Facade Pattern
- ADR-008: Decisões de Simplificação
