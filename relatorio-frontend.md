# Relatório de Análise do Frontend (SGC)

## Visão Geral

O frontend do SGC (Sistema de Gestão de Competências) apresenta uma arquitetura moderna baseada em Vue 3, TypeScript e Pinia. O código está bem estruturado em módulos e segue boas práticas de separação de responsabilidades (Services, Stores, Views).

No entanto, foram identificados padrões que corroboram a hipótese de um "protótipo sofisticado" que foi integrado ao backend. Existem diversas áreas onde o frontend assume responsabilidades excessivas de orquestração de dados, manipulação de estruturas complexas e regras de negócio que deveriam estar centralizadas no servidor.

## Principais Problemas Identificados

### 1. Orquestração de Chamadas (API Chaining) no Cliente

Várias Views realizam múltiplas chamadas sequenciais ou paralelas à API para montar o contexto da tela. Isso gera latência desnecessária, aumenta a complexidade de tratamento de erros e desperdiça banda.

*   **Arquivo:** `frontend/src/views/CadMapa.vue`
*   **Problema:** O método `onMounted` dispara uma cadeia de dependências:
    1.  `unidadesStore.buscarUnidade(sigla)`
    2.  `subprocessosStore.buscarSubprocessoPorProcessoEUnidade(...)`
    3.  `Promise.all` para buscar:
        *   Mapa Completo
        *   Detalhes do Subprocesso
        *   Atividades
*   **Recomendação:** Criar um endpoint no backend (ex: `GET /subprocessos/{id}/editor-context`) que retorne um DTO Agregado (Pattern: *Backend For Frontend - BFF*) contendo todas as informações necessárias para a inicialização da tela de edição.

### 2. Lógica de Negócio e Travessia de Árvores no Cliente

O frontend contém lógica para navegar em estruturas de dados complexas retornadas pelo backend, em vez de solicitar o dado específico.

*   **Arquivo:** `frontend/src/composables/useSubprocessoResolver.ts`
*   **Problema:** A função `buscarUnidadeNaArvore` realiza uma busca recursiva no array de unidades/filhos do processo para encontrar um subprocesso específico baseado na sigla da unidade.
*   **Impacto:** O frontend precisa conhecer a estrutura hierárquica interna do processo, criando um acoplamento forte. Se a estrutura da árvore mudar no backend, o frontend quebra.
*   **Recomendação:** Utilizar o endpoint já existente (ou aprimorá-lo) que resolve diretamente o Subprocesso a partir do Processo + Unidade, eliminando a necessidade de trazer a árvore completa para o cliente apenas para encontrar um nó.

### 3. Tratamento de Erros Acoplado à Estrutura de Validação

A lógica de mapeamento de erros de validação está duplicada e hardcoded nos componentes.

*   **Arquivo:** `frontend/src/views/CadMapa.vue` (função `handleApiErrors`)
*   **Problema:** O componente possui um `switch/case` ou `if/else` gigante mapeando campos do erro (`atividadesAssociadas`, `dataLimite`) para campos do formulário.
*   **Impacto:** Qualquer alteração nos nomes dos campos no DTO de erro do backend exige manutenção no frontend.
*   **Recomendação:** Padronizar a resposta de erro de validação do backend para incluir o nome do campo de forma que o frontend possa iterar e atribuir erros dinamicamente, ou usar uma diretiva genérica de formulário.

### 4. Gestão de Estado e Duplicação de Regras

Algumas stores do Pinia replicam lógica que parece tentar antecipar o estado do backend.

*   **Arquivo:** `frontend/src/stores/mapas.ts` e `frontend/src/views/CadMapa.vue`
*   **Problema:** Ao remover uma atividade de uma competência, o frontend tenta atualizar o objeto localmente. Em `mapas.ts`, há comentários como `// Garantir que o mapa foi recarregado com códigos corretos`, indicando desconfiança na sincronia dos dados.
*   **Recomendação:** Confiar na resposta do comando de escrita (POST/PUT). O backend deve retornar o estado atualizado da entidade afetada. O frontend deve apenas substituir o objeto local pelo retornado, sem tentar "calcular" o novo estado.

## Dados Estáticos e Mocks

Não foram encontrados grandes volumes de dados mockados ("hardcoded") nos arquivos analisados (`services`, `stores`, `views` principais). O código parece ter sido limpo dessa herança do protótipo, o que é um ponto positivo. As dependências são injetadas ou buscadas via `apiClient`.

## Recomendações de Refatoração (Prioridade)

1.  **Criar DTOs Agregados (ViewObjects) no Backend:**
    *   Para a tela de Mapa (`CadMapa`), retornar um objeto único com: Unidade, Subprocesso (Resumo), Mapa (se existir) e Atividades Disponíveis.
    *   Para a tela de Processo (`ProcessoView`), evitar trazer a árvore completa de unidades se a visualização for tabular ou plana.

2.  **Eliminar `useSubprocessoResolver`:**
    *   Substituir a lógica recursiva por chamadas diretas de resolução de ID no backend (`subprocessoService.buscarSubprocessoPorProcessoEUnidade`).

3.  **Simplificar Stores:**
    *   Remover lógica de "patch" manual de arrays em `processos.ts` e `mapas.ts`. Após uma mutação, substituir o objeto pelo retorno da API ou invalidar a query para forçar um refresh limpo.

4.  **Centralizar Tratamento de Erros de Formulário:**
    *   Criar um *composable* `useFormError` que receba o objeto de erro da API e popule automaticamente os campos do formulário baseando-se nas chaves, evitando lógica manual em cada View.
