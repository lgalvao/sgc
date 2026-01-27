# CDU-30 - Manter Administradores

Ator: ADMIN

## Descrição

Permite o gerenciamento dos usuários que possuem perfil de administrador no sistema.

## Fluxo principal

1. O usuário clica em Configurações (ícone de engrenagem) e escolhe `Administradores`.

2. O sistema exibe a lista de administradores cadastrados, mostrando nome, título de eleitor, matricula e unidade de
   lotação.

3. O sistema apresenta opções para:

    - Adicionar novo administrador.
    - Remover administrador existente.

4. **<<Início de fluxo de adição de administrador>>** O usuário aciona a opção "Adicionar".

5. O sistema apresenta um modal com título "Adicionar administrador" contendo um campo de texto para o título eleitoral
   do usuário e botões "Cancelar" e "Adicionar".

6. O usuário informa o título eleitoral e clica em "Adicionar".

7. O sistema valida se o usuário existe e se já é administrador. Se houver erro, exibe mensagem de erro.

8. Sistema insere o registro na tabela ADMINISTRADOR e mostra uma mensagem de sucesso "Administrador adicionado com
   sucesso!". **<<Término de fluxo de adição de administrador>>**

9. **<<Início de fluxo de remoção de administrador>>** O usuário aciona o ícone de exclusão em um registro da lista.

10. O sistema exibe um modal com título "Confirmar Remoção" e a mensagem "Deseja realmente
    remover [NOME_DO_ADMINISTRADOR] como administrador do sistema?", com botões "Cancelar" e "Remover".

11. O usuário confirma clicando em "Remover".

12. O sistema valida se a exclusão é permitida:
    - Verifica se o usuário está tentando remover a si mesmo.
    - Verifica se é o único administrador do sistema.

13. Se a validação falhar, o sistema exibe mensagem de erro correspondente.

14. Se a validação for bem sucedida, o sistema remove o registro da tabela ADMINISTRADOR.

15. O sistema exibe mensagem de sucesso "Administrador removido com sucesso!" e atualiza a lista. *
    *<<Término de fluxo de remoção de administrador>>**
