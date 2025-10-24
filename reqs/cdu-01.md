# CDU-01 - Realizar login e exibir estrutura das telas

Ator: Qualquer pessoa autorizada a acessar o sistema (com qualquer dos perfis).

Ator secundário: Sistema Acesso do TRE-PE

Pré-condições:
● Usuário deve possuir credenciais válidas (título e senha de rede no TRE-PE)
● Usuário deve estar cadastrado no SGRH com lotação ativa em alguma unidade

Fluxo principal:

1. Usuário acessa o sistema
2. O sistema exibe a tela de Login
3. Usuário informa suas credenciais: número do título de eleitor e senha
4. O sistema verifica título/senha através da API do O sistema Acesso
5. Caso o sistema não consiga autenticar o usuário com as credenciais fornecidas, deverá mostrar a mensagem 'Título ou
   senha inválidos.'
6. O sistema consulta perfis e unidades do usuário
   6.1. Um usuário pode estar em várias unidades (ex. substituição, atribuição temporária) e também ter mais de um
   perfil.
7. O sistema determina os perfis disponíveis para usuário seguindo estas regras:
   7.1. ADMIN: Servidor cadastrado como administrador do sistema.
   7.2. GESTOR: Servidor responsável por uma unidade intermediária ou interoperacional..
   7.3. CHEFE: Servidor responsável por unidades operacionais, interoperacionais ou pela SEDOC.
   7.4. SERVIDOR: Servidor que não é o responsável pela sua unidade de lotação.
8. O sistema determina quais pares 'perfil-unidade' se aplicam ao usuário logado.
   8.1. Se usuário possuir apenas um perfil e uma unidade:
   ■ O sistema guarda perfil e unidade definidos
   8.2. Se usuário possuir múltiplos perfis ou unidades:
   ■ O sistema expande a tela de login para permitir a seleção de perfil e unidade
   ■ Usuário seleciona o perfil/unidade com o qual vai atuar
   ■ O sistema guarda perfil e unidade definidos
9. O sistema exibe a estrutura de telas da aplicação, composta pelas seções: Barra de navegação, Conteúdo e Rodapé.
   9.1. A Barra de Navegação, que é sempre mostrada no topo das telas (exceto para tela de login) tem as seguintes
   regras de exibição:
   ■ Elementos alinhado à esquerda:
   ● Ícone/logotipo do sistema (link para abrir a tela Painel)
   ● Link Painel, para tela Painel
   ● Link Minha unidade, para tela Detalhe da unidade, apresentando os dados da unidade do usuário logado
   ● Link Relatórios, para tela Relatórios
   ● Link Histórico, para tela Histórico de processos
   ■ Elementos alinhado à direita:
   ● Se perfil ADMIN: Ícone de engrenagem para acesso à tela de configurações do sistema
   ● [Perfil] - [Sigla da unidade] - Texto fixo, sem interatividade (ex. CHEFE - SESEL)
   ● Ícone de logout - faz logout e mostra tela Login
   9.2. O conteúdo compõe a parte central da tela, onde serão exibidas todas as telas.
   9.3. O Rodapé é a seção localizada na parte inferior das telas (exceto na tela de login) em que aparecerá a
   informação da versão do sistema assim como a informação 'Desenvolvido por SESEL/COSIS/TRE-PE'.
10. O sistema mostra inicialmente a tela Painel na seção de conteúdo das telas.