# Pacote Usuario

## Visão Geral

O pacote `usuario` é responsável pelo gerenciamento de usuários, perfis de acesso, autenticação e autorização dentro do SGC. Ele atua como o ponto central para informações de identidade e estrutura organizacional do usuário.

## Responsabilidades Principais

1.  **Autenticação e Autorização:** Gerencia o processo de login (`entrar`, `autenticar`) e verifica permissões de acesso baseadas em **Perfis** (`ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`) e **Unidades**.
2.  **Gestão de Identidade:** Mantém os dados dos usuários (título eleitoral, nome, e-mail) e seus vínculos com unidades.
3.  **Fachada de Integração:** O `UsuarioService` atua como uma fachada para o sistema, fornecendo dados de usuários e hierarquia de unidades para outros módulos, abstraindo a complexidade de consultas e integrações (como o mock de SGRH/AD).

## Componentes Chave

### `UsuarioService`

A classe principal do módulo.

*   **Autenticação:** Valida credenciais (via `AcessoAdClient` ou mock em testes).
*   **Autorização:** Recupera e valida as atribuições (`UsuarioPerfil`) do usuário para uma determinada unidade.
*   **Fornecimento de Dados:** Métodos como `buscarUsuarioPorTitulo`, `buscarResponsavelUnidade`, e `construirArvoreHierarquica` são amplamente utilizados por outros serviços (`Processo`, `Notificacao`) para obter contexto.

### `Usuario` (Entidade)

Representa o usuário do sistema.
*   **ID:** Título Eleitoral (String).
*   **Relacionamentos:** Possui uma coleção de `UsuarioPerfil` que define seus papéis em diferentes unidades.

### `Perfil` (Enum)

Define os níveis de acesso:
*   `ADMIN`: Acesso total ao sistema.
*   `GESTOR`: Gestão de processos e configurações da unidade.
*   `CHEFE`: Aprovação e validação.
*   `SERVIDOR`: Execução de tarefas operacionais.

## Integração com Segurança

O módulo trabalha em conjunto com o pacote `seguranca` para:
*   Gerar tokens JWT (`GerenciadorJwt`).
*   Validar requisições via filtros de segurança.
