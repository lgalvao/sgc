# Plano de Simplificação do Controle de Acesso

## 1. Introdução
O sistema atual utiliza uma arquitetura de segurança customizada ("Homegrown Framework") que envolve múltiplas camadas (`AccessControlService`, `Policies`, `SubprocessoSecurity` com mapas estáticos complexos). Embora funcional, essa abordagem gera código boilerplate, dificulta a manutenção e reinventa rodas que o Spring Security já provê nativamente.

Para um cenário de 5 a 10 usuários em intranet, a complexidade atual é injustificável. Este plano propõe uma migração para o padrão "Spring Security Native", utilizando recursos modernos (Spring Boot 3+ / Spring Security 6) para reduzir linhas de código e centralizar a lógica de forma declarativa.

## 2. Análise da Situação Atual

### Pontos de Complexidade Identificados
1.  **Duplicação de Verificações:** `AccessControlService` delega para `SubprocessoSecurity`, que é chamado tanto via `@PreAuthorize` (nos controllers) quanto programaticamente (nos services).
2.  **Lógica de "Mapão" Estático:** `SubprocessoSecurity` mantém um `Map<Acao, RegrasAcao>` gigante que mistura:
    *   Permissões de Perfil (RBAC).
    *   Validação de Estado do Negócio (Situação do Subprocesso).
    *   Regras de Localização (Hierarquia/Unidade).
3.  **Extração Manual de Usuário:** Os controllers repetem frequentemente a lógica `obterUsuarioAutenticado(principal)`, ignorando que o `FiltroJwt` já popula o `SecurityContext` corretamente.
4.  **Mistura de Responsabilidades:** A validação de "Estado" (ex: "Só pode editar se estiver em ANDAMENTO") está misturada com a segurança ("Só ADMIN pode editar").

## 3. Arquitetura Proposta (Spring Native)

### 3.1. PermissionEvaluator Customizado
Substituiremos o `SubprocessoSecurity` e `AccessControlService` por uma implementação padrão de `PermissionEvaluator`. Isso habilita o uso elegante da expressão `hasPermission`.

**Como ficará no Controller:**
```java
@PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR')")
public void editar(@PathVariable Long codigo, ...)
```

### 3.2. Lógica de Acesso Centralizada ("Regra de Ouro")
A lógica será simplificada para dois conceitos, conforme `acesso.md`:
1.  **Visualização (Leitura):** Baseada na hierarquia da Unidade Responsável.
2.  **Execução (Escrita):** Baseada estritamente na Localização Atual (Unidade do Usuário == Localização do Subprocesso).

### 3.3. Injeção de Usuário
Removeremos o método `obterUsuarioAutenticado` e usaremos a injeção direta:
```java
public void acao(@AuthenticationPrincipal Usuario usuario)
```

## 4. Plano de Implementação (Passo a Passo)

### Passo 1: Limpeza e Preparação
*   **Ação:** Remover usos de `AccessControlService` dos Services de negócio (`SubprocessoCadastroWorkflowService`, etc.).
    *   *Justificativa:* A segurança deve ser declarativa na borda (Controller). Se o Service precisa validar estado (ex: "não pode editar finalizado"), isso é Regra de Negócio, não Segurança, e deve ser feito via `if/else` simples ou validação de domínio, não via `AccessControlService`.

### Passo 2: Implementar `SgcPermissionEvaluator`
*   **Ação:** Criar `SgcPermissionEvaluator` implementando `PermissionEvaluator`.
*   **Lógica:**
    *   Recebe `Authentication`, `targetId`, `targetType`, `permission`.
    *   Carrega o `Subprocesso` (com cache L1 do Hibernate, o impacto é mínimo).
    *   Verifica:
        1.  **Perfil:** O usuário tem o perfil exigido? (Simples `hasRole` ou verificação no objeto Usuario).
        2.  **Localização (Escrita):** `usuario.getUnidadeAtiva().equals(subprocesso.getLocalizacaoAtual())`.
        3.  **Hierarquia (Leitura):** `hierarquiaService.isMesmaOuSubordinada(...)`.

### Passo 3: Refatorar Controllers
*   **Ação:** Substituir `@PreAuthorize("@subprocessoSecurity.canExecute(...)")` por `@PreAuthorize("hasPermission(..., '...')")`.
*   **Ação:** Remover chamadas manuais ao `AccessControlService`.
*   **Ação:** Substituir `obterUsuarioAutenticado` por `@AuthenticationPrincipal`.

### Passo 4: Remover Código Morto
*   **Ação:** Excluir `AccessControlService`, `SubprocessoSecurity` (versão antiga), `*AccessPolicy`.
*   **Ação:** Excluir DTOs e Enums de suporte que não sejam mais usados.

## 5. Exemplo de Código (Target)

**SgcPermissionEvaluator.java (Esboço):**
```java
@Component
public class SgcPermissionEvaluator implements PermissionEvaluator {
    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        if ("Subprocesso".equals(targetType)) {
             Subprocesso sp = repo.findById((Long) targetId).orElseThrow();
             return verificarAcesso(usuario, sp, (String) permission);
        }
        return false;
    }

    private boolean verificarAcesso(Usuario u, Subprocesso sp, String acao) {
        // Implementação direta da Regra de Ouro do acesso.md
        // 1. É Admin?
        // 2. É Leitura? -> Checa Hierarquia
        // 3. É Escrita? -> Checa Localização (Mesmo para Admin!)
    }
}
```

## 6. Benefícios
1.  **Menos Código:** Redução estimada de ~500 linhas de código (policies, maps, wrappers).
2.  **Padrão de Mercado:** Qualquer desenvolvedor Spring entende `hasPermission`.
3.  **Performance:** Menos indireções e lookups redundantes.
4.  **Manutenibilidade:** Regras centralizadas em um único ponto (`Evaluator`) e claras no `acesso.md`.
