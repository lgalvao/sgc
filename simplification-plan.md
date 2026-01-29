# Plano de Simplificação do SGC - BACKLOG

**Data:** 2026-01-29  
**Foco Atual:** Refatoração Estrutural (Fase 2)  
**Status:** Fase 1 (Quick Wins) concluída. 5/8 Facades refatoradas.

---

## 🎯 O QUE FALTA FAZER (Prioritário)

Este é o foco imediato para as próximas sprints.

### 1. Refatoração de Facades (Violação ArchUnit)
Existem 3 facades que ainda injetam repositories diretamente, violando o padrão `Facade -> Service -> Repository`.

| Facade | Complexidade | Violations | Ação |
|--------|--------------|------------|------|
| **ProcessoFacade** | Média | 0 | ✅ CONCLUÍDO |
| **MapaFacade** | Média/Alta | 18 | Criar `MapaRepositoryService`, resolver circularidade |
| **UnidadeFacade** | Alta | 0 | ✅ CONCLUÍDO |
| **UsuarioFacade** | Alta | 0 | ✅ CONCLUÍDO |
| **SubprocessoFacade** | Alta | 0 | ✅ CONCLUÍDO |

### 2. Saneamento de DTOs e Repositories
| Item | Descrição | Esforço |
|------|-----------|---------|
| **DTOs Duplicados** | Resolver `ResponsavelDto` e `PerfilUnidadeDto` (duplicados em packages diferentes) | ✅ CONCLUÍDO |
| **Wrappers Triviais** | Remover wrappers como `ProcessoContextoDto` e `EmailDto` | ✅ CONCLUÍDO |
| **Padronização Repos** | `UsuarioRepo`: Padronizar JOIN vs LEFT JOIN FETCH em métodos de chefes | ✅ CONCLUÍDO |

---

## 📈 Resumo do Progresso

| Categoria | Status | Resultado |
|-----------|--------|-----------|
| **Mappers Backend** | ✅ 100% | Purificados (Subprocesso, Conhecimento). Sem injeção de Repo. |
| **Mappers Frontend** | ✅ 100% | Type safety adicionada (DTOs tipados), mappers triviais removidos. |
| **Stores Pinia** | ✅ 100% | Removido `.catch()` redundante, unificado tratamento de erro. |
| **Computed Anti-patterns**| ✅ 100% | Convertidos para getters reativos. |
| **ArchUnit** | ✅ Ativo | Regra ativa impedindo novos acessos diretos de Facade a Repo. |
| **Limpeza de Código** | ✅ 100% | DTOs unificados, Services de repositório criados. |

---

## 🔍 Detalhes das Pendências Prioritárias

### Resolver Duplicatas de DTOs
- **ResponsavelDto**: Existe em `sgc.subprocesso.dto` e `sgc.organizacao.dto` com conteúdos diferentes. Renomear para refletir o domínio (ex: `ParticipanteSubprocessoDto` vs `UnidadeResponsavelDto`).
- **PerfilUnidadeDto**: Resolver conflito entre `sgc.organizacao.dto` e `sgc.seguranca.login.dto`.

### Simplificação de Facades (Estratégia)
Para cada facade pendente:
1. Criar um Service de infraestrutura (ex: `XxxxRepositoryService`).
2. Mover métodos de persistência e busca básica do Repository para este Service.
3. Injetar o Service na Facade.
4. Validar via ArchUnit.

---

## 📅 Roadmap de Longo Prazo (Fase 3 - Opcional)

Estas tarefas têm menor impacto ou maior risco e devem ser avaliadas após a conclusão do backlog acima.

1. **Simplificação de Hierarquia (3→2 níveis)**: Eliminar o nível de Facade onde for redundante (Controller -> Service).
2. **Remoção de `@Transactional(readOnly=true)`**: Limpeza estética, ganho marginal de performance foi desconsiderado para o cenário de 20 usuários.
3. **Consolidação Final de DTOs**: Reduzir de 46 para ~30-35 DTOs através de herança ou @JsonView.

---

## ⚠️ Riscos Atuais
- **Regressão em Testes Complexos**: Facades como `UsuarioFacade` e `SubprocessoFacade` possuem muitos testes unitários que precisarão de refatoração nos mocks.
- **Quebra de Contrato Frontend**: A consolidação de DTOs pode impactar a tipagem no Vue 3 se não for feita em conjunto com os mappers já purificados.

---
*Plano confirmado e focado em dívida técnica estrutural.*
