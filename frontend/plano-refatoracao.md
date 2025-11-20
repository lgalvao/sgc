# Plano de Refatoração Frontend e Integração Backend

Este documento descreve a análise do estado atual do frontend e backend, identificando lógicas duplicadas, mocks e pontos de melhoria na integração, além de traçar um plano para adoção completa do BootstrapVueNext.

## 1. Análise de Lógica Duplicada e Mockada

### 1.1. Cálculo de Impacto no Mapa (Crítico)
**Problema:** O frontend implementa uma lógica complexa de "Revisão" e cálculo de impactos no cliente.
- **Arquivos Afetados:** `frontend/src/stores/revisao.ts`, `frontend/src/components/ImpactoMapaModal.vue`.
- **Lógica:** O store `revisao.ts` armazena mudanças em `sessionStorage` e calcula manualmente quais competências são impactadas por alterações em atividades.
- **Solução Backend:** O backend já possui `MapaService.verificarImpactosMapa` (retornando `ImpactoMapaDto`) e endpoints para salvar ajustes.
- **Ação:** Remover a lógica de cálculo do frontend. O frontend deve apenas enviar as ações (adicionar/remover atividade) para o backend e, ao solicitar visualização de impacto, consultar o endpoint `/subprocessos/{id}/impactos-mapa`. O `ImpactoMapaModal.vue` deve renderizar o objeto `ImpactoMapa` retornado pelo backend, em vez de iterar sobre `mudancasParaImpacto`.

### 1.2. Hierarquia e Busca de Unidades
**Problema:** O frontend carrega árvores de unidades e realiza buscas recursivas pesadas no cliente.
- **Arquivos Afetados:** `frontend/src/stores/unidades.ts` (métodos `pesquisarUnidadePorCodigo`, `getUnidadesSubordinadas`, `getUnidadeSuperior`), `frontend/src/views/CadMapa.vue` (`buscarUnidade`).
- **Solução Backend:** O backend deve fornecer endpoints eficientes:
    - Buscar unidade por ID/Sigla (já existe `buscarUnidadePorSigla`).
    - Listar unidades filhas imediatas ou subárvore achatada se necessário.
    - Obter a unidade superior imediata.
- **Ação:** Substituir a recursão no frontend por chamadas diretas à API ou uso de estruturas de dados indexadas (Map) se a árvore completa for realmente necessária (ex: para o componente `ArvoreUnidades`). Para busca simples, usar endpoint.

### 1.3. Regras de Visualização e Permissões
**Problema:** Regras de negócio sobre visibilidade de botões e cards estão hardcoded no frontend.
- **Arquivos Afetados:**
    - `CadMapa.vue`: `podeVerImpacto` verifica `Perfil.ADMIN` e status do subprocesso.
    - `SubprocessoCards.vue`: Verifica `TipoProcesso` para decidir quais cards exibir.
- **Solução Backend:** O DTO `SubprocessoDetalhe` já retorna um objeto `permissoes`. Este objeto deve ser a única fonte de verdade.
- **Ação:**
    - Em `CadMapa.vue`, usar uma propriedade de permissão vinda do backend (ex: `podeVisualizarImpacto`) em vez de reimplementar a regra.
    - Em `SubprocessoCards.vue`, o backend deveria retornar uma lista de "Ações Disponíveis" ou "Menus Disponíveis" para o subprocesso, abstraindo a lógica de `TipoProcesso`.

### 1.4. Mock de Credenciais
**Problema:** Credenciais de desenvolvimento expostas.
- **Arquivo:** `frontend/src/views/LoginView.vue`.
- **Ação:** Remover valores default de `titulo` e `senha` ou garantir que sejam removidos em build de produção via variáveis de ambiente.

## 2. Adoção do BootstrapVueNext

O projeto já utiliza BootstrapVueNext, mas a migração está incompleta.

### 2.1. Componentes a Migrar
- **`frontend/src/components/SubprocessoModal.vue`**:
    - Substituir `<input type="date">` por `<BFormInput type="date">`.
    - Padronizar botões e modal (já usa `BModal`, verificar props).
- **`frontend/src/views/PainelView.vue`**:
    - Adicionar paginação usando `<BPagination>` ligado ao `processosStore.fetchProcessosPainel`, já que o backend suporta paginação (`Page<T>`), mas o frontend fixa page 0.
- **`frontend/src/components/SubprocessoCards.vue`**:
    - Converter os `div.card` manuais para componentes `<BCard>` para melhor consistência visual e de API.
- **`frontend/src/components/ArvoreUnidades.vue`**:
    - Avaliar uso de componentes de Lista ou Accordion do BootstrapVueNext se a estrutura de árvore se tornar muito complexa, ou manter customizado mas usando inputs do framework (já usa `BFormCheckbox`).

## 3. Plano de Execução

Este plano deve ser executado sequencialmente.

### Passo 1: Refatoração da Store de Revisão e Impacto (Backend-First)
1.  Verificar se o endpoint `GET /api/subprocessos/{id}/impactos-mapa` retorna todos os dados necessários para o modal de impacto.
2.  Modificar `frontend/src/services/mapaService.ts` para garantir tipagem correta do `ImpactoMapa`.
3.  Refatorar `frontend/src/components/ImpactoMapaModal.vue`:
    - Remover dependência de `revisaoStore.mudancasParaImpacto`.
    - Aceitar `ImpactoMapa` como prop ou buscar via store `mapasStore.impactoMapa`.
    - Renderizar listas baseadas em `atividadesInseridas`, `atividadesRemovidas`, etc., vindas do DTO.
4.  Remover lógica de cálculo de impacto de `frontend/src/stores/revisao.ts`.

### Passo 2: Centralização de Regras de Negócio
1.  Em `frontend/src/views/CadMapa.vue`, remover a computed `podeVerImpacto` local e utilizar a flag correspondente em `subprocesso.permissoes` (se necessário, solicitar adição dessa flag ao backend).
2.  Em `frontend/src/components/SubprocessoCards.vue`, refatorar para depender menos de `TipoProcesso` e mais de flags de capacidade/permissão, se possível, ou manter como está mas ciente da dívida técnica.

### Passo 3: Otimização de Unidades
1.  Em `frontend/src/stores/unidades.ts`, substituir buscas recursivas (`pesquisarUnidadePorSigla`) por chamadas ao endpoint `buscarUnidadePorSigla` quando o dado não estiver em cache.
2.  Refatorar `CadMapa.vue` para buscar a unidade alvo diretamente via API/Store em vez de varrer a lista de unidades do processo.

### Passo 4: Migração UI e Paginação
1.  Em `frontend/src/views/PainelView.vue`, implementar `<BPagination>`:
    - Criar estado local `currentPage` e `perPage`.
    - Ligar evento `@change` do pagination à action `fetchProcessosPainel`.
2.  Refatorar `SubprocessoModal.vue` para usar `<BFormInput>`.
3.  Refatorar `SubprocessoCards.vue` para usar `<BCard>`.

### Passo 5: Limpeza
1.  Remover credenciais hardcoded em `LoginView.vue`.
2.  Rodar `npm run typecheck` e corrigir erros residuais.
