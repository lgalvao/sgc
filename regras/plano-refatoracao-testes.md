### Guia de Refatoração para Testes Semânticos com Playwright

O objetivo principal é fazer com que os testes E2E se leiam como **cenários de usuário** ou **casos de uso**, e não como um script técnico. Para isso, vamos estruturar o código em três camadas de abstração.

#### Camada 1: Constantes (A "Fonte da Verdade" da UI)

Esta é a camada mais baixa e desacopla os testes das implementações específicas do HTML.

*   **Arquivo:** `e2e/cdu/constantes-teste.ts`
*   **Diretriz:**
    *   **NUNCA** use seletores de string (ex: `'[data-testid="meu-botao"]'`) diretamente nos testes ou em funções de ação.
    *   **SEMPRE** defina todos os seletores (Test IDs, classes, etc.) e URLs como constantes exportadas neste arquivo.
*   **Exemplo:**
    ```typescript
    export const SELETORES = {
      BTN_CRIAR_PROCESSO: '[data-testid="btn-criar-processo"]',
      TABELA_PROCESSOS: '[data-testid="tabela-processos"]',
      // ...
    };

    export const URLS = {
      PAINEL: '/painel',
      // ...
    };
    ```

#### Camada 2: Ações e Verificações (A "Linguagem de Domínio")

Esta é a camada mais importante para a semântica. Ela traduz comandos técnicos do Playwright em ações de negócio claras.

*   **Arquivos:** `e2e/cdu/auxiliares-acoes.ts`, `e2e/cdu/auxiliares-verificacoes.ts`, `e2e/cdu/auxiliares-navegacao.ts`
*   **Diretrizes:**
    1.  **Crie Funções para Ações de Negócio:** Encapsule qualquer ação que um usuário realiza em uma função com um nome de negócio. Essas funções usam as constantes da Camada 1.
    2.  **Crie Funções para Verificações de Estado:** Encapsule as asserções (`expect`) em funções que descrevem o estado esperado do sistema.
    3.  **Nomenclatura e Idioma:**
        *   **Idioma:** Todas as variáveis, funções e identificadores devem ser escritos em **português**.
        *   **Padrão de Nomenclatura:** Use nomes diretos e concisos, evitando preposições e artigos (ex: `de`, `para`, `no`). Use sufixos para adicionar clareza quando necessário.
        *   **Ações:** `clicarProcesso`, `preencherFormularioMapa`, `ordenarTabelaPorColuna`.
        *   **Verificações:** `verificarVisibilidadeProcesso`, `verificarNavegacaoPaginaDetalhes`, `assegurarBotaoDesabilitado`.
*   **Exemplo de Função de Ação:**
    ```typescript
    // Em 'auxiliares-navegacao.ts' ou 'auxiliares-acoes.ts'
    import { Page } from '@playwright/test';
    import { SELETORES } from './constantes-teste';

    export async function clicarProcesso(page: Page, nomeProcesso: string | RegExp) {
      await page.getByTestId(SELETORES.TABELA_PROCESSOS).getByRole('row', { name: nomeProcesso }).click();
    }
    ```
*   **Exemplo de Função de Verificação:**
    ```typescript
    // Em 'auxiliares-verificacoes.ts'
    import { expect, Page } from '@playwright/test';
    import { SELETORES } from './constantes-teste';

    export async function verificarNavegacaoPaginaDetalhesProcesso(page: Page) {
      await expect(page).toHaveURL(/.*\/processo\/\d+$/);
      await expect(page.locator(SELETORES.PROCESSO_INFO)).toBeVisible();
    }
    ```

#### Camada 3: Especificações (Os "Cenários de Teste")

Esta é a camada mais alta, onde os testes são escritos. O código aqui deve ser quase inteiramente composto por chamadas às funções da Camada 2.

*   **Arquivo:** `e2e/cdu/cdu-XX.spec.ts`
*   **Diretrizes:**
    1.  **Conte uma História:** Um teste deve ler como uma sequência lógica de ações e verificações do usuário.
    2.  **Mantenha a Simplicidade:** Evite lógica complexa (`for`, `if/else`) dentro de um teste. Se precisar, isso provavelmente indica a necessidade de uma função auxiliar mais poderosa na Camada 2.
    3.  **Setup Limpo:** Use `test.beforeEach` para ações repetitivas de setup, como login.

#### Processo de Refatoração (Passo a Passo)

Para refatorar um arquivo de teste existente (`cdu-XX.spec.ts`):

1.  **Identifique o "Ruído":** Leia o teste e encontre todas as chamadas diretas ao `page.getBy...`, `page.locator`, `page.click`, e `expect` com seletores.
2.  **Busque ou Crie Abstrações:**
    *   Para cada pedaço de "ruído", verifique se já existe uma função na Camada 2 (`auxiliares-*.ts`) que faça o que você precisa.
    *   Se não existir, crie uma nova função de ação ou verificação seguindo as diretrizes da Camada 2. Adicione quaisquer novos seletores necessários na Camada 1.
3.  **Substitua e Simplifique:** Volte ao arquivo de teste e substitua o código de baixo nível pela chamada à nova função semântica da Camada 2.
4.  **Revise:** Leia o teste refatorado em voz alta. Ele soa como um cenário de usuário? A intenção está clara sem precisar ler os detalhes da implementação? Se sim, o objetivo foi alcançado.
