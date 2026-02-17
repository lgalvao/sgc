# ADR-008: Decisões de Simplificação Arquitetural

**Data:** 17 de Fevereiro de 2026  
**Status:** ✅ Fase 2 Concluída / 🚀 Fase 4 Iniciada  
**Versão:** 1.1

---

## Contexto

O sistema SGC identificou sobre-engenharia técnica em aproximadamente 60-70% acima do necessário para sua escala real. Iniciou-se um processo de simplificação incremental em fases.

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

## Métricas e Resultados (Atualizado 17/02/2026)

| Componente | Baseline | Atual | Redução | Status |
|------------|----------|-------|---------|--------|
| Services | 17 | 17 | 0% | ✅ |
| Facades | 14 | 12 | -14% | ✅ |
| DTOs | 86 | 64 | -25% | 🚀 Fase 4 |
| Mappers | 15 | 9 | -40% | 🚀 Fase 4 |
| Composables | 19 | 13 | -32% | ✅ |

---

## Próximos Passos
1. Eliminar `AlertaDto` e `AlertaMapper` (Módulo Alerta).
2. Simplificar `ProcessoDto` e mappers associados.
3. Consolidar requests de campo único no módulo Subprocesso.
