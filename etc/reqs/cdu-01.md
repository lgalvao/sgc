# CDU-01 - Realizar login e exibir estrutura das telas

Ator: Qualquer pessoa autorizada a acessar o sistema (com qualquer dos perfis).

Ator secundário: Sistema Acesso do TRE-PE

Pré-condições:

- Usuário deve possuir credenciais válidas (título e senha de rede no TRE-PE)
- Usuário deve estar cadastrado no SGRH com lotação ativa em alguma unidade

Fluxo principal:

1. O usuário acessa o sistema

2. O sistema exibe a tela `Login`

3. O usuário informa suas credenciais: número do título de eleitor e senha

4. O sistema verifica título e a senha (autenticação simples) através da API do Sistema 'Acesso AD' do TRE-PE

5. Caso o usuário não seja autenticado com as credenciais fornecidas, sistema mostra a mensagem
   `Título ou senha inválidos.`

6. Caso o usuário seja autenticado, o sistema consulta os perfis e as unidades do usuário nas views do banco de dados.
   **Importante:** Um usuário pode estar em várias unidades (por conta de substituição, atribuição temporária, etc.) e também ter mais de um perfil (por exemplo, ser chefe de uma unidade e administrador do sistema).

7. O sistema determina os perfis disponíveis para o usuário, seguindo estas regras, não exclusivas:
    - ADMIN: Se o usuário estiver cadastrado como administrador do sistema.
    - GESTOR: Se o usuário for responsável por uma unidade intermediária ou interoperacional.
    - CHEFE: Se o usuário for responsável por uma unidade operacional ou interoperacional.
    - SERVIDOR: Se o usuário não for o responsável pela sua unidade de lotação.

8. O sistema determina quais pares 'perfil-unidade' se aplicam ao usuário logado, seguindo estas regras:

   **Se o usuário possuir apenas um perfil e uma unidade:**

   8.1. O sistema guarda o perfil e a unidade definidos

   **Se o usuário possuir múltiplos perfis ou unidades:**

   8.2. O sistema expande a tela de login para permitir a seleção de perfil/unidade

   8.3. O usuário seleciona o perfil/unidade com o qual vai atuar

   8.4. O sistema guarda o perfil e a unidade definidos

9. O sistema exibe a estrutura de telas da aplicação, composta pelas seções: `Barra de navegação`, `Conteúdo` e
   `Rodapé`.

   9.1. A `Barra de navegação` é sempre mostrada no topo das telas (exceto para tela de login) e tem as seguintes regras de exibição:

   9.1.1. Itens principais de navegação:
    - Ícone/logotipo do sistema (abre a tela `Painel`)
    - Link `Painel`, para tela `Painel`
    - Menu das unidades, de acordo com as regras:
        - Se ADMIN: Link `Unidades`, para a tela `Unidades`, que apresenta a hierarquia completa de unidades do TRE-PE
        - Se GESTOR: CHEFE, ou SERVIDOR: Link `Minha unidade`, para a tela `Detalhe da unidade`, que apresenta os dados da unidade do usuário logado
    - Link `Relatórios`, para tela `Relatórios`
    - Link `Histórico`, para tela `Histórico de processos`

   9.1.2. Itens adicionais, alinhados à direita:
    - Para todos os perfis:
        - `[Perfil] - [Sigla da unidade]` - Texto fixo, sem interatividade (ex. `CHEFE - SESEL`).
        - `Ícone de logout` - faz logout e mostra tela `Login`
    - Se perfil ADMIN:
        - Mostrar adicionalmente dois ícones para acesso as telas  `Configurações` e `Administradores`

   9.2. O `Conteúdo` compõe a parte central onde serão 'encaixadas' todas as telas.

   9.3. O `Rodapé` é a seção localizada na parte inferior das telas (exceto na tela de login), em que aparecerá a
   informação da versão do sistema assim como a informação `Desenvolvido por SESEL/COSIS/TRE-PE`.

10. O sistema mostra inicialmente a tela `Painel`.
