# CDU-30 - Manter Administradores

**Ator:** ADMIN

## Descrição
Permite o gerenciamento dos usuários que possuem perfil de administrador no sistema.

## Regras de Negócio
- Apenas usuários cadastrados no sistema (existentes na view de usuários) podem ser tornados administradores.
- Um administrador não pode remover o seu próprio acesso de administrador (regra de segurança para evitar bloqueio total), a menos que exista outro administrador ativo.

## Fluxo principal

1. O usuário acessa a funcionalidade de Manter Administradores (Configurações -> Administradores).
2. O sistema exibe a lista de administradores cadastrados, mostrando Nome, Título Eleitoral e Lotação.
3. O sistema apresenta opções para:
    - Adicionar novo administrador.
    - Remover administrador existente.

### Fluxo alternativo: Adicionar Administrador
3.1. O usuário aciona a opção "Adicionar Administrador".
3.2. O sistema apresenta um campo de busca de usuário (por nome ou matrícula).
3.3. O usuário pesquisa e seleciona o usuário desejado.
3.4. O sistema confirma a ação e insere o registro na tabela ADMINISTRADOR.
3.5. O sistema exibe mensagem de sucesso.

### Fluxo alternativo: Remover Administrador
3.1. O usuário aciona o ícone de exclusão em um registro da lista.
3.2. O sistema solicita confirmação.
3.3. O usuário confirma.
3.4. O sistema valida se a exclusão é permitida (regras de negócio).
3.5. O sistema remove o registro da tabela ADMINISTRADOR.
3.6. O sistema exibe mensagem de sucesso.
