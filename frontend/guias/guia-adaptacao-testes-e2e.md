# Guia para Adaptação de Testes End-to-End (E2E)

## 1. Visão Geral da Nova Abordagem

Os testes E2E foram refatorados para eliminar a fragilidade e a lentidão causadas pelo login via interface gráfica (UI) e pela dependência de um estado de banco de dados preexistente.

A nova abordagem se baseia em dois pilares principais:

1.  **Backend em Modo de Teste**: O backend pode ser iniciado em um perfil `e2e` que utiliza um banco de dados em memória (H2) e expõe endpoints específicos para testes.
2.  **Login Programático (para a maioria dos testes)**: Para a maioria dos testes de funcionalidade, em vez de interagir com a tela de login, os testes agora utilizam helpers semânticos (ex: `loginComoAdmin`) que realizam a autenticação de forma instantânea e confiável por baixo dos panos.

O **seeding de dados** (limpeza e população do banco) agora é feito automaticamente antes da execução de toda a suíte de testes.

---

## 2. Arquitetura da Solução

### Backend (Perfil `e2e`)

Para iniciar o backend no modo de teste, utilize a tarefa Gradle customizada no diretório `C:\sgc\backend`:

```bash
gradle bootRunE2E
```

Isso ativa o perfil `e2e`, que expõe endpoints para `seed` do banco e para `login` programático, que são consumidos pelos nossos helpers de teste.

### Frontend (Playwright)

-   **`global-setup.ts`**: Script que garante que o banco de dados seja preparado antes dos testes.
-   **`helpers/auth.ts`**: **Este é o arquivo central da nova abordagem.** Ele restaura a simplicidade dos testes, fornecendo funções semânticas como `loginComoAdmin(page)`, `loginComoGestor(page)`, etc.
    -   **Como funciona?**: Cada uma dessas funções agora encapsula a lógica de:
        1.  Chamar o backend para criar uma sessão de autenticação.
        2.  Injetar o perfil de usuário correto (`ADMIN`, `GESTOR`, etc.) no `localStorage` do navegador.
        3.  Navegar para a página inicial (`/painel`) e recarregá-la, deixando tudo pronto para o teste.

---

## 3. Caso Especial: Testando o Fluxo de Login (CDU-01)

O arquivo `cdu-01.spec.ts` é um caso especial e **o único** que testa o fluxo de login da interface de usuário (UI) de ponta a ponta. Ele valida:

-   **Login Convencional (via UI)**: Simula a interação do usuário com a tela de login, verificando o redirecionamento para o painel em caso de perfil único e a exibição da tela de seleção de perfil em caso de múltiplos perfis.
    -   **Como funciona?**: Este teste utiliza os usuários criados pelo `seed` (ex: `chefeUsername` para perfil único, `multiPerfilUsername` para múltiplos perfis) e interage diretamente com os elementos da UI de login.
    -   **Importante**: Este teste **não** utiliza os helpers de login programático (`loginComoAdmin`, etc.) para testar a UI de login, pois o objetivo é justamente validar essa UI.
-   **Login Programático (Validação dos Helpers)**: Dentro do `cdu-01.spec.ts`, há também testes que utilizam os helpers de login programático (`loginComoAdmin`, `loginComoGestor`) para garantir que essas ferramentas de teste funcionam corretamente e produzem o estado esperado na aplicação.

---

## 4. Passo a Passo para Adaptar Outros Testes E2E (CDU-02 em diante)

Para todos os outros testes de funcionalidade (a partir do CDU-02), siga estes passos para refatorar um arquivo de teste `*.spec.ts` antigo, utilizando o login programático.

### Exemplo de Refatoração

O objetivo é substituir o login manual pela chamada de um único helper semântico.

**Antes (Frágil e Lento):**

```typescript
// O teste precisava navegar, preencher campos, clicar, etc.
import { test, expect } from '@playwright/test';
import { performLogin } from './helpers/authHelpers'; // Helper antigo

test('deve acessar o painel como ADMIN', async ({ page }) => {
    await performLogin(page, '7', 'ADMIN - SEDOC'); // Lógica complexa de UI

    await expect(page.getByTitle('Configurações do sistema')).toBeVisible();
});
```

**Depois (Robusto e Rápido):**

```typescript
// O teste apenas declara a intenção: logar como admin.
import { test, expect } from '@playwright/test';
import { loginComoAdmin } from './helpers/auth'; // Novo helper semântico

test('deve acessar o painel como ADMIN e ver o menu de configurações', async ({ page }) => {
    await loginComoAdmin(page);

    // A página já está no /painel, pronta para ser testada.
    await expect(page.getByTitle('Configurações do sistema')).toBeVisible();
});
```

### Guia de Adaptação

1.  **Remova as importações antigas**: Delete qualquer importação relacionada a `authHelpers` ou a helpers de interação de login (clicar, preencher, etc.).
2.  **Importe o novo helper semântico**: Importe a função de login desejada de `./helpers/auth` (ex: `loginComoChefe`).
3.  **Substitua o bloco de login**: Troque todo o bloco de código que fazia o login (navegar para a página, preencher campos, clicar em botões) pela chamada única ao novo helper (ex: `await loginComoChefe(page);`).
4.  **Remova navegações redundantes**: Como os novos helpers já te deixam no `/painel`, remova qualquer `page.goto('/painel')` que venha logo após a chamada de login.

## 5. Conclusão

Esta nova abordagem mantém a **clareza e a semântica** da DSL de testes, ao mesmo tempo que resolve os problemas de **velocidade e confiabilidade**.

-   **Desenvolvedores de Testes**: Devem continuar a usar apenas os helpers de alto nível (`loginComoAdmin`, etc.), sem se preocupar com os detalhes da implementação.
-   **Manutenção**: A lógica complexa de autenticação e injeção de estado fica centralizada em `helpers/auth.ts`, facilitando futuras atualizações.
