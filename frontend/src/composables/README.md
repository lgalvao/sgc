# Composables (Vue Composition API)

Este diretório contém funções que encapsulam lógica de estado e UI reutilizável.

## Composables Disponíveis

### Lógica de UI e Estado
* **`useApi.ts`**: Wrapper para chamadas de API com estados de `loading` e `error` integrados.
* **`useErrorHandler.ts`**: Tratamento padronizado de erros de API e exibição de feedbacks.
* **`useLoadingManager.ts`**: Controle centralizado de indicadores de carregamento.
* **`useModalManager.ts`**: Abstração para abertura e fechamento de modais programaticamente.
* **`useLocalStorage.ts`**: Persistência reativa de dados no navegador.
* **`useFormErrors.ts`**: Gestão e exibição de erros de validação em formulários.

### Domínio e Negócio
* **`usePerfil.ts`**: Lógica para cálculo de permissões e contexto do usuário logado.
* **`useBreadcrumbs.ts`**: Geração dinâmica da trilha de navegação (breadcrumbs).
* **`useRelatorios.ts`**: Lógica de busca e filtros para a central de relatórios.

### Fluxos Específicos (Views)
* **`useProcessoView.ts` / `useProcessoForm.ts`**: Lógica para telas de detalhes e cadastro de processos.
* **`useUnidadeView.ts`**: Lógica para a tela de detalhes da unidade.
* **`useCadAtividades.ts` / `useAtividadeForm.ts`**: Suporte ao fluxo de cadastro de atividades.
* **`useVisAtividades.ts`**: Lógica para visualização de atividades.
* **`useVisMapa.ts`**: Lógica para exibição do mapa de competências.

## Boas Práticas

1. **Nomenclatura**: Sempre começar com o prefixo `use` (ex: `useAuth`).
2. **Ciclo de Vida**: Podem utilizar `onMounted`, `watch`, etc., para gerenciar efeitos colaterais.
3. **Puro/Impuro**: Prefira composables que retornam apenas estado reativo (`Ref`, `ComputedRef`) e funções.
