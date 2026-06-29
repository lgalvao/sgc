# CDU-28 - Manter atribuição temporária

## Atores

- ADMIN

## Pré-condições

- Usuário autenticado com perfil ADMIN.

## Fluxo principal

1. O usuário clica em `Unidades` no menu principal do sistema.

2. O sistema mostra a árvore completa de unidades.

3. O usuário aciona uma das unidades.

4. O sistema mostra a tela `Detalhes da unidade`, mostrando, entre outros detalhes, o titular e o responsável atual da
   unidade.

5. Se **não houver** atribuição temporária vigente para a unidade, o sistema mostra o botão `Criar atribuição`.

6. Se **houver** atribuição temporária vigente, o sistema mostra:
    - o responsável com o tipo `Atrib. temporária (até [DATA_TERMINO_ATRIB])`;
    - botão `Editar atribuição` (em vez de `Criar atribuição`).

7. Ao acionar em `Criar/Editar atribuição`, o sistema apresenta a tela `Atribuição temporária`, com estes campos:
    - Controle *drop-down* pesquisável `Usuário` com os nomes dos servidores da unidade
    - `Data de início`
    - `Data de término`
    - `Justificativa`
    - Botão `Criar`, se não houver atribuição temporária vigente;
    - Botões `Salvar`e `Remover`, se houver atribuição temporária vigente
    - Botão `Cancelar`

8. Se houver atribuição temporária vigente, o sistema apresenta a tela já preenchida com os dados dessa atribuição.

---

### Criação de atribuição temporária

9. O usuário seleciona o servidor, define as datas e inclui uma justificativa; depois clica em `Criar` ou `Salvar`.
   Regras:
    - Todos os campos são obrigatórios;
    - Não pode haver sobreposição entre períodos de atribuição temporária da mesma unidade.

10. O sistema registra internamente a atribuição temporária e mostra o *toast* `Atribuição criada`.

11. O sistema envia uma notificação por e-mail para o usuário que recebeu a atribuição temporária, com este modelo:

    ```text
    Assunto: SGC: Atribuição de perfil CHEFE na unidade [SIGLA_UNIDADE]

    Prezado(a) [NOME_SERVIDOR],

    Foi registrada uma atribuição temporária de perfil de CHEFE para você na unidade [SIGLA_UNIDADE].

    Período: [DATA_INICIO] a [DATA_TERMINO].

    Justificativa: [JUSTIFICATIVA].

    Acesse o sistema em [URL_SISTEMA] e escolha o perfil 'CHEFE' para a unidade da atribuição.
    ```

12. O sistema registra internamente um alerta:
    - `Data/hora`: [Data/hora atual]
    - `Descrição`: "Atribuição temporária para unidade [SIGLA_UNIDADE]"
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: (**Não preencher**)
    - `Usuário de destino`: [Usuário destinatário da atribuição]

13. O usuário que recebe a atribuição temporária passa a ter os mesmos direitos do perfil CHEFE durante o período
    especificado, para a unidade da atribuição. Em um próximo login, o novo par `CHEFE-[UNIDADE_ATRIBUICAO]` será
    mostrado pra o usuário entre as opções de login.

    14.1. O novo perfil será incluído automaticamente através de cálculos das views no banco de dados. O sistema não
    precisa realizar nenhuma operação, além das especificadas neste caso de uso, para que o novo perfil se torne
    disponível ao usuário.

14. Se houver atribuição temporária vigente, o usuário altera os dados e aciona `Salvar`.

15. O sistema atualiza internamente a atribuição temporária vigente e mostra *toast*
    `Atribuição atualizada`.

---

### Remoção de atribuição temporária

16. O usuário aciona `Remover`.

17. O sistema mostra um modal de confirmação "Confirma a remoção da atribuição temporária desta unidade?", com botões
    `Remover` e `Cancelar`.

18. Se o usuário confirmar, o sistema remove a atribuição temporária e mostra o *toast* "Atribuição removida".
