# Diretório de Composables

Este diretório contém os _"composables"_ da aplicação. Composables são funções que utilizam a [Composition API](https://vuejs.org/guide/reusability/composables.html) do Vue 3 para encapsular e reutilizar **lógica com estado** (_stateful logic_).

## Objetivo

O principal objetivo dos composables é extrair lógica reativa e com estado de dentro dos componentes, tornando-a compartilhável entre múltiplos componentes sem a necessidade de herança ou mixins. Isso torna os componentes mais limpos, focados em sua responsabilidade de template e UI, e facilita a manutenção e os testes da lógica de negócio.

## Casos de Uso Comuns

- **Abstração de API**: Encapsular a lógica de chamadas a um _endpoint_ específico, gerenciando estado de carregamento (`loading`), erros (`error`) e os dados (`data`) retornados.
- **Lógica de UI Complexa**: Gerenciar o estado de funcionalidades complexas que podem ser usadas em vários lugares, como o controle de um modal, paginação de dados ou lógica de _drag-and-drop_.
- **Interação com o Navegador**: Abstrair o uso de APIs do navegador, como `localStorage`, `sessionStorage`, ou o monitoramento da posição do _mouse_ ou do tamanho da janela.

## Convenção de Nomenclatura

Composables são nomeados seguindo a convenção `use<NomeDaFuncionalidade>`. Por exemplo:

- `useApiSubprocesso.ts`: Para interagir com a API de subprocessos.
- `usePaginacao.ts`: Para encapsular a lógica de paginação.
- `useLocalStorage.ts`: Para interagir com o `localStorage` do navegador de forma reativa.

Cada arquivo deve exportar uma função com o mesmo nome.