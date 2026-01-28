# Relatário de Análise Técnica Frontend - SGC

## Visão Geral
A arquitetura do frontend do projeto SGC é robusta, baseada em **Vue 3**, **Pinia**, **TypeScript** e **BootstrapVueNext**. O projeto segue boas práticas modernas (Composition API, Store pattern), mas apresenta áreas de dívida técnica relacionadas à verborragia, duplicação de camadas, complexidade excessiva em componentes de visualização e inconsistências de padronização.

Este relatório detalha os pontos de atenção identificados para refatoração e melhoria.

---

## 1. Dívida Técnica Identificada

### 1.1. Camada de Serviço e Store Redundante (Overengineering)
**Problema:** Existe uma duplicidade sistemática entre a camada de serviço (`src/services/*.ts`) e a camada de estado (`src/stores/*.ts`).
**Evidência:**
- Os arquivos de serviço (ex: `processoService.ts`) são meros wrappers do Axios.
- As Stores (ex: `processos.ts`) frequentemente apenas redeclaram os mesmos métodos da camada de serviço, envelopando-os em tratamento de erro (`withErrorHandling`).
- **Exemplo:** A função `obterDetalhesProcesso` no serviço apenas chama `apiClient.get`. A store chama o serviço e salva no estado.
**Solução Recomendada:**
- Simplificar a camada de serviço. Para operações de leitura simples (GET), a store poderia chamar utilitários de API genéricos ou o serviço poderia ser eliminado onde não há transformação de dados.
- Automatizar ou usar um padrão de "Factory" para stores que são apenas CRUD, reduzindo o boilerplate manual.

### 1.2. Complexidade Acoplada nas Views (God Components)
**Problema:** Componentes de visualização (Views) assumem responsabilidades excessivas, atuando como controladores de lógica de negócio e orquestradores de fluxo, dificultando testes unitários e manutenção.
**Evidência:**
- `CadAtividades.vue` (~540 linhas) e `VisMapa.vue` (~580 linhas) detêm lógica de scroll, regras de permissão complexas, controle de múltiplos modais e validação de estado local misturada com estado global.
- A View `CadAtividades` importa e orquestra **7 stores diferentes** (`analises`, `atividades`, `mapas`, `processos`, `subprocessos`, `unidades`, `feedback`).
**Solução Recomendada:**
- Extrair lógica de orquestração para **Composables de funcionalidade**. Ex: `useGerenciamentoAtividades` que encapsule a interação entre as stores.
- Quebrar views em componentes menores e mais focados (ex: `AtividadeList`, `MapaActions`, `AnaliseHistoricoTable`).

### 1.3. Mappers Triviais e inconsistência de Nomenclatura
**Problema:** Uso de mappers que não agregam valor real e inconsistência no nome dos campos entre DTO e Frontend.
**Evidência:**
- `src/mappers/processos.ts` contém funções como `mapProcessoResumoDtoToFrontend` que apenas fazem spread (`...dto`).
- Existem campos com nomes variados para a mesma informação: `codigo`, `codUnidade`, `unidadeCodigo`.
**Solução Recomendada:**
- Remover mappers identidade. Utilizar Interfaces TypeScript para garantir contrato.
- Padronizar nomenclaturas (preferencialmente seguindo o backend, a menos que haja forte justificativa para renomeação).

### 1.4. Inconsistência de Modais e Componentes UI
**Problema:** Mistura de modais específicos e genéricos, e uso de `BModal` inline contra as diretrizes de design.
**Evidência:**
- `VisMapa.vue` utiliza `BModal` inline para "Ver Sugestões" (linha 176) enquanto usa `ModalConfirmacao` para outras ações.
- `CadAtividades.vue` utiliza uma mistura de `ConfirmacaoDisponibilizacaoModal` e `ModalConfirmacao`.
**Solução Recomendada:**
- Centralizar todos os modais de confirmação no `ModalConfirmacao` genérico.
- Criar componentes dedicados para modais de visualização complexos para limpar o template das Views.

### 1.5. Lógica de Negócio no Frontend (Leakage)
**Problema:** Regras de transição de status e labels de negócio estão duplicadas ou fixas no frontend.
**Evidência:**
- Em `UseProcessosStore`, a função `executarAcaoBloco` contém `if/else` baseados em `SituacaoSubprocesso`.
- A utilidade `situacaoLabel` em `utils/index.ts` mantém um mapa manual de strings de status que deveriam vir do backend ou de um enum centralizado compartilhado.
**Solução Recomendada:**
- O backend deve retornar as "ações permitidas" (`allowedActions`).
- Centralizar definições de status em constantes/enums e buscar labels do backend sempre que possível.

## 2. Novos Achados de Análise

### 2.1. Bloat de Tipos e Interfaces
**Problema:** Proliferação de interfaces quase idênticas para a mesma entidade.
**Evidência:**
- Em `tipos.ts`, existem `Competencia`, `CompetenciaCompleta` e `CompetenciaVisualizacao`. Isso dificulta a manutenção e aumenta a confusão sobre qual tipo usar em cada componente.
**Solução:** Unificar interfaces usando `Pick`, `Omit` ou herança (`extends`) de forma mais criteriosa, ou consolidar em um modelo de domínio robusto no frontend.

### 2.2. Tratamento Manual de Datas vs date-fns
**Problema:** Duplicação de lógica de tratamento de datas.
**Evidência:**
- O projeto possui `date-fns` como dependência, mas `utils/index.ts` contém funções manuais complexas como `parseDate` (linha 85) que tentam resolver problemas de fuso horário e formatos variados manualmente.
**Solução:** Substituir utilitários manuais por funções do `date-fns` ou `dayjs` para garantir robustez e reduzir código customizado propenso a erros.

### 2.3. Inconsistência em Tratamento de API
**Problema:** Uso divergente de composables para chamadas de API.
**Evidência:**
- Alguns lugares usam `useApi`, outros usam `withErrorHandling` diretamente nas stores.
- A `baseURL` da API está hardcoded em `axios-setup.ts` como `http://localhost:10000/api`.
**Solução:** Padronizar o uso de um único composable de API e mover a URL base para variáveis de ambiente (`.env`).

## 3. Métricas de Repetição e Tamanho

- **Arquivos Grandes (+500 linhas):**
    - `CadAtividades.vue` (539 linhas)
    - `VisMapa.vue` (576 linhas)
- **Boilerplate de API:** 3 camadas (interface, service, store) para cada nova funcionalidade.
- **Acoplamento:** Views principais dependendo de mais de 5 stores simultaneamente.

## 4. Recomendações Prioritárias

1.  **Refatoração de Views Grandes:** Extrair composables funcionais para `CadAtividades` e `VisMapa`.
2.  **Consolidação de Tipos:** Auditar `tipos.ts` e remover duplicidades.
3.  **Padronização de Datas:** Migrar utilitários de data para `date-fns`.
4.  **Configuração de Ambiente:** Remover URLs hardcoded e usar variáveis de ambiente.
5.  **Unificação de Modais:** Substituir `BModal` inline por componentes encapsulados ou `ModalConfirmacao`.

---
*Gerado por Agente AI - Análise de Código Frontend Estendida*
