# Visão Geral - Refatoração Frontend SGC

## Contexto

O frontend do SGC está bem arquitetado mas apresenta padrões de "protótipo sofisticado" que impactam performance e manutenibilidade. Esta refatoração visa transformá-lo em uma aplicação production-grade através de melhorias incrementais executadas por agentes de IA.

## Problemas Identificados

### 🔴 Alta Prioridade

1. **API Chaining (Orquestração no Cliente)**
   - **Impacto**: 5 requisições HTTP → 800ms de latência
   - **Locais**: `CadMapa.vue`, `ProcessoView.vue`
   - **Solução**: Criar endpoints BFF agregados

2. **Travessia de Árvores no Cliente**
   - **Impacto**: Lógica O(n) que deveria ser O(1) no backend
   - **Locais**: `useSubprocessoResolver.ts`
   - **Solução**: Eliminar composable e usar endpoints diretos

### 🟡 Média Prioridade

3. **Tratamento de Erros Duplicado**
   - **Impacto**: ~135 linhas de código repetido
   - **Locais**: `CadMapa.vue`, `CadProcesso.vue`, `UnidadeView.vue`
   - **Solução**: Criar `useFormErrors` composable

4. **Lógica Defensiva nas Stores**
   - **Impacto**: Re-fetches desnecessários, requisições duplicadas
   - **Locais**: `stores/mapas.ts`, `stores/processos.ts`
   - **Solução**: Confiar no backend como fonte única de verdade

## Objetivo da Refatoração

### Métricas de Sucesso

| Métrica | Antes | Meta |
|---------|-------|------|
| **Requisições/tela** | 4-5 | 1-2 (-60%) |
| **Latência média** | ~800ms | ~250ms (-69%) |
| **Código duplicado** | ~135 linhas | 0 linhas (-100%) |
| **Linhas totais (Views)** | ~4.884 | ~3.500 (-28%) |

### Ganhos Esperados

- ⚡ **Performance**: 60-75% redução de latência
- 🧹 **Código**: 25-30% menos linhas, mais simples
- 🐛 **Confiabilidade**: 40-50% menos pontos de falha
- ⏱️ **Produtividade**: 30% mais rápido para novas features

## Estrutura de Sprints

### Sprint 1: BFF e Agregação de Dados (1-2 semanas)
- Criar endpoints agregados no backend
- Refatorar `CadMapa.vue` e `ProcessoView.vue`
- Eliminar `useSubprocessoResolver`

### Sprint 2: Composables Reutilizáveis (1 semana)
- Criar `useFormErrors` composable
- Migrar todas as Views para usar o novo padrão
- Padronizar tratamento de erros

### Sprint 3: Simplificação de Stores (1 semana)
- Remover lógica defensiva
- Simplificar fluxo de dados
- Garantir backend como fonte única de verdade

### Sprint 4: Otimizações Avançadas (Opcional)
- Cache local para dados estáticos
- Optimistic updates
- Lazy loading de componentes

## Princípios para Agentes de IA

### ✅ Fazer

1. **Seguir os padrões existentes** do projeto
2. **Manter compatibilidade** com código não refatorado
3. **Adicionar testes** para cada mudança
4. **Documentar** decisões técnicas
5. **Validar** com checklist de cada sprint

### ❌ Evitar

1. **Não quebrar** funcionalidades existentes
2. **Não modificar** endpoints backend existentes sem coordenação
3. **Não usar** `any` em TypeScript
4. **Não remover** código sem validar que está obsoleto
5. **Não criar** abstrações prematuras

## Stack Tecnológica

- **Framework**: Vue 3.5 (Composition API com `<script setup>`)
- **Linguagem**: TypeScript
- **Build**: Vite
- **Estado**: Pinia (Setup Stores)
- **Roteamento**: Vue Router
- **UI**: BootstrapVueNext
- **HTTP**: Axios
- **Testes**: Vitest

## Arquitetura de Camadas

```
View (*.vue)
    ↓
Store (Pinia)
    ↓
Service (API Client)
    ↓
Backend API
```

**Regra**: Nunca pular camadas. Views não chamam Services diretamente.

## Convenções de Código

### Nomenclatura
- **Componentes**: `PascalCase.vue`
- **Composables**: `use*.ts`
- **Stores**: `camelCase.ts`
- **Services**: `*Service.ts`

### TypeScript
- Usar interfaces explícitas
- Evitar `any`
- Preferir `type` para unions, `interface` para objetos

### Git
- **Branches**: `feature/sprint-N-descricao`
- **Commits**: Convenção Conventional Commits
  - `feat:`, `fix:`, `refactor:`, `test:`, `docs:`

## Recursos

- **Código atual**: Branch `main`
- **Padrões Frontend**: `regras/frontend-padroes.md`
- **Padrões Backend**: `regras/backend-padroes.md`
- **Relatório Original**: `relatorio-frontend.md`

## Próximos Passos

1. Ler Sprint 1: `sprint-01-bff-agregacao.md`
2. Executar checklist técnica
3. Validar critérios de aceitação
4. Reportar conclusão e métricas

---

**Última atualização**: 2025-12-20
**Responsável**: Agentes de IA
**Status**: 🟡 Em Planejamento