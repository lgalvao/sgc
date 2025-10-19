# Guia para Criação de Testes End-to-End (E2E) com Playwright

Este guia estabelece as diretrizes e a filosofia para a criação de testes E2E no projeto, garantindo que sejam legíveis, manuteníveis e robustos.

## 📜 Filosofia Principal: Testes como Documentação Viva

O objetivo central dos nossos testes E2E é que eles sirvam como uma **documentação viva** dos casos de uso (CDUs) do sistema. Cada arquivo de teste (`cdu-xx.spec.ts`) deve contar uma história clara e concisa sobre o que o usuário pode fazer, sem se afogar em detalhes de implementação do Playwright.

Para alcançar isso, adotamos uma abordagem de **Domain-Specific Language (DSL)**. Em vez de escrever código Playwright diretamente nos testes, nós o abstraímos em **funções auxiliares (helpers)** com nomes que descrevem a **intenção** da ação no domínio do negócio.

**❌ Ruim: Código imperativo no teste**
```typescript
test('deve criar um novo processo', async ({ page }) => {
  await page.getByRole('button', { name: /novo processo/i }).click();
  await page.locator('#tipo-processo').selectOption('REVISAO');
  await page.locator('input[data-testid="unidade-participante"]').fill('TRE-GO');
  await page.getByRole('button', { name: 'Salvar' }).click();
  await expect(page.getByText(/processo criado com sucesso/i)).toBeVisible();
});
```

**✅ Bom: Usando a DSL para contar uma história**
```typescript
test('deve criar um novo processo de revisão', async ({ page }) => {
  await abrirModalNovoProcesso(page);
  await preencherFormularioNovoProcesso(page, { tipo: 'REVISAO', unidade: 'TRE-GO' });
  await submeterFormularioNovoProcesso(page);
  await verificarNotificacaoSucesso(page, 'Processo criado com sucesso');
});
```

## 📂 Estrutura dos Helpers

A "mágica" acontece no diretório `frontend/e2e/cdu/helpers`. Ele organiza todas as nossas funções auxiliares em subdiretórios com responsabilidades bem definidas.

```
helpers/
├── 📁 acoes/         # Ações do usuário (clicar, preencher, submeter)
├── 📁 verificacoes/  # Asserções e verificações (verificar texto, estado, visibilidade)
├── 📁 navegacao/     # Funções de login e navegação entre páginas
├── 📁 dados/         # Constantes de teste (seletores, URLs, credenciais)
├── 📁 utils/         # Funções utilitárias genéricas (ex: esperar por loading)
└── 📜 index.ts       # Ponto de entrada que exporta tudo
```

### `index.ts`: O Coração da DSL
Este arquivo é o único ponto de importação para os arquivos de teste. Ele re-exporta todas as funções dos subdiretórios, criando uma interface coesa e fácil de usar.

- **Responsabilidade:** Unificar e expor a DSL.
- **Como usar:** `import { loginComoAdmin, verificarEstruturaServidor } from './helpers';`

### `acoes/`
Contém funções que executam ações do usuário em componentes ou páginas específicas.

- **Exemplos:** `clicarBotaoSalvar()`, `preencherCampoDeBusca(texto)`, `submeterFormularioLogin()`.
- **Princípio:** Se o usuário "faz" algo, a função correspondente vive aqui.

### `verificacoes/`
Contém funções que fazem asserções sobre o estado da aplicação. Elas **verificam** se o resultado de uma ação é o esperado.

- **Exemplos:** `verificarMensagemDeErro()`, `verificarVisibilidadeDoPainel()`, `verificarTabelaDeProcessosContem(item)`.
- **Princípio:** Se o teste precisa "checar" algo, a função vive aqui.

### `navegacao/`
Responsável por toda a lógica de navegação, incluindo o fluxo de login e o acesso a URLs específicas.

- **Exemplos:** `navegarParaHome()`, `loginComoServidor()`, `fazerLogout()`.
- **Princípio:** Funções que movem o usuário entre diferentes partes da aplicação.

### `dados/`
Centraliza todos os dados estáticos e constantes usados nos testes para evitar "números mágicos" e strings repetidas.

- **O que contém:**
    - `constantes-teste.ts`: Principalmente o objeto `SELETORES`, que mapeia `data-testid` para nomes legíveis.
    - `usuarios.ts`: Credenciais de usuários de teste.
- **Princípio:** Manter os dados de teste separados da lógica de teste. **Sempre** use `SELETORES` em vez de strings como `'[data-testid="meu-seletor"]'`.

### `utils/`
Funções auxiliadres que não se encaixam nas outras categorias, geralmente relacionadas a controle de fluxo ou interações de baixo nível.

- **Exemplos:** `esperarLoadingAparecer()`, `esperarNotificacao()`.
- **Princípio:** Utilitários técnicos que apoiam as ações e verificações.

---

## 🚀 Como Criar um Novo Teste E2E

Siga estes passos para criar um teste novo, limpo e manutenível.

### Passo 1: Escreva a "História" no Arquivo `.spec.ts`

Antes de escrever qualquer código de helper, comece pelo arquivo de teste (`cdu-xx.spec.ts`). Escreva o teste como se todas as funções auxiliares que você precisa **já existissem**. Isso força você a pensar no fluxo do usuário em alto nível.

```typescript
// em cdu-99.spec.ts
import { test } from '@playwright/test';
import {
  loginComoChefe,
  navegarParaPaginaDeProcessos,
  // Funções que ainda não existem!
  abrirModalDeDelegacao,
  delegarProcessoPara,
  verificarProcessoFoiDelegado,
} from './helpers';

test.describe('CDU-99: Delegar Processo', () => {
  test('chefe de unidade deve conseguir delegar um processo para um servidor', async ({ page }) => {
    await loginComoChefe(page);
    await navegarParaPaginaDeProcessos(page);

    // Escreva a história do que o usuário faz
    await abrirModalDeDelegacao(page, 'Processo XPTO-123');
    await delegarProcessoPara(page, 'Servidor Fulano');
    await verificarProcessoFoiDelegado(page, 'Processo XPTO-123', 'Servidor Fulano');
  });
});
```

### Passo 2: Reutilize ou Crie os Helpers Necessários

Agora, implemente as funções que você idealizou.

1.  **Verifique se já existe:** Antes de criar uma função nova, procure nos diretórios de `helpers` se uma função similar já existe. Muitas vezes, uma ação como `clicarBotaoSalvar` ou uma verificação como `verificarNotificacaoSucesso` já foi criada.

2.  **Crie a função no lugar certo:**
    - Ação do usuário? Crie em `acoes/`.
    - Verificação de estado? Crie em `verificacoes/`.
    - E assim por diante.

3.  **Use `SELETORES`:** Ao interagir com elementos, **nunca** use seletores de CSS ou XPath diretamente. Adicione uma entrada ao objeto `SELETORES` em `dados/constantes-teste.ts` e use-a na sua função.

    ```typescript
    // Em dados/constantes-teste.ts
    export const SELETORES = {
      // ...outros seletores
      modalDelegacao: {
        selectServidor: 'delegacao-select-servidor',
        botaoConfirmar: 'delegacao-btn-confirmar',
      }
    };

    // Em acoes/acoes-processo.ts
    import { SELETORES } from '../dados';

    export async function delegarProcessoPara(page: Page, nomeServidor: string) {
      await page.getByTestId(SELETORES.modalDelegacao.selectServidor).selectOption(nomeServidor);
      await page.getByTestId(SELETORES.modalDelegacao.botaoConfirmar).click();
    }
    ```

### Passo 3: Exporte as Novas Funções

Para que a sua nova função `delegarProcessoPara` fique disponível para o teste, você precisa exportá-la a partir do `index.ts` do seu módulo e, finalmente, do `index.ts` principal dos `helpers`.

1.  **Exporte do módulo (`acoes/index.ts`):**
    ```typescript
    // Em acoes/index.ts
    export * from './acoes-autenticacao';
    export * from './acoes-processo'; // Adicione o seu arquivo aqui
    ```

2.  **Verifique a exportação principal (`helpers/index.ts`):**
    O `index.ts` principal já deve estar exportando tudo de `acoes` com `export * from './acoes'`. Não é preciso fazer alterações aqui.

### Passo 4: Rode o Teste

Execute o seu novo teste para garantir que a "história" que você escreveu agora passa com sucesso.

---

## Exemplo Prático: CDU-02 - Visualizar Painel

Vamos analisar um exemplo real e simplificado do projeto (`cdu-02.spec.ts`) para consolidar os conceitos.

**Objetivo do Teste:** Garantir que um usuário com perfil `SERVIDOR` vê os elementos corretos no painel após o login.

### 1. O Teste (`cdu-02.spec.ts`)

O arquivo de teste é declarativo. Ele apenas descreve as ações e verificações em alto nível.

```typescript
import { test } from '@playwright/test';
import {
  loginComoServidor,
  verificarElementosPainel,
  verificarEstruturaServidor,
} from './helpers';

test.describe('CDU-02: Visualizar Painel do Servidor', () => {
  test('deve exibir a estrutura correta para o perfil SERVIDOR', async ({ page }) => {
    // Ação de Navegação
    await loginComoServidor(page);

    // Verificações de UI
    await verificarElementosPainel(page);
    await verificarEstruturaServidor(page);
  });
});
```

### 2. As Funções Auxiliares (`helpers/`)

A "mágica" está nas funções auxiliares que o teste chama.

#### `navegacao/login.ts`
A função `loginComoServidor` abstrai todo o processo de login. Ela reutiliza uma função de login genérica, passando os dados do usuário `SERVIDOR`.

```typescript
// Em navegacao/login.ts
import { USUARIOS } from '../dados'; // Importa as credenciais
import { login } from './autenticacao'; // Importa a lógica base de login

export async function loginComoServidor(page: Page): Promise<void> {
  await login(page, USUARIOS.servidor.titulo, USUARIOS.servidor.senha);
}
```

#### `verificacoes/verificacoes-painel.ts`
As funções de verificação contêm as asserções (`expect`). Elas usam o objeto `SELETORES` para encontrar os elementos na página de forma segura.

```typescript
// Em verificacoes/verificacoes-painel.ts
import { expect, Page } from '@playwright/test';
import { SELETORES } from '../dados';

// Verifica se os elementos básicos do painel (comuns a todos os perfis) estão visíveis
export async function verificarElementosPainel(page: Page): Promise<void> {
  await expect(page.getByTestId(SELETORES.painel.cabecalho)).toBeVisible();
  await expect(page.getByTestId(SELETORES.painel.menuLateral)).toBeVisible();
  await expect(page.getByTestId(SELETORES.painel.areaConteudo)).toBeVisible();
}

// Verifica se o item de menu específico do SERVIDOR está visível e o de ADMIN não
export async function verificarEstruturaServidor(page: Page): Promise<void> {
  await expect(page.getByTestId(SELETORES.menu.processos)).toBeVisible();
  await expect(page.getByTestId(SELETORES.menu.configuracoes)).not.toBeVisible();
}
```

### 3. As Constantes (`dados/constantes-teste.ts`)

Finalmente, o arquivo `constantes-teste.ts` fornece um local centralizado e confiável para os seletores `data-testid`, evitando que strings se espalhem pelo código.

```typescript
// Em dados/constantes-teste.ts
export const SELETORES = {
  painel: {
    cabecalho: 'painel-cabecalho',
    menuLateral: 'painel-menu-lateral',
    areaConteudo: 'painel-conteudo',
  },
  menu: {
    processos: 'menu-item-processos',
    configuracoes: 'menu-item-configuracoes',
  },
  // ... outros seletores
};
```

Seguindo esta estrutura, mantemos nossos testes limpos, fáceis de entender e resistentes a mudanças na implementação da UI.
