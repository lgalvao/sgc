# CDU-30 - Manter Administradores

**Ator:** ADMIN

## Descrição

Permite o gerenciamento dos usuários que possuem perfil de administrador no sistema.

## Regras de Negócio

- Apenas usuários cadastrados no sgrh podem ser tornados administradores.
- Um administrador não pode remover o seu próprio acesso de administrador.

## Fluxo principal

1. O usuário acessa a funcionalidade de Manter administradores (Configurações -> Administradores).

2. O sistema exibe a lista de administradores cadastrados, mostrando nome, título de eleitor, matricula e unidade de lotação.

3. O sistema apresenta opções para:

    - Adicionar novo administrador.
    - Remover administrador existente.

### Fluxo alternativo: Adicionar Administrador

3.1. O usuário aciona a opção "Adicionar".

3.2. O sistema apresenta um modal com um campo de busca de usuário, por nome ou matrícula).

3.3. O usuário pesquisa e seleciona o usuário desejado.

3.4. O sistema mostra uma tela de confirmação.

3.5. Sistema insere o registro na tabela ADMINISTRADOR e mostra uma mensagem de sucesso.

### Fluxo alternativo: Remover Administrador

3.1. O usuário aciona o ícone de exclusão em um registro da lista.

3.2. O sistema solicita confirmação.

3.3. O usuário confirma.

3.4. O sistema valida se a exclusão é permitida.

3.5. O sistema remove o registro da tabela ADMINISTRADOR.

3.6. O sistema exibe mensagem de sucesso.
