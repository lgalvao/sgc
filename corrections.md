# Guia para Correção dos Erros de Tipo nos Testes E2E

## 1. Visão Geral

Uma refatoração recente na biblioteca de helpers dos testes E2E (`frontend/e2e/helpers`) introduziu uma série de erros de tipo que impedem a compilação e execução dos testes. A análise revelou que os problemas se enquadram em cinco categorias principais:

1.  **Exportações Ausentes**: Funções existem, mas não são exportadas corretamente pelos arquivos-barril (`index.ts`).
2.  **Funções Renomeadas**: Funções foram renomeadas, mas suas chamadas nos arquivos de teste (`.spec.ts`) não foram atualizadas.
3.  **Alterações Funcionais**: A lógica de algumas funções foi alterada (ex: remoção de um modal), tornando os testes antigos incompatíveis.
4.  **Erros de Sintaxe e Caminho**: Problemas simples como chaves duplicadas em objetos e caminhos de importação incorretos.
5.  **Argumentos Incorretos**: Funções são chamadas com um número incorreto de argumentos.

Este documento detalha os passos necessários para que um agente de IA corrija cada um desses problemas.

---

## 2. Plano de Correção Detalhado

### Passo 2.1: Corrigir Exportações Ausentes

**Problema:** A maioria dos erros `TS2305: Module '"./helpers"' has no exported member '...'` ocorre porque os arquivos `index.ts` nos subdiretórios de `e2e/helpers` (como `navegacao`, `acoes`, `verificacoes`) não estão exportando todas as funções necessárias.

**Instruções:**

1.  Para cada erro de exportação ausente, identifique o arquivo onde a função está definida (ex: `navegarParaLogin` está em `e2e/helpers/navegacao/navegacao.ts`).
2.  Abra o arquivo `index.ts` do respectivo subdiretório (ex: `e2e/helpers/navegacao/index.ts`).
3.  Adicione o nome da função ausente à lista de `export` do arquivo. Se o arquivo usa `export * from './...'`, verifique se o arquivo de origem está correto.

**Exemplo Prático (para `navegarParaLogin`):**

-   **Arquivo a ser modificado:** `frontend/e2e/helpers/navegacao/index.ts`
-   **Modificação:** Adicionar `navegarParaLogin` à lista de exportações.

```typescript
// ANTES
export {
    navegarParaCriacaoProcesso,
    // ... outras funções
} from './navegacao';

// DEPOIS
export {
    navegarParaLogin, // <--- ADICIONAR AQUI
    navegarParaCriacaoProcesso,
    // ... outras funções
} from './navegacao';
```

**Lista de Funções a Corrigir (mapeamento de erro para arquivo):**

-   `navegarParaLogin`: `e2e/helpers/navegacao/index.ts`
-   `abrirDialogoRemocaoProcesso`: `e2e/helpers/acoes/index.ts`
-   `verificarDescricaoCompetencia`: `e2e/helpers/verificacoes/index.ts`
-   `navegarParaMapaRevisao`: `e2e/helpers/navegacao/index.ts`
-   `navegarParaMapaMapeamento`: `e2e/helpers/navegacao/index.ts`
-   `apresentarSugestoes`: `e2e/helpers/acoes/index.ts`
-   `validarMapa`: `e2e/helpers/acoes/index.ts`

### Passo 2.2: Atualizar Funções Renomeadas

**Problema:** O type checker sugere nomes corretos para funções que foram renomeadas. O erro é `TS2724: '...' has no exported member named '...'. Did you mean '...'?`.

**Instruções:**

1.  Para cada erro `TS2724`, substitua o nome da função antiga pelo nome sugerido pelo compilador no arquivo `.spec.ts` correspondente.
2.  Após a renomeação, verifique a assinatura da nova função para garantir que os argumentos passados ainda são válidos.

**Exemplo Prático:**

-   **Arquivo a ser modificado:** `e2e/cdu-04.spec.ts`
-   **Modificação:** Renomear `abrirModalInicializacaoProcesso` para `abrirModalFinalizacaoProcesso` e `confirmarInicializacaoNoModal` para `confirmarFinalizacaoNoModal`.

**Lista de Renomeações a Fazer:**

-   `abrirModalInicializacaoProcesso` -> `abrirModalFinalizacaoProcesso`
-   `confirmarInicializacaoNoModal` -> `confirmarFinalizacaoNoModal`
-   `adicionarConhecimento` -> `editarConhecimento` ou `adicionarConhecimentoNaAtividade`
-   `adicionarConhecimentoPrimeiraAtividade` -> `adicionarConhecimentoNaAtividade`
-   `cancelarModal` -> `cancelarNoModal`

### Passo 2.3: Lidar com Alterações Funcionais

**Problema:** Alguns helpers foram alterados fundamentalmente. Por exemplo, `disponibilizarCadastro` agora lida com a interação modal que antes era feita por várias funções.

**Instruções:**

1.  **Para `devolverParaAjustes` em `e2e/cdu-07.spec.ts`:**
    -   A chamada `devolverParaAjustes({ observacoes: '...' })` está incorreta.
    -   Consulte a definição da função em `e2e/helpers/acoes/acoes-processo.ts`. Ela espera um argumento `(page: Page, observacao?: string)`.
    -   Corrija a chamada para `devolverParaAjustes(page, '...')`.

2.  **Para a lógica de `disponibilizarCadastro`:**
    -   As funções `clicarBotaoDisponibilizar`, `preencherDataModal`, `disponibilizarMapaComData`, `abrirModalDisponibilizacao` e `preencherObservacoesModal` foram removidas em favor de uma única função `disponibilizarCadastro` em `acoes-processo.ts` que lida com a confirmação diretamente.
    -   Nos testes que usavam a combinação antiga (ex: `cdu-15.spec.ts`, `cdu-16.spec.ts`, `cdu-17.spec.ts`), substitua as chamadas antigas por uma única chamada para `disponibilizarCadastro(page)`.

### Passo 2.4: Corrigir Erros de Sintaxe e Caminho

**Problema:** Existem erros pontuais de sintaxe e resolução de caminhos.

**Instruções:**

1.  **Para Chaves Duplicadas em `constantes-teste.ts`:**
    -   **Arquivo:** `e2e/helpers/dados/constantes-teste.ts`
    -   **Problema:** O objeto `SELETORES` tem duas chaves `MODAL_APRESENTAR_SUGESTOES` e duas `MODAL_VALIDAR`.
    -   **Correção:** Remova as duplicatas.

2.  **Para o Caminho de Importação em `acoes-login.ts`:**
    -   **Arquivo:** `e2e/helpers/acoes/acoes-login.ts`
    -   **Problema:** `error TS2307: Cannot find module '../../dados'`
    -   **Correção:** O caminho relativo está incorreto. Altere a importação de `'../../dados'` para `'../dados/constantes-teste'`.

### Passo 2.5: Corrigir Argumentos Incorretos

**Problema:** Funções são chamadas com um número incorreto de argumentos.

**Instruções:**

1.  Para cada erro `TS2554: Expected X arguments, but got Y.`, verifique a definição da função no arquivo de helper correspondente.
2.  Adicione os argumentos que faltam.

**Exemplo Prático:**

-   **Arquivo a ser modificado:** `e2e/cdu-12.spec.ts`
-   **Problema:** `adicionarConhecimentoNaAtividade` é chamada com 2 argumentos, mas espera 3.
-   **Correção:** Adicione o argumento `nomeAtividade` que faltava.

---

## 3. Ordem de Execução Sugerida

1.  Comece pelos erros de sintaxe e caminho (`Passo 2.4`), pois são os mais simples e podem desbloquear outras verificações do type checker.
2.  Em seguida, corrija as exportações ausentes (`Passo 2.1`). Isso resolverá a maioria dos erros.
3.  Depois, atualize as funções renomeadas (`Passo 2.2`).
4.  Lide com as alterações funcionais (`Passo 2.3`).
5.  Corrija os argumentos incorretos (`Passo 2.5`).
6.  Após aplicar todas as correções, execute `npm run typecheck` novamente para garantir que todos os erros foram resolvidos.
