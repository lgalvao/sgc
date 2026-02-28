# Diretório de Stores (Pinia)

Este diretório contém as **Stores Pinia**, responsáveis pelo gerenciamento de estado global da aplicação.

## Padrão Arquitetural

Utilizamos o padrão **Setup Stores** (Composition API).

```typescript
export const useExemploStore = defineStore('exemplo', () => {
  const itens = ref<Item[]>([]);
  const carregar = async () => { itens.value = await Service.get(); };
  return { itens, carregar };
});
```

## Stores Disponíveis

### Autenticação e Perfil

* **`perfil.ts` (`usePerfilStore`)**: Sessão, token JWT, perfis e unidade selecionada.
* **`usuarios.ts` (`useUsuariosStore`)**: Dados do usuário logado e busca de usuários.

### Processos e Fluxo

* **`processos/`**: Pasta com lógica dividida para o domínio de processos.
    - **`core.ts`**: Listagem, filtros e operações CRUD de processos.
    - **`context.ts`**: Estado do processo atualmente selecionado/aberto.
    - **`workflow.ts`**: Gerenciamento do fluxo de estados e transições.
* **`subprocessos.ts` (`useSubprocessosStore`)**: Gestão de subprocessos de unidades.

### Funcionalidades Específicas

* **`atividades.ts`**: CRUD de atividades.
* **`mapas.ts`**: Estado do mapa de competências e revisões.
* **`diagnosticos.ts`**: Dados de autoavaliação e monitoramento.
* **`atribuicoes.ts`**: Gestão de atribuições temporárias/substituições.
* **`analises.ts`**: Histórico de análises e auditoria.
* **`unidades.ts`**: Árvore e dados de unidades organizacionais.
* **`configuracoes.ts`**: Parâmetros globais do sistema.

### UI e Feedback

* **`feedback.ts` (`useFeedbackStore`)**: Centralização de Toasts, alertas e estados de carregamento globais.
* **`alertas.ts`**: Notificações e alertas contextuais para o usuário.

## Boas Práticas

1. **Separação de Preocupações**: Stores delegam chamadas HTTP para os `services/`.
2. **Reatividade**: Use `storeToRefs()` ao desestruturar estado em componentes.
3. **Persistência**: Dados de sessão são persistidos automaticamente no `localStorage`.
