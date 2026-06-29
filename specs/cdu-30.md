# CDU-30 - Manter administradores

## Atores

- ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.

## Fluxo principal

1. O usuário aciona o ícone `Administradores do sistema` na barra principal.

2. O sistema mostra, botão `Adicionar` no cabeçalho; e uma tabela com os administradores cadastrados, com nome, título
   de eleitor, matrícula, unidade de lotação e uma coluna de ações, com ícone para remover. Regras:
    - Não deve ser mostrado o ícone de exclusão para o usuário logado.
    - Não deve ser mostrado o ícone de exclusão para o usuário se ele for o único administrador cadastrado.

---

### Adição de administrador

3. O usuário aciona `Adicionar`.

4. O sistema mostra um modal com título `Adicionar administrador` com um campo para título ou nome, e botões `Cancelar`
   e`Adicionar`.
    - O campo deve ter texto informativo "Digite o nome ou título" e a funcionalidade de 'autocompletar', reconhecendo o
      título ou parte do nome do usuário.

5. O usuário digita o título eleitoral ou parte do nome do usuário.

6. O sistema realiza o "autocompletar", filtrando pelo título ou parte do nome.
    - A lista de opções não deve incluir o usuários que já sejam administradores do sistema.

7. usuário seleciona o usuário da lista e aciona `Adicionar`.

8. O sistema registra o usuário como administrador e mostra *toast* "Administrador adicionado".

---

### Remoção de administrador

9. Ao lado de um servidor, o usuário aciona o ícone de exclusão.

10. O sistema valida se a exclusão é permitida: um usuário não pode remover a si mesmo; e não pode remover o único
    administrador.
    - Essas ações são protegidas pela própria interface gráfica, mas devem ser validadas defensivamente no servidor.

11. O sistema mostra um modal com título `Confirmar remoção` e a mensagem "Realmente remover :NOME_DO_ADMINISTRADOR:
    dos administradores do sistema?", com botões `Cancelar` e `Remover`.

12. O usuário confirma acionando `Remover`.
    
13. O sistema remove o usuário, mostra *toast* "Administrador removido", e atualiza a tabela de administradores na tela.
