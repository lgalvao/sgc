# Plano de Redução de Complexidade Desnecessária - SGC Backend

## Sumário Executivo

Este plano detalha a remoção sistemática de complexidade desnecessária no backend do SGC.

**Status Atual:** Sprints 1, 2, 3 e 4 concluídas. Foco atual na Sprint 5.

---

## Sprints Concluídas (Resumo)

- **Sprint 1:** Análise e Preparação (Auditorias de null checks e validações).
- **Sprint 2:** Refatoração das Top 5 Classes Mais Complexas (`SubprocessoWorkflowService`, `SubprocessoAccessPolicy`, `SubprocessoFacade`, `UsuarioFacade`, `UnidadeFacade`).
- **Sprint 3:** Refatoração de Validações de DTOs (`@NotEmpty`, `@NotBlank`).
- **Sprint 4:** Simplificação de `ValidadorDadosOrgService` (Foco em validação de startup).

---

## Sprint 5: Refatoração de Classes Médias (EM ANDAMENTO)

### Objetivo
Aplicar aprendizados das sprints anteriores nas classes ranqueadas 6-20.

### Task 5.1: Batch Refactoring - PainelFacade, ImpactoMapaService, ProcessoFacade
**Abordagem:** Aplicar mesmos padrões de Sprint 2.

**Ações por classe:**

#### PainelFacade
- Remover verificação redundante em `formatarUnidadesParticipantes` (`unidade != null && unidade.getSigla() != null`).

#### ImpactoMapaService
- Remover filtros redundantes `.filter(dto -> dto.getCodigo() != null)` e `.filter(dto -> dto.getDescricao() != null)` nos métodos de detecção de mudanças.

#### ProcessoFacade
- Remover validação explícita de `unidade.getTipo() == INTERMEDIARIA` nos métodos `criar` e `atualizar`.

### Task 5.2: Batch Refactoring - SubprocessoCrudService
**Foco:** Remover checks de Mapa e relacionamentos sempre presentes.

#### SubprocessoCrudService
- Remover throw de "Mapa não associado" em `buscarSubprocessoComMapa`. Mapa é invariante após criação.

### Task 5.3: EventoProcessoListener - Simplificação de Lógica Condicional
**Problemas Identificados:**
- Listener assíncrono com muitos branches e aninhamento.

**Ações:**
1. Extrair lógica interna dos loops para métodos privados.
2. Usar guard clauses para reduzir nesting.

---

## Sprint 6: Consolidação e Documentação (PENDENTE)

### Objetivo
Consolidar mudanças, atualizar documentação e criar guias para evitar regressão.

### Task 6.1: Atualizar Guias de Desenvolvimento
- Atualizar `regras/backend-padroes.md`.
- Atualizar `regras/guia-validacao.md`.
- Criar `regras/guia-nullability.md`.

### Task 6.2: Criar ArchUnit Rules para Prevenir Regressão
- Criar regras para proibir validações redundantes sobre views e null checks desnecessários.

### Task 6.3: Métricas de Impacto
- Re-executar análise de complexidade e comparar métricas.

---

## Sprint 7: Validação Final e Rollout (PENDENTE)

### Objetivo
Garantir que todas as mudanças foram validadas e sistema está estável.

### Task 7.1: Execução Completa da Suite de Testes
- Executar TODOS os testes unitários e E2E.

### Task 7.2: Code Review por Pares
- Revisão humana das mudanças.

### Task 7.3: Monitoramento Pós-Deploy
- Monitorar logs para novos NPEs.
