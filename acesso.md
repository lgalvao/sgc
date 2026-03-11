## Controle de acesso e permissões

Este documento detalha o funcionamento técnico e as regras de negócio que governam o acesso ao SGC. O sistema utiliza uma arquitetura de segurança baseada em **Contexto de Usuário**, **Hierarquia Organizacional** e **Localização do Processo**.

### 1. Autenticação e contexto (Frontend)
Ao realizar o login, o sistema estabelece o contexto do usuário:
* **Identidade:** Quem é o usuário (com base no título de eleitor).
* **Perfil ativo:** O usuário seleciona um **Perfil**.
* **Unidade ativa:** O usuário seleciona uma **Unidade**.
* **Localização:** A localização de cada subprocesso é mantida pelo backend, sendo calculada com base nas movimentações do subprocesso. A localização sempre começa na unidade do subprocesso.
* **Token JWT:** O token guarda a unidade e o perfil ativos.

### 2. Grupos de ações
O sistema divide as ações em dois grupos, cada um com uma lógica de proteção distinta:

#### **A. Ações de fluxo (dependem de perfil e localização do subprocesso)**
São as ações que fazem o subprocesso tramitar ou alteram seus dados (Cadastrar/Salvar, Disponibilizar, Aceitar, Homologar, Devolver). 
*   **Lógica de validação:**
    1.  O perfil no token deve ser compatível com a ação (ex: apenas o perfil CHEFE pode disponibilizar um cadastro).
    2.  A unidade ativa no token deve ser **EXATAMENTE IGUAL** à localização atual do subprocesso no backend.

#### **B. Ações privativas do ADMIN (independem da localização)**
São ações administrativas globais que **ignoram a localização do processo**.
*   **Lógica de validação:** 
    1. O sistema checa se o perfil no token é ADMIN.
    2. Para algumas ações, o sistema verifica a **Situação** do processo e do subprocesso (ex: não se pode editar um processo que já foi 'FINALIZADO').
*   **Ações:**
    - Criar e editar processos (edição permitida apenas na situação 'CRIADO').
    - Alterar data limite de subprocessos.
    - Cadastrar outros administradores.
    - Alterar configurações globais.
    - Reabrir cadastro de atividade/conhecimentos (permitido apenas a partir de certa situação do subprocesso).

### 3. Regras de visibilidade (Leitura)
A permissão de visualização é determinada pela **Hierarquia organizacional**, consultada no banco de dados:
*   **ADMIN:** Visão total (acesso global à árvore de unidades e subprocessos).
*   **GESTOR:** Vê sua unidade ativa e todas as subordinadas (descendentes na árvore de unidades e subprocessos).
*   **CHEFE/SERVIDOR:** Vê apenas sua própria unidade ativa.

### 4. Regras de frontend
As ações disponíveis para os usuários no frontend devem seguir essas regras:

*   **Esconder:** Se o **perfil ativo** do usuário nunca tiver permissão para realizar aquela ação, o controle (botão, link, item de menu) não deve ser mostrado. Ex: O botão `Criar Processo` nunca aparece para um usuário com perfil CHEFE.
*   **Desabilitar:** Se o perfil do usuário permite realizar a ação, mas a **situação** ou a **localização** do subprocesso não permitem no momento, o controle deve ser exibido, mas ficar desabilitado (geralmente com um tooltip explicando o motivo). Ex: um usuário com perfil CHEFE vê o botão `Disponibilizar cadastro`, mas este fica desabilitado se o subprocesso estiver localizado em unidade diferente da sua.

### 5. Implementação Técnica (Backend)
As proteções são aplicadas nos controladores via anotações `@PreAuthorize`:

*   **Para ações de fluxo:** Usa `hasPermission`. O motor de segurança (`SgcPermissionEvaluator`) cruza os dados do **Token** com o estado do **Banco**.

    ```java
    // Checa: Perfil GESTOR no Token + Unidade no Token == Localização no Banco
    @PreAuthorize("hasPermission(#id, 'Subprocesso', 'ACEITAR_CADASTRO')")
    ```
*   **Para ações de gestão:** Usa `hasRole`. Checagem simples do perfil contido no **Token**.
    ```java
    @PreAuthorize("hasRole('ADMIN')")
    ```
