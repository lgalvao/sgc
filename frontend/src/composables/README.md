# Composables (Vue Composition API)
Última atualização: 2025-12-04 14:18:38Z

Este diretório contém **Composables** do Vue.js. Composables são funções que encapsulam lógica de estado com a Composition API para ser reutilizada entre componentes.

Por convenção, o nome dessas funções começa com `use` (ex: `usePerfil`).

## Composables Disponíveis

### `useApi.ts`
- **Objetivo:** Fornecer um wrapper reativo em torno das chamadas de API, gerenciando estados comuns como `loading` (carregamento), `error` (erro) e `data` (dados).
- **Uso:** Simplifica o código nos componentes, evitando a repetição de blocos `try/catch` e variáveis de estado para controle de feedback visual.

### `usePerfil.ts`
- **Objetivo:** Centraliza a lógica complexa de cálculo de perfis de acesso do usuário.
- **Funcionalidades:**
    - **Cálculo de Perfis:** Determina dinamicamente quais perfis (`ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`) um usuário possui com base em sua lotação, titularidade de unidades e atribuições temporárias.
    - **Estado do Usuário:** Fornece propriedades computadas reativas para o `servidorLogado`, `perfilSelecionado` e `unidadeSelecionada`.
    - **Integração com Stores:** Orquestra dados das stores `useUsuariosStore`, `useUnidadesStore` e `useAtribuicaoTemporariaStore`.


## Detalhamento técnico (gerado em 2025-12-04T14:22:48Z)

Resumo detalhado dos artefatos, comandos e observações técnicas gerado automaticamente.
