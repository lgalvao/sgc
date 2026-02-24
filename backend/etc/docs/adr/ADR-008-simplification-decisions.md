# ADR-008: Decisões de Simplificação Arquitetural

**Data:** 17 de Fevereiro de 2026  
**Status:** ✅ Fases 1-2 Concluídas / 🚀 Fases 4-5 Iniciadas  
**Versão:** 1.2 (Atualizado 2026-02-24)

---

## Contexto

O sistema SGC identificou sobre-engenharia técnica em aproximadamente 60-70% acima do necessário para sua escala real (5-10 usuários simultâneos, intranet). Iniciou-se um processo de simplificação incremental em fases.

---

## Fase 1: Quick Wins (Concluída ✅)

**Objetivo:** Eliminações de baixo risco com alto impacto.  
**Conclusão:** 16 de Fevereiro de 2026.

- **Consolidação de Services:** Redução de 9 para 4 services de organização.
- **Simplificação de Stores:** Consolidação da store de processos no frontend.
- **Eliminação de Composables:** Remoção de lógica view-specific para dentro dos componentes Vue.

---

## Fase 2: Simplificação Estrutural (Concluída ✅)

**Objetivo:** Reduzir camadas de indireção e redundância de dados.  
**Conclusão:** 17 de Fevereiro de 2026.

- **Eliminação de Facades Pass-Through:** `AcompanhamentoFacade` e `ConfiguracaoFacade` removidas.
- **Introdução de @JsonView:** Substituição inicial de DTOs Response por Entidades anotadas.
- **Reforço do ArchUnit:** Regras automatizadas para garantir o uso correto de Facades e impedir vazamento de dados.
- **Eliminação do Framework de Segurança Custom:** `AccessControlService`, `AccessPolicy<T>`, `AccessAuditService`, enum `Acao` removidos. Substituídos por `SgcPermissionEvaluator` (implementa `PermissionEvaluator` do Spring Security). Ver ADR-003.

---

## Fase 4: Consolidação de DTOs e Mappers (Iniciada 🚀)

**Objetivo:** Eliminar boilerplate de formatação e unificar modelos de dados.

### Decisões Principais

#### 4.1. Responsabilidade de Formatação no Frontend
**Decisão:** O Backend deixará de enviar campos pré-formatados (`dataCriacaoFormatada`, `situacaoLabel`).
- **Motivo:** Redução drástica de boilerplate em Mappers e payload JSON.
- **Implementação:** Criação de `utils/formatters.ts` no frontend para lidar com a apresentação.

#### 4.2. Unificação de Tipos no Frontend
**Decisão:** Mesclar `tipos.ts` e `dtos.ts` para eliminar mapeamentos manuais redundantes no frontend.

---

## Fase 5: Consolidação Arquitetural (Planejada 🚀)

**Objetivo:** Eliminar fragmentação desnecessária e código morto. Ver [implementation_plan.md](/simplification-suggestions.md).

### 5.1. Remoção de Código Morto (Pact)
- Pact já foi removido das dependências, mas arquivos de teste e config permanecem como código morto
- Alvo: `FrontendBackendPactTest.java`, `ProcessoService.pact.spec.ts`, `frontend/pact/`, `vitest.pact.config.ts`

### 5.2. Consolidação do Módulo Subprocesso
- `SubprocessoFacade` (353 linhas, pass-through puro) → Controllers injetarão services diretamente
- 4 controllers → 1 `SubprocessoController` (ver ADR-005 reavaliação)
- Workflow services fragmentados → Consolidar em `SubprocessoService`

### 5.3. Remoção de Mappers Manuais do Frontend
- 9 mappers + 6 testes → Usar tipos da API diretamente ou tipos TypeScript manuais

---

## Métricas e Resultados

| Componente | Baseline | Atual | Meta Fase 5 | Status |
|------------|----------|-------|-------------|--------|
| Services | 17 | 17 | ~12 | 🚀 |
| Facades | 14 | 12 | 11 | 🚀 |
| Controllers (subprocesso) | 4 | 4 | 1 | 🚀 |
| DTOs | 86 | 64 | - | 🚀 Fase 4 |
| Mappers (backend) | 15 | 9 | - | 🚀 Fase 4 |
| Mappers (frontend) | 9 | 9 | 0 | 🚀 Fase 5 |
| Código morto Pact | 4+ arq. | 4+ arq. | 0 | 🚀 Fase 5 |
| Composables | 19 | 13 | - | ✅ |

---

## Referências

- [acesso.md](/acesso.md) — Regras de negócio e casos de uso
- ADR-001: Facade Pattern (em revisão)
- ADR-003: Security Architecture (reescrito)
- ADR-005: Controller Organization (em revisão)
