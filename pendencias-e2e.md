# Análise e Plano de Ação para Integração dos Testes E2E com o Backend

## 1. Visão Geral

Os testes E2E (end-to-end) foram desenvolvidos com base em um ambiente mockado, utilizando dados estáticos de um arquivo `.test-data.json` e um helper de autenticação (`auth.ts`) que simulava o login. Com a implementação do backend real e a utilização de um banco de dados semeado via `data.sql`, os testes atuais estão incompatíveis e precisam ser adaptados.

Este documento detalha as pendências e o plano de ação para migrar os testes E2E para que funcionem de forma integrada com o backend.

## 2. Pontos Críticos de Divergência

A análise inicial revelou os seguintes pontos de divergência principais:

*   **Autenticação:** O helper `auth.ts` usa um endpoint de login falso (`/api/test/login`) e injeta dados diretamente no `localStorage`. Isso não reflete o fluxo de autenticação real do backend.
*   **Dados de Teste:** Os IDs de usuário, perfis e unidades hardcoded nos testes e no arquivo `.test-data.json` não correspondem aos dados presentes no `data.sql`.
*   **Fluxos de Navegação:** A interface e os fluxos de navegação podem ter mudado desde que os testes foram escritos, exigindo a atualização de seletores e lógicas de teste.
*   **Estado da Aplicação:** Os testes dependem de um estado inicial fixo que era garantido pelo ambiente mockado. Com um backend real, o estado pode variar, exigindo uma preparação de dados mais robusta antes da execução dos testes.

## 3. Plano de Ação Detalhado

A seguir, são detalhados os passos necessários para a integração.

### 3.1. Análise e Mapeamento de Dados

A primeira etapa é mapear os dados disponíveis no `data.sql` para substituir os dados mockados.

**Usuários e Perfis Disponíveis:**

| Perfil   | Título Eleitoral | Nome                  | Unidade            | Sigla      |
|----------|------------------|-----------------------|--------------------|------------|
| `ADMIN`  | `6`              | Ricardo Alves         | Secretaria de Informática e Comunicações | `STIC`     |
| `GESTOR` | `8`              | Paulo Horta           | Seção de Desenvolvimento de Sistemas     | `SEDESENV` |
| `CHEFE`  | `2`              | Carlos Henrique Lima  | Secretaria de Gestao de Pessoas          | `SGP`      |
| `CHEFE`  | `3`              | Fernanda Oliveira     | Seção de Desenvolvimento de Sistemas     | `SEDESENV` |
| `SERVIDOR`| `1`              | Ana Paula Souza       | Seção de Sistemas Eleitorais             | `SESEL`    |

**Pontos de Atenção:**

*   **Usuário Multi-Perfil:** O `data.sql` **não contém** um usuário com múltiplos perfis, que era um cenário de teste importante no `cdu-01.spec.ts` (`multiPerfilUsername`). Será necessário **adicionar um usuário com múltiplos perfis** ao `data.sql` para garantir a cobertura deste caso.
*   **IDs de Usuário:** Os IDs de usuário (`tituloEleitoral`) são diferentes dos que estavam no `.test-data.json`. Os testes devem ser atualizados para usar os novos IDs.
*   **Unidades:** As unidades e suas siglas estão definidas no `data.sql`. Os testes que dependem de unidades específicas (`SEDOC`, `SGP`, `STIC`) devem ser revisados para garantir que os usuários selecionados pertencem a elas.

### 3.2. Refatoração do Helper de Autenticação (`auth.ts`)

O helper `frontend/e2e/helpers/auth.ts` precisa ser completamente refatorado.

**Problemas Atuais:**

*   A função `autenticarComo` chama um endpoint mockado (`http://localhost:10000/api/test/login`) que não existe no backend real.
*   As funções `injetarPerfil` e `loginProgramatico` manipulam o `localStorage` para simular uma sessão de usuário, o que não funcionará com o sistema de autenticação do backend (que provavelmente usa cookies de sessão ou tokens JWT).

**Plano de Ação:**

1.  **Remover `autenticarComo` e `injetarPerfil`:** Essas funções são específicas do ambiente mockado e devem ser removidas.
2.  **Implementar Login via UI:** A função `loginProgramatico` deve ser substituída por uma nova função que simule o login real através da interface de usuário. A nova função deve:
    *   Navegar para a página de login (`/login`).
    *   Preencher os campos de título de eleitor e senha com os dados do usuário.
    *   Clicar no botão de "Entrar".
    *   Lidar com a seleção de perfil, caso o usuário tenha múltiplos perfis.
3.  **Atualizar Funções de Abstração:** As funções `loginComoAdmin`, `loginComoGestor`, etc., devem ser atualizadas para chamar a nova função de login via UI, passando os dados corretos do `data.sql`.

### 3.3. Estratégia para Dados de Teste

O arquivo `.test-data.json` deve ser removido. Os dados de teste devem ser gerenciados da seguinte forma:

1.  **Constantes de Teste:** Criar um novo arquivo de constantes em `frontend/e2e/helpers/dados/constantes-teste.ts` para armazenar os dados dos usuários que serão utilizados nos testes. Isso centraliza os dados e facilita a manutenção.

    ```typescript
    // Exemplo para constantes-teste.ts
    export const USUARIOS = {
      ADMIN: {
        titulo: '6',
        senha: '123', // A senha real deve ser definida no backend
      },
      GESTOR: {
        titulo: '8',
        senha: '123',
      },
      // ... outros usuários
    };
    ```

2.  **Uso nos Testes:** Os testes devem importar essas constantes em vez de ler o arquivo JSON.

    ```typescript
    // Exemplo em um arquivo .spec.ts
    import { USUARIOS } from './helpers/dados/constantes-teste';

    test('deve fazer login como admin', async ({ page }) => {
      // ...
      await page.getByTestId('input-titulo').fill(USUARIOS.ADMIN.titulo);
      // ...
    });
    ```

3.  **Dados Dinâmicos:** Para dados que são criados ou modificados durante os testes (ex: um novo processo), os testes devem extrair e armazenar esses dados em variáveis para uso em etapas posteriores do mesmo teste.

### 3.4. Guia de Atualização dos Arquivos de Teste (`.spec.ts`)

Todos os arquivos `cdu-xx.spec.ts` precisarão de revisão e atualização.

**Passos para cada arquivo de teste:**

1.  **Remover Leitura do `.test-data.json`:** Remover o código que lê o arquivo `.test-data.json`.
2.  **Importar Constantes:** Importar os novos helpers e constantes de dados.
    ```typescript
    import { loginComoAdmin } from './helpers/auth';
    import { USUARIOS } from './helpers/dados/constantes-teste';
    ```
3.  **Atualizar Chamadas de Login:** Substituir as chamadas de `loginComoX` e o preenchimento manual do formulário de login para usar a nova abordagem.
4.  **Verificar Seletores:** Executar os testes no modo "headed" (`npx playwright test --headed`) para verificar se os seletores (`getByTestId`, `getByText`, etc.) ainda são válidos. A estrutura do HTML pode ter mudado, exigindo a atualização dos seletores.
5.  **Validar Asserções:** As asserções (`expect(...)`) devem ser revisadas para garantir que os resultados esperados (mensagens de sucesso, estado da UI, etc.) correspondem ao comportamento real do backend. Por exemplo, mensagens de erro de validação ou confirmação podem ter textos diferentes.
6.  **Aguardar Respostas de Rede:** Em operações que envolvem chamadas de API (ex: salvar um formulário), adicionar esperas explícitas (`await page.waitForResponse(...)`) para garantir que o teste só continue após a conclusão da chamada de rede, tornando-o mais robusto.

## 4. Conclusão

A integração dos testes E2E com o backend é um passo crucial para garantir a qualidade e a estabilidade da aplicação. As ações descritas neste documento fornecem um roteiro claro para a execução dessa tarefa, que envolve a refatoração da autenticação, a atualização dos dados de teste e a revisão de todos os cenários de teste. A execução deste plano resultará em uma suíte de testes E2E mais confiável e alinhada com o ambiente de produção.
