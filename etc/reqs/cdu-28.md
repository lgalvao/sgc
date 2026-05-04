# CDU-28 - Manter atribuição temporária

**Ator:** ADMIN

## Pré-condições

- Usuário autenticado com perfil ADMIN.

## Fluxo principal

1. O usuário clica em `Unidades` no menu (este é o comando equivalente a `Minha unidade`, visto por outros perfis).

2. O sistema mostra a árvore completa de unidades.

3. Usuário clica em uma das unidades.

4. O sistema mostra a tela `Detalhes da unidade` para a unidade selecionada.

5. O usuário clica no botão `Criar atribuição`.

6. O sistema apresenta uma tela com estes campos:

    - Dropdown pesquisável `Servidores` com os nomes dos servidores da unidade
    - `Data de início`
    - `Data de término`
    - `Justificativa`
    - Botões `Criar` e `Cancelar`

7. O usuário seleciona o servidor, define as datas e inclui uma justificativa (todos os campos são obrigatórios); depois
   clica em `Criar`.

8. O sistema registra internamente a atribuição temporária e mostra uma mensagem "Atribuição criada".

9. O sistema envia uma notificação por e-mail para o usuário que recebeu a atribuição temporária (não para unidade!),
   com este modelo:

   ```text
   Assunto: SGC: Atribuição de perfil CHEFE na unidade [SIGLA_UNIDADE]

   Prezado(a) [NOME_SERVIDOR],

   Foi registrada uma atribuição temporária de perfil de CHEFE para você na unidade [SIGLA_UNIDADE].

   Período: [DATA_INICIO] a [DATA_TERMINO].

   Justificativa: [JUSTIFICATIVA].

   Acesse o sistema em [URL_SISTEMA] e escolha o perfil 'CHEFE' para a unidade da atribuição.
   ```
10. O sistema registra internamente um alerta:

- `Data/hora`: [Data/hora atual]
- `Descrição`: "Atribuição temporária para unidade [SIGLA_UNIDADE]"
- `Unidade de origem`: ADMIN
- `Usuário de destino`: [Usuário destinatário da atribuição]

11. O usuário que recebe a atribuição temporária passa a ter os mesmos direitos do perfil CHEFE durante o período
    especificado, para a unidade especificada na atribuição. Em um próximo login, o novo par "
    CHEFE-[UNIDADE_ATRIBUICAO]" será mostrado entre as opções de login.
    10.1. *IMPORTANTE*: O novo perfil será incluído automaticamente através de cálculos das views no banco de dados. O
    sistema atual nao precisa realizar nenhuma operação, além das especificadas neste caso de uso, para que o novo
    perfil se torne disponível ao usuário.