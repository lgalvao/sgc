# Composables (Vue Composition API)

Este diretório contém **Composables** do Vue.js. Composables são funções que encapsulam lógica de estado com a Composition API para ser reutilizada entre componentes.

Por convenção, o nome dessas funções começa com `use` (ex: `usePerfil`).

## Composables Disponíveis

### `useApi.ts`
- **Objetivo:** Fornecer um wrapper reativo em torno das chamadas de API, gerenciando estados comuns como `loading` (carregamento), `error` (erro) e `data` (dados).
- **Uso:** Permite que os componentes façam requisições e exibam spinners ou mensagens de erro de forma padronizada sem repetir a lógica de `try/catch` e gerenciamento de estado.

### `usePerfil.ts`
- **Objetivo:** Gerenciar a lógica relacionada ao perfil do usuário logado.
- **Funcionalidades:**
    - Acesso ao perfil atual (ex: `ADMIN`, `GESTOR`).
    - Verificação de permissões.
    - Lógica para alternar entre perfis (se o usuário tiver múltiplos).
