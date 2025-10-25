# Análise Detalhada e Plano de Ação para Integração dos Testes E2E

## 1. Visão Geral

Os testes E2E (end-to-end) foram desenvolvidos em um ambiente mockado, dependendo de dados estáticos (`.test-data.json`) e um helper de autenticação (`auth.ts`) que simulava o login. Com a implementação do backend real, que utiliza um banco de dados semeado (`data.sql`) e um fluxo de autenticação real, os testes se tornaram incompatíveis.

Este documento fornece um guia **altamente detalhado e prescritivo** para migrar os testes E2E, garantindo que eles funcionem de forma integrada e confiável com o backend. Ele expande a análise inicial com detalhes técnicos específicos para ser consumido por um agente de IA.

## 2. Diagnóstico Técnico Detalhado

### 2.1. Incompatibilidade de Dados

A fonte da verdade para os dados de teste agora é o `backend/src/main/resources/data.sql`. Os dados hardcoded nos testes estão obsoletos.

**Mapeamento de Usuários (data.sql):**

| Perfil | Título Eleitoral | Nome | Unidade | Sigla |
|---|---|---|---|---|
| `ADMIN` | `6` | Ricardo Alves | Secretaria de Informática e Comunicações | `STIC` |
| `GESTOR`| `8` | Paulo Horta | Seção de Desenvolvimento de Sistemas | `SEDESENV`|
| `CHEFE` | `2` | Carlos Henrique Lima| Secretaria de Gestao de Pessoas | `SGP` |
| `CHEFE` | `3` | Fernanda Oliveira | Seção de Desenvolvimento de Sistemas | `SEDESENV`|
| `SERVIDOR`| `1` | Ana Paula Souza | Seção de Sistemas Eleitorais | `SESEL` |

**Ação Crítica - Adição de Usuário Multi-Perfil:**

O teste `cdu-01.spec.ts` valida o fluxo de login para um usuário com múltiplos perfis (`multiPerfilUsername`), mas **não existe tal usuário em `data.sql`**. É necessário adicioná-lo para garantir a cobertura do teste.

**SQL a ser adicionado em `data.sql`:**

```sql
-- Usuário com múltiplos perfis para teste E2E
INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES
(999999999999, 'Usuario Multi Perfil', 'multi.perfil@tre-pe.jus.br', '9999', 2); -- Associado à STIC

INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES
(999999999999, 'ADMIN'),
(999999999999, 'GESTOR');
```

### 2.2. Fluxo de Autenticação Real vs. Mock

O `frontend/e2e/helpers/auth.ts` atual é **completamente incompatível**. Ele usa um endpoint falso (`/api/test/login`) e manipula o `localStorage`, o que não reflete a realidade.

**O fluxo de autenticação real, conforme `UsuarioControle.java`, ocorre em 3 etapas:**

1.  **`POST /api/usuarios/autenticar`**: Envia `{ tituloEleitoral, senha }` e recebe `true` ou `false`.
2.  **`POST /api/usuarios/autorizar`**: Envia o `tituloEleitoral` e recebe uma lista de perfis/unidades. Ex: `[{ perfil: 'ADMIN', unidade: 'STIC' }]`.
3.  **`POST /api/usuarios/entrar`**: Envia o `{ tituloEleitoral, perfil, unidadeCodigo }` selecionado e recebe os dados da sessão, incluindo um token.

A UI do frontend (`Login.vue`) orquestra essas três chamadas. **Os testes E2E devem simular a interação do usuário com esta UI.**

## 3. Plano de Ação Prescritivo

### 3.1. Centralização e Correção dos Dados de Teste

**1. Remover Arquivo Obsoleto:**
   - Delete o arquivo `frontend/e2e/.test-data.json`.

**2. Substituir Conteúdo de `constantes-teste.ts`:**
   - O arquivo `frontend/e2e/helpers/dados/constantes-teste.ts` deve ser **completamente sobrescrito**. Os seletores e textos devem ser mantidos, mas os dados de usuário (`DADOS_TESTE`) devem ser substituídos por uma estrutura `USUARIOS` alinhada com `data.sql`.

   **Novo conteúdo para `constantes-teste.ts`:**
   ```typescript
   // (Mantenha as exportações SELETORES, TEXTOS, URLS, etc., existentes no arquivo)

   export const USUARIOS = {
     ADMIN: {
       titulo: '6',
       nome: 'Ricardo Alves',
       senha: '123', // A senha é '123' para todos no ambiente de teste
       unidade: 'STIC',
     },
     GESTOR: {
       titulo: '8',
       nome: 'Paulo Horta',
       senha: '123',
       unidade: 'SEDESENV',
     },
     CHEFE_SGP: {
       titulo: '2',
       nome: 'Carlos Henrique Lima',
       senha: '123',
       unidade: 'SGP',
     },
     CHEFE_SEDESENV: {
       titulo: '3',
       nome: 'Fernanda Oliveira',
       senha: '123',
       unidade: 'SEDESENV',
     },
     SERVIDOR: {
       titulo: '1',
       nome: 'Ana Paula Souza',
       senha: '123',
       unidade: 'SESEL',
     },
     MULTI_PERFIL: {
       titulo: '999999999999',
       nome: 'Usuario Multi Perfil',
       senha: '123',
       perfis: ['ADMIN - STIC', 'GESTOR - STIC'], // Formato 'PERFIL - SIGLA' como na UI
     },
   } as const;

   // (Mantenha as exportações restantes como SELETORES_CSS)
   ```

### 3.2. Refatoração Completa do Helper de Autenticação

O arquivo `frontend/e2e/helpers/auth.ts` deve ser **sobrescrito** com uma nova implementação que interage com a UI de login.

**Novo conteúdo para `auth.ts`:**

```typescript
import { Page } from '@playwright/test';
import { USUARIOS } from './dados/constantes-teste';

/**
 * Realiza o login completo através da interface de usuário.
 * Lida com a seleção de perfil para usuários com múltiplos perfis.
 *
 * @param page A instância da página do Playwright.
 * @param usuario O objeto do usuário (ex: USUARIOS.ADMIN).
 * @param perfilUnidadeLabel O perfil a ser selecionado, se houver múltiplos (ex: 'ADMIN - STIC').
 */
async function loginPelaUI(
  page: Page,
  usuario: { titulo: string; senha: string },
  perfilUnidadeLabel?: string
) {
  await page.goto('/login');
  await page.getByTestId('input-titulo').fill(usuario.titulo);
  await page.getByTestId('input-senha').fill(usuario.senha);
  await page.getByTestId('botao-entrar').click();

  // Se um seletor de perfil aparecer, selecione a opção desejada
  if (perfilUnidadeLabel) {
    const seletorPerfil = page.getByTestId('select-perfil-unidade');
    await seletorPerfil.waitFor({ state: 'visible', timeout: 5000 });
    await seletorPerfil.selectOption({ label: perfilUnidadeLabel });
    await page.getByTestId('botao-entrar').click();
  }

  // Aguarda o redirecionamento para o painel
  await page.waitForURL('/painel', { timeout: 10000 });
}

// --- Funções de Abstração Semântica (DSL) ---

export async function loginComoAdmin(page: Page) {
  await loginPelaUI(page, USUARIOS.ADMIN);
}

export async function loginComoGestor(page: Page) {
  await loginPelaUI(page, USUARIOS.GESTOR);
}

export async function loginComoChefe(page: Page) {
  // Usar um chefe específico como padrão
  await loginPelaUI(page, USUARIOS.CHEFE_SGP);
}

export async function loginComoServidor(page: Page) {
  await loginPelaUI(page, USUARIOS.SERVIDOR);
}

export async function loginComMultiPerfilAdmin(page: Page) {
  await loginPelaUI(page, USUARIOS.MULTI_PERFIL, 'ADMIN - STIC');
}

export async function loginComMultiPerfilGestor(page: Page) {
    await loginPelaUI(page, USUARIOS.MULTI_PERFIL, 'GESTOR - STIC');
}
```

### 3.3. Guia de Migração para Arquivos de Teste (`.spec.ts`)

Todos os testes `cdu-xx.spec.ts` devem ser atualizados. Use `cdu-01.spec.ts` como exemplo.

**Passos de Migração para `cdu-01.spec.ts`:**

1.  **Remover Imports e Leitura de Dados Antigos:**
    - Remova `import * as fs from 'fs';`, `import * as path from 'path';`, `import { fileURLToPath } from 'url';` e toda a lógica de leitura do `.test-data.json`.

2.  **Atualizar Imports:**
    - Importe os novos `USUARIOS` e as funções de login atualizadas.

3.  **Refatorar Testes:**
    - Substitua o preenchimento manual de dados e as chamadas antigas de login.

**Exemplo de `cdu-01.spec.ts` Refatorado:**

```typescript
import { test, expect } from '@playwright/test';
import {
  loginComoAdmin,
  loginComoChefe,
  loginComMultiPerfilAdmin,
} from './helpers/auth';
import { USUARIOS } from './helpers/dados/constantes-teste';

test.describe('CDU-01: Fluxo de Login e Seleção de Perfil', () => {

  test.describe('Login Convencional (via UI)', () => {

    test('deve fazer login e ir direto para o painel com perfil único', async ({ page }) => {
      await page.goto('/login');
      await page.getByTestId('input-titulo').fill(USUARIOS.CHEFE_SGP.titulo);
      await page.getByTestId('input-senha').fill(USUARIOS.CHEFE_SGP.senha);
      await page.getByTestId('botao-entrar').click();

      await expect(page).toHaveURL('/painel');
      await expect(page.getByTestId('titulo-processos')).toBeVisible();
    });

    test('deve mostrar seleção de perfis para usuário com múltiplos perfis', async ({ page }) => {
      await page.goto('/login');
      await page.getByTestId('input-titulo').fill(USUARIOS.MULTI_PERFIL.titulo);
      await page.getByTestId('input-senha').fill(USUARIOS.MULTI_PERFIL.senha);
      await page.getByTestId('botao-entrar').click();

      await expect(page.getByText('Selecione o perfil e a unidade')).toBeVisible();
      const seletor = page.getByTestId('select-perfil-unidade');
      await expect(seletor).toBeVisible();

      await seletor.selectOption({ label: 'ADMIN - STIC' });
      await page.getByTestId('botao-entrar').click();

      await expect(page).toHaveURL('/painel');
      await expect(page.getByTitle('Configurações do sistema')).toBeVisible(); // Ícone de engrenagem do admin
    });
  });

  test.describe('Login Programático (Helpers de Teste)', () => {

    test('deve funcionar para loginComoAdmin', async ({ page }) => {
      await loginComoAdmin(page);
      await expect(page).toHaveURL('/painel');
      await expect(page.getByTitle('Configurações do sistema')).toBeVisible();
    });

    test('deve funcionar para loginComoChefe', async ({ page }) => {
      await loginComoChefe(page);
      await expect(page).toHaveURL('/painel');
      // Chefe não vê o ícone de admin
      await expect(page.getByTitle('Configurações do sistema')).not.toBeVisible();
    });

    test('deve funcionar para login com múltiplos perfis', async ({ page }) => {
        await loginComMultiPerfilAdmin(page);
        await expect(page).toHaveURL('/painel');
        await expect(page.getByTitle('Configurações do sistema')).toBeVisible();
    });
  });
});
```

## 4. Próximos Passos

A execução deste plano de ação detalhado irá alinhar a suíte de testes E2E com o backend. Após a atualização dos helpers e dos dados, cada arquivo `cdu-xx.spec.ts` deve ser revisado e adaptado seguindo o modelo acima. É crucial verificar seletores e asserções, pois a UI pode ter divergências em relação à versão mockada.
