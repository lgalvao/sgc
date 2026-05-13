# CDU-28 - Manter atribuição temporária

**Ator:** ADMIN

## Pré-condições

- Usuário autenticado com perfil ADMIN.

## Fluxo principal

1. O usuário clica em `Unidades` no menu.

2. O sistema mostra a árvore completa de unidades.

3. O usuário clica em uma das unidades.

4. O sistema mostra a tela `Detalhes da unidade` para a unidade selecionada, exibindo o titular e o responsável atual da unidade.

5. Se **não houver atribuição temporária vigente** para a unidade, o sistema exibe o botão `Criar atribuição`.

6. Se **houver atribuição temporária vigente** para a unidade, o sistema:

   - exibe o responsável com o tipo `Atrib. temporária (até [DATA_TERMINO_ATRIB])`, na própria tela de detalhes da unidade;
   - mostra o botão `Criar atribuição` no lugar do botão `Editar atribuição`.

7. Ao clicar em `Criar atribuição` ou `Editar atribuição`, o sistema apresenta a tela `Atribuição temporária`, no contexto da unidade selecionada, com estes campos:

   - Dropdown pesquisável `Usuário` com os nomes dos servidores da unidade
   - `Data de início`
   - `Data de término`
   - `Justificativa`
   - Botão principal:
     - `Criar`, se não houver atribuição temporária vigente;
     - `Salvar`, se houver atribuição temporária vigente.
   - Botão `Cancelar`
   - Botão `Remover`, exibido apenas quando houver atribuição temporária vigente

8. Se houver atribuição temporária vigente, o sistema apresenta a tela já preenchida com os dados dessa atribuição.

## Criação de atribuição temporária

9. O usuário seleciona o servidor, define as datas e inclui uma justificativa; depois clica em `Criar`.

10. O sistema valida os dados informados.

11. Estando tudo válido, o sistema registra internamente a atribuição temporária e mostra a mensagem `Atribuição criada`.

12. O sistema envia uma notificação por e-mail para o usuário que recebeu a atribuição temporária, com este modelo:

    ```text
    Assunto: SGC: Atribuição de perfil CHEFE na unidade [SIGLA_UNIDADE]

    Prezado(a) [NOME_SERVIDOR],

    Foi registrada uma atribuição temporária de perfil de CHEFE para você na unidade [SIGLA_UNIDADE].

    Período: [DATA_INICIO] a [DATA_TERMINO].

    Justificativa: [JUSTIFICATIVA].

    Acesse o sistema em [URL_SISTEMA] e escolha o perfil 'CHEFE' para a unidade da atribuição.
    ```

13. O sistema registra internamente um alerta:

- `Data/hora`: [Data/hora atual]
- `Descrição`: "Atribuição temporária para unidade [SIGLA_UNIDADE]"
- `Unidade de origem`: ADMIN
- `Unidade de destino`: [Vazia]
- `Usuário de destino`: [Usuário destinatário da atribuição]

14. O usuário que recebe a atribuição temporária passa a ter os mesmos direitos do perfil CHEFE durante o período especificado, para a unidade especificada na atribuição. Em um próximo login, o novo par `CHEFE-[UNIDADE_ATRIBUICAO]` será mostrado entre as opções de login.

14.1. O novo perfil será incluído automaticamente através de cálculos das views no banco de dados. O sistema não precisa realizar nenhuma operação, além das especificadas neste caso de uso, para que o novo perfil se torne disponível ao usuário.

## Edição de atribuição temporária

15. Se houver atribuição temporária vigente, o usuário pode alterar os dados desejados e clicar em `Salvar`.

16. O sistema valida os dados informados.

17. Estando tudo válido, o sistema atualiza internamente a atribuição temporária vigente e mostra a mensagem `Atribuição atualizada`.

## Remoção de atribuição temporária

18. O usuário clica em `Remover`.

19. O sistema mostra um modal de confirmação `Confirma a remoção da atribuição temporária vigente desta unidade?`, com botões `Remover` e `Cancelar`.

20. Se o usuário confirmar, o sistema remove a atribuição temporária e mostra a mensagem `Atribuição removida`.

21. Após a remoção, a tela volta ao estado sem atribuição temporária vigente:

   - o responsável efetivo da unidade passa a ser determinado novamente pelas regras gerais de responsabilidade;
   - o botão da tela `Detalhes da unidade` volta a ser `Criar atribuição`.

## Regras
- Todos os campos do formulário são obrigatórios.
- A data de término deve ser posterior ou igual à data de início.
- Não pode haver sobreposição entre períodos de atribuição temporária da mesma unidade.