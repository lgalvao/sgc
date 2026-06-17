# CDU-45 - Manter avaliação de consenso

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Existência de processo de diagnóstico em andamento
- Existência ao menos um servidor da unidade com uma dessas situações:
    - 'Autoavaliação concluída'
    - 'Avaliação de consenso criada'
    - 'Avaliação de consenso aprovada'

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md)`.

3. Na grade de servidores da unidade, o usuário escolhe a ação `Manter avaliação de consenso` para um servidor.

3. O sistema mostra a tela `Avaliação de consenso` para o servidor escolhido, com:
    - título: "Avaliação de consenso";
    - subtítulo: [nome e título do servidor];
    - na barra de botões do cabeçalho, o botão `Concluir avaliação`;
    - grade de competências da unidade, com estrutura como no exemplo abaixo:

|                     | Importância |       |          | Domínio  |       |          |
|:--------------------|-------------|-------|----------|----------|-------|----------|
| Competência         | Servidor    | Chefe | Consenso | Servidor | Chefe | Consenso |
| Desc. competência 1 | 1           | 5     | 3        | 2        | 2     | 2        |
| Desc. competência 2 | NA          | 2     | 2        | 2        | 3     | 2        |
| Desc. competência 3 | 3           | 2     | 3        | 3        | 1     | 2        |

Regras de apresentação, para cada competência:

- os campos `Importância` e `Domínio` do servidor devem ser **não editáveis**, sendo preenchidos com os valores
  fornecidos na autoavaliação correspondente;
- os campos `Importância` e `Domínio` do chefe devem ser **editáveis** (via seleção de lista fechada de opções);
- os dois campos de `Consenso` devem ser **editáveis** (via seleção de lista fechada de opções) apenas quando os campos
  correspondentes do servidor e do chefe estiverem preenchidos;
- se os valores `Importância` e `Domínio` forem iguais, o sistema deve preencher automaticamente `Consenso`
  com o valor repetido.

**IMPORTANTE**: Se a situação do servidor for 'Avaliação de consenso aprovada', ou o subprocesso não estiver localizado
na unidade do usuário, o sistema mostra os elementos acima apenas em modo somente-leitura, desabilita o botão
`Concluir avaliação` -- e o **caso de uso termina**.

4. O usuário atribui (com a presença física do servidor) o valor de consenso para cada competência.

5. O sistema salva automaticamente cada alteração realizada. Não é necessária nenhuma ação para concluir a operação (a
   validação dos dados será feita na conclusão do diagnóstico, como um todo).

6. Se o usuário acionar o botão `Voltar`, o sistema não valida o preenchimento dos dados, mantendo os dados salvos e o
   **caso de uso termina**

7. Se usuário acionar o botão `Concluir avaliação`, o sistema **valida** o preenchimento dos campos `Importância` e
   `Domínio` do chefe e de consenso.

   7.1. Se houver campos não preenchidos o sistema mostra a mensagem "Preencha todos os campos" e interrompe a operação.

   7.2. Se preenchidos, o sistema muda a situação do servidor para 'Avaliação de consenso criada', redireciona para a
   tela `Detalhes do subprocesso` e mostra um *toast* com a mensagem "Avaliação de consenso criada".