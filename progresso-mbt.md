# Progresso MBT (Mutation Based Testing)

Este arquivo registra o progresso e as pendências da melhoria de testes usando Mutation Testing, separado do guia estático.

## Baseline Atual (2025-12-24) - Atualizado

| Métrica | Valor Anterior | Valor Atual | Meta |
|---------|----------------|-------------|------|
| **KILLED** | 535 (51.7%) | **599 (58%)** | - |
| **SURVIVED** | 242 (23.4%) | **203 (19.6%)** | 0% |
| **NO_COVERAGE** | 257 (24.9%) | **233 (22.5%)** | 0% |
| **Mutation Score** | ~52% | **58%** | ≥ 70% |

### Classes com mais mutantes sobreviventes (Top 5)

| Classe | SURVIVED (Anterior) | SURVIVED (Atual) | Status |
|--------|---------------------|------------------|--------|
| ProcessoService | 38 | 38 | ⚠️ Pendente |
| ImpactoMapaService | 37 | 37 | ⚠️ Pendente |
| SubprocessoPermissoesService | 29 | **~20** | ✅ Melhorado |
| SubprocessoMapaWorkflowService | 26 | 26 | ⚠️ Pendente |
| SubprocessoDtoService | 18 | 18 | ⚠️ Pendente |

### Melhorias Realizadas

#### 1. SubprocessoPermissoesService
- Adicionados testes para hierarquia de unidades (casos negativos de `isSubordinada`).
- Adicionados testes explícitos para ramificações de validação de `AJUSTAR_MAPA` (`REVISAO_MAPA_AJUSTADO` vs `REVISAO_CADASTRO_HOMOLOGADA`).
- **Resultado**: Redução de mutantes sobreviventes em métodos críticos de segurança e lógica de loop.
