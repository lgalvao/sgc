### Guia de Refatoração para Testes Semânticos com Playwright

O objetivo principal é fazer com que os testes E2E se leiam como **cenários de usuário** ou **casos de uso**, e não como um script técnico. Para isso, vamos estruturar o código em três camadas de abstração.

#### Camada 1: Constantes (A "Fonte da Verdade" da UI)

Esta é a camada mais baixa e desacopla os testes das implementações específicas do HTML.

*   **Arquivo:** `e2e/cdu/constantes-teste.ts`
*   **Diretriz:**
    *   **NUNCA** use seletores de string (ex: `'[data-testid="meu-botao"]'`) diretamente nos testes ou em funções de ação.
    *   **SEMPRE** defina todos os seletores (Test IDs, classes, etc.) e URLs como constantes exportadas neste arquivo.
    *   **Organize por categoria:** Agrupe constantes relacionadas em objetos (`SELETORES`, `SELETORES_CSS`, `TEXTOS`, `URLS`, `ROTULOS`).
*   **Exemplo:**
    ```typescript
    export const SELETORES = {
      BTN_CRIAR_PROCESSO: 'btn-criar-processo',
      TABELA_PROCESSOS: 'tabela-processos',
      // Use apenas o identificador, sem [data-testid=""]
    };

    export const SELETORES_CSS = {
      MODAL_VISIVEL: '.modal.show',
      NOTIFICACAO_ERRO: '.notification-error',
      CHECKBOX: 'input[type="checkbox"]',
      CHECKBOX_MARCADO: 'input[type="checkbox"]:checked',
      // Para seletores CSS específicos
    };

    export const URLS = {
      PAINEL: '/painel',
      PROCESSO_CADASTRO: '/processo/cadastro',
    };
    ```

#### Camada 2: Ações e Verificações (A "Linguagem de Domínio")

Esta é a camada mais importante para a semântica. Ela traduz comandos técnicos do Playwright em ações de negócio claras, organizadas de forma modular.

*   **Estrutura de Arquivos:** As funções auxiliares (helpers) estão organizadas por domínio dentro do diretório `e2e/cdu/helpers/`:
    *   `helpers/acoes/`: Funções que executam ações do usuário (ex: `adicionarAtividade`, `confirmarNoModal`).
    *   `helpers/verificacoes/`: Funções que contêm asserções `expect` (ex: `verificarProcessoCriado`, `verificarUrlPainel`).
    *   `helpers/navegacao/`: Funções para `page.goto()` e fluxos de navegação (ex: `navegarParaCadastroAtividades`).
    *   `helpers/dados/`: Onde as constantes da Camada 1 residem.
    *   `helpers/utils/`: Funções utilitárias genéricas.
    *   Cada subdiretório possui um `index.ts` para re-exportar suas funções, e um `helpers/index.ts` centraliza todas as importações.

*   **Diretrizes:**
    1.  **Crie Funções para Ações de Negócio:** Encapsule qualquer ação que um usuário realiza em uma função com um nome de negócio. Essas funções usam as constantes da Camada 1.
    2.  **Crie Funções para Verificações de Estado:** Encapsule as asserções (`expect`) em funções que descrevem o estado esperado do sistema.
    3.  **Funções Compostas:** Crie funções que combinam várias ações relacionadas (ex: `criarProcessoCompleto()`).
    4.  **Reutilização Inteligente:** Identifique padrões repetitivos e crie funções genéricas.
    5.  **Nomenclatura e Idioma:**
        *   **Idioma:** Todas as variáveis, funções e identificadores devem ser escritos em **português**.
        *   **Padrão de Nomenclatura:** Use nomes diretos e concisos, evitando preposições e artigos (ex: `de`, `para`, `no`). Use sufixos para adicionar clareza quando necessário.
        *   **Ações:** `clicarProcesso`, `preencherFormularioMapa`, `criarProcessoCompleto`, `navegarParaProcesso`.
        *   **Verificações:** `verificarVisibilidadeProcesso`, `verificarNavegacaoPaginaDetalhes`, `aguardarProcessoNoPainel`.

*   **Padrões de Função por Categoria:**

    **Ações Simples:**
    ```typescript
    export async function tentarSalvarProcessoVazio(page: Page): Promise<void> {
      await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
    }
    ```

    **Ações Compostas:**
    ```typescript
    export async function criarProcessoCompleto(page: Page, descricao: string, tipo: string, dataLimite?: string): Promise<void> {
      await preencherFormularioProcesso(page, descricao, tipo, dataLimite);
      await selecionarPrimeiraUnidade(page);
      await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
    }
    ```

    **Verificações Simples:**
    ```typescript
    export async function verificarNotificacaoErro(page: Page): Promise<void> {
      await expect(page.locator(SELETORES_CSS.NOTIFICACAO_ERRO)).toBeVisible();
    }
    ```

    **Verificações Compostas:**
    ```typescript
    export async function verificarProcessoRemovidoComSucesso(page: Page, descricaoProcesso: string): Promise<void> {
      await expect(page.getByText(`${TEXTOS.PROCESSO_REMOVIDO_INICIO}${descricaoProcesso}${TEXTOS.PROCESSO_REMOVIDO_FIM}`)).toBeVisible();
      await expect(page).toHaveURL(URLS.PAINEL);
      await expect(page.locator('[data-testid="tabela-processos"] tbody').getByText(descricaoProcesso)).not.toBeVisible();
    }
    ```

#### Camada 3: Especificações (Os "Cenários de Teste")

Esta é a camada mais alta, onde os testes são escritos. O código aqui deve ser quase inteiramente composto por chamadas às funções da Camada 2.

*   **Arquivo:** `e2e/cdu/cdu-XX.spec.ts`
*   **Diretrizes:**
    1.  **Conte uma História:** Um teste deve ler como uma sequência lógica de ações e verificações do usuário.
    2.  **Mantenha a Simplicidade:** Evite lógica complexa (`for`, `if/else`) dentro de um teste. Se precisar, isso provavelmente indica a necessidade de uma função auxiliar mais poderosa na Camada 2.
    3.  **Setup Limpo:** Use `test.beforeEach` para ações repetitivas de setup, como login.
    4.  **Organize por Cenário:** Agrupe testes relacionados e use nomes descritivos que expressem o comportamento esperado.
    5.  **Imports Limpos:** Importe apenas o que usar, organizando por categoria (verificações, ações, constantes).
    6.  **🚫 REGRA ABSOLUTA - ZERO IFs:** Nunca use condicionais (`if`, `for`, `while`) dentro dos testes. Toda lógica condicional deve estar nas funções auxiliares.
    7.  **🚫 REGRA ABSOLUTA - ZERO Verificações Técnicas:** Nunca exponha verificações de URL, seletores CSS ou outras verificações técnicas nos testes.

*   **Exemplo de Teste Bem Refatorado:**
    ```typescript
    test('deve remover processo com sucesso após confirmação', async ({ page }) => {
      // Pré-condição: Criar um processo para ser removido
      const descricaoProcesso = 'Processo para Remover';
      await navegarParaCriacaoProcesso(page);
      await criarProcessoCompleto(page, descricaoProcesso, 'Mapeamento', '2025-12-31');
      await aguardarProcessoNoPainel(page, descricaoProcesso);

      // Ação principal: Remover processo
      await navegarParaProcessoNaTabela(page, descricaoProcesso);
      await verificarPaginaEdicaoProcesso(page);
      await abrirDialogoRemocaoProcesso(page);
      await verificarDialogoConfirmacaoRemocao(page, descricaoProcesso);
      await confirmarRemocaoNoModal(page);

      // Verificação: Confirmar remoção bem-sucedida
      await verificarProcessoRemovidoComSucesso(page, descricaoProcesso);
    });
    ```

#### Anti-Padrões Críticos e Como Corrigi-los

Durante a refatoração, alguns anti-padrões foram identificados e precisam ser eliminados completamente:

##### 🚫 Anti-Padrão 1: IFs e Lógica Condicional nos Testes

**❌ PROBLEMA (Antes):**
```typescript
test('deve permitir selecionar unidade interoperacional...', async ({page}) => {
  await navegarParaCriacaoProcesso(page);
  const chkStic = page.locator('#chk-STIC');
  const chkCosis = page.locator('#chk-COSIS');
  await chkStic.click();
  
  // ❌ IF no teste - anti-padrão crítico
  if (await chkCosis.count() > 0) {
    await expect(chkCosis).not.toBeChecked();
  }
});
```

**✅ SOLUÇÃO (Depois):**
```typescript
// 1. Criar função auxiliar que encapsula a lógica condicional
export async function verificarComportamentoCheckboxInteroperacional(page: Page) {
  await page.waitForSelector('#chk-STIC');
  const chkStic = page.locator('#chk-STIC');
  const chkCosis = page.locator('#chk-COSIS');
  
  await chkStic.click();
  await expect(chkStic).toBeChecked();
  
  // Lógica condicional fica na função auxiliar, não no teste
  const cosisExists = await chkCosis.count() > 0;
  if (cosisExists) {
    await expect(chkCosis).not.toBeChecked();
  }
}

// 2. Teste fica limpo e semântico
test('deve permitir selecionar unidade interoperacional sem selecionar subordinadas', async ({page}) => {
  await navegarParaCriacaoProcesso(page);
  await verificarComportamentoCheckboxInteroperacional(page);
});
```

##### 🚫 Anti-Padrão 2: Verificações Técnicas Ruidosas

**❌ PROBLEMA (Antes):**
```typescript
test('deve editar processo...', async ({page}) => {
  await navegarParaProcessoNaTabela(page, descricaoOriginal);
  // ❌ Verificação técnica ruidosa exposta no teste
  await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);
  await editarDescricaoProcesso(page, descricaoEditada);
});
```

**✅ SOLUÇÃO (Depois):**
```typescript
// 1. Criar função auxiliar que encapsula a verificação técnica
export async function verificarPaginaEdicaoProcesso(page: Page) {
  await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);
}

// 2. Teste fica semântico, sem ruído técnico
test('deve editar processo...', async ({page}) => {
  await navegarParaProcessoNaTabela(page, descricaoOriginal);
  await verificarPaginaEdicaoProcesso(page);
  await editarDescricaoProcesso(page, descricaoEditada);
});
```

##### 🚫 Anti-Padrão 3: Ações Técnicas Diretas

**❌ PROBLEMA (Antes):**
```typescript
test('deve remover processo...', async ({page}) => {
  await abrirDialogoRemocaoProcesso(page);
  // ❌ Ação técnica direta no teste
  await page.locator('.modal.show .btn-danger').click();
});
```

**✅ SOLUÇÃO (Depois):**
```typescript
// 1. Criar função auxiliar semântica
export async function confirmarRemocaoNoModal(page: Page): Promise<void> {
  await page.locator('.modal.show .btn-danger').click();
}

// 2. Teste usa linguagem de negócio
test('deve remover processo...', async ({page}) => {
  await abrirDialogoRemocaoProcesso(page);
  await confirmarRemocaoNoModal(page);
});
```

##### 🚫 Anti-Padrão 4: Verificações Múltiplas Repetitivas

**❌ PROBLEMA (Antes):**
```typescript
test('deve criar processo...', async ({page}) => {
  await criarProcessoCompleto(page, descricaoProcesso, 'Mapeamento', '2025-12-31');
  // ❌ Verificações repetitivas em múltiplos testes
  await expect(page).toHaveURL(URLS.PAINEL);
  await expect(page.getByText(descricaoProcesso)).toBeVisible();
});
```

**✅ SOLUÇÃO (Depois):**
```typescript
// 1. Criar função auxiliar que agrupa verificações relacionadas
export async function aguardarProcessoNoPainel(page: Page, descricaoProcesso: string) {
  await page.waitForURL(URLS.PAINEL);
  await expect(page).toHaveURL(URLS.PAINEL);
  await expect(page.getByText(descricaoProcesso)).toBeVisible();
}

// 2. Teste usa função semântica única
test('deve criar processo...', async ({page}) => {
  await criarProcessoCompleto(page, descricaoProcesso, 'Mapeamento', '2025-12-31');
  await aguardarProcessoNoPainel(page, descricaoProcesso);
});
```

#### Processo de Refatoração (Passo a Passo)

Para refatorar um arquivo de teste existente (`cdu-XX.spec.ts`):

1.  **Análise Inicial:** 
    *   Leia todos os testes do arquivo para identificar padrões repetitivos.
    *   **🔍 Caça aos Anti-Padrões:** Identifique especificamente:
        - IFs, loops ou qualquer lógica condicional
        - Verificações de URL ou seletores CSS diretos
        - Ações com `.locator()`, `.click()` diretos
        - Verificações `expect()` repetitivas
    *   Mapeie os cenários de usuário que os testes representam.

2.  **Busque ou Crie Abstrações:**
    *   **Para IFs e condicionais:** Mova toda lógica condicional para funções auxiliares na Camada 2
    *   **Para verificações técnicas:** Crie funções de verificação semânticas (ex: `verificarPaginaEdicaoProcesso()`)
    *   **Para ações técnicas:** Crie funções de ação semânticas (ex: `confirmarRemocaoNoModal()`)
    *   **Priorize funções compostas** que encapsulem fluxos completos (ex: criar processo, editar processo).
    *   Adicione quaisquer novos seletores necessários na Camada 1.

3.  **Substitua e Simplifique:** 
    *   **Elimine IFs completamente:** Todo `if` deve desaparecer dos testes
    *   **Elimine verificações ruidosas:** Toda verificação técnica deve virar função semântica
    *   **Elimine duplicação agressivamente:** Se duas linhas de código aparecem em múltiplos testes, extraia para uma função.
    *   Mantenha apenas lógica específica do cenário no teste.

4.  **Revise e Valide:** 
    *   **Teste da Leitura:** Leia o teste refatorado em voz alta. Ele soa como um cenário de usuário? A intenção está clara sem precisar ler os detalhes da implementação?
    *   **Auditoria de Anti-Padrões:** Verifique se não há mais IFs, verificações técnicas ou ações diretas
    *   Execute os testes para garantir que funcionam corretamente.
    *   Limpe imports não utilizados.
    *   **🎯 Meta de Qualidade:** O teste deve ser lido e compreendido em segundos por qualquer desenvolvedor

#### Padrões e Convenções Específicas

**Nomenclatura de Funções:**
- **Ações Simples:** `clicar`, `preencher`, `selecionar`, `navegar`
- **Ações Compostas:** `criarCompleto`, `editarDescricao`, `removerComConfirmacao`
- **Ações de Modal:** `confirmarNoModal`, `cancelarNoModal`, `abrirDialogo`
- **Verificações:** `verificar`, `aguardar`, `assegurar`
- **Navegação:** `navegarPara`, `irPara`, `acessar`

**Padrões Específicos para Modais:**
```typescript
// Padrão de nomenclatura para ações de modal
export async function abrirDialogoRemocaoProcesso(page: Page): Promise<void> {
  await page.getByRole('button', { name: TEXTOS.REMOVER }).click();
}

export async function confirmarNoModal(page: Page): Promise<void> {
  await page.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
}

export async function cancelarNoModal(page: Page): Promise<void> {
  await page.getByRole('button', { name: TEXTOS.CANCELAR }).click();
}

export async function confirmarRemocaoNoModal(page: Page): Promise<void> {
  await page.locator('.modal.show .btn-danger').click();
}
```

**Padrões para Verificações de Estado da UI:**
```typescript
// Sempre criar função semântica para verificações técnicas
export async function verificarPaginaEdicaoProcesso(page: Page) {
  await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);
}

export async function verificarComportamentoMarcacaoCheckbox(page: Page) {
  const primeiroCheckbox = page.locator(SELETORES_CSS.CHECKBOX).first();
  await expect(primeiroCheckbox).toBeChecked();
  await primeiroCheckbox.click();
  await expect(primeiroCheckbox).not.toBeChecked();
}
```

**Organização de Parâmetros:**
```typescript
// Sempre Page primeiro, depois parâmetros específicos
export async function criarProcessoCompleto(
  page: Page, 
  descricao: string, 
  tipo: string, 
  dataLimite?: string
): Promise<void>
```

**Tratamento de Cenários Complexos:**
```typescript
// Para cenários que precisam de setup complexo, crie funções de cenário
export async function criarEEditarProcesso(page: Page, descricaoOriginal: string, descricaoNova: string): Promise<void> {
  await navegarParaCriacaoProcesso(page);
  await criarProcessoCompleto(page, descricaoOriginal, 'Mapeamento', '2025-12-31');
  await aguardarProcessoNoPainel(page, descricaoOriginal);
  await navegarParaProcessoNaTabela(page, descricaoOriginal);
  await editarDescricaoProcesso(page, descricaoNova);
}
```

#### Métricas de Sucesso da Refatoração

Uma refatoração bem-sucedida deve apresentar:

- **Redução de 40-60%** no número de linhas dos arquivos de teste (baseado no CDU-03: 344 → 167 linhas = 51% de redução)
- **Eliminação de 100%** dos IFs e lógica condicional nos testes
- **Eliminação de 100%** das verificações técnicas ruidosas nos testes (URLs, seletores CSS)
- **Eliminação de 90%+** dos seletores CSS diretos nos testes
- **Taxa de sucesso de 100%** nos testes após refatoração
- **Criação de 20-30 funções auxiliares** reutilizáveis por arquivo (CDU-03 criou 24 funções)
- **Tempo de leitura reduzido:** Um desenvolvedor deve entender o cenário em segundos
- **Zero imports de `expect`** nos arquivos de teste (todas as asserções ficam nas funções auxiliares)

#### Benchmarks de Qualidade (Baseados em CDU-03)

**📊 Métricas Quantitativas Alcançadas:**
- Linhas de código: 344 → 167 (-51%)
- IFs eliminados: 4 condicionais → 0 
- Verificações técnicas: 8 verificações ruidosas → 0
- Funções auxiliares: 0 → 24 funções semânticas
- Taxa de sucesso: 16/16 testes (100%)
- Imports limpos: `expect` removido dos testes

**🎯 Critérios de Qualidade:**
- ✅ Teste deve ler como história de usuário
- ✅ Zero conhecimento técnico necessário para entender o teste
- ✅ Cada linha do teste expressa uma intenção de negócio
- ✅ Manutenção concentrada nas funções auxiliares

#### Exemplos Completos: Transformação Antes vs Depois

##### Exemplo 1: Eliminação de IFs e Lógica Condicional

**❌ ANTES (Código Técnico com IFs):**
```typescript
test('deve permitir selecionar unidade interoperacional sem selecionar subordinadas', async ({page}) => {
  await navegarParaCriacaoProcesso(page);
  
  // Garantir que os checkboxes foram renderizados
  await page.waitForSelector('#chk-STIC');
  
  const chkStic = page.locator('#chk-STIC');
  const chkCosis = page.locator('#chk-COSIS');
  
  // Selecionar a unidade interoperacional raiz (STIC)
  await chkStic.click();
  
  // A raiz deve estar marcada...
  await expect(chkStic).toBeChecked();
  // ...mas as subordinadas NÃO devem ser marcadas automaticamente
  if (await chkCosis.count() > 0) {  // ❌ IF no teste!
    await expect(chkCosis).not.toBeChecked();
  }
  
  // Desmarcar STIC não deve afetar filhos
  await chkStic.click();
  await expect(chkStic).not.toBeChecked();
  if (await chkCosis.count() > 0) {  // ❌ Outro IF!
    await expect(chkCosis).not.toBeChecked();
  }
});
```

**✅ DEPOIS (Cenário Limpo):**
```typescript
test('deve permitir selecionar unidade interoperacional sem selecionar subordinadas', async ({page}) => {
  await navegarParaCriacaoProcesso(page);
  await verificarComportamentoCheckboxInteroperacional(page);
});
```

##### Exemplo 2: Eliminação de Verificações Técnicas Ruidosas

**❌ ANTES (Verificações Ruidosas):**
```typescript
test('deve remover processo com sucesso após confirmação', async ({page}) => {
  const descricaoProcesso = 'Processo para Remover';
  await navegarParaCriacaoProcesso(page);
  await page.getByLabel('Descrição').fill(descricaoProcesso);
  await page.getByLabel('Tipo').selectOption('Mapeamento');
  await page.getByLabel('Data limite').fill('2025-12-31');
  await page.waitForSelector('input[type="checkbox"]');
  const firstCheckbox = page.locator('input[type="checkbox"]').first();
  await firstCheckbox.click();
  await page.getByRole('button', { name: 'Salvar' }).click();
  await page.waitForURL('/painel');
  await expect(page).toHaveURL('/painel');  // ❌ Verificação técnica
  await expect(page.getByText(descricaoProcesso)).toBeVisible();
  
  await page.getByText(descricaoProcesso).click();
  await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);  // ❌ Ruído técnico
  
  await page.getByRole('button', { name: 'Remover' }).click();
  await expect(page.getByText(`Remover o processo '${descricaoProcesso}'?`)).toBeVisible();
  await page.locator('.modal.show .btn-danger').click();  // ❌ Seletor CSS direto
  
  await expect(page.getByText(`Processo ${descricaoProcesso} removido`)).toBeVisible();
  await expect(page).toHaveURL('/painel');  // ❌ Verificação técnica repetida
});
```

**✅ DEPOIS (História de Usuário):**
```typescript
test('deve remover processo com sucesso após confirmação', async ({page}) => {
  // Pré-condição: Criar um processo para ser removido
  const descricaoProcesso = 'Processo para Remover';
  await navegarParaCriacaoProcesso(page);
  await criarProcessoCompleto(page, descricaoProcesso, 'Mapeamento', '2025-12-31');
  await aguardarProcessoNoPainel(page, descricaoProcesso);

  // Ação principal: Remover processo  
  await navegarParaProcessoNaTabela(page, descricaoProcesso);
  await verificarPaginaEdicaoProcesso(page);
  await abrirDialogoRemocaoProcesso(page);
  await verificarDialogoConfirmacaoRemocao(page, descricaoProcesso);
  await confirmarRemocaoNoModal(page);

  // Verificação: Confirmar remoção bem-sucedida
  await verificarProcessoRemovidoComSucesso(page, descricaoProcesso);
});
```

##### Exemplo 3: Transformação Quantitativa Completa

**📊 Estatísticas da Transformação:**

| Métrica | Antes | Depois | Melhoria |
|---------|-------|--------|----------|
| **Linhas de código** | 344 | 167 | -51% |
| **IFs no teste** | 4 | 0 | -100% |
| **Verificações técnicas** | 8 | 0 | -100% |
| **Seletores CSS diretos** | 15+ | 0 | -100% |
| **Funções auxiliares** | 0 | 24 | +∞ |
| **Legibilidade** | Técnica | História de usuário | Transformação completa |

**🎯 Benefício Final:** O teste refatorado expressa claramente a **intenção** (remover processo com confirmação) e o **resultado esperado** (processo removido com sucesso), sem expor nenhum detalhe técnico. Qualquer pessoa da equipe pode entender o cenário instantaneamente.
