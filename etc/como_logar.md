# Como Logar no SGC (Perfil E2E)

Este documento descreve as credenciais e o processo de login para o ambiente de desenvolvimento/teste utilizando o perfil `e2e`.

## Credenciais de Teste

A senha padrão para todos os usuários populados pelo script de seed é: `senha`

### Principais Usuários

| Perfil/Papel | Título (Usuário) | Descrição |
| :--- | :--- | :--- |
| **Administrador** | `191919` | Acesso total ao sistema (ADMIN - SEDOC) |
| **Gestor** | `222222` | Perfil de Gestor da Unidade COORD_11 |
| **Chefe de Unidade** | `333333` | Perfil de Chefe da Unidade SECAO_111 |
| **Servidor** | `121212` | Perfil de Servidor da Unidade SECAO_113 |
| **Múltiplos Perfis** | `111111` | Permite escolher entre ADMIN e CHEFE da SEDOC |

## Processo de Login

1. Acesse a página de login da aplicação (geralmente em `/login`).
2. No campo **Título**, insira o número do título do usuário desejado (ex: `191919`).
3. No campo **Senha**, pode ser qualquer coisa.
4. Clique em **Entrar**.
5. **Seleção de Perfil:** Caso o usuário possua mais de um perfil vinculado (como o usuário `111111`), será exibida uma tela para seleção do perfil e da unidade que deseja utilizar na sessão atual.

## Origem dos Dados
Estas credenciais são baseadas nos seguintes arquivos do projeto:
*   [seed.sql](file:///Users/leonardo/sgc/e2e/setup/seed.sql): Script SQL que popula o banco de dados.
*   [helpers-auth.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-auth.ts): Definições de usuários utilizadas nos testes automatizados.
