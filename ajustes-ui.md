# Ajustes no sistema oriuentados pelas telas capturadas

## 01-autenticacao--01-login-inicial.png

Deixar o botão de mostrar senha mais discreto, com moldura mais clara ou sem moldura

## 01-autenticacao--02-login-erro-credenciais.png

A mensagem de erro no login nao esta boa. Mude para:

- Titulo: Erro no login
- Mensagem: Título ou senha inválidos.

## 01-autenticacao--04-painel-apos-login.png

- Ao lado do perfil - unidade (no caso da imagem, ADMIN - SEDOC), fazer com que um hover no ícone mostre o nome completo do usuário logado em um tooltip.

- A barra de navegação nao está fazendo o collapse e os menus estao ficando cortados. Corrigir. Talvez a barra de navegação nao esteja baseada no componente Navbar do BootstrapVueNext... investigue.

## 02-painel--02-criar-processo-form-vazio.png

No campo tipo, usar valor 'humanizado', no caso 'Mapeamento', mas fazer para os outros dois tipos de projetos tambem.

- Incluir ícones nos botões (sempre do Bootstrap Icons).
- Deixar mais clara a hierarquia dos botões:
  - Botão 'Iniciar processo é o primeiro', cor sólida.
  - Botão 'Salvar' é o segundo, outline
  - Botão 'Cancelar' com menos destaque, separado à direita.

## painel--06-painel-admin-com-processo.png

O título dessa imagem deixa a entender que deveria haver um processo aqui, mas nao há nada. Verifique o que esta errado na parte do teste que gera a imagem, em 'captura-telas.spec.ts'

## teste 'captura-telas.spec.ts'

Faça o teste criar um processo com várias unidades, para testarmos a exibição na coluna 'Unidade participantes'

## 02-painel--10-painel-gestor.png

- Outra vez o nome do arquivo indica que deveriam ser exibidos aqui processos para o GESTOR, mas está tudo vazio. Reveja o teste 'captura-telas.spec.ts' para criar uma estrutura que faça com sejam exibidas unidades nessa tela.

## 02-painel--11-painel-chefe.png

Idem, só que para o CHEFE
