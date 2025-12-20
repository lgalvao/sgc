# Backlog de Otimizações Futuras (Frontend)

## Visão Geral
Estas melhorias têm prioridade menor que as refatorações estruturais (Sprints 1-3), mas oferecem grande valor em UX e performance. Devem ser atacadas após a estabilização do código base.

## Oportunidades Identificadas

### 1. Caching Local (LocalStorage)
**Alvo:** Dados estáticos ou de baixa volatilidade.
- Lista de Unidades (`/api/unidades`).
- Tipos de Processo.
- Perfil do Usuário.
**Estratégia:** Implementar wrapper no Axios ou na Store com TTL (ex: 24h).
**Ganho:** Economia de ~30% nas requisições de "bootstrap" da aplicação.

### 2. Lazy Loading de Componentes
**Alvo:** Componentes pesados ou pouco acessados.
- Modais complexos.
- `CompetenciaCard.vue` (se a lista for muito longa).
**Ação:** Usar `defineAsyncComponent`.
**Ganho:** Redução do bundle inicial (LCP mais rápido).

### 3. Optimistic Updates (UI Otimista)
**Alvo:** Ações de interação frequente.
- Adicionar/Remover atividade.
- Marcar notificação como lida.
**Ação:** Atualizar o estado da store *antes* do `await api.call()`. Reverter em caso de erro.
**Ganho:** Percepção de latência zero.

### 4. Prefetching Preditivo
**Alvo:** Detalhes de itens em listas.
**Ação:** Ao passar o mouse (hover) em um item da lista de processos, disparar requisição GET dos detalhes.
**Ganho:** Navegação instantânea ao clicar.

### 5. Infinite Scroll / Virtualização
**Alvo:** Listas longas (Painel, Logs).
**Ação:** Implementar paginação virtual em vez de renderizar todo o DOM.
**Ganho:** Redução drástica de uso de memória e CPU no navegador.
