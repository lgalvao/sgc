# Guia para Correção dos Erros de Tipo nos Testes E2E

## 1. Visão Geral

Uma refatoração recente na biblioteca de helpers dos testes E2E (`frontend/e2e/helpers`) introduziu uma série de erros de tipo que impedem a compilação e execução dos testes. A análise revelou que os problemas se enquadram em quatro categorias principais:

1.  **Exportações Ausentes**: Funções existem, mas não são exportadas corretamente pelos arquivos-barril (`index.ts`).
2.  **Funções Renomeadas**: Funções foram renomeadas, mas suas chamadas nos arquivos de teste (`.spec.ts`) não foram atualizadas.
3.  **Alterações Funcionais**: A lógica de algumas funções foi alterada (ex: remoção de um modal), tornando os testes antigos incompatíveis.
4.  **Erros de Sintaxe e Caminho**: Problemas simples como chaves duplicadas em objetos e caminhos de importação incorretos.

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
-   `abrirDialogoRemocaoProcesso`: `e2e/helpers/acoes/index.ts` (provavelmente de `acoes-processo.ts`)
-   `clicarBotaoDisponibilizar`: `e2e/helpers/acoes/index.ts` (provavelmente de `acoes-mapa.ts`)
-   `preencherDataModal`: `e2e/helpers/acoes/index.ts` (provavelmente de `acoes-modais.ts`)
-   `verificarDescricaoCompetencia`: `e2e/helpers/verificacoes/index.ts` (provavelmente de `verificacoes-mapa.ts`)
-   `disponibilizarMapaComData`: `e2e/helpers/acoes/index.ts` (provavelmente de `acoes-mapa.ts`)
-   `navegarParaMapaRevisao`: `e2e/helpers/navegacao/index.ts`
-   `abrirModalDisponibilizacao`: `e2e/helpers/acoes/index.ts` (provavelmente de `acoes-modais.ts`)
-   `preencherObservacoesModal`: `e2e/helpers/acoes/index.ts` (provavelmente de `acoes-modais.ts`)
-   `navegarParaMapaMapeamento`: `e2e/helpers/navegacao/index.ts`
-   `apresentarSugestoes`: `e2e/helpers/acoes/index.ts` (provavelmente de `acoes-mapa.ts` ou `acoes-validacao.ts`)
-   `validarMapa`: `e2e/helpers/acoes/index.ts` (provavelmente de `acoes-validacao.ts`)

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
-   `verificarCardAcaoVisivel` -> `verificarModalVisivel` (ou similar, requer investigação)
-   `verificarCardAcaoInvisivel` -> `verificarPainelVisivel` (ou similar, requer investigação)
-   `adicionarConhecimento` -> `editarConhecimento`
-   `adicionarConhecimentoPrimeiraAtividade` -> `adicionarConhecimentoNaAtividade`
-   `cancelarModal` -> `cancelarNoModal`

### Passo 2.3: Lidar com Alterações Funcionais

**Problema:** Alguns helpers foram alterados fundamentalmente. Por exemplo, a função `iniciarProcesso` não abre mais um modal, mas a função antiga `confirmarInicializacaoProcesso` (agora inexistente) era usada para confirmar essa ação. Além disso, a função `devolverParaAjustes` agora espera um único argumento de string, não um objeto.

**Instruções:**

1.  **Para `devolverParaAjustes` em `e2e/cdu-07.spec.ts`:**
    -   A chamada `devolverParaAjustes({ observacoes: '...' })` está incorreta.
    -   Consulte a definição da função em `e2e/helpers/acoes/acoes-processo.ts`. Ela espera um argumento `(page: Page, observacao?: string)`.
    -   Corrija a chamada para `devolverParaAjustes(page, '...')`.

2.  **Para a lógica de `iniciarProcesso`:**
    -   As funções `abrirModalInicializacaoProcesso` e `confirmarInicializacaoNoModal` foram removidas em favor de uma única função `iniciarProcesso` em `acoes-processo.ts` que lida com a confirmação diretamente.
    -   Nos testes que usavam a combinação antiga (ex: `cdu-04.spec.ts` e `cdu-05.spec.ts`), substitua as duas chamadas antigas por uma única chamada para `iniciarProcesso(page)`.

### Passo 2.4: Corrigir Erros de Sintaxe e Caminho

**Problema:** Existem erros pontuais de sintaxe e resolução de caminhos.

**Instruções:**

1.  **Para Chaves Duplicadas em `constantes-teste.ts`:**
    -   **Arquivo:** `e2e/helpers/dados/constantes-teste.ts`
    -   **Problema:** O objeto `SELETORES` tem duas chaves `MODAL_APRESENTAR_SUGESTOES` e duas `MODAL_VALIDAR`.
    -   **Correção:** Remova as duplicatas. Mantenha a versão que usa o sufixo `-btn` ou `-modal` se for mais descritiva, ou unifique-as se representarem o mesmo elemento. A análise sugere que uma se refere ao botão e outra ao modal em si. Renomeie para que fiquem distintas e claras, por exemplo:
        -   `BTN_APRESENTAR_SUGESTOES` e `MODAL_APRESENTAR_SUGESTOES`
        -   `BTN_VALIDAR` e `MODAL_VALIDAR`

2.  **Para o Caminho de Importação em `acoes-login.ts`:**
    -   **Arquivo:** `e2e/helpers/acoes/acoes-login.ts`
    -   **Problema:** `error TS2307: Cannot find module '../../dados'`
    -   **Correção:** O caminho relativo está incorreto. Altere a importação de `'../../dados'` para `'../dados'`.

---

## 3. Ordem de Execução Sugerida

1.  Comece pelos erros de sintaxe e caminho (`Passo 2.4`), pois são os mais simples e podem desbloquear outras verificações do type checker.
2.  Em seguida, corrija as exportações ausentes (`Passo 2.1`). Isso resolverá a maioria dos erros.
3.  Depois, atualize as funções renomeadas (`Passo 2.2`).
4.  Finalmente, lide com as alterações funcionais (`Passo 2.3`), que exigem uma compreensão mais aprofundada da lógica de teste.
5.  Após aplicar todas as correções, execute `npm run typecheck` novamente para garantir que todos os erros foram resolvidos.
