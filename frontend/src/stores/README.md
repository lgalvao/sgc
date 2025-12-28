# Diretório de Stores (Pinia)

Este diretório contém as **Stores Pinia**, responsáveis pelo gerenciamento de estado global da aplicação.

## Padrão Arquitetural

Utilizamos o padrão **Setup Stores** (similar à Composition API) para definição das stores.

```typescript
export const useExemploStore = defineStore('exemplo', () => {
  // State (ref)
  const itens = ref<Item[]>([]);

  // Getters (computed)
  const itensAtivos = computed(() => itens.value.filter(i => i.ativo));

  // Actions (function)
  async function carregarItens() {
    itens.value = await ExemploService.listar();
  }

  return { itens, itensAtivos, carregarItens };
});
```

## Stores Principais

*   **`useAuthStore`** (ou `usePerfilStore`): Gerencia a sessão do usuário, token JWT e perfil selecionado.
*   **`useProcessoStore`**: Gerencia a lista e o estado do processo selecionado.
*   **`useSubprocessoStore`**: Gerencia os dados do subprocesso, incluindo o mapa de competências em edição.
*   **`useNotificacoesStore`**: Centraliza alertas e notificações do sistema (Toast).

## Boas Práticas

1.  **Acesso à API**: Stores nunca chamam `axios` diretamente. Elas delegam para a camada `services/`.
2.  **Reset**: Implemente um método `$reset` ou uma action `limparEstado` se a store precisar ser resetada entre navegações.
3.  **Persistência**: Dados sensíveis de sessão (token, usuário) são persistidos no `localStorage` para sobreviver ao refresh (F5).
